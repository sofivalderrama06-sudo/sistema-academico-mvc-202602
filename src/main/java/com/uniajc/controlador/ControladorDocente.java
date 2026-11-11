package com.uniajc.controlador;

import java.util.List;
import java.util.Optional;

import com.uniajc.dao.DocenteDao;
import com.uniajc.modelo.Docente;
import com.uniajc.servicios.DocenteService;

/**
 * Controlador: orquesta el flujo entre la Vista y el Servicio.
 * No imprime nada: eso le toca a la Vista.
 */
public class ControladorDocente {

    private final DocenteService docenteService;

    public ControladorDocente() {
        this.docenteService = new DocenteService(new DocenteDao());
    }

    public void registrar(String nombre, String especialidad, String email) {
        Docente docente = new Docente();
        docente.setNombre(nombre);
        docente.setEspecialidad(especialidad);
        docente.setEmail(email);
        docenteService.registrarDocente(docente);
    }

    public List<Docente> listar() {
        return docenteService.obtenerTodosLosDocentes();
    }

    public Optional<Docente> buscarPorId(int id) {
        return docenteService.buscarDocentePorId(id);
    }

    public void actualizar(int id, String nombre, String especialidad, String email) {
        Docente docente = new Docente(id, nombre, especialidad, email);
        docenteService.actualizarDocente(docente);
    }

    public void eliminar(int id) {
        docenteService.eliminarDocente(id);
    }
}