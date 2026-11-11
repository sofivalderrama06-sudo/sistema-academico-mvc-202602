package com.uniajc.servicios;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
 
import com.uniajc.dao.EstudianteDao;
import com.uniajc.modelo.Estudiante;

public class EstudianteService {
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
 
    private final EstudianteDao estudianteDao;
 
    public EstudianteService(EstudianteDao estudianteDao) {
        this.estudianteDao = estudianteDao;
    }
 
    public void registrarEstudiante(Estudiante estudiante) {
        validarEstudiante(estudiante);
        if (estudianteDao.buscarPorEmail(estudiante.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un estudiante con ese correo.");
        }
        estudianteDao.guardar(estudiante);
    }
 
    public List<Estudiante> obtenerTodosLosEstudiantes() {
        return estudianteDao.obtenerTodos();
    }
 
    public Optional<Estudiante> buscarEstudiantePorId(int id) {
        return estudianteDao.buscarPorId(id);
    }
 
    public void actualizarEstudiante(Estudiante estudiante) {
        if (estudiante == null || estudiante.getId() <= 0) {
            throw new IllegalArgumentException("Estudiante invalido.");
        }
        if (!estudianteDao.buscarPorId(estudiante.getId()).isPresent()) {
            throw new IllegalArgumentException("Estudiante no encontrado.");
        }
        validarEstudiante(estudiante);
 
        // El correo no puede pertenecer a otro estudiante distinto.
        Optional<Estudiante> conMismoCorreo = estudianteDao.buscarPorEmail(estudiante.getEmail());
        if (conMismoCorreo.isPresent() && conMismoCorreo.get().getId() != estudiante.getId()) {
            throw new IllegalArgumentException("Ese correo ya lo usa otro estudiante.");
        }
        estudianteDao.actualizar(estudiante);
    }
 
    public void eliminarEstudiante(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("ID invalido.");
        }
        if (!estudianteDao.buscarPorId(id).isPresent()) {
            throw new IllegalArgumentException("Estudiante no encontrado.");
        }
        estudianteDao.eliminar(id);
    }
 
    private void validarEstudiante(Estudiante estudiante) {
        if (estudiante == null) {
            throw new IllegalArgumentException("El estudiante no puede ser nulo.");
        }
        String nombre = limpiar(estudiante.getNombre());
        String apellido = limpiar(estudiante.getApellido());
        String email = limpiar(estudiante.getEmail());
 
        if (nombre.isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        if (apellido.isEmpty()) {
            throw new IllegalArgumentException("El apellido es obligatorio.");
        }
        if (nombre.length() > 100 || apellido.length() > 100) {
            throw new IllegalArgumentException("Nombre y apellido admiten maximo 100 caracteres.");
        }
        if (email.isEmpty() || email.length() > 100 || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("El correo no tiene un formato valido.");
        }
 
        estudiante.setNombre(nombre);
        estudiante.setApellido(apellido);
        estudiante.setEmail(email);
    }
 
    private String limpiar(String texto) {
        return texto == null ? "" : texto.trim();
    }
}
