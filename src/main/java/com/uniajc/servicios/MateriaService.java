package com.uniajc.servicios;

import java.util.List;
import java.util.Optional;

import com.uniajc.dao.MateriaDao;
import com.uniajc.modelo.Materia;

/**
 * Aplica las validaciones de negocio antes de delegar persistencia al DAO.
 */
public class MateriaService {

    // DAO encargado de las operaciones SQL de materias.
    private final MateriaDao materiaDao;

    // Recibe el DAO para mantener separadas las reglas de negocio y la persistencia.
    public MateriaService(MateriaDao materiaDao) {
        this.materiaDao = materiaDao;
    }

    // Verifica los datos antes de crear una materia.
    public void registrarMateria(Materia materia) {
        validarMateria(materia);
        materiaDao.guardar(materia);
    }

    // Obtiene todas las materias registradas.
    public List<Materia> obtenerTodasLasMaterias() {
        return materiaDao.obtenerTodos();
    }

    // Busca una materia por su identificador.
    public Optional<Materia> buscarMateriaPorId(int id) {
        return materiaDao.buscarPorId(id);
    }

    // Confirma que la materia exista y sus datos sean válidos antes de actualizarla.
    public void actualizarMateria(Materia materia) {
        if (materia == null || materia.getId() <= 0) {
            throw new IllegalArgumentException("Materia invalida.");
        }
        if (!materiaDao.buscarPorId(materia.getId()).isPresent()) {
            throw new IllegalArgumentException("Materia no encontrada.");
        }
        validarMateria(materia);
        materiaDao.actualizar(materia);
    }

    // Confirma que el identificador corresponda a una materia antes de eliminarla.
    public void eliminarMateria(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("ID invalido.");
        }
        if (!materiaDao.buscarPorId(id).isPresent()) {
            throw new IllegalArgumentException("Materia no encontrada.");
        }
        materiaDao.eliminar(id);
    }

    // Exige nombre no vacío y una cantidad positiva de créditos.
    private void validarMateria(Materia materia) {
        if (materia == null) {
            throw new IllegalArgumentException("La materia no puede ser nula.");
        }
        String nombre = materia.getName() == null ? "" : materia.getName().trim();
        if (nombre.isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        if (materia.getCredits() <= 0) {
            throw new IllegalArgumentException("Los creditos deben ser mayores que cero.");
        }
    }
}
