package com.uniajc.servicios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.uniajc.modelo.Inscripcion;
import com.uniajc.soporte.DaosFalsos.InscripcionDaoFalso;

/**
 * PRUEBAS UNITARIAS: Capa de Servicio InscripcionService
 * ¿Por qué probar esta capa con InscripcionDaoFalso?
 * 1. Reglas de Negocio en Aislamiento: InscripcionService contiene todas las reglas:
 *    - Rango válido de notas (0.0 a 5.0).
 *    - Estados permitidos ('ACTIVO', 'APROBADO', 'REPROBADO', 'CANCELADO').
 *    - Llaves foráneas obligatorias (id_estudiante > 0, id_grupo > 0).
 *    - Unicidad: No se permite registrar al mismo estudiante dos veces en el mismo grupo.
 * 2. Rapidez y Confiabilidad: Al usar InscripcionDaoFalso (almacén en RAM), las pruebas se ejecutan
 *    en milisegundos y no dependen de la red ni de Neon/PostgreSQL.
 */
public class InscripcionServiceTest {

    private InscripcionDaoFalso dao;
    private InscripcionService servicio;

    @BeforeEach
    void preparar() {
        dao = new InscripcionDaoFalso();
        servicio = new InscripcionService(dao);
    }

    /** Helper para capturar y verificar excepciones lanzadas en el registro */
    private IllegalArgumentException registrarConError(Inscripcion i) {
        return assertThrows(IllegalArgumentException.class, () -> servicio.registrarInscripcion(i));
    }

    // REGISTRO DE INSCRIPCIONES (CASOS EXITOSOS Y VALIDACIONES)

    @Test
    @DisplayName("Registrar una inscripción válida la guarda correctamente en el DAO")
    void registrarValido() {
        Inscripcion inscripcion = new Inscripcion(1, 2, 4.5, "ACTIVO");

        servicio.registrarInscripcion(inscripcion);

        assertEquals(1, dao.guardados, "Debe registrarse exactamente 1 inscripción");
        assertEquals(1, dao.datos.size());
        assertEquals("ACTIVO", dao.datos.get(0).getStatus());
    }

    @Test
    @DisplayName("Registrar limpia espacios en blanco y pasa el estado a mayúsculas")
    void registrarNormalizaEstado() {
        Inscripcion inscripcion = new Inscripcion(1, 2, 3.5, "  activo  ");

        servicio.registrarInscripcion(inscripcion);

        assertEquals("ACTIVO", inscripcion.getStatus());
    }

    @Test
    @DisplayName("Registrar una inscripción nula lanza IllegalArgumentException")
    void registrarNulo() {
        IllegalArgumentException ex = registrarConError(null);

        assertEquals("La inscripcion no puede ser nula.", ex.getMessage());
        assertEquals(0, dao.guardados);
    }

    @Test
    @DisplayName("El estudiante es obligatorio (ID menor o igual a cero lanza excepción)")
    void estudianteObligatorio() {
        String mensajeEsperado = "El estudiante es obligatorio.";

        assertEquals(mensajeEsperado, registrarConError(new Inscripcion(0, 1, 4.0, "ACTIVO")).getMessage());
        assertEquals(mensajeEsperado, registrarConError(new Inscripcion(-5, 1, 4.0, "ACTIVO")).getMessage());
        assertEquals(0, dao.guardados);
    }

    @Test
    @DisplayName("El grupo es obligatorio (ID menor o igual a cero lanza excepción)")
    void grupoObligatorio() {
        String mensajeEsperado = "El grupo es obligatorio.";

        assertEquals(mensajeEsperado, registrarConError(new Inscripcion(1, 0, 4.0, "ACTIVO")).getMessage());
        assertEquals(mensajeEsperado, registrarConError(new Inscripcion(1, -2, 4.0, "ACTIVO")).getMessage());
        assertEquals(0, dao.guardados);
    }

    @Test
    @DisplayName("La nota final debe estar entre 0.0 y 5.0 (notas de borde permitidas)")
    void notaBordesValidos() {
        servicio.registrarInscripcion(new Inscripcion(1, 1, 0.0, "ACTIVO"));
        servicio.registrarInscripcion(new Inscripcion(2, 1, 5.0, "APROBADO"));

        assertEquals(2, dao.guardados);
    }

    @Test
    @DisplayName("Nota menor a 0.0 lanza IllegalArgumentException")
    void notaMenorACero() {
        IllegalArgumentException ex = registrarConError(new Inscripcion(1, 2, -0.1, "ACTIVO"));

        assertEquals("La nota final debe estar entre 0 y 5.", ex.getMessage());
        assertEquals(0, dao.guardados);
    }

    @Test
    @DisplayName("Nota mayor a 5.0 lanza IllegalArgumentException")
    void notaMayorACinco() {
        IllegalArgumentException ex = registrarConError(new Inscripcion(1, 2, 5.1, "ACTIVO"));

        assertEquals("La nota final debe estar entre 0 y 5.", ex.getMessage());
        assertEquals(0, dao.guardados);
    }

    @Test
    @DisplayName("Acepta todos los estados válidos del sistema")
    void estadosValidos() {
        String[] validos = {"ACTIVO", "APROBADO", "REPROBADO", "CANCELADO", "activo", "aprobado"};
        for (String estado : validos) {
            Inscripcion i = new Inscripcion(1, 1, 3.0, estado);
            dao.datos.clear();
            dao.guardados = 0;
            servicio.registrarInscripcion(i);

            assertEquals(1, dao.guardados);
            assertEquals(estado.trim().toUpperCase(), i.getStatus());
        }
    }

    @Test
    @DisplayName("Estados no reconocidos o vacíos lanzan excepción")
    void estadosInvalidos() {
        String[] invalidos = {"", "   ", "MATRICULADO", "PENDIENTE", "DESCONOCIDO"};
        for (String estado : invalidos) {
            IllegalArgumentException ex = registrarConError(new Inscripcion(1, 1, 3.0, estado));
            assertEquals("El estado debe ser ACTIVO, APROBADO, REPROBADO o CANCELADO.", ex.getMessage());
        }
        assertEquals(0, dao.guardados);
    }

    @Test
    @DisplayName("Estado nulo lanza excepción")
    void estadoNulo() {
        IllegalArgumentException ex = registrarConError(new Inscripcion(1, 1, 3.0, null));

        assertEquals("El estado debe ser ACTIVO, APROBADO, REPROBADO o CANCELADO.", ex.getMessage());
    }

    @Test
    @DisplayName("No permite registrar dos veces al mismo estudiante en el mismo grupo (restricción UNIQUE)")
    void estudianteDuplicadoEnGrupo() {
        //  Precargar al estudiante 1 ya inscrito en el grupo 10
        dao.con(1, 1, 10, 3.5, "ACTIVO");

        //  Intentar inscribirlo nuevamente en el mismo grupo 10
        Inscripcion duplicada = new Inscripcion(1, 10, 4.0, "ACTIVO");
        IllegalArgumentException ex = registrarConError(duplicada);

        assertEquals("El estudiante ya esta inscrito en este grupo.", ex.getMessage());
        assertEquals(0, dao.guardados, "No debe registrarse en el DAO");
    }

    // CONSULTAS (LISTAR Y BUSCAR POR ID)

    @Test
    @DisplayName("obtenerTodasLasInscripciones retorna la lista completa del DAO")
    void listarTodas() {
        dao.con(1, 10, 20, 4.0, "ACTIVO")
           .con(2, 11, 20, 3.0, "ACTIVO")
           .con(3, 12, 21, 5.0, "APROBADO");

        List<Inscripcion> resultado = servicio.obtenerTodasLasInscripciones();

        assertEquals(3, resultado.size());
    }

    @Test
    @DisplayName("buscarInscripcionPorId devuelve la inscripción si existe y vacío si no")
    void buscarPorId() {
        dao.con(15, 1, 2, 4.2, "APROBADO");

        Optional<Inscripcion> encontrada = servicio.buscarInscripcionPorId(15);
        Optional<Inscripcion> noEncontrada = servicio.buscarInscripcionPorId(99);

        assertTrue(encontrada.isPresent());
        assertEquals(15, encontrada.get().getId());
        assertEquals(4.2, encontrada.get().getFinal_grade(), 0.001);
        assertFalse(noEncontrada.isPresent());
    }

    // ACTUALIZACIÓN DE INSCRIPCIONES

    @Test
    @DisplayName("Actualizar una inscripción existente modifica sus valores en el DAO")
    void actualizarValido() {
        dao.con(1, 10, 20, 3.0, "ACTIVO");

        Inscripcion actualizada = new Inscripcion(1, 10, 20, 4.8, "APROBADO", null);
        servicio.actualizarInscripcion(actualizada);

        assertEquals(1, dao.actualizados);
        assertEquals(4.8, dao.datos.get(0).getFinal_grade(), 0.001);
        assertEquals("APROBADO", dao.datos.get(0).getStatus());
    }

    @Test
    @DisplayName("Actualizar con ID menor o igual a cero o nulo lanza 'Inscripcion invalida.'")
    void actualizarInvalido() {
        IllegalArgumentException idCero = assertThrows(IllegalArgumentException.class,
                () -> servicio.actualizarInscripcion(new Inscripcion(0, 1, 1, 4.0, "ACTIVO", null)));
        IllegalArgumentException nulo = assertThrows(IllegalArgumentException.class,
                () -> servicio.actualizarInscripcion(null));

        assertEquals("Inscripcion invalida.", idCero.getMessage());
        assertEquals("Inscripcion invalida.", nulo.getMessage());
        assertEquals(0, dao.actualizados);
    }

    @Test
    @DisplayName("Actualizar una inscripción que no existe lanza 'Inscripcion no encontrada.'")
    void actualizarNoEncontrada() {
        Inscripcion inexistente = new Inscripcion(88, 1, 1, 4.0, "ACTIVO", null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> servicio.actualizarInscripcion(inexistente));

        assertEquals("Inscripcion no encontrada.", ex.getMessage());
        assertEquals(0, dao.actualizados);
    }

    @Test
    @DisplayName("Actualizar con datos inválidos (nota fuera de rango) no ejecuta actualización")
    void actualizarDatosInvalidos() {
        dao.con(1, 10, 20, 3.0, "ACTIVO");

        Inscripcion invalida = new Inscripcion(1, 10, 20, 6.0, "ACTIVO", null);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> servicio.actualizarInscripcion(invalida));

        assertEquals("La nota final debe estar entre 0 y 5.", ex.getMessage());
        assertEquals(0, dao.actualizados);
    }

    // ELIMINACIÓN DE INSCRIPCIONES

    @Test
    @DisplayName("Eliminar una inscripción existente la retira del DAO")
    void eliminarValido() {
        dao.con(5, 10, 20, 3.5, "ACTIVO");

        servicio.eliminarInscripcion(5);

        assertEquals(1, dao.eliminados);
        assertTrue(dao.datos.isEmpty());
    }

    @Test
    @DisplayName("Eliminar con ID menor o igual a cero lanza 'ID invalido.'")
    void eliminarIdInvalido() {
        IllegalArgumentException cero = assertThrows(IllegalArgumentException.class,
                () -> servicio.eliminarInscripcion(0));
        IllegalArgumentException negativo = assertThrows(IllegalArgumentException.class,
                () -> servicio.eliminarInscripcion(-3));

        assertEquals("ID invalido.", cero.getMessage());
        assertEquals("ID invalido.", negativo.getMessage());
        assertEquals(0, dao.eliminados);
    }

    @Test
    @DisplayName("Eliminar una inscripción que no existe lanza 'Inscripcion no encontrada.'")
    void eliminarNoEncontrada() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> servicio.eliminarInscripcion(77));

        assertEquals("Inscripcion no encontrada.", ex.getMessage());
        assertEquals(0, dao.eliminados);
    }

    // ========================================================================
    // PROCEDIMIENTOS ALMACENADOS (sp_eliminar_inscripcion_segura & sp_inscribir_estudiante)
    // ========================================================================

    @Test
    @DisplayName("eliminarConProcedimiento elimina una inscripción activa en el DAO")
    void eliminarConProcedimientoValido() {
        dao.con(10, 1, 2, 2.5, "ACTIVO");

        servicio.eliminarConProcedimiento(10);

        assertEquals(1, dao.eliminados);
        assertTrue(dao.datos.isEmpty());
    }

    @Test
    @DisplayName("eliminarConProcedimiento lanza excepción si la inscripción ya está APROBADA")
    void eliminarConProcedimientoBloqueaAprobado() {
        dao.con(12, 1, 2, 4.5, "APROBADO");

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> servicio.eliminarConProcedimiento(12));

        assertTrue(ex.getMessage().contains("APROBADA"), "No se debe permitir eliminar una materia aprobada");
        assertEquals(0, dao.eliminados);
    }

    @Test
    @DisplayName("eliminarConProcedimiento valida ID antes de llamar al DAO")
    void eliminarConProcedimientoValidaciones() {
        dao.con(1, 1, 1, 3.0, "ACTIVO");

        IllegalArgumentException idInvalido = assertThrows(IllegalArgumentException.class,
                () -> servicio.eliminarConProcedimiento(0));
        IllegalArgumentException noEncontrada = assertThrows(IllegalArgumentException.class,
                () -> servicio.eliminarConProcedimiento(999));

        assertEquals("ID invalido.", idInvalido.getMessage());
        assertEquals("Inscripcion no encontrada.", noEncontrada.getMessage());
        assertEquals(0, dao.eliminados);
    }

    @Test
    @DisplayName("registrarConProcedimiento matricula al estudiante delegando al SP")
    void registrarConProcedimientoValido() {
        servicio.registrarConProcedimiento(5, 8, 4.5, "ACTIVO");

        assertEquals(1, dao.guardados);
        assertEquals(1, dao.datos.size());
        assertEquals(5, dao.datos.get(0).getId_estudiante());
        assertEquals(8, dao.datos.get(0).getId_grupo());
        assertEquals(4.5, dao.datos.get(0).getFinal_grade(), 0.001);
        assertEquals("ACTIVO", dao.datos.get(0).getStatus());
    }

    @Test
    @DisplayName("registrarConProcedimiento valida campos obligatorios antes del DAO")
    void registrarConProcedimientoValidaciones() {
        IllegalArgumentException estInvalido = assertThrows(IllegalArgumentException.class,
                () -> servicio.registrarConProcedimiento(0, 1, 3.5, "ACTIVO"));
        IllegalArgumentException grpInvalido = assertThrows(IllegalArgumentException.class,
                () -> servicio.registrarConProcedimiento(1, -2, 3.5, "ACTIVO"));
        IllegalArgumentException notaInvalida = assertThrows(IllegalArgumentException.class,
                () -> servicio.registrarConProcedimiento(1, 1, 5.5, "ACTIVO"));

        assertEquals("El estudiante es obligatorio.", estInvalido.getMessage());
        assertEquals("El grupo es obligatorio.", grpInvalido.getMessage());
        assertEquals("La nota final debe estar entre 0 y 5.", notaInvalida.getMessage());
        assertEquals(0, dao.guardados);
    }
}
