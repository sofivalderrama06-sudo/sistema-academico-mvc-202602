package com.uniajc.servicios;

import java.util.List;
import java.util.Optional;

import com.uniajc.dao.InscripcionDao;
import com.uniajc.modelo.Inscripcion;

/**
 * ============================================================================
 * CAPA DE SERVICIO / LÓGICA DE NEGOCIO (Service Layer)
 * ============================================================================
 * RESPONSABILIDAD EXCLUSIVA:
 * 1. Aplicar las Reglas de Negocio del Dominio:
 *    - Rango válido de notas (0.0 a 5.0).
 *    - Estados permitidos ('ACTIVO', 'APROBADO', 'REPROBADO', 'CANCELADO').
 *    - Llaves foráneas obligatorias (id_estudiante > 0, id_grupo > 0).
 *    - Unicidad: un estudiante no puede estar dos veces en el mismo grupo.
 * 2. Lanzar excepciones descriptivas (IllegalArgumentException) ante datos inválidos.
 * 3. Orquestar el flujo antes de delegar la persistencia al DAO.
 * 
 * REGLA DE SEPARACIÓN DE CAPAS:
 * - NO contiene código SQL, JDBC, Connection ni CallableStatement (eso pertenece al DAO).
 * - NO contiene código Swing, botones ni diálogos gráficos (eso pertenece a la Vista).
 * - Es 100% testeable con DaosFalsos en memoria sin tocar bases de datos.
 */
public class InscripcionService {

    private final InscripcionDao inscripcionDao;

    /** Recibe el DAO que realizará las consultas de inscripciones. */
    public InscripcionService(InscripcionDao inscripcionDao) {
        this.inscripcionDao = inscripcionDao;
    }

    /** Valida y registra la inscripción, evitando duplicados para estudiante y grupo. */
    public void registrarInscripcion(Inscripcion inscripcion) {
        validarInscripcion(inscripcion);
        if (inscripcionDao.buscarPorEstudianteYGrupo(inscripcion.getId_estudiante(), inscripcion.getId_grupo()).isPresent()) {
            throw new IllegalArgumentException("El estudiante ya esta inscrito en este grupo.");
        }
        inscripcionDao.guardar(inscripcion);
    }

    /** Devuelve todas las inscripciones registradas. */
    public List<Inscripcion> obtenerTodasLasInscripciones() {
        return inscripcionDao.obtenerTodos();
    }

    /** Busca una inscripción por su ID. */
    public Optional<Inscripcion> buscarInscripcionPorId(int id) {
        return inscripcionDao.buscarPorId(id);
    }

    /** Verifica la inscripción y actualiza sus datos. */
    public void actualizarInscripcion(Inscripcion inscripcion) {
        if (inscripcion == null || inscripcion.getId() <= 0) {
            throw new IllegalArgumentException("Inscripcion invalida.");
        }
        if (!inscripcionDao.buscarPorId(inscripcion.getId()).isPresent()) {
            throw new IllegalArgumentException("Inscripcion no encontrada.");
        }
        validarInscripcion(inscripcion);
        inscripcionDao.actualizar(inscripcion);
    }

    /** Verifica que exista la inscripción antes de eliminarla. */
    public void eliminarInscripcion(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("ID invalido.");
        }
        if (!inscripcionDao.buscarPorId(id).isPresent()) {
            throw new IllegalArgumentException("Inscripcion no encontrada.");
        }
        inscripcionDao.eliminar(id);
    }

    /**
     * Elimina una inscripción ejecutando el procedimiento almacenado sp_eliminar_inscripcion_segura.
     * En base de datos valida que la inscripción exista y que no esté en estado 'APROBADO'.
     */
    public void eliminarConProcedimiento(int idInscripcion) {
        if (idInscripcion <= 0) {
            throw new IllegalArgumentException("ID invalido.");
        }
        if (!inscripcionDao.buscarPorId(idInscripcion).isPresent()) {
            throw new IllegalArgumentException("Inscripcion no encontrada.");
        }
        inscripcionDao.eliminarConSP(idInscripcion);
    }

    /**
     * Matricula un estudiante ejecutando el procedimiento almacenado sp_inscribir_estudiante.
     */
    public void registrarConProcedimiento(int idEstudiante, int idGrupo, double nota, String status) {
        if (idEstudiante <= 0) {
            throw new IllegalArgumentException("El estudiante es obligatorio.");
        }
        if (idGrupo <= 0) {
            throw new IllegalArgumentException("El grupo es obligatorio.");
        }
        if (nota < 0 || nota > 5) {
            throw new IllegalArgumentException("La nota final debe estar entre 0 y 5.");
        }
        String estado = (status == null || status.trim().isEmpty()) ? "ACTIVO" : status.trim().toUpperCase();
        inscripcionDao.inscribirEstudianteConSP(idEstudiante, idGrupo, nota, estado);
    }

    /** Valida las FK, el rango de nota y los estados permitidos. */
    private void validarInscripcion(Inscripcion inscripcion) {
        if (inscripcion == null) {
            throw new IllegalArgumentException("La inscripcion no puede ser nula.");
        }
        if (inscripcion.getId_estudiante() <= 0) {
            throw new IllegalArgumentException("El estudiante es obligatorio.");
        }
        if (inscripcion.getId_grupo() <= 0) {
            throw new IllegalArgumentException("El grupo es obligatorio.");
        }
        if (inscripcion.getFinal_grade() < 0 || inscripcion.getFinal_grade() > 5) {
            throw new IllegalArgumentException("La nota final debe estar entre 0 y 5.");
        }

        String estado = inscripcion.getStatus() == null ? "" : inscripcion.getStatus().trim().toUpperCase();
        if (!estado.equals("ACTIVO") && !estado.equals("APROBADO")
                && !estado.equals("REPROBADO") && !estado.equals("CANCELADO")) {
            throw new IllegalArgumentException("El estado debe ser ACTIVO, APROBADO, REPROBADO o CANCELADO.");
        }
        inscripcion.setStatus(estado);
    }
}
