package com.uniajc.servicios;

import java.util.List;
import java.util.Optional;

import com.uniajc.dao.PeriodoDao;
import com.uniajc.modelo.Periodo;

/**
 * Aplica las validaciones de negocio antes de delegar la persistencia al DAO.
 */
public class PeriodoService {

    private final PeriodoDao periodoDao;

    public PeriodoService(PeriodoDao periodoDao) {
        this.periodoDao = periodoDao;
    }

    public void registrarPeriodo(Periodo periodo) {
        validarPeriodo(periodo);
        periodoDao.guardar(periodo);
    }

    public List<Periodo> obtenerTodosLosPeriodos() {
        return periodoDao.obtenerTodos();
    }

    public Optional<Periodo> buscarPeriodoPorId(int id) {
        return periodoDao.buscarPorId(id);
    }

    public void actualizarPeriodo(Periodo periodo) {
        if (periodo == null || periodo.getId() <= 0) {
            throw new IllegalArgumentException("Periodo invalido.");
        }
        if (!periodoDao.buscarPorId(periodo.getId()).isPresent()) {
            throw new IllegalArgumentException("Periodo no encontrado.");
        }
        validarPeriodo(periodo);
        periodoDao.actualizar(periodo);
    }

    public void eliminarPeriodo(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("ID invalido.");
        }
        if (!periodoDao.buscarPorId(id).isPresent()) {
            throw new IllegalArgumentException("Periodo no encontrado.");
        }
        periodoDao.eliminar(id);
    }

    private void validarPeriodo(Periodo periodo) {
        if (periodo == null) {
            throw new IllegalArgumentException("El periodo no puede ser nulo.");
        }
        String nombre = periodo.getName() == null ? "" : periodo.getName().trim();
        if (nombre.isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        if (periodo.getStartDate() == null || periodo.getEndDate() == null) {
            throw new IllegalArgumentException("Las fechas de inicio y fin son obligatorias.");
        }
        if (periodo.getEndDate().before(periodo.getStartDate())) {
            throw new IllegalArgumentException("La fecha fin no puede ser anterior a la fecha inicio.");
        }
    }
}
