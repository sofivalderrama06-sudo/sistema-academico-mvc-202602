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

import com.uniajc.modelo.Grupo;
import com.uniajc.soporte.DaosFalsos.GrupoDaoFalso;

public class GrupoServiceTest {

    private GrupoDaoFalso dao;
    private GrupoService servicio;

    @BeforeEach
    void preparar() {
        dao = new GrupoDaoFalso();
        servicio = new GrupoService(dao);
    }

    private IllegalArgumentException registrarConError(Grupo g) {
        return assertThrows(IllegalArgumentException.class, () -> servicio.registrarGrupo(g));
    }

    // ------------------------------------------------------------ registrar

    @Test
    @DisplayName("Registrar un grupo valido lo guarda en el DAO")
    void registrarValido() {
        servicio.registrarGrupo(new Grupo(1, 2, "Aula 101", "Lunes 8-10", null));

        assertEquals(1, dao.guardados);
    }

    @Test
    @DisplayName("Registrar elimina los espacios sobrantes de aula y horario")
    void registrarLimpiaEspacios() {
        Grupo g = new Grupo(1, 2, "  Aula 101 ", " Lunes 8-10  ", null);

        servicio.registrarGrupo(g);

        assertEquals("Aula 101", g.getClassroom());
        assertEquals("Lunes 8-10", g.getSchedule());
    }

    @Test
    @DisplayName("Registrar un grupo nulo lanza IllegalArgumentException")
    void registrarNulo() {
        IllegalArgumentException ex = registrarConError(null);

        assertEquals("El grupo no puede ser nulo.", ex.getMessage());
        assertEquals(0, dao.guardados);
    }

    @Test
    @DisplayName("Los id de materia y docente deben ser mayores que cero")
    void idsPositivos() {
        String mensaje = "Los ID de materia y docente deben ser mayores que cero.";

        assertEquals(mensaje, registrarConError(new Grupo(0, 2, "A", "B", null)).getMessage());
        assertEquals(mensaje, registrarConError(new Grupo(1, 0, "A", "B", null)).getMessage());
        assertEquals(mensaje, registrarConError(new Grupo(-1, -1, "A", "B", null)).getMessage());
        assertEquals(0, dao.guardados);
    }

    @Test
    @DisplayName("El aula es obligatoria (vacia, solo espacios o nula)")
    void aulaObligatoria() {
        assertEquals("El aula es obligatoria.", registrarConError(new Grupo(1, 2, "", "B", null)).getMessage());
        assertEquals("El aula es obligatoria.", registrarConError(new Grupo(1, 2, "   ", "B", null)).getMessage());
        assertEquals("El aula es obligatoria.", registrarConError(new Grupo(1, 2, null, "B", null)).getMessage());
    }

    @Test
    @DisplayName("El horario es obligatorio (vacio, solo espacios o nulo)")
    void horarioObligatorio() {
        assertEquals("El horario es obligatorio.", registrarConError(new Grupo(1, 2, "A", "", null)).getMessage());
        assertEquals("El horario es obligatorio.", registrarConError(new Grupo(1, 2, "A", "  ", null)).getMessage());
        assertEquals("El horario es obligatorio.", registrarConError(new Grupo(1, 2, "A", null, null)).getMessage());
    }

    // ------------------------------------------------------------ consultar

    @Test
    @DisplayName("obtenerTodosLosGrupos devuelve la lista del DAO")
    void listarTodos() {
        dao.con(1, 1, 1, "A", "H1").con(2, 1, 2, "B", "H2");

        List<Grupo> lista = servicio.obtenerTodosLosGrupos();

        assertEquals(2, lista.size());
    }

    @Test
    @DisplayName("buscarGrupoPorId devuelve el grupo si existe y vacio si no")
    void buscarPorId() {
        dao.con(5, 1, 1, "A", "H1");

        Optional<Grupo> existe = servicio.buscarGrupoPorId(5);

        assertTrue(existe.isPresent());
        assertEquals("A", existe.get().getClassroom());
        assertFalse(servicio.buscarGrupoPorId(99).isPresent());
    }

    // ------------------------------------------------------------ actualizar

    @Test
    @DisplayName("Actualizar un grupo valido modifica sus datos")
    void actualizarValido() {
        dao.con(1, 1, 1, "Aula 101", "Lunes");

        servicio.actualizarGrupo(new Grupo(1, 2, 3, "Aula 202", "Martes", null));

        assertEquals(1, dao.actualizados);
        assertEquals("Aula 202", dao.datos.get(0).getClassroom());
        assertEquals(2, dao.datos.get(0).getId_materia());
    }

    @Test
    @DisplayName("Actualizar con id menor o igual a cero o nulo es invalido")
    void actualizarInvalido() {
        IllegalArgumentException cero = assertThrows(IllegalArgumentException.class,
                () -> servicio.actualizarGrupo(new Grupo(0, 1, 1, "A", "B", null)));
        IllegalArgumentException nulo = assertThrows(IllegalArgumentException.class,
                () -> servicio.actualizarGrupo(null));

        assertEquals("Grupo invalido.", cero.getMessage());
        assertEquals("Grupo invalido.", nulo.getMessage());
        assertEquals(0, dao.actualizados);
    }

    @Test
    @DisplayName("Actualizar un grupo que no existe lanza error")
    void actualizarNoEncontrado() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> servicio.actualizarGrupo(new Grupo(9, 1, 1, "A", "B", null)));

        assertEquals("Grupo no encontrado.", ex.getMessage());
    }

    @Test
    @DisplayName("Actualizar valida los datos igual que registrar")
    void actualizarValidaDatos() {
        dao.con(1, 1, 1, "Aula 101", "Lunes");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> servicio.actualizarGrupo(new Grupo(1, 1, 1, "", "Lunes", null)));

        assertEquals("El aula es obligatoria.", ex.getMessage());
        assertEquals(0, dao.actualizados);
    }

    // ------------------------------------------------------------ eliminar

    @Test
    @DisplayName("Eliminar un grupo existente lo borra")
    void eliminarExistente() {
        dao.con(1, 1, 1, "A", "B");

        servicio.eliminarGrupo(1);

        assertEquals(1, dao.eliminados);
        assertEquals(0, dao.datos.size());
    }

    @Test
    @DisplayName("Eliminar con id menor o igual a cero es invalido")
    void eliminarIdInvalido() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> servicio.eliminarGrupo(0));

        assertEquals("ID invalido.", ex.getMessage());
    }

    @Test
    @DisplayName("Eliminar un grupo que no existe lanza error")
    void eliminarNoEncontrado() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> servicio.eliminarGrupo(77));

        assertEquals("Grupo no encontrado.", ex.getMessage());
        assertEquals(0, dao.eliminados);
    }
}
