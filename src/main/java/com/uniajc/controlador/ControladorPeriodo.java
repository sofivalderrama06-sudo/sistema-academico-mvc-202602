package com.uniajc.controlador;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import com.uniajc.dao.PeriodoDao;
import com.uniajc.modelo.Periodo;
import com.uniajc.servicios.PeriodoService;

/**
 * Coordina las solicitudes de la vista con el servicio de periodos.
 */
public class ControladorPeriodo {

    private final PeriodoService periodoService;

    public ControladorPeriodo() {
        this.periodoService = new PeriodoService(new PeriodoDao());
    }

    public void registrar(String nombre, Timestamp fechaInicio, Timestamp fechaFin, boolean estado) {
        Periodo periodo = crearPeriodo(0, nombre, fechaInicio, fechaFin, estado);
        periodoService.registrarPeriodo(periodo);
    }

    public List<Periodo> listar() {
        return periodoService.obtenerTodosLosPeriodos();
    }

    public Optional<Periodo> buscarPorId(int id) {
        return periodoService.buscarPeriodoPorId(id);
    }

    public void actualizar(int id, String nombre, Timestamp fechaInicio, Timestamp fechaFin, boolean estado) {
        Periodo periodo = crearPeriodo(id, nombre, fechaInicio, fechaFin, estado);
        periodoService.actualizarPeriodo(periodo);
    }

    public void eliminar(int id) {
        periodoService.eliminarPeriodo(id);
    }

    private Periodo crearPeriodo(
            int id, String nombre, Timestamp fechaInicio, Timestamp fechaFin, boolean estado) {
        Periodo periodo = new Periodo();
        periodo.setId(id);
        periodo.setName(nombre);
        periodo.setStartDate(fechaInicio);
        periodo.setEndDate(fechaFin);
        periodo.setState(estado);
        return periodo;
    }
}
