package com.uniajc.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.uniajc.db.DatabaseConexion;
import com.uniajc.modelo.Inscripcion;

public class InscripcionDao {

    private static final String TABLA = "\"practica-mvc\".inscripciones";

    /** Obtiene la conexión activa o informa que no está disponible. */
    private Connection obtenerConexion() {
        Connection conn = DatabaseConexion.getConnection();
        if (conn == null) {
            throw new RuntimeException("No hay conexion con la base de datos. Revisa config.properties.");
        }
        return conn;
    }

    /** Inserta una inscripción y deja que la base de datos genere ID y fecha. */
    public void guardar(Inscripcion inscripcion) {
        String sql = "INSERT INTO " + TABLA
                + " (id_estudiante, id_grupo, final_grade, status) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = obtenerConexion().prepareStatement(sql)) {
            ps.setInt(1, inscripcion.getId_estudiante());
            ps.setInt(2, inscripcion.getId_grupo());
            ps.setDouble(3, inscripcion.getFinal_grade());
            ps.setString(4, inscripcion.getStatus());
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Error al guardar la inscripcion: " + ex.getMessage(), ex);
        }
    }

    /** Consulta y devuelve todas las inscripciones ordenadas por identificador. */
    public List<Inscripcion> obtenerTodos() {
        List<Inscripcion> lista = new ArrayList<>();
        String sql = "SELECT id, id_estudiante, id_grupo, final_grade, status, created_at FROM "
                + TABLA + " ORDER BY id";
        try (PreparedStatement ps = obtenerConexion().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapearInscripcion(rs));
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Error al listar las inscripciones: " + ex.getMessage(), ex);
        }
        return lista;
    }

    /** Busca una inscripción por ID y devuelve vacío si no existe. */
    public Optional<Inscripcion> buscarPorId(int id) {
        String sql = "SELECT id, id_estudiante, id_grupo, final_grade, status, created_at FROM "
                + TABLA + " WHERE id = ?";
        try (PreparedStatement ps = obtenerConexion().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearInscripcion(rs));
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Error al buscar la inscripcion: " + ex.getMessage(), ex);
        }
        return Optional.empty();
    }

    /** Busca si ya existe una inscripción para la combinación de estudiante y grupo. */
    public Optional<Inscripcion> buscarPorEstudianteYGrupo(int idEstudiante, int idGrupo) {
        String sql = "SELECT id, id_estudiante, id_grupo, final_grade, status, created_at FROM "
                + TABLA + " WHERE id_estudiante = ? AND id_grupo = ?";
        try (PreparedStatement ps = obtenerConexion().prepareStatement(sql)) {
            ps.setInt(1, idEstudiante);
            ps.setInt(2, idGrupo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearInscripcion(rs));
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Error al buscar la inscripcion por estudiante y grupo: " + ex.getMessage(), ex);
        }
        return Optional.empty();
    }

    /** Actualiza los datos de una inscripción existente. */
    public void actualizar(Inscripcion inscripcion) {
        String sql = "UPDATE " + TABLA
                + " SET id_estudiante = ?, id_grupo = ?, final_grade = ?, status = ? WHERE id = ?";
        try (PreparedStatement ps = obtenerConexion().prepareStatement(sql)) {
            ps.setInt(1, inscripcion.getId_estudiante());
            ps.setInt(2, inscripcion.getId_grupo());
            ps.setDouble(3, inscripcion.getFinal_grade());
            ps.setString(4, inscripcion.getStatus());
            ps.setInt(5, inscripcion.getId());
            int filas = ps.executeUpdate();
            if (filas == 0) {
                throw new RuntimeException("No se encontro la inscripcion con ID: " + inscripcion.getId());
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Error al actualizar la inscripcion: " + ex.getMessage(), ex);
        }
    }

    /** Elimina la inscripción identificada por el ID recibido. */
    public void eliminar(int id) {
        String sql = "DELETE FROM " + TABLA + " WHERE id = ?";
        try (PreparedStatement ps = obtenerConexion().prepareStatement(sql)) {
            ps.setInt(1, id);
            int filas = ps.executeUpdate();
            if (filas == 0) {
                throw new RuntimeException("No se encontro la inscripcion con ID: " + id);
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Error al eliminar la inscripcion: " + ex.getMessage(), ex);
        }
    }

    /** Convierte la fila actual del resultado SQL en un modelo Inscripcion. */
    private Inscripcion mapearInscripcion(ResultSet rs) throws SQLException {
        return new Inscripcion(
                rs.getInt("id"),
                rs.getInt("id_estudiante"),
                rs.getInt("id_grupo"),
                rs.getDouble("final_grade"),
                rs.getString("status"),
                rs.getTimestamp("created_at")
        );
    }
}
