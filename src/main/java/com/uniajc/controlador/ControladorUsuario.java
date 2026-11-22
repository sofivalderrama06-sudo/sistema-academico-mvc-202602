package com.uniajc.controlador;

import java.util.List;
import java.util.Optional;

import com.uniajc.dao.DocenteDao;
import com.uniajc.dao.EstudianteDao;
import com.uniajc.dao.UsuarioDao;
import com.uniajc.modelo.Docente;
import com.uniajc.modelo.Estudiante;
import com.uniajc.modelo.Usuario;
import com.uniajc.servicios.DocenteService;
import com.uniajc.servicios.EstudianteService;
import com.uniajc.servicios.UsuarioService;

/**
 * Controlador de usuarios: conecta la vista con el servicio correspondiente.
 * Permite además al administrador consultar la lista de estudiantes y docentes
 * para enlazar automáticamente sus datos a una cuenta de usuario.
 */
public class ControladorUsuario {

    private final UsuarioService usuarioService;
    private final EstudianteService estudianteService;
    private final DocenteService docenteService;

    public ControladorUsuario() {
        this.usuarioService = new UsuarioService(new UsuarioDao());
        this.estudianteService = new EstudianteService(new EstudianteDao());
        this.docenteService = new DocenteService(new DocenteDao());
    }

    public List<Usuario> listar() {
        return usuarioService.obtenerTodosLosUsuarios();
    }

    public Optional<Usuario> buscarPorId(int id) {
        return usuarioService.buscarUsuarioPorId(id);
    }

    public void registrar(String usuario, String password, String nombre, String correo, String rol) {
        Usuario nuevoUsuario = new Usuario("0", usuario, nombre, correo, rol, true);
        usuarioService.registrarUsuario(nuevoUsuario, password);
    }

    public void actualizar(int id, String usuario, String nuevaPassword, String nombre, String correo, String rol, boolean activo) {
        Usuario usuarioActualizado = new Usuario(String.valueOf(id), usuario, nombre, correo, rol, activo);
        usuarioService.actualizarUsuario(usuarioActualizado, nuevaPassword);
    }

    public void eliminar(int id) {
        usuarioService.eliminarUsuario(id);
    }

    /**
     * Retorna la lista de estudiantes registrados para que el administrador pueda enlazarlos.
     */
    public List<Estudiante> listarEstudiantes() {
        return estudianteService.obtenerTodosLosEstudiantes();
    }

    /**
     * Retorna la lista de docentes registrados para que el administrador pueda enlazarlos.
     */
    public List<Docente> listarDocentes() {
        return docenteService.obtenerTodosLosDocentes();
    }
}
