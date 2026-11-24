package com.uniajc.controlador;

import java.util.List;
import java.util.Optional;

import com.uniajc.dao.InscripcionDao;
import com.uniajc.modelo.Inscripcion;
import com.uniajc.servicios.InscripcionService;

/**
 * ============================================================================
 * CAPA CONTROLADOR (Controller Layer - MVC)
 * ============================================================================
 * RESPONSABILIDAD EXCLUSIVA:
 * 1. Actuar como intermediario desacoplado entre la Vista Swing y el Servicio.
 * 2. Recibir los datos primitivos capturados por la Vista (IDs, notas, estados).
 * 3. Instanciar los modelos o parámetros y delegar la ejecución a InscripcionService.
 * 
 * REGLA DE SEPARACIÓN DE CAPAS:
 * - NO contiene sentencias SQL ni transacciones de base de datos (eso pertenece al DAO).
 * - NO valida rangos de notas ni reglas de negocio (eso pertenece al Servicio).
 * - NO manipula componentes gráficos de Swing como JTextField ni JTable (eso pertenece a la Vista).
 */
public class ControladorInscripcion {

    private final InscripcionService inscripcionService;

    /** Inicializa el servicio de inscripciones y su acceso a datos. */
    public ControladorInscripcion() {
        this.inscripcionService = new InscripcionService(new InscripcionDao());
    }

    /** Registra una inscripción con los identificadores seleccionados en la vista. */
    public void registrar(int idEstudiante, int idGrupo, double finalGrade, String status) {
        inscripcionService.registrarInscripcion(new Inscripcion(idEstudiante, idGrupo, finalGrade, status));
    }

    /** Obtiene todas las inscripciones disponibles. */
    public List<Inscripcion> listar() {
        return inscripcionService.obtenerTodasLasInscripciones();
    }

    /** Busca una inscripción por su identificador. */
    public Optional<Inscripcion> buscarPorId(int id) {
        return inscripcionService.buscarInscripcionPorId(id);
    }

    /** Actualiza los datos de la inscripción indicada. */
    public void actualizar(int id, int idEstudiante, int idGrupo, double finalGrade, String status) {
        Inscripcion inscripcion = new Inscripcion(id, idEstudiante, idGrupo, finalGrade, status, null);
        inscripcionService.actualizarInscripcion(inscripcion);
    }

    /** Elimina la inscripción identificada por el valor recibido. */
    public void eliminar(int id) {
        inscripcionService.eliminarInscripcion(id);
    }

    /**
     * Elimina la inscripción ejecutando el procedimiento almacenado sp_eliminar_inscripcion_segura.
     */
    public void eliminarConProcedimiento(int id) {
        inscripcionService.eliminarConProcedimiento(id);
    }

    /**
     * Registra la inscripción ejecutando el procedimiento almacenado sp_inscribir_estudiante.
     */
    public void registrarConProcedimiento(int idEstudiante, int idGrupo, double nota, String status) {
        inscripcionService.registrarConProcedimiento(idEstudiante, idGrupo, nota, status);
    }
}
