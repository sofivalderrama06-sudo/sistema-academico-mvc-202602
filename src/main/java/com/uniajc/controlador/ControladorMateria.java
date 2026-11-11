package com.uniajc.controlador;

import java.util.List;
import java.util.Optional;

import com.uniajc.dao.MateriaDao;
import com.uniajc.modelo.Materia;
import com.uniajc.servicios.MateriaService;

/**
 * Coordina las solicitudes de la vista y las operaciones del servicio de materias.
 */
public class ControladorMateria {

    // El servicio contiene las validaciones y reglas del caso de uso.
    private final MateriaService materiaService;

    // Construye el flujo con el servicio y el DAO concretos de materias.
    public ControladorMateria() {
        this.materiaService = new MateriaService(new MateriaDao());
    }

    // Crea el modelo de materia y solicita al servicio que lo registre.
    public void registrar(String nombre, int creditos) {
        materiaService.registrarMateria(new Materia(0, nombre, creditos, null));
    }

    // Devuelve todas las materias para que la vista pueda mostrarlas.
    public List<Materia> listar() {
        return materiaService.obtenerTodasLasMaterias();
    }

    // Busca una materia por ID y conserva el resultado opcional del servicio.
    public Optional<Materia> buscarPorId(int id) {
        return materiaService.buscarMateriaPorId(id);
    }

    // Actualiza los campos editables; la fecha de creación permanece en la base de datos.
    public void actualizar(int id, String nombre, int creditos) {
        materiaService.actualizarMateria(new Materia(id, nombre, creditos, null));
    }

    // Solicita al servicio que elimine la materia indicada.
    public void eliminar(int id) {
        materiaService.eliminarMateria(id);
    }
}
