package com.uniajc.controlador;

import java.util.List;
import java.util.Optional;

import com.uniajc.dao.GrupoDao;
import com.uniajc.modelo.Grupo;
import com.uniajc.servicios.GrupoService;

/**
 * Coordina las solicitudes de la vista y las operaciones del servicio de grupos.
 */
public class ControladorGrupo {

    private final GrupoService grupoService;

    public ControladorGrupo() {
        this.grupoService = new GrupoService(new GrupoDao());
    }

    public void registrar(int idMateria, int idDocente, String aula, String horario) {
        grupoService.registrarGrupo(new Grupo(idMateria, idDocente, aula, horario, null));
    }

    public List<Grupo> listar() {
        return grupoService.obtenerTodosLosGrupos();
    }

    public Optional<Grupo> buscarPorId(int id) {
        return grupoService.buscarGrupoPorId(id);
    }

    public void actualizar(int id, int idMateria, int idDocente, String aula, String horario) {
        grupoService.actualizarGrupo(new Grupo(id, idMateria, idDocente, aula, horario, null));
    }

    public void eliminar(int id) {
        grupoService.eliminarGrupo(id);
    }
}
