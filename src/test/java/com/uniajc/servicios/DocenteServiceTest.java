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

import com.uniajc.modelo.Docente;
import com.uniajc.soporte.DaosFalsos.DocenteDaoFalso;
import com.uniajc.soporte.Util;

public class DocenteServiceTest {

    private DocenteDaoFalso dao;
    private DocenteService servicio;

    @BeforeEach
    void preparar() {
        dao = new DocenteDaoFalso();
        servicio = new DocenteService(dao);
    }

    private IllegalArgumentException registrarConError(Docente e) {
        return assertThrows(IllegalArgumentException.class, () -> servicio.registrarDocente(e));
    }

    //  registrar

    @Test
    @DisplayName("Registrar un docente valido lo guarda en el DAO")
    void registrarValido() {
        servicio.registrarDocente(new Docente(0, "Ana Rios", "Matematicas", "ana@uni.edu.co"));

        assertEquals(1, dao.guardados);
        assertEquals(1, dao.datos.size());
    }

    @Test
    @DisplayName("Registrar elimina los espacios sobrantes de nombre, especialidad y correo")
    void registrarLimpiaEspacios() {
        Docente e = new Docente(0, "  Ana ", " Matematicas  ", "  ana@uni.edu.co ");

        servicio.registrarDocente(e);

        assertEquals("Ana", e.getNombre());
        assertEquals("Matematicas", e.getEspecialidad());
        assertEquals("ana@uni.edu.co", e.getEmail());
    }

    @Test
    @DisplayName("Registrar un docente nulo lanza IllegalArgumentException")
    void registrarNulo() {
        IllegalArgumentException ex = registrarConError(null);

        assertEquals("El docente no puede ser nulo.", ex.getMessage());
        assertEquals(0, dao.guardados);
    }

    @Test
    @DisplayName("El nombre es obligatorio (vacio, solo espacios o nulo)")
    void nombreObligatorio() {
        assertEquals("El nombre es obligatorio.",
                registrarConError(new Docente(0, "", "Matematicas", "a@b.co")).getMessage());
        assertEquals("El nombre es obligatorio.",
                registrarConError(new Docente(0, "   ", "Matematicas", "a@b.co")).getMessage());
        assertEquals("El nombre es obligatorio.",
                registrarConError(new Docente(0, null, "Matematicas", "a@b.co")).getMessage());
        assertEquals(0, dao.guardados);
    }

    @Test
    @DisplayName("La especialidad es obligatoria (vacia, solo espacios o nula)")
    void especialidadObligatoria() {
        assertEquals("La especialidad es obligatoria.",
                registrarConError(new Docente(0, "Ana", "", "a@b.co")).getMessage());
        assertEquals("La especialidad es obligatoria.",
                registrarConError(new Docente(0, "Ana", "   ", "a@b.co")).getMessage());
        assertEquals("La especialidad es obligatoria.",
                registrarConError(new Docente(0, "Ana", null, "a@b.co")).getMessage());
    }

    @Test
    @DisplayName("Nombre y especialidad admiten maximo 100 caracteres")
    void longitudMaximaDeNombreYEspecialidad() {
        String largo = Util.repetir('a', 101);

        assertEquals("Nombre y especialidad admiten maximo 100 caracteres.",
                registrarConError(new Docente(0, largo, "Matematicas", "a@b.co")).getMessage());
        assertEquals("Nombre y especialidad admiten maximo 100 caracteres.",
                registrarConError(new Docente(0, "Ana", largo, "a@b.co")).getMessage());
    }

    @Test
    @DisplayName("Un nombre de exactamente 100 caracteres si se acepta")
    void nombreDeCienCaracteres() {
        servicio.registrarDocente(new Docente(0, Util.repetir('a', 100), "Matematicas", "a@b.co"));

        assertEquals(1, dao.guardados);
    }

    @Test
    @DisplayName("Un correo con formato invalido se rechaza")
    void correoInvalido() {
        String[] invalidos = { "sin-arroba", "a@b", "@uni.edu.co", "ana@.co", "ana perez@uni.co", "", "   ", null };

        for (String correo : invalidos) {
            IllegalArgumentException ex = registrarConError(new Docente(0, "Ana Rios", "Matematicas", correo));
            assertEquals("El correo no tiene un formato valido.", ex.getMessage(), "correo: " + correo);
        }
        assertEquals(0, dao.guardados);
    }

    @Test
    @DisplayName("Un correo de mas de 100 caracteres se rechaza")
    void correoDemasiadoLargo() {
        String correo = Util.repetir('a', 95) + "@uni.co"; // 102 caracteres

        assertEquals("El correo no tiene un formato valido.",
                registrarConError(new Docente(0, "Ana Rios", "Matematicas", correo)).getMessage());
    }

    @Test
    @DisplayName("Correos con puntos, signo + y subdominios se aceptan")
    void correosValidos() {
        servicio.registrarDocente(new Docente(0, "Ana Rios", "Matematicas", "ana.perez+x@mail.uni.edu.co"));

        assertEquals(1, dao.guardados);
    }

    @Test
    @DisplayName("No se registra un correo que ya existe (sin importar mayusculas)")
    void correoDuplicado() {
        dao.con(1, "Juan", "Diaz", "ana@uni.edu.co");

        IllegalArgumentException ex = registrarConError(new Docente(0, "Ana Rios", "Matematicas", "ANA@uni.edu.co"));

        assertEquals("Ya existe un docente con ese correo.", ex.getMessage());
        assertEquals(0, dao.guardados);
    }

    //  consultar

    @Test
    @DisplayName("obtenerTodosLosDocentes devuelve la lista del DAO")
    void listarTodos() {
        dao.con(1, "Ana", "Matematicas", "a@b.co").con(2, "Juan", "Diaz", "j@b.co");

        List<Docente> lista = servicio.obtenerTodosLosDocentes();

        assertEquals(2, lista.size());
    }

    @Test
    @DisplayName("buscarDocentePorId devuelve el docente si existe")
    void buscarExistente() {
        dao.con(5, "Ana", "Matematicas", "a@b.co");

        Optional<Docente> r = servicio.buscarDocentePorId(5);

        assertTrue(r.isPresent());
        assertEquals("Ana", r.get().getNombre());
    }

    @Test
    @DisplayName("buscarDocentePorId devuelve vacio si no existe")
    void buscarInexistente() {
        assertFalse(servicio.buscarDocentePorId(99).isPresent());
    }

    //  actualizar

    @Test
    @DisplayName("Actualizar un docente valido modifica sus datos")
    void actualizarValido() {
        dao.con(1, "Ana", "Matematicas", "ana@uni.edu.co");

        servicio.actualizarDocente(new Docente(1, "Ana Maria", "Matematicas", "ana@uni.edu.co"));

        assertEquals(1, dao.actualizados);
        assertEquals("Ana Maria", dao.datos.get(0).getNombre());
    }

    @Test
    @DisplayName("Actualizar con id menor o igual a cero o nulo es invalido")
    void actualizarInvalido() {
        IllegalArgumentException cero = assertThrows(IllegalArgumentException.class,
                () -> servicio.actualizarDocente(new Docente(0, "Ana Rios", "Matematicas", "a@b.co")));
        IllegalArgumentException nulo = assertThrows(IllegalArgumentException.class,
                () -> servicio.actualizarDocente(null));

        assertEquals("Docente invalido.", cero.getMessage());
        assertEquals("Docente invalido.", nulo.getMessage());
        assertEquals(0, dao.actualizados);
    }

    @Test
    @DisplayName("Actualizar un docente que no existe lanza error")
    void actualizarNoEncontrado() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> servicio.actualizarDocente(new Docente(8, "Ana", "Matematicas", "a@b.co")));

        assertEquals("Docente no encontrado.", ex.getMessage());
    }

    @Test
    @DisplayName("Actualizar valida los datos igual que registrar")
    void actualizarValidaDatos() {
        dao.con(1, "Ana", "Matematicas", "ana@uni.edu.co");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> servicio.actualizarDocente(new Docente(1, "", "Matematicas", "ana@uni.edu.co")));

        assertEquals("El nombre es obligatorio.", ex.getMessage());
        assertEquals(0, dao.actualizados);
    }

    @Test
    @DisplayName("No se puede usar el correo de otro docente")
    void actualizarConCorreoDeOtro() {
        dao.con(1, "Ana", "Matematicas", "ana@uni.edu.co").con(2, "Juan", "Diaz", "juan@uni.edu.co");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> servicio.actualizarDocente(new Docente(2, "Juan", "Diaz", "ana@uni.edu.co")));

        assertEquals("Ese correo ya lo usa otro docente.", ex.getMessage());
        assertEquals(0, dao.actualizados);
    }

    @Test
    @DisplayName("Un docente puede conservar su propio correo al actualizar")
    void actualizarConSuPropioCorreo() {
        dao.con(1, "Ana", "Matematicas", "ana@uni.edu.co");

        servicio.actualizarDocente(new Docente(1, "Ana", "Fisica", "ana@uni.edu.co"));

        assertEquals(1, dao.actualizados);
    }

    //  eliminar

    @Test
    @DisplayName("Eliminar un docente existente lo borra")
    void eliminarExistente() {
        dao.con(1, "Ana", "Matematicas", "a@b.co");

        servicio.eliminarDocente(1);

        assertEquals(1, dao.eliminados);
        assertEquals(0, dao.datos.size());
    }

    @Test
    @DisplayName("Eliminar con id menor o igual a cero es invalido")
    void eliminarIdInvalido() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> servicio.eliminarDocente(0));

        assertEquals("ID invalido.", ex.getMessage());
        assertEquals(0, dao.eliminados);
    }

    @Test
    @DisplayName("Eliminar un docente que no existe lanza error")
    void eliminarNoEncontrado() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> servicio.eliminarDocente(50));

        assertEquals("Docente no encontrado.", ex.getMessage());
        assertEquals(0, dao.eliminados);
    }
}
