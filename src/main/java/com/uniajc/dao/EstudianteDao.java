package com.uniajc.dao;

import java.sql.Connection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
 
import com.uniajc.db.DatabaseConexion;
import com.uniajc.modelo.Estudiante;

public class EstudianteDao {
    // El nombre de la tabla va en una constante: si cambia, se cambia en un solo lugar.
    private static final String TABLA = "\"practica-mvc\".estudiantes";
 
    // OJO: la conexion es unica (singleton). Por eso NO se cierra aqui:
    // solo se cierran el PreparedStatement y el ResultSet.
    private Connection obtenerConexion() {
        Connection conn = DatabaseConexion.getConnection();
        if (conn == null) {
            throw new RuntimeException("No hay conexion con la base de datos. Revisa config.properties.");
        }
        return conn;
    }
 
    public void guardar(Estudiante estudiante) {
        String sql = "INSERT INTO " + TABLA + " (name, lastname, email) VALUES (?, ?, ?)";
        try (PreparedStatement ps = obtenerConexion().prepareStatement(sql)) {
            ps.setString(1, estudiante.getNombre());
            ps.setString(2, estudiante.getApellido());
            ps.setString(3, estudiante.getEmail());
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Error al guardar el estudiante: " + ex.getMessage(), ex);
        }
    }
 
    public List<Estudiante> obtenerTodos() {
        List<Estudiante> lista = new ArrayList<>();
        String sql = "SELECT id, name, lastname, email FROM " + TABLA + " ORDER BY id";
        try (PreparedStatement ps = obtenerConexion().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapearEstudiante(rs));
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Error al listar los estudiantes: " + ex.getMessage(), ex);
        }
        return lista;
    }
 
    public Optional<Estudiante> buscarPorId(int id) {
        String sql = "SELECT id, name, lastname, email FROM " + TABLA + " WHERE id = ?";
        try (PreparedStatement ps = obtenerConexion().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearEstudiante(rs));
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Error al buscar el estudiante: " + ex.getMessage(), ex);
        }
        return Optional.empty();
    }
 
    public Optional<Estudiante> buscarPorEmail(String email) {
        String sql = "SELECT id, name, lastname, email FROM " + TABLA + " WHERE LOWER(email) = LOWER(?)";
        try (PreparedStatement ps = obtenerConexion().prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearEstudiante(rs));
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Error al buscar el estudiante por correo: " + ex.getMessage(), ex);
        }
        return Optional.empty();
    }
 
    public void actualizar(Estudiante estudiante) {
        String sql = "UPDATE " + TABLA + " SET name = ?, lastname = ?, email = ? WHERE id = ?";
        try (PreparedStatement ps = obtenerConexion().prepareStatement(sql)) {
            ps.setString(1, estudiante.getNombre());
            ps.setString(2, estudiante.getApellido());
            ps.setString(3, estudiante.getEmail());
            ps.setInt(4, estudiante.getId());
            int filas = ps.executeUpdate();
            if (filas == 0) {
                throw new RuntimeException("No se encontro el estudiante con ID: " + estudiante.getId());
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Error al actualizar el estudiante: " + ex.getMessage(), ex);
        }
    }
 
    public void eliminar(int id) {
        String sql = "DELETE FROM " + TABLA + " WHERE id = ?";
        try (PreparedStatement ps = obtenerConexion().prepareStatement(sql)) {
            ps.setInt(1, id);
            int filas = ps.executeUpdate();
            if (filas == 0) {
                throw new RuntimeException("No se encontro el estudiante con ID: " + id);
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Error al eliminar el estudiante: " + ex.getMessage(), ex);
        }
    }
 
    // Convierte una fila de la tabla en un objeto Estudiante (columna name -> nombre, lastname -> apellido).
    private Estudiante mapearEstudiante(ResultSet rs) throws SQLException {
        Estudiante estudiante = new Estudiante();
        estudiante.setId(rs.getInt("id"));
        estudiante.setNombre(rs.getString("name"));
        estudiante.setApellido(rs.getString("lastname"));
        estudiante.setEmail(rs.getString("email"));
        return estudiante;
    }
}