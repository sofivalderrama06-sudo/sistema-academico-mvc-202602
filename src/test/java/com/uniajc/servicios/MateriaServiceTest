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

import com.uniajc.modelo.Materia;
import com.uniajc.soporte.DaosFalsos.MateriaDaoFalso;

public class MateriaServiceTest {

    private MateriaDaoFalso dao;
    private MateriaService servicio;

    @BeforeEach
    void preparar() {
        dao = new MateriaDaoFalso();
        servicio = new MateriaService(dao);
    }

    private IllegalArgumentException registrarConError(Materia m) {
        return assertThrows(IllegalArgumentException.class, () -> servicio.registrarMateria(m));
    }

    // ------------------------------------------------------------ registrar

    @Test
    @DisplayName("Registrar una materia valida la guarda en el DAO")
    void registrarValida() {
        servicio.registrarMateria(new Materia(0, "Calculo", 3, null));

        assertEquals(1, dao.guardados);
        assertEquals("Calculo", dao.datos.get(0).getName());
        assertEquals(3, dao.datos.get(0).getCredits());
    }

    @Test
    @DisplayName("Registrar una materia nula lanza IllegalArgumentException")
    void registrarNula() {
        IllegalArgumentException ex = registrarConError(null);

        assertEquals("La materia no puede ser nula.", ex.getMessage());
        assertEquals(0, dao.guardados);
    }

    @Test
    @DisplayName("El nombre es obligatorio (vacio, solo espacios o nulo)")
    void nombreObligatorio() {
        assertEquals("El nombre es obligatorio.", registrarConError(new Materia(0, "", 3, null)).getMessage());
        assertEquals("El nombre es obligatorio.", registrarConError(new Materia(0, "   ", 3, null)).getMessage());
        assertEquals("El nombre es obligatorio.", registrarConError(new Materia(0, null, 3, null)).getMessage());
        assertEquals(0, dao.guardados);
    }

    @Test
    @DisplayName("Los creditos deben ser mayores que cero")
    void creditosPositivos() {
        assertEquals("Los creditos deben ser mayores que cero.",
                registrarConError(new Materia(0, "Calculo", 0, null)).getMessage());
        assertEquals("Los creditos deben ser mayores que cero.",
                registrarConError(new Materia(0, "Calculo", -2, null)).getMessage());
        assertEquals(0, dao.guardados);
    }

    @Test
    @DisplayName("Un credito (el minimo valido) se acepta")
    void unCreditoEsValido() {
        servicio.registrarMateria(new Materia(0, "Seminario", 1, null));

        assertEquals(1, dao.guardados);
    }

    // ------------------------------------------------------------ consultar

    @Test
    @DisplayName("obtenerTodasLasMaterias devuelve la lista del DAO")
    void listarTodas() {
        dao.con(1, "Calculo", 3).con(2, "Fisica", 4);

        List<Materia> lista = servicio.obtenerTodasLasMaterias();

        assertEquals(2, lista.size());
    }

    @Test
    @DisplayName("buscarMateriaPorId devuelve la materia si existe")
    void buscarExistente() {
        dao.con(5, "Calculo", 3);

        Optional<Materia> r = servicio.buscarMateriaPorId(5);

        assertTrue(r.isPresent());
        assertEquals("Calculo", r.get().getName());
    }

    @Test
    @DisplayName("buscarMateriaPorId devuelve vacio si no existe")
    void buscarInexistente() {
        assertFalse(servicio.buscarMateriaPorId(99).isPresent());
    }

    // ------------------------------------------------------------ actualizar

    @Test
    @DisplayName("Actualizar una materia valida modifica sus datos")
    void actualizarValida() {
        dao.con(1, "Calculo", 3);

        servicio.actualizarMateria(new Materia(1, "Calculo II", 4, null));

        assertEquals(1, dao.actualizados);
        assertEquals("Calculo II", dao.datos.get(0).getName());
        assertEquals(4, dao.datos.get(0).getCredits());
    }

    @Test
    @DisplayName("Actualizar con id menor o igual a cero o nula es invalido")
    void actualizarInvalida() {
        IllegalArgumentException cero = assertThrows(IllegalArgumentException.class,
                () -> servicio.actualizarMateria(new Materia(0, "Calculo", 3, null)));
        IllegalArgumentException nula = assertThrows(IllegalArgumentException.class,
                () -> servicio.actualizarMateria(null));

        assertEquals("Materia invalida.", cero.getMessage());
        assertEquals("Materia invalida.", nula.getMessage());
        assertEquals(0, dao.actualizados);
    }

    @Test
    @DisplayName("Actualizar una materia que no existe lanza error")
    void actualizarNoEncontrada() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> servicio.actualizarMateria(new Materia(9, "Calculo", 3, null)));

        assertEquals("Materia no encontrada.", ex.getMessage());
    }

    @Test
    @DisplayName("Actualizar valida el nombre y los creditos")
    void actualizarValidaDatos() {
        dao.con(1, "Calculo", 3);

        IllegalArgumentException nombre = assertThrows(IllegalArgumentException.class,
                () -> servicio.actualizarMateria(new Materia(1, " ", 3, null)));
        IllegalArgumentException creditos = assertThrows(IllegalArgumentException.class,
                () -> servicio.actualizarMateria(new Materia(1, "Calculo", 0, null)));

        assertEquals("El nombre es obligatorio.", nombre.getMessage());
        assertEquals("Los creditos deben ser mayores que cero.", creditos.getMessage());
        assertEquals(0, dao.actualizados);
    }

    // ------------------------------------------------------------ eliminar

    @Test
    @DisplayName("Eliminar una materia existente la borra")
    void eliminarExistente() {
        dao.con(1, "Calculo", 3);

        servicio.eliminarMateria(1);

        assertEquals(1, dao.eliminados);
        assertEquals(0, dao.datos.size());
    }

    @Test
    @DisplayName("Eliminar con id menor o igual a cero es invalido")
    void eliminarIdInvalido() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> servicio.eliminarMateria(-1));

        assertEquals("ID invalido.", ex.getMessage());
        assertEquals(0, dao.eliminados);
    }

    @Test
    @DisplayName("Eliminar una materia que no existe lanza error")
    void eliminarNoEncontrada() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> servicio.eliminarMateria(50));

        assertEquals("Materia no encontrada.", ex.getMessage());
        assertEquals(0, dao.eliminados);
    }
}
