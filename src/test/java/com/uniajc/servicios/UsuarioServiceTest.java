package com.uniajc.servicios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.uniajc.modelo.Usuario;
import com.uniajc.soporte.DaosFalsos.UsuarioDaoFalso;

/**
 * Pruebas unitarias para la capa de servicio UsuarioService.
 * Verifica reglas de negocio, validaciones y cifrado BCrypt con el DAO simulado.
 */
public class UsuarioServiceTest {

    private final Usuario admin = new Usuario("1", "admin", "Laura", "Valderrama");

    // ========================================================================
    // PRUEBAS DE INICIO DE SESIÓN (LOGIN)
    // ========================================================================

    @Test
    @DisplayName("Con credenciales correctas devuelve el usuario")
    void credencialesCorrectas() {
        UsuarioDaoFalso dao = new UsuarioDaoFalso(admin);
        UsuarioService servicio = new UsuarioService(dao);

        Optional<Usuario> r = servicio.iniciarSesion("admin", "1234");

        assertTrue(r.isPresent());
        assertEquals("Laura Valderrama", r.get().getNombreCompleto());
    }

    @Test
    @DisplayName("Con credenciales incorrectas devuelve vacio (no lanza error)")
    void credencialesIncorrectas() {
        UsuarioService servicio = new UsuarioService(new UsuarioDaoFalso(null));

        Optional<Usuario> r = servicio.iniciarSesion("admin", "mala");

        assertFalse(r.isPresent());
    }

    @Test
    @DisplayName("El usuario es obligatorio (nulo, vacio o solo espacios)")
    void usuarioObligatorio() {
        UsuarioDaoFalso dao = new UsuarioDaoFalso(admin);
        UsuarioService servicio = new UsuarioService(dao);

        String[] invalidos = { null, "", "    " };
        for (String u : invalidos) {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> servicio.iniciarSesion(u, "1234"));
            assertEquals("El usuario es obligatorio.", ex.getMessage());
        }
        assertEquals(0, dao.llamadas);
    }

    @Test
    @DisplayName("La contrasena es obligatoria (nula o vacia)")
    void contrasenaObligatoria() {
        UsuarioDaoFalso dao = new UsuarioDaoFalso(admin);
        UsuarioService servicio = new UsuarioService(dao);

        IllegalArgumentException nula = assertThrows(IllegalArgumentException.class,
                () -> servicio.iniciarSesion("admin", null));
        IllegalArgumentException vacia = assertThrows(IllegalArgumentException.class,
                () -> servicio.iniciarSesion("admin", ""));

        assertEquals("La contrasena es obligatoria.", nula.getMessage());
        assertEquals("La contrasena es obligatoria.", vacia.getMessage());
        assertEquals(0, dao.llamadas);
    }

    @Test
    @DisplayName("El usuario se envia al DAO sin espacios en los extremos")
    void usuarioSeLimpia() {
        UsuarioDaoFalso dao = new UsuarioDaoFalso(admin);

        new UsuarioService(dao).iniciarSesion("  admin  ", "1234");

        assertEquals("admin", dao.usuarioRecibido);
    }

    // ========================================================================
    // PRUEBAS DE REGISTRO Y GESTIÓN DE USUARIOS (BCRYPT Y VALIDACIONES)
    // ========================================================================

    @Test
    @DisplayName("Registrar un usuario válido encripta la contraseña con BCrypt y guarda en el DAO")
    void registrarUsuarioValido() {
        UsuarioDaoFalso dao = new UsuarioDaoFalso();
        UsuarioService servicio = new UsuarioService(dao);
        Usuario nuevo = new Usuario("0", "carlos", "Carlos Pérez", "carlos@uniajc.edu.co", "USUARIO", true);

        servicio.registrarUsuario(nuevo, "claveSegura123");

        assertEquals(1, dao.guardados);
        assertNotNull(dao.ultimoHashGuardado);
        assertTrue(dao.ultimoHashGuardado.startsWith("$2a$12$"), "La contraseña guardada debe tener hash BCrypt");
    }

    @Test
    @DisplayName("Registrar usuario con contraseña menor a 6 caracteres lanza IllegalArgumentException")
    void registrarPasswordCortaLanzaError() {
        UsuarioDaoFalso dao = new UsuarioDaoFalso();
        UsuarioService servicio = new UsuarioService(dao);
        Usuario nuevo = new Usuario("0", "carlos", "Carlos Pérez", "carlos@uniajc.edu.co", "USUARIO", true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> servicio.registrarUsuario(nuevo, "12345"));

        assertEquals("La contraseña debe tener al menos 6 caracteres.", ex.getMessage());
        assertEquals(0, dao.guardados);
    }

    @Test
    @DisplayName("Registrar usuario con correo sin formato válido lanza IllegalArgumentException")
    void registrarCorreoInvalidoLanzaError() {
        UsuarioDaoFalso dao = new UsuarioDaoFalso();
        UsuarioService servicio = new UsuarioService(dao);
        Usuario nuevo = new Usuario("0", "carlos", "Carlos Pérez", "correo_sin_arroba", "USUARIO", true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> servicio.registrarUsuario(nuevo, "123456"));

        assertEquals("El correo electrónico no es válido.", ex.getMessage());
        assertEquals(0, dao.guardados);
    }

    @Test
    @DisplayName("Registrar usuario con rol diferente a ADMIN, DOCENTE, ESTUDIANTE o USUARIO lanza IllegalArgumentException")
    void registrarRolInvalidoLanzaError() {
        UsuarioDaoFalso dao = new UsuarioDaoFalso();
        UsuarioService servicio = new UsuarioService(dao);
        Usuario nuevo = new Usuario("0", "carlos", "Carlos Pérez", "carlos@uniajc.edu.co", "SUPER_ADMIN", true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> servicio.registrarUsuario(nuevo, "123456"));

        assertEquals("El rol debe ser ADMIN, DOCENTE, ESTUDIANTE o USUARIO.", ex.getMessage());
        assertEquals(0, dao.guardados);
    }

    @Test
    @DisplayName("Registrar usuario con rol DOCENTE o ESTUDIANTE es permitido exitosamente")
    void registrarRolesValidosDocenteYEstudiante() {
        UsuarioDaoFalso dao = new UsuarioDaoFalso();
        UsuarioService servicio = new UsuarioService(dao);

        Usuario docente = new Usuario("0", "profe_marta", "Marta Rios", "marta@uniajc.edu.co", "DOCENTE", true);
        servicio.registrarUsuario(docente, "clave123");
        assertEquals(1, dao.guardados);

        Usuario estudiante = new Usuario("0", "est_juan", "Juan Diaz", "juan@uniajc.edu.co", "ESTUDIANTE", true);
        servicio.registrarUsuario(estudiante, "clave123");
        assertEquals(2, dao.guardados);
    }

    @Test
    @DisplayName("Eliminar usuario con ID menor o igual a cero lanza IllegalArgumentException")
    void eliminarIdInvalido() {
        UsuarioDaoFalso dao = new UsuarioDaoFalso();
        UsuarioService servicio = new UsuarioService(dao);

        assertThrows(IllegalArgumentException.class, () -> servicio.eliminarUsuario(0));
        assertThrows(IllegalArgumentException.class, () -> servicio.eliminarUsuario(-5));
        assertEquals(0, dao.eliminados);
    }
}
