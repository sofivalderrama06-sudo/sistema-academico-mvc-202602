package com.uniajc.controlador;

import java.util.List;
import java.util.Optional;
 
import com.uniajc.dao.EstudianteDao;
import com.uniajc.modelo.Estudiante;
import com.uniajc.servicios.EstudianteService;

public class ControladorEstudiante {

    private final EstudianteService estudianteService;

    public ControladorEstudiante() {
        this.estudianteService = new EstudianteService(new EstudianteDao());
    }

    public void registrar(String nombre, String apellido, String email) {
        Estudiante estudiante = new Estudiante();
        estudiante.setNombre(nombre);
        estudiante.setApellido(apellido);
        estudiante.setEmail(email);
        estudianteService.registrarEstudiante(estudiante);
    }
 
    public List<Estudiante> listar() {
        return estudianteService.obtenerTodosLosEstudiantes();
    }
 
    public Optional<Estudiante> buscarPorId(int id) {
        return estudianteService.buscarEstudiantePorId(id);
    }
 
    public void actualizar(int id, String nombre, String apellido, String email) {
        Estudiante estudiante = new Estudiante(id, nombre, apellido, email);
        estudianteService.actualizarEstudiante(estudiante);
    }
 
    public void eliminar(int id) {
        estudianteService.eliminarEstudiante(id);
    }
}