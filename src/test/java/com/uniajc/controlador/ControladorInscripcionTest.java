package com.uniajc.controlador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.uniajc.modelo.Inscripcion;
import com.uniajc.soporte.BdFalsa;

/**
 * ============================================================================
 * PRUEBAS UNITARIAS: ControladorInscripcion (Flujo MVC con BdFalsa)
 * ============================================================================
 * ¿Qué validamos aquí?
 * 1. Integración Controlador -> Servicio -> DAO simulado a través de BdFalsa.
 * 2. Sentencias SQL generadas:
 *    - SELECT para verificar duplicados por estudiante y grupo.
 *    - INSERT INTO con parámetros: id_estudiante, id_grupo, final_grade, status.
 *    - UPDATE con parámetros actualizados.
 *    - DELETE FROM con el ID.
 * 3. Manejo de excepciones antes de tocar la base de datos (datos inválidos).
 */
public class ControladorInscripcionTest {

    private BdFalsa bd;
    private ControladorInscripcion controlador;

    @BeforeEach
    void preparar() {
        // Intercepta la conexión JDBC estática con el simulador de BD en memoria
        bd = new BdFalsa().instalar();
        controlador = new ControladorInscripcion();
    }

    @AfterEach
    void limpiar() {
        // Restaura la conexión para aislar cada prueba unitaria
        BdFalsa.desinstalar();
    }

    /** Helper para simular filas de resultados que devolvería PostgreSQL */
    private void filaDeInscripcion(int id, int idEstudiante, int idGrupo, double finalGrade, String status) {
        bd.fila("id", id,
                "id_estudiante", idEstudiante,
                "id_grupo", idGrupo,
                "final_grade", finalGrade,
                "status", status,
                "created_at", null);
    }

    // ========================================================================
    // REGISTRO
    // ========================================================================

    @Test
    @DisplayName("registrar verifica no duplicidad e inserta la inscripción con sus parámetros")
    void registrar() {
        // Sin filas en BD -> la validación de duplicidad pasa y procede al INSERT
        controlador.registrar(10, 20, 4.5, "ACTIVO");

        // Verificamos que se ejecutó primero el SELECT de validación y luego el INSERT
        assertEquals(2, bd.sqls.size());
        assertTrue(bd.sqls.get(0).startsWith("SELECT"), "Primero verifica si ya existe");
        assertTrue(bd.ultimoSql().startsWith("INSERT INTO \"practica-mvc\".inscripciones"));

        // Verificamos que los parámetros del INSERT coincidan exactamente
        assertEquals(10, bd.ultimosParametros().get(1), "Parámetro 1: id_estudiante");
        assertEquals(20, bd.ultimosParametros().get(2), "Parámetro 2: id_grupo");
        assertEquals(4.5, (Double) bd.ultimosParametros().get(3), 0.001, "Parámetro 3: final_grade");
        assertEquals("ACTIVO", bd.ultimosParametros().get(4), "Parámetro 4: status");
    }

    @Test
    @DisplayName("registrar con estudiante ya inscrito en el grupo lanza error y no inserta")
    void registrarDuplicado() {
        // Simulamos que la BD ya tiene al estudiante 10 inscrito en el grupo 20
        filaDeInscripcion(1, 10, 20, 3.5, "ACTIVO");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> controlador.registrar(10, 20, 4.0, "ACTIVO"));

        assertEquals("El estudiante ya esta inscrito en este grupo.", ex.getMessage());
        // Solo se ejecutó el SELECT, no se llegó al INSERT
        assertEquals(1, bd.sqls.size());
        assertTrue(bd.ultimoSql().startsWith("SELECT"));
    }

    @Test
    @DisplayName("registrar con datos inválidos lanza error antes de enviar SQL a la base")
    void registrarInvalido() {
        IllegalArgumentException notaInvalida = assertThrows(IllegalArgumentException.class,
                () -> controlador.registrar(10, 20, 5.5, "ACTIVO"));
        IllegalArgumentException estudianteCero = assertThrows(IllegalArgumentException.class,
                () -> controlador.registrar(0, 20, 4.0, "ACTIVO"));
        IllegalArgumentException estadoInvalido = assertThrows(IllegalArgumentException.class,
                () -> controlador.registrar(10, 20, 4.0, "PENDIENTE"));

        assertEquals("La nota final debe estar entre 0 y 5.", notaInvalida.getMessage());
        assertEquals("El estudiante es obligatorio.", estudianteCero.getMessage());
        assertEquals("El estado debe ser ACTIVO, APROBADO, REPROBADO o CANCELADO.", estadoInvalido.getMessage());
        assertTrue(bd.sqls.isEmpty(), "No debe haber tocado la base de datos");
    }

    // ========================================================================
    // CONSULTAS
    // ========================================================================

    @Test
    @DisplayName("listar devuelve las inscripciones mapeadas desde la base de datos")
    void listar() {
        filaDeInscripcion(1, 10, 20, 4.0, "ACTIVO");
        filaDeInscripcion(2, 11, 21, 3.2, "APROBADO");

        List<Inscripcion> lista = controlador.listar();

        assertEquals(2, lista.size());
        assertEquals(1, lista.get(0).getId());
        assertEquals(4.0, lista.get(0).getFinal_grade(), 0.001);
        assertEquals(2, lista.get(1).getId());
        assertEquals("APROBADO", lista.get(1).getStatus());
    }

    @Test
    @DisplayName("buscarPorId devuelve la inscripción si existe y vacío si no")
    void buscarPorId() {
        assertFalse(controlador.buscarPorId(99).isPresent());

        filaDeInscripcion(15, 10, 20, 4.8, "APROBADO");
        Optional<Inscripcion> r = controlador.buscarPorId(15);

        assertTrue(r.isPresent());
        assertEquals(15, r.get().getId());
        assertEquals(10, r.get().getId_estudiante());
        assertEquals(20, r.get().getId_grupo());
        assertEquals(4.8, r.get().getFinal_grade(), 0.001);
        assertEquals("APROBADO", r.get().getStatus());
    }

    // ========================================================================
    // ACTUALIZACIÓN
    // ========================================================================

    @Test
    @DisplayName("actualizar ejecuta el UPDATE con los parámetros recibidos")
    void actualizar() {
        // Simulamos que la inscripción con ID=1 existe en la base
        filaDeInscripcion(1, 10, 20, 3.0, "ACTIVO");

        controlador.actualizar(1, 10, 20, 4.2, "APROBADO");

        assertTrue(bd.ultimoSql().startsWith("UPDATE \"practica-mvc\".inscripciones"));
        assertEquals(10, bd.ultimosParametros().get(1));
        assertEquals(20, bd.ultimosParametros().get(2));
        assertEquals(4.2, (Double) bd.ultimosParametros().get(3), 0.001);
        assertEquals("APROBADO", bd.ultimosParametros().get(4));
        assertEquals(1, bd.ultimosParametros().get(5));
    }

    @Test
    @DisplayName("actualizar una inscripción que no existe lanza error")
    void actualizarInexistente() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> controlador.actualizar(50, 10, 20, 4.0, "ACTIVO"));

        assertEquals("Inscripcion no encontrada.", ex.getMessage());
    }

    // ========================================================================
    // ELIMINACIÓN
    // ========================================================================

    @Test
    @DisplayName("eliminar borra la inscripción cuando existe en la base")
    void eliminar() {
        filaDeInscripcion(5, 10, 20, 3.5, "ACTIVO");

        controlador.eliminar(5);

        assertTrue(bd.ultimoSql().startsWith("DELETE FROM \"practica-mvc\".inscripciones"));
        assertEquals(5, bd.ultimosParametros().get(1));
    }

    @Test
    @DisplayName("eliminar con ID inválido o inexistente lanza error")
    void eliminarInvalido() {
        IllegalArgumentException cero = assertThrows(IllegalArgumentException.class,
                () -> controlador.eliminar(0));
        IllegalArgumentException inexistente = assertThrows(IllegalArgumentException.class,
                () -> controlador.eliminar(99));

        assertEquals("ID invalido.", cero.getMessage());
        assertEquals("Inscripcion no encontrada.", inexistente.getMessage());
    }

    // ========================================================================
    // INTEGRACIÓN CON PROCEDIMIENTOS ALMACENADOS
    // ========================================================================

    @Test
    @DisplayName("eliminarConProcedimiento ejecuta el CALL del procedimiento sp_eliminar_inscripcion_segura")
    void eliminarConProcedimiento() {
        filaDeInscripcion(9, 10, 20, 2.0, "ACTIVO");

        controlador.eliminarConProcedimiento(9);

        assertTrue(bd.ultimoSql().startsWith("CALL \"practica-mvc\".sp_eliminar_inscripcion_segura"));
        assertEquals(9, bd.ultimosParametros().get(1), "Parámetro 1 del SP: id_inscripcion");
    }

    @Test
    @DisplayName("registrarConProcedimiento ejecuta el CALL del procedimiento sp_inscribir_estudiante")
    void registrarConProcedimiento() {
        controlador.registrarConProcedimiento(12, 34, 4.0, "ACTIVO");

        assertTrue(bd.ultimoSql().startsWith("CALL \"practica-mvc\".sp_inscribir_estudiante"));
        assertEquals(12, bd.ultimosParametros().get(1), "Parámetro 1 del SP: id_estudiante");
        assertEquals(34, bd.ultimosParametros().get(2), "Parámetro 2 del SP: id_grupo");
        assertEquals(4.0, (Double) bd.ultimosParametros().get(3), 0.001, "Parámetro 3 del SP: nota_final");
        assertEquals("ACTIVO", bd.ultimosParametros().get(4), "Parámetro 4 del SP: status");
    }
}
