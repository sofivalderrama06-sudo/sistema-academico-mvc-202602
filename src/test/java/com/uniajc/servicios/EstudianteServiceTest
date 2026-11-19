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

import com.uniajc.modelo.Estudiante;
import com.uniajc.soporte.DaosFalsos.EstudianteDaoFalso;
import com.uniajc.soporte.Util;

public class EstudianteServiceTest {

    private EstudianteDaoFalso dao;
    private EstudianteService servicio;

    @BeforeEach
    void preparar() {
        dao = new EstudianteDaoFalso();
        servicio = new EstudianteService(dao);
    }

    private IllegalArgumentException registrarConError(Estudiante e) {
        return assertThrows(IllegalArgumentException.class, () -> servicio.registrarEstudiante(e));
    }

    // ------------------------------------------------------------ registrar

    @Test
    @DisplayName("Registrar un estudiante valido lo guarda en el DAO")
    void registrarValido() {
        servicio.registrarEstudiante(new Estudiante(0, "Ana", "Perez", "ana@uni.edu.co"));

        assertEquals(1, dao.guardados);
        assertEquals(1, dao.datos.size());
    }

    @Test
    @DisplayName("Registrar elimina los espacios sobrantes de nombre, apellido y correo")
    void registrarLimpiaEspacios() {
        Estudiante e = new Estudiante(0, "  Ana ", " Perez  ", "  ana@uni.edu.co ");

        servicio.registrarEstudiante(e);

        assertEquals("Ana", e.getNombre());
        assertEquals("Perez", e.getApellido());
        assertEquals("ana@uni.edu.co", e.getEmail());
    }

    @Test
    @DisplayName("Registrar un estudiante nulo lanza IllegalArgumentException")
    void registrarNulo() {
        IllegalArgumentException ex = registrarConError(null);

        assertEquals("El estudiante no puede ser nulo.", ex.getMessage());
        assertEquals(0, dao.guardados);
    }

    @Test
    @DisplayName("El nombre es obligatorio (vacio, solo espacios o nulo)")
    void nombreObligatorio() {
        assertEquals("El nombre es obligatorio.",
                registrarConError(new Estudiante(0, "", "Perez", "a@b.co")).getMessage());
        assertEquals("El nombre es obligatorio.",
                registrarConError(new Estudiante(0, "   ", "Perez", "a@b.co")).getMessage());
        assertEquals("El nombre es obligatorio.",
                registrarConError(new Estudiante(0, null, "Perez", "a@b.co")).getMessage());
        assertEquals(0, dao.guardados);
    }

    @Test
    @DisplayName("El apellido es obligatorio (vacio, solo espacios o nulo)")
    void apellidoObligatorio() {
        assertEquals("El apellido es obligatorio.",
                registrarConError(new Estudiante(0, "Ana", "", "a@b.co")).getMessage());
        assertEquals("El apellido es obligatorio.",
                registrarConError(new Estudiante(0, "Ana", "   ", "a@b.co")).getMessage());
        assertEquals("El apellido es obligatorio.",
                registrarConError(new Estudiante(0, "Ana", null, "a@b.co")).getMessage());
    }

    @Test
    @DisplayName("Nombre y apellido admiten maximo 100 caracteres")
    void longitudMaximaDeNombreYApellido() {
        String largo = Util.repetir('a', 101);

        assertEquals("Nombre y apellido admiten maximo 100 caracteres.",
                registrarConError(new Estudiante(0, largo, "Perez", "a@b.co")).getMessage());
        assertEquals("Nombre y apellido admiten maximo 100 caracteres.",
                registrarConError(new Estudiante(0, "Ana", largo, "a@b.co")).getMessage());
    }

    @Test
    @DisplayName("Un nombre de exactamente 100 caracteres si se acepta")
    void nombreDeCienCaracteres() {
        servicio.registrarEstudiante(new Estudiante(0, Util.repetir('a', 100), "Perez", "a@b.co"));

        assertEquals(1, dao.guardados);
    }

    @Test
    @DisplayName("Un correo con formato invalido se rechaza")
    void correoInvalido() {
        String[] invalidos = { "sin-arroba", "a@b", "@uni.edu.co", "ana@.co", "ana perez@uni.co", "", "   ", null };

        for (String correo : invalidos) {
            IllegalArgumentException ex = registrarConError(new Estudiante(0, "Ana", "Perez", correo));
            assertEquals("El correo no tiene un formato valido.", ex.getMessage(), "correo: " + correo);
        }
        assertEquals(0, dao.guardados);
    }

    @Test
    @DisplayName("Un correo de mas de 100 caracteres se rechaza")
    void correoDemasiadoLargo() {
        String correo = Util.repetir('a', 95) + "@uni.co"; // 102 caracteres

        assertEquals("El correo no tiene un formato valido.",
                registrarConError(new Estudiante(0, "Ana", "Perez", correo)).getMessage());
    }

    @Test
    @DisplayName("Correos con puntos, signo + y subdominios se aceptan")
    void correosValidos() {
        servicio.registrarEstudiante(new Estudiante(0, "Ana", "Perez", "ana.perez+x@mail.uni.edu.co"));

        assertEquals(1, dao.guardados);
    }

    @Test
    @DisplayName("No se registra un correo que ya existe (sin importar mayusculas)")
    void correoDuplicado() {
        dao.con(1, "Juan", "Diaz", "ana@uni.edu.co");

        IllegalArgumentException ex = registrarConError(new Estudiante(0, "Ana", "Perez", "ANA@uni.edu.co"));

        assertEquals("Ya existe un estudiante con ese correo.", ex.getMessage());
        assertEquals(0, dao.guardados);
    }

    // ------------------------------------------------------------ consultar

    @Test
    @DisplayName("obtenerTodosLosEstudiantes devuelve la lista del DAO")
    void listarTodos() {
        dao.con(1, "Ana", "Perez", "a@b.co").con(2, "Juan", "Diaz", "j@b.co");

        List<Estudiante> lista = servicio.obtenerTodosLosEstudiantes();

        assertEquals(2, lista.size());
    }

    @Test
    @DisplayName("buscarEstudiantePorId devuelve el estudiante si existe")
    void buscarExistente() {
        dao.con(5, "Ana", "Perez", "a@b.co");

        Optional<Estudiante> r = servicio.buscarEstudiantePorId(5);

        assertTrue(r.isPresent());
        assertEquals("Ana", r.get().getNombre());
    }

    @Test
    @DisplayName("buscarEstudiantePorId devuelve vacio si no existe")
    void buscarInexistente() {
        assertFalse(servicio.buscarEstudiantePorId(99).isPresent());
    }

    // ------------------------------------------------------------ actualizar

    @Test
    @DisplayName("Actualizar un estudiante valido modifica sus datos")
    void actualizarValido() {
        dao.con(1, "Ana", "Perez", "ana@uni.edu.co");

        servicio.actualizarEstudiante(new Estudiante(1, "Ana Maria", "Perez", "ana@uni.edu.co"));

        assertEquals(1, dao.actualizados);
        assertEquals("Ana Maria", dao.datos.get(0).getNombre());
    }

    @Test
    @DisplayName("Actualizar con id menor o igual a cero o nulo es invalido")
    void actualizarInvalido() {
        IllegalArgumentException cero = assertThrows(IllegalArgumentException.class,
                () -> servicio.actualizarEstudiante(new Estudiante(0, "Ana", "Perez", "a@b.co")));
        IllegalArgumentException nulo = assertThrows(IllegalArgumentException.class,
                () -> servicio.actualizarEstudiante(null));

        assertEquals("Estudiante invalido.", cero.getMessage());
        assertEquals("Estudiante invalido.", nulo.getMessage());
        assertEquals(0, dao.actualizados);
    }

    @Test
    @DisplayName("Actualizar un estudiante que no existe lanza error")
    void actualizarNoEncontrado() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> servicio.actualizarEstudiante(new Estudiante(8, "Ana", "Perez", "a@b.co")));

        assertEquals("Estudiante no encontrado.", ex.getMessage());
    }

    @Test
    @DisplayName("Actualizar valida los datos igual que registrar")
    void actualizarValidaDatos() {
        dao.con(1, "Ana", "Perez", "ana@uni.edu.co");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> servicio.actualizarEstudiante(new Estudiante(1, "", "Perez", "ana@uni.edu.co")));

        assertEquals("El nombre es obligatorio.", ex.getMessage());
        assertEquals(0, dao.actualizados);
    }

    @Test
    @DisplayName("No se puede usar el correo de otro estudiante")
    void actualizarConCorreoDeOtro() {
        dao.con(1, "Ana", "Perez", "ana@uni.edu.co").con(2, "Juan", "Diaz", "juan@uni.edu.co");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> servicio.actualizarEstudiante(new Estudiante(2, "Juan", "Diaz", "ana@uni.edu.co")));

        assertEquals("Ese correo ya lo usa otro estudiante.", ex.getMessage());
        assertEquals(0, dao.actualizados);
    }

    @Test
    @DisplayName("Un estudiante puede conservar su propio correo al actualizar")
    void actualizarConSuPropioCorreo() {
        dao.con(1, "Ana", "Perez", "ana@uni.edu.co");

        servicio.actualizarEstudiante(new Estudiante(1, "Ana", "Lopez", "ana@uni.edu.co"));

        assertEquals(1, dao.actualizados);
    }

    // ------------------------------------------------------------ eliminar

    @Test
    @DisplayName("Eliminar un estudiante existente lo borra")
    void eliminarExistente() {
        dao.con(1, "Ana", "Perez", "a@b.co");

        servicio.eliminarEstudiante(1);

        assertEquals(1, dao.eliminados);
        assertEquals(0, dao.datos.size());
    }

    @Test
    @DisplayName("Eliminar con id menor o igual a cero es invalido")
    void eliminarIdInvalido() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> servicio.eliminarEstudiante(0));

        assertEquals("ID invalido.", ex.getMessage());
        assertEquals(0, dao.eliminados);
    }

    @Test
    @DisplayName("Eliminar un estudiante que no existe lanza error")
    void eliminarNoEncontrado() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> servicio.eliminarEstudiante(50));

        assertEquals("Estudiante no encontrado.", ex.getMessage());
        assertEquals(0, dao.eliminados);
    }
}
