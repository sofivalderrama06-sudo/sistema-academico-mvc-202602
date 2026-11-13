package com.uniajc.servicios;

import java.util.List;
import java.util.Optional;

import com.uniajc.dao.GrupoDao;
import com.uniajc.modelo.Grupo;

/**
 * Aplica las validaciones de negocio antes de delegar persistencia al DAO.
 */
public class GrupoService {

    private final GrupoDao grupoDao;

    public GrupoService(GrupoDao grupoDao) {
        this.grupoDao = grupoDao;
    }

    public void registrarGrupo(Grupo grupo) {
        validarGrupo(grupo);
        grupoDao.guardar(grupo);
    }

    public List<Grupo> obtenerTodosLosGrupos() {
        return grupoDao.obtenerTodos();
    }

    public Optional<Grupo> buscarGrupoPorId(int id) {
        return grupoDao.buscarPorId(id);
    }

    public void actualizarGrupo(Grupo grupo) {
        if (grupo == null || grupo.getId() <= 0) {
            throw new IllegalArgumentException("Grupo invalido.");
        }
        if (!grupoDao.buscarPorId(grupo.getId()).isPresent()) {
            throw new IllegalArgumentException("Grupo no encontrado.");
        }
        validarGrupo(grupo);
        grupoDao.actualizar(grupo);
    }

    public void eliminarGrupo(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("ID invalido.");
        }
        if (!grupoDao.buscarPorId(id).isPresent()) {
            throw new IllegalArgumentException("Grupo no encontrado.");
        }
        grupoDao.eliminar(id);
    }

    private void validarGrupo(Grupo grupo) {
        if (grupo == null) {
            throw new IllegalArgumentException("El grupo no puede ser nulo.");
        }
        String aula = limpiar(grupo.getClassroom());
        String horario = limpiar(grupo.getSchedule());
        if (grupo.getId_materia() <= 0 || grupo.getId_docente() <= 0) {
            throw new IllegalArgumentException("Los ID de materia y docente deben ser mayores que cero.");
        }
        if (aula.isEmpty()) {
            throw new IllegalArgumentException("El aula es obligatoria.");
        }
        if (horario.isEmpty()) {
            throw new IllegalArgumentException("El horario es obligatorio.");
        }
        grupo.setClassroom(aula);
        grupo.setSchedule(horario);
    }

    private String limpiar(String valor) {
        return valor == null ? "" : valor.trim();
    }
}
