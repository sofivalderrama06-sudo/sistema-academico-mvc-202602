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

import com.uniajc.modelo.Grupo;
import com.uniajc.soporte.BdFalsa;

public class ControladorGrupoTest {

    private BdFalsa bd;
    private ControladorGrupo controlador;

    @BeforeEach
    void preparar() {
        bd = new BdFalsa().instalar();
        controlador = new ControladorGrupo();
    }

    @AfterEach
    void limpiar() {
        BdFalsa.desinstalar();
    }

    private void filaDeGrupo(int id, int materia, int docente, String aula, String horario) {
        bd.fila("id", id, "id_materia", materia, "id_docente", docente,
                "classroom", aula, "schedule", horario, "created_at", null);
    }

    @Test
    @DisplayName("registrar inserta el grupo con materia, docente, aula y horario")
    void registrar() {
        controlador.registrar(2, 3, "Aula 101", "Lunes 8-10");

        assertEquals(1, bd.sqls.size());
        assertTrue(bd.ultimoSql().startsWith("INSERT INTO"));
        assertEquals(2, bd.ultimosParametros().get(1));
        assertEquals(3, bd.ultimosParametros().get(2));
        assertEquals("Aula 101", bd.ultimosParametros().get(3));
        assertEquals("Lunes 8-10", bd.ultimosParametros().get(4));
    }

    @Test
    @DisplayName("registrar con datos invalidos lanza error y no toca la base")
    void registrarInvalido() {
        IllegalArgumentException ids = assertThrows(IllegalArgumentException.class,
                () -> controlador.registrar(0, 3, "A", "B"));
        IllegalArgumentException aula = assertThrows(IllegalArgumentException.class,
                () -> controlador.registrar(1, 3, "", "B"));

        assertEquals("Los ID de materia y docente deben ser mayores que cero.", ids.getMessage());
        assertEquals("El aula es obligatoria.", aula.getMessage());
        assertTrue(bd.sqls.isEmpty());
    }

    @Test
    @DisplayName("listar devuelve los grupos que entrega la base de datos")
    void listar() {
        filaDeGrupo(1, 2, 3, "Aula 101", "Lunes");
        filaDeGrupo(2, 4, 5, "Aula 202", "Martes");

        List<Grupo> lista = controlador.listar();

        assertEquals(2, lista.size());
        assertEquals("Aula 202", lista.get(1).getClassroom());
    }

    @Test
    @DisplayName("buscarPorId devuelve el grupo si existe y vacio si no")
    void buscarPorId() {
        assertFalse(controlador.buscarPorId(1).isPresent());

        filaDeGrupo(1, 2, 3, "Aula 101", "Lunes");
        Optional<Grupo> r = controlador.buscarPorId(1);

        assertTrue(r.isPresent());
        assertEquals("Aula 101", r.get().getClassroom());
    }

    @Test
    @DisplayName("actualizar ejecuta el UPDATE con los datos recibidos")
    void actualizar() {
        filaDeGrupo(1, 2, 3, "Aula 101", "Lunes");

        controlador.actualizar(1, 4, 5, "Aula 303", "Viernes");

        assertTrue(bd.ultimoSql().startsWith("UPDATE"));
        assertEquals(4, bd.ultimosParametros().get(1));
        assertEquals(5, bd.ultimosParametros().get(2));
        assertEquals("Aula 303", bd.ultimosParametros().get(3));
        assertEquals("Viernes", bd.ultimosParametros().get(4));
        assertEquals(1, bd.ultimosParametros().get(5));
    }

    @Test
    @DisplayName("actualizar un grupo que no existe lanza error")
    void actualizarInexistente() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> controlador.actualizar(5, 1, 1, "A", "B"));

        assertEquals("Grupo no encontrado.", ex.getMessage());
    }

    @Test
    @DisplayName("eliminar borra el grupo cuando existe")
    void eliminar() {
        filaDeGrupo(3, 1, 1, "A", "B");

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
