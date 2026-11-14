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

import com.uniajc.modelo.Materia;
import com.uniajc.soporte.BdFalsa;

public class ControladorMateriaTest {

    private BdFalsa bd;
    private ControladorMateria controlador;

    @BeforeEach
    void preparar() {
        bd = new BdFalsa().instalar();
        controlador = new ControladorMateria();
    }

    @AfterEach
    void limpiar() {
        BdFalsa.desinstalar();
    }

    @Test
    @DisplayName("registrar inserta la materia con su nombre y creditos")
    void registrar() {
        controlador.registrar("Calculo", 3);

        assertEquals(1, bd.sqls.size());
        assertTrue(bd.ultimoSql().startsWith("INSERT INTO"));
        assertEquals("Calculo", bd.ultimosParametros().get(1));
        assertEquals(3, bd.ultimosParametros().get(2));
    }

    @Test
    @DisplayName("registrar con nombre vacio o creditos en cero lanza error y no toca la base")
    void registrarInvalido() {
        IllegalArgumentException nombre = assertThrows(IllegalArgumentException.class,
                () -> controlador.registrar(" ", 3));
        IllegalArgumentException creditos = assertThrows(IllegalArgumentException.class,
                () -> controlador.registrar("Calculo", 0));

        assertEquals("El nombre es obligatorio.", nombre.getMessage());
        assertEquals("Los creditos deben ser mayores que cero.", creditos.getMessage());
        assertTrue(bd.sqls.isEmpty());
    }

    @Test
    @DisplayName("listar devuelve las materias que entrega la base de datos")
    void listar() {
        bd.fila("id", 1, "name", "Calculo", "credits", 3, "created_at", null);
        bd.fila("id", 2, "name", "Fisica", "credits", 4, "created_at", null);

        List<Materia> lista = controlador.listar();

        assertEquals(2, lista.size());
        assertEquals("Fisica", lista.get(1).getName());
    }

    @Test
    @DisplayName("buscarPorId devuelve la materia si existe y vacio si no")
    void buscarPorId() {
        assertFalse(controlador.buscarPorId(1).isPresent());

        bd.fila("id", 1, "name", "Calculo", "credits", 3, "created_at", null);
        Optional<Materia> r = controlador.buscarPorId(1);

        assertTrue(r.isPresent());
        assertEquals("Calculo", r.get().getName());
    }

    @Test
    @DisplayName("actualizar ejecuta el UPDATE con los datos recibidos")
    void actualizar() {
        bd.fila("id", 1, "name", "Calculo", "credits", 3, "created_at", null);

        controlador.actualizar(1, "Calculo II", 4);

        assertTrue(bd.ultimoSql().startsWith("UPDATE"));
        assertEquals("Calculo II", bd.ultimosParametros().get(1));
        assertEquals(4, bd.ultimosParametros().get(2));
        assertEquals(1, bd.ultimosParametros().get(3));
    }

    @Test
    @DisplayName("actualizar una materia que no existe lanza error")
    void actualizarInexistente() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> controlador.actualizar(5, "Calculo", 3));

        assertEquals("Materia no encontrada.", ex.getMessage());
    }

    @Test
    @DisplayName("eliminar borra la materia cuando existe")
    void eliminar() {
        bd.fila("id", 3, "name", "Calculo", "credits", 3, "created_at", null);

        controlador.eliminar(3);

        assertTrue(bd.ultimoSql().startsWith("DELETE FROM"));
        assertEquals(3, bd.ultimosParametros().get(1));
    }

    @Test
    @DisplayName("eliminar con id invalido lanza error")
    void eliminarIdInvalido() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> controlador.eliminar(-1));

        assertEquals("ID invalido.", ex.getMessage());
    }
}
