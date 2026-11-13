package com.uniajc.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.uniajc.db.DatabaseConexion;
import com.uniajc.modelo.Grupo;

/**
 * DAO encargado de ejecutar las consultas SQL para la tabla grupos.
 */
public class GrupoDao {

    // Mantiene centralizados el esquema y el nombre de la tabla de grupos.
    private static final String TABLA = "\"practica-mvc\".grupos";

    // Obtiene la conexión compartida del proyecto; cada operación cierra sus consultas.
    private Connection obtenerConexion() {
        Connection conn = DatabaseConexion.getConnection();
        if (conn == null) {
            throw new RuntimeException("No hay conexion con la base de datos. Revisa config.properties.");
        }
        return conn;
    }

    // CREATE: inserta un grupo y deja que la base de datos asigne ID y created_at.
    public void guardar(Grupo grupo) {
        String sql = "INSERT INTO " + TABLA
                + " (id_materia, id_docente, classroom, schedule) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = obtenerConexion().prepareStatement(sql)) {
            ps.setInt(1, grupo.getId_materia());
            ps.setInt(2, grupo.getId_docente());
            ps.setString(3, grupo.getClassroom());
            ps.setString(4, grupo.getSchedule());
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Error al guardar el grupo: " + ex.getMessage(), ex);
        }
    }

    // READ - All: obtiene todos los grupos ordenados por su identificador.
    public List<Grupo> obtenerTodos() {
        List<Grupo> lista = new ArrayList<>();
        String sql = "SELECT id, id_materia, id_docente, classroom, schedule, created_at FROM "
                + TABLA + " ORDER BY id";
        try (PreparedStatement ps = obtenerConexion().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapearGrupo(rs));
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Error al listar los grupos: " + ex.getMessage(), ex);
        }
        return lista;
    }

    // READ - By ID: busca por clave primaria y devuelve vacío cuando no hay coincidencias.
    public Optional<Grupo> buscarPorId(int id) {
        String sql = "SELECT id, id_materia, id_docente, classroom, schedule, created_at FROM "
                + TABLA + " WHERE id = ?";
        try (PreparedStatement ps = obtenerConexion().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearGrupo(rs));
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Error al buscar el grupo: " + ex.getMessage(), ex);
        }
        return Optional.empty();
    }

    // READ - By Field: busca coincidencias exactas de aula u horario, sin distinguir mayúsculas.
    public Optional<Grupo> buscarPorCampo(String campo) {
        String sql = "SELECT id, id_materia, id_docente, classroom, schedule, created_at FROM "
                + TABLA + " WHERE LOWER(classroom) = LOWER(?) OR LOWER(schedule) = LOWER(?) ORDER BY id";
        try (PreparedStatement ps = obtenerConexion().prepareStatement(sql)) {
            ps.setString(1, campo);
            ps.setString(2, campo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearGrupo(rs));
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Error al buscar el grupo por aula u horario: " + ex.getMessage(), ex);
        }
        return Optional.empty();
    }

    // UPDATE: modifica materia, docente, aula y horario del grupo identificado por ID.
    public void actualizar(Grupo grupo) {
        String sql = "UPDATE " + TABLA
                + " SET id_materia = ?, id_docente = ?, classroom = ?, schedule = ? WHERE id = ?";
        try (PreparedStatement ps = obtenerConexion().prepareStatement(sql)) {
            ps.setInt(1, grupo.getId_materia());
            ps.setInt(2, grupo.getId_docente());
            ps.setString(3, grupo.getClassroom());
            ps.setString(4, grupo.getSchedule());
            ps.setInt(5, grupo.getId());
            int filas = ps.executeUpdate();
            if (filas == 0) {
                throw new RuntimeException("No se encontro el grupo con ID: " + grupo.getId());
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Error al actualizar el grupo: " + ex.getMessage(), ex);
        }
    }

    // DELETE: elimina el grupo con el identificador recibido.
    public void eliminar(int id) {
        String sql = "DELETE FROM " + TABLA + " WHERE id = ?";
        try (PreparedStatement ps = obtenerConexion().prepareStatement(sql)) {
            ps.setInt(1, id);
            int filas = ps.executeUpdate();
            if (filas == 0) {
                throw new RuntimeException("No se encontro el grupo con ID: " + id);
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Error al eliminar el grupo: " + ex.getMessage(), ex);
        }
    }

    // Convierte los campos de una fila SQL en un objeto Grupo.
    private Grupo mapearGrupo(ResultSet rs) throws SQLException {
        return new Grupo(
                rs.getInt("id"),
                rs.getInt("id_materia"),
                rs.getInt("id_docente"),
                rs.getString("classroom"),
                rs.getString("schedule"),
                rs.getTimestamp("created_at"));
    }
}
