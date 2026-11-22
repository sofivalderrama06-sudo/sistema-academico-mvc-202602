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

import com.uniajc.modelo.Docente;
import com.uniajc.soporte.BdFalsa;

/**
 * PRUEBAS DE INTEGRACIÓN / CONTROLADOR: ControladorDocenteTest
 * ¿Cómo funciona esta prueba?
 * 1. Simulación JDBC: Utiliza 'BdFalsa' para interceptar la conexión estática
 *    de DatabaseConexion. Así, cuando el Controlador crea su propio DocenteDao,
 *    el DAO se conecta a la base falsa en memoria en lugar de a PostgreSQL.
 * 2. Validación de SQL: Permite verificar que el flujo Controlador -> Servicio -> DAO
 *    genere las sentencias SQL correctas (SELECT para verificar duplicados, INSERT, etc.)
 *    y que los parámetros (?, ?) contengan los valores exactos.
 * 3. Aislamiento por Prueba:
 *    - @BeforeEach: Instala una conexión simulada limpia antes de cada método.
 *    - @AfterEach: Desinstala la base falsa para no afectar otras pruebas.
 */
public class ControladorDocenteTest {

    private BdFalsa bd;
    private ControladorDocente controlador;

    @BeforeEach
    void preparar() {
        bd = new BdFalsa().instalar();
        controlador = new ControladorDocente();
    }

    @AfterEach
    void limpiar() {
        BdFalsa.desinstalar();
    }

    @Test
    @DisplayName("registrar verifica el correo y luego inserta el docente")
    void registrar() {
        controlador.registrar("Ana", "Matematicas", "ana@uni.edu.co");

        assertEquals(2, bd.sqls.size());
        assertTrue(bd.sqls.get(0).startsWith("SELECT"));   // busca el correo repetido
        assertTrue(bd.sqls.get(1).startsWith("INSERT INTO"));
        assertEquals("Ana", bd.ultimosParametros().get(1));
        assertEquals("Matematicas", bd.ultimosParametros().get(2));
        assertEquals("ana@uni.edu.co", bd.ultimosParametros().get(3));
    }

    @Test
    @DisplayName("registrar con datos invalidos lanza error y no toca la base de datos")
    void registrarInvalido() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> controlador.registrar("", "Matematicas", "ana@uni.edu.co"));

        assertEquals("El nombre es obligatorio.", ex.getMessage());
        assertTrue(bd.sqls.isEmpty());
    }

    @Test
    @DisplayName("registrar con un correo ya existente lanza error y no inserta")
    void registrarCorreoRepetido() {
        bd.fila("id", 1, "name", "Otro", "specialty", "Fisica", "email", "ana@uni.edu.co");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> controlador.registrar("Ana", "Matematicas", "ana@uni.edu.co"));

        assertEquals("El correo ya esta registrado.", ex.getMessage());
        assertEquals(1, bd.sqls.size()); // solo el SELECT, ningun INSERT
    }

    @Test
    @DisplayName("listar devuelve todos los docentes encontrados en la base")
    void listar() {
        bd.fila("id", 1, "name", "Ana", "specialty", "Matematicas", "email", "ana@uni.edu.co")
          .fila("id", 2, "name", "Luis", "specialty", "Fisica", "email", "luis@uni.edu.co");

        List<Docente> docentes = controlador.listar();

        assertEquals(2, docentes.size());
        assertEquals("Ana", docentes.get(0).getNombre());
        assertEquals("Luis", docentes.get(1).getNombre());
    }

    @Test
    @DisplayName("buscarPorId devuelve el docente cuando existe")
    void buscarPorIdExiste() {
        bd.fila("id", 5, "name", "Ana", "specialty", "Matematicas", "email", "ana@uni.edu.co");

        Optional<Docente> r = controlador.buscarPorId(5);

        assertTrue(r.isPresent());
        assertEquals(5, r.get().getId());
        assertEquals(5, bd.ultimosParametros().get(1));
    }

    @Test
    @DisplayName("buscarPorId con id invalido lanza error y no consulta la base")
    void buscarPorIdInvalido() {
        assertThrows(IllegalArgumentException.class, () -> controlador.buscarPorId(0));
        assertThrows(IllegalArgumentException.class, () -> controlador.buscarPorId(-3));
        assertTrue(bd.sqls.isEmpty());
    }

    @Test
    @DisplayName("actualizar modifica el docente en la base de datos")
    void actualizar() {
        bd.fila("id", 1, "name", "Ana", "specialty", "Matematicas", "email", "ana@uni.edu.co");

        controlador.actualizar(1, "Ana Maria", "Algebra", "ana.maria@uni.edu.co");

        assertEquals(2, bd.sqls.size());
        assertTrue(bd.sqls.get(1).startsWith("UPDATE"));
        assertEquals("Ana Maria", bd.ultimosParametros().get(1));
        assertEquals("Algebra", bd.ultimosParametros().get(2));
        assertEquals("ana.maria@uni.edu.co", bd.ultimosParametros().get(3));
        assertEquals(1, bd.ultimosParametros().get(4));
    }

    @Test
    @DisplayName("actualizar con datos invalidos lanza error y no actualiza")
    void actualizarInvalido() {
        assertThrows(IllegalArgumentException.class,
                () -> controlador.actualizar(1, "", "Matematicas", "ana@uni.edu.co"));
        assertThrows(IllegalArgumentException.class,
                () -> controlador.actualizar(0, "Ana", "Matematicas", "ana@uni.edu.co"));
        assertTrue(bd.sqls.isEmpty());
    }

    @Test
    @DisplayName("eliminar borra el docente de la base de datos")
    void eliminar() {
        bd.fila("id", 1, "name", "Ana", "specialty", "Matematicas", "email", "ana@uni.edu.co");

        controlador.eliminar(1);

        assertEquals(2, bd.sqls.size()); // SELECT para validar que exista + DELETE
        assertTrue(bd.sqls.get(1).startsWith("DELETE"));
        assertEquals(1, bd.ultimosParametros().get(1));
    }

    @Test
    @DisplayName("eliminar con id invalido lanza error y no consulta la base")
    void eliminarInvalido() {
        assertThrows(IllegalArgumentException.class, () -> controlador.eliminar(0));
        assertThrows(IllegalArgumentException.class, () -> controlador.eliminar(-1));
        assertTrue(bd.sqls.isEmpty());
    }
}
