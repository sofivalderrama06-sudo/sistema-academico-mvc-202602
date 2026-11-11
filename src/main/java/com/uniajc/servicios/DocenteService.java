package com.uniajc.servicios;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import com.uniajc.dao.DocenteDao;
import com.uniajc.modelo.Docente;

/**
 * Servicio: reglas de negocio y validaciones. Decide si algo se puede guardar
 * antes de llegar a la base de datos.
 */
public class DocenteService {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final DocenteDao docenteDao;

    public DocenteService(DocenteDao docenteDao) {
        this.docenteDao = docenteDao;
    }

    public void registrarDocente(Docente docente) {
        validarDocente(docente);
        if (docenteDao.buscarPorEmail(docente.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un docente con ese correo.");
        }
        docenteDao.guardar(docente);
    }

    public List<Docente> obtenerTodosLosDocentes() {
        return docenteDao.obtenerTodos();
    }

    public Optional<Docente> buscarDocentePorId(int id) {
        return docenteDao.buscarPorId(id);
    }

    public void actualizarDocente(Docente docente) {
        if (docente == null || docente.getId() <= 0) {
            throw new IllegalArgumentException("Docente invalido.");
        }
        if (!docenteDao.buscarPorId(docente.getId()).isPresent()) {
            throw new IllegalArgumentException("Docente no encontrado.");
        }
        validarDocente(docente);

        // El correo no puede pertenecer a otro docente distinto.
        Optional<Docente> conMismoCorreo = docenteDao.buscarPorEmail(docente.getEmail());
        if (conMismoCorreo.isPresent() && conMismoCorreo.get().getId() != docente.getId()) {
            throw new IllegalArgumentException("Ese correo ya lo usa otro docente.");
        }
        docenteDao.actualizar(docente);
    }

    public void eliminarDocente(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("ID invalido.");
        }
        if (!docenteDao.buscarPorId(id).isPresent()) {
            throw new IllegalArgumentException("Docente no encontrado.");
        }
        docenteDao.eliminar(id);
    }

    private void validarDocente(Docente docente) {
        if (docente == null) {
            throw new IllegalArgumentException("El docente no puede ser nulo.");
        }
        String nombre = limpiar(docente.getNombre());
        String especialidad = limpiar(docente.getEspecialidad());
        String email = limpiar(docente.getEmail());

        if (nombre.isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        if (especialidad.isEmpty()) {
            throw new IllegalArgumentException("La especialidad es obligatoria.");
        }
        if (nombre.length() > 100 || especialidad.length() > 100) {
            throw new IllegalArgumentException("Nombre y especialidad admiten maximo 100 caracteres.");
        }
        if (email.isEmpty() || email.length() > 100 || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("El correo no tiene un formato valido.");
        }

        docente.setNombre(nombre);
        docente.setEspecialidad(especialidad);
        docente.setEmail(email);
    }

    private String limpiar(String texto) {
        return texto == null ? "" : texto.trim();
    }
}