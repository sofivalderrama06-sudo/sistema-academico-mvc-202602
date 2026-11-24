package com.uniajc.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.uniajc.util.PermisoManager.Modulo;

/**
 * ============================================================================
 * PRUEBAS UNITARIAS: Control de Acceso y Autorización (PermisoManagerTest)
 * ============================================================================
 * Valida la matriz RBAC para los roles del sistema:
 * 1. ADMIN: Acceso global a todos los módulos y operaciones de escritura.
 * 2. DOCENTE: Acceso a Grupos e Inscripciones (registro de notas según grupo asignado).
 * 3. ESTUDIANTE: Acceso exclusivo a consultar sus notas en Inscripciones.
 * 4. Casos límite: Roles nulos, cadenas vacías y mayúsculas/minúsculas.
 */
public class PermisoManagerTest {

    // ========================================================================
    // MATRIZ DE ACCESO A MÓDULOS
    // ========================================================================

    @Test
    @DisplayName("El rol ADMIN tiene acceso a todos los módulos del sistema")
    void adminAccesoATodosLosModulos() {
        for (Modulo modulo : Modulo.values()) {
            assertTrue(PermisoManager.tieneAccesoModulo("ADMIN", modulo),
                    "ADMIN debe tener acceso al módulo: " + modulo);
            assertTrue(PermisoManager.tieneAccesoModulo("admin", modulo),
                    "Debe ser insensible a mayúsculas/minúsculas");
        }
    }

    @Test
    @DisplayName("El rol DOCENTE solo tiene acceso a Grupos e Inscripciones (Calificaciones)")
    void docenteAccesoModulosPermitidos() {
        assertTrue(PermisoManager.tieneAccesoModulo("DOCENTE", Modulo.GRUPOS));
        assertTrue(PermisoManager.tieneAccesoModulo("docente", Modulo.INSCRIPCIONES));

        // Módulos que NO debe ver
        assertFalse(PermisoManager.tieneAccesoModulo("DOCENTE", Modulo.ESTUDIANTES));
        assertFalse(PermisoManager.tieneAccesoModulo("DOCENTE", Modulo.DOCENTES));
        assertFalse(PermisoManager.tieneAccesoModulo("DOCENTE", Modulo.MATERIAS));
        assertFalse(PermisoManager.tieneAccesoModulo("DOCENTE", Modulo.PERIODOS));
        assertFalse(PermisoManager.tieneAccesoModulo("DOCENTE", Modulo.USUARIOS));
    }

    @Test
    @DisplayName("El rol ESTUDIANTE solo tiene acceso a Inscripciones (Mis Calificaciones)")
    void estudianteAccesoSoloInscripciones() {
        assertTrue(PermisoManager.tieneAccesoModulo("ESTUDIANTE", Modulo.INSCRIPCIONES));
        assertTrue(PermisoManager.tieneAccesoModulo("estudiante", Modulo.INSCRIPCIONES));

        // Módulos administrativos que NO debe ver
        assertFalse(PermisoManager.tieneAccesoModulo("ESTUDIANTE", Modulo.ESTUDIANTES));
        assertFalse(PermisoManager.tieneAccesoModulo("ESTUDIANTE", Modulo.DOCENTES));
        assertFalse(PermisoManager.tieneAccesoModulo("ESTUDIANTE", Modulo.MATERIAS));
        assertFalse(PermisoManager.tieneAccesoModulo("ESTUDIANTE", Modulo.PERIODOS));
        assertFalse(PermisoManager.tieneAccesoModulo("ESTUDIANTE", Modulo.GRUPOS));
        assertFalse(PermisoManager.tieneAccesoModulo("ESTUDIANTE", Modulo.USUARIOS));
    }

    @Test
    @DisplayName("Roles nulos, vacíos o desconocidos no tienen acceso a ningún módulo")
    void rolesInvalidosSinAcceso() {
        String[] invalidos = { null, "", "   ", "INVITADO", "OTRO" };
        for (String rol : invalidos) {
            for (Modulo m : Modulo.values()) {
                assertFalse(PermisoManager.tieneAccesoModulo(rol, m));
            }
        }
    }

    // ========================================================================
    // PERMISOS DE OPERACIONES SOBRE INSCRIPCIONES Y GRUPOS
    // ========================================================================

    @Test
    @DisplayName("puedeCrearInscripcion (matricular): solo permitido para ADMIN")
    void crearInscripcionSoloAdmin() {
        assertTrue(PermisoManager.puedeCrearInscripcion("ADMIN"));
        assertTrue(PermisoManager.puedeCrearInscripcion("admin"));

        assertFalse(PermisoManager.puedeCrearInscripcion("DOCENTE"));
        assertFalse(PermisoManager.puedeCrearInscripcion("ESTUDIANTE"));
        assertFalse(PermisoManager.puedeCrearInscripcion(null));
    }

    @Test
    @DisplayName("puedeEditarInscripcion (calificar o modificar): permitido para ADMIN y DOCENTE")
    void editarInscripcionAdminYDocente() {
        assertTrue(PermisoManager.puedeEditarInscripcion("ADMIN"));
        assertTrue(PermisoManager.puedeEditarInscripcion("DOCENTE"));
        assertTrue(PermisoManager.puedeEditarInscripcion("docente"));

        // El estudiante solo consulta, no puede editar
        assertFalse(PermisoManager.puedeEditarInscripcion("ESTUDIANTE"));
        assertFalse(PermisoManager.puedeEditarInscripcion(null));
    }

    @Test
    @DisplayName("puedeModificarEstructuraInscripcion (cambiar alumno/grupo): solo ADMIN")
    void modificarEstructuraSoloAdmin() {
        assertTrue(PermisoManager.puedeModificarEstructuraInscripcion("ADMIN"));

        // El docente solo asienta notas, no puede mover de grupo al estudiante
        assertFalse(PermisoManager.puedeModificarEstructuraInscripcion("DOCENTE"));
        assertFalse(PermisoManager.puedeModificarEstructuraInscripcion("ESTUDIANTE"));
    }

    @Test
    @DisplayName("puedeEliminarInscripcion: solo permitido para ADMIN")
    void eliminarInscripcionSoloAdmin() {
        assertTrue(PermisoManager.puedeEliminarInscripcion("ADMIN"));

        assertFalse(PermisoManager.puedeEliminarInscripcion("DOCENTE"));
        assertFalse(PermisoManager.puedeEliminarInscripcion("ESTUDIANTE"));
    }

    @Test
    @DisplayName("puedeGestionarGrupos (crear/editar/eliminar oferta académica): solo ADMIN")
    void gestionarGruposSoloAdmin() {
        assertTrue(PermisoManager.puedeGestionarGrupos("ADMIN"));

        // El docente solo consulta sus grupos asignados
        assertFalse(PermisoManager.puedeGestionarGrupos("DOCENTE"));
        assertFalse(PermisoManager.puedeGestionarGrupos("ESTUDIANTE"));
    }

    @Test
    @DisplayName("obtenerIdEstudianteVinculado y obtenerIdDocenteVinculado retornan 0 si el correo es nulo o vacío")
    void resolucionDeCorreoInvalido() {
        assertEquals(0, PermisoManager.obtenerIdEstudianteVinculado(null));
        assertEquals(0, PermisoManager.obtenerIdEstudianteVinculado(""));
        assertEquals(0, PermisoManager.obtenerIdEstudianteVinculado("    "));

        assertEquals(0, PermisoManager.obtenerIdDocenteVinculado(null));
        assertEquals(0, PermisoManager.obtenerIdDocenteVinculado(""));
        assertEquals(0, PermisoManager.obtenerIdDocenteVinculado("    "));
    }
}
