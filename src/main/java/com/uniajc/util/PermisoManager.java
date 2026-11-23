package com.uniajc.util;

import java.util.Optional;

import com.uniajc.dao.DocenteDao;
import com.uniajc.dao.EstudianteDao;
import com.uniajc.modelo.Docente;
import com.uniajc.modelo.Estudiante;

/**
 * ============================================================================
 * CONTROL DE ACCESO BASADO EN ROLES (RBAC) - PermisoManager
 * ============================================================================
 * Centraliza la matriz de permisos y reglas de autorización del sistema:
 * 
 * 1. ADMIN:
 *    - Acceso total a todos los módulos (Estudiantes, Docentes, Materias,
 *      Periodos, Grupos, Inscripciones y Usuarios).
 *    - Puede matricular, editar, calificar y eliminar cualquier registro.
 * 
 * 2. DOCENTE:
 *    - Solo puede ver:
 *      * "Grupos": Muestra exclusivamente los grupos que tiene asignados (solo lectura).
 *      * "Calificaciones" (Inscripciones): Lista los estudiantes inscritos en
 *        sus grupos para registrar/modificar sus notas finales y estado.
 *    - No puede crear matrículas nuevas ni eliminar inscripciones de la institución.
 *    - No tiene acceso a: Usuarios, Docentes, Estudiantes, Materias, Periodos.
 * 
 * 3. ESTUDIANTE:
 *    - Solo puede ver:
 *      * "Mis Calificaciones" (Inscripciones): Consulta sus propias notas de solo lectura.
 *    - No puede modificar notas ni acceder a ningún módulo administrativo.
 */
public final class PermisoManager {

    private PermisoManager() {
        // Clase de utilidades estáticas: no instanciable
    }

    /**
     * Módulos funcionales disponibles en la aplicación.
     */
    public enum Modulo {
        ESTUDIANTES,
        DOCENTES,
        MATERIAS,
        PERIODOS,
        GRUPOS,
        INSCRIPCIONES,
        USUARIOS
    }

    /**
     * Determina si un rol de usuario tiene autorización para ver y acceder a un módulo específico.
     *
     * @param rol    Rol del usuario autenticado (ADMIN, DOCENTE, ESTUDIANTE).
     * @param modulo Módulo del sistema que se desea consultar.
     * @return true si tiene permiso, false en caso contrario.
     */
    public static boolean tieneAccesoModulo(String rol, Modulo modulo) {
        if (rol == null || modulo == null) {
            return false;
        }

        String rolNormalizado = rol.trim().toUpperCase();

        switch (rolNormalizado) {
            case "ADMIN":
                // El administrador tiene acceso a todos los módulos sin excepción
                return true;

            case "DOCENTE":
                // El docente solo accede a consultar sus grupos y asentar notas en inscripciones
                return modulo == Modulo.GRUPOS || modulo == Modulo.INSCRIPCIONES;

            case "ESTUDIANTE":
                // El estudiante solo tiene acceso para consultar sus propias calificaciones
                return modulo == Modulo.INSCRIPCIONES;

            default:
                return false;
        }
    }

    /**
     * Verifica si el rol tiene permiso para registrar (matricular) un estudiante en un grupo.
     * Esta acción está restringida exclusivamente al ADMINISTRADOR.
     */
    public static boolean puedeCrearInscripcion(String rol) {
        return esAdmin(rol);
    }

    /**
     * Verifica si el rol puede editar una inscripción:
     * - ADMIN: Puede editar cualquier dato (estudiante, grupo, nota, estado).
     * - DOCENTE: Puede registrar o actualizar la nota final y el estado del estudiante.
     * - ESTUDIANTE: No tiene permisos de edición (solo lectura).
     */
    public static boolean puedeEditarInscripcion(String rol) {
        if (rol == null) {
            return false;
        }
        String r = rol.trim().toUpperCase();
        return "ADMIN".equals(r) || "DOCENTE".equals(r);
    }

    /**
     * Verifica si el rol puede modificar la estructura de la inscripción (cambiar de estudiante o grupo).
     * El docente NO puede mover a un estudiante de grupo; solo el ADMIN puede hacerlo.
     */
    public static boolean puedeModificarEstructuraInscripcion(String rol) {
        return esAdmin(rol);
    }

    /**
     * Verifica si el rol puede eliminar inscripciones del sistema.
     * Acción restringida al ADMINISTRADOR.
     */
    public static boolean puedeEliminarInscripcion(String rol) {
        return esAdmin(rol);
    }

    /**
     * Verifica si el rol puede realizar operaciones de escritura (crear, editar, eliminar) en Grupos.
     * - ADMIN: true (diseña la oferta académica).
     * - DOCENTE: false (solo lectura de los grupos asignados).
     */
    public static boolean puedeGestionarGrupos(String rol) {
        return esAdmin(rol);
    }

    /**
     * Identifica el ID de estudiante en la tabla "practica-mvc".estudiantes a partir
     * del correo institucional del usuario que inició sesión.
     *
     * @param correo Correo electrónico del usuario autenticado.
     * @return ID del estudiante asociado o 0 si no se encuentra.
     */
    public static int obtenerIdEstudianteVinculado(String correo) {
        if (correo == null || correo.trim().isEmpty()) {
            return 0;
        }
        try {
            Optional<Estudiante> estudiante = new EstudianteDao().buscarPorEmail(correo.trim());
            return estudiante.map(Estudiante::getId).orElse(0);
        } catch (Exception ex) {
            // Manejo tolerante a fallos si la base de datos no está disponible o es ambiente de prueba
            return 0;
        }
    }

    /**
     * Identifica el ID de docente en la tabla "practica-mvc".docentes a partir
     * del correo institucional del usuario que inició sesión.
     *
     * @param correo Correo electrónico del usuario autenticado.
     * @return ID del docente asociado o 0 si no se encuentra.
     */
    public static int obtenerIdDocenteVinculado(String correo) {
        if (correo == null || correo.trim().isEmpty()) {
            return 0;
        }
        try {
            Optional<Docente> docente = new DocenteDao().buscarPorEmail(correo.trim());
            return docente.map(Docente::getId).orElse(0);
        } catch (Exception ex) {
            return 0;
        }
    }

    private static boolean esAdmin(String rol) {
        return rol != null && "ADMIN".equalsIgnoreCase(rol.trim());
    }
}
