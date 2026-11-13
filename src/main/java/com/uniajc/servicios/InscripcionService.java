package com.uniajc.servicios;

import java.util.List;
import java.util.Optional;

import com.uniajc.dao.InscripcionDao;
import com.uniajc.modelo.Inscripcion;

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
