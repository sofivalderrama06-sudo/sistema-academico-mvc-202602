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

import com.uniajc.modelo.Estudiante;
import com.uniajc.soporte.BdFalsa;

/**
 * Prueba el flujo completo Controlador -> Servicio -> DAO sobre una base de
 * datos falsa. El controlador crea sus propias dependencias, por eso se
 * controla la base que usa el DAO.
 */
public class ControladorEstudianteTest {

    private BdFalsa bd;
    private ControladorEstudiante controlador;

    @BeforeEach
    void preparar() {
        //  Se instala una base de datos falsa en memoria y se crea un controlador que la usará
        bd = new BdFalsa().instalar();
        controlador = new ControladorEstudiante();
    }

    @AfterEach
    void limpiar() {
        //  Se desinstala la base de datos falsa para no afectar otras pruebas
        BdFalsa.desinstalar();
    }

    @Test
    @DisplayName("registrar verifica el correo y luego inserta el estudiante")
    void registrar() {
        //  Se registra un estudiante con nombre, apellido y correo
        controlador.registrar("Ana", "Perez", "ana@uni.edu.co");
        //  Se valida que se hayan ejecutado dos sentencias SQL: una SELECT para verificar duplicados y un INSERT
        assertEquals(2, bd.sqls.size());
        assertTrue(bd.sqls.get(0).startsWith("SELECT"));   // busca el correo repetido
        assertTrue(bd.sqls.get(1).startsWith("INSERT INTO"));
        assertEquals("Ana", bd.ultimosParametros().get(1));
        assertEquals("Perez", bd.ultimosParametros().get(2));
        assertEquals("ana@uni.edu.co", bd.ultimosParametros().get(3));
    }

    @Test
    @DisplayName("registrar con datos invalidos lanza error y no toca la base de datos")
    void registrarInvalido() {
        //  Se intenta registrar un estudiante con nombre vacío, lo que debería lanzar una excepción
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> controlador.registrar("", "Perez", "ana@uni.edu.co"));

        assertEquals("El nombre es obligatorio.", ex.getMessage());
        assertTrue(bd.sqls.isEmpty());//  No se ejecutó ninguna sentencia SQL
    }

    @Test
    @DisplayName("registrar con un correo ya existente lanza error y no inserta")
    void registrarCorreoRepetido() {
        bd.fila("id", 1, "name", "Otro", "lastname", "Apellido", "email", "ana@uni.edu.co");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> controlador.registrar("Ana", "Perez", "ana@uni.edu.co"));

        assertEquals("Ya existe un estudiante con ese correo.", ex.getMessage());
        assertEquals(0, bd.actualizacionesEjecutadas);
    }

    @Test
    @DisplayName("listar devuelve los estudiantes que entrega la base de datos")
    void listar() {
        bd.fila("id", 1, "name", "Ana", "lastname", "Perez", "email", "a@b.co");
        bd.fila("id", 2, "name", "Juan", "lastname", "Diaz", "email", "j@b.co");

        List<Estudiante> lista = controlador.listar();//  Se obtiene la lista de estudiantes desde el controlador, que a su vez consulta la base de datos falsa
        //  Se valida que la lista contenga los estudiantes esperados
        assertEquals(2, lista.size());
        assertEquals("Juan", lista.get(1).getNombre());
    }

    @Test
    @DisplayName("buscarPorId devuelve el estudiante si existe y vacio si no")
    void buscarPorId() {
        assertFalse(controlador.buscarPorId(1).isPresent());

        bd.fila("id", 1, "name", "Ana", "lastname", "Perez", "email", "a@b.co");
        Optional<Estudiante> r = controlador.buscarPorId(1);

        assertTrue(r.isPresent());
        assertEquals("Ana", r.get().getNombre());
    }

    @Test
    @DisplayName("actualizar arma el estudiante con los datos recibidos y ejecuta el UPDATE")
    void actualizar() {
        //  Se simula que el estudiante con id=1 ya existe en la base de datos
        bd.fila("id", 1, "name", "Ana", "lastname", "Perez", "email", "ana@uni.edu.co");
        //  Se actualizan los datos del estudiante con id=1
        controlador.actualizar(1, "Ana Maria", "Perez", "ana@uni.edu.co");
        //  Se valida que se haya ejecutado un UPDATE y que los parámetros sean los esperados
        assertTrue(bd.ultimoSql().startsWith("UPDATE"));
        assertEquals("Ana Maria", bd.ultimosParametros().get(1));
        assertEquals(1, bd.ultimosParametros().get(4));
    }

    @Test
    @DisplayName("actualizar un estudiante que no existe lanza error")
    void actualizarInexistente() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> controlador.actualizar(5, "Ana", "Perez", "a@b.co"));

        assertEquals("Estudiante no encontrado.", ex.getMessage());
    }

    @Test
    @DisplayName("eliminar borra el estudiante cuando existe")
    void eliminar() {
        bd.fila("id", 3, "name", "Ana", "lastname", "Perez", "email", "a@b.co");

        controlador.eliminar(3);

        assertTrue(bd.ultimoSql().startsWith("DELETE FROM"));
        assertEquals(3, bd.ultimosParametros().get(1));
    }

    @Test
    @DisplayName("eliminar con id invalido lanza error")
    void eliminarIdInvalido() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> controlador.eliminar(0));

        assertEquals("ID invalido.", ex.getMessage());
    }
}
