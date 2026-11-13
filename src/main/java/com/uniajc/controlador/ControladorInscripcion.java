package com.uniajc.controlador;

import java.util.List;
import java.util.Optional;

import com.uniajc.dao.InscripcionDao;
import com.uniajc.modelo.Inscripcion;
import com.uniajc.servicios.InscripcionService;

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
}
