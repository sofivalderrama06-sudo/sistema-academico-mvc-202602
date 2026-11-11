package com.uniajc.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.uniajc.db.DatabaseConexion;
import com.uniajc.modelo.Docente;

/**
 * DAO: unica clase que escribe SQL para la tabla docentes.
 * Si algo falla al leer o guardar datos, el error esta aqui.
 */
public class DocenteDao {

    // El nombre de la tabla va en una constante: si cambia, se cambia en un solo lugar.
    private static final String TABLA = "\"practica-mvc\".docentes";

    // OJO: la conexion es unica (singleton). Por eso NO se cierra aqui:
    // solo se cierran el PreparedStatement y el ResultSet.
    private Connection obtenerConexion() {
        Connection conn = DatabaseConexion.getConnection();
        if (conn == null) {
            throw new RuntimeException("No hay conexion con la base de datos. Revisa config.properties.");
        }
        return conn;
    }

    public void guardar(Docente docente) {
        String sql = "INSERT INTO " + TABLA + " (name, specialty, email) VALUES (?, ?, ?)";
        try (PreparedStatement ps = obtenerConexion().prepareStatement(sql)) {
            ps.setString(1, docente.getNombre());
            ps.setString(2, docente.getEspecialidad());
            ps.setString(3, docente.getEmail());
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Error al guardar el docente: " + ex.getMessage(), ex);
        }
    }

    public List<Docente> obtenerTodos() {
        List<Docente> lista = new ArrayList<>();
        String sql = "SELECT id, name, specialty, email FROM " + TABLA + " ORDER BY id";
        try (PreparedStatement ps = obtenerConexion().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapearDocente(rs));
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Error al listar los docentes: " + ex.getMessage(), ex);
        }
        return lista;
    }

    public Optional<Docente> buscarPorId(int id) {
        String sql = "SELECT id, name, specialty, email FROM " + TABLA + " WHERE id = ?";
        try (PreparedStatement ps = obtenerConexion().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearDocente(rs));
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Error al buscar el docente: " + ex.getMessage(), ex);
        }
        return Optional.empty();
    }

    public Optional<Docente> buscarPorEmail(String email) {
        String sql = "SELECT id, name, specialty, email FROM " + TABLA + " WHERE LOWER(email) = LOWER(?)";
        try (PreparedStatement ps = obtenerConexion().prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearDocente(rs));
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Error al buscar el docente por correo: " + ex.getMessage(), ex);
        }
        return Optional.empty();
    }

    public void actualizar(Docente docente) {
        String sql = "UPDATE " + TABLA + " SET name = ?, specialty = ?, email = ? WHERE id = ?";
        try (PreparedStatement ps = obtenerConexion().prepareStatement(sql)) {
            ps.setString(1, docente.getNombre());
            ps.setString(2, docente.getEspecialidad());
            ps.setString(3, docente.getEmail());
            ps.setInt(4, docente.getId());
            int filas = ps.executeUpdate();
            if (filas == 0) {
                throw new RuntimeException("No se encontro el docente con ID: " + docente.getId());
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Error al actualizar el docente: " + ex.getMessage(), ex);
        }
    }

    public void eliminar(int id) {
        String sql = "DELETE FROM " + TABLA + " WHERE id = ?";
        try (PreparedStatement ps = obtenerConexion().prepareStatement(sql)) {
            ps.setInt(1, id);
            int filas = ps.executeUpdate();
            if (filas == 0) {
                throw new RuntimeException("No se encontro el docente con ID: " + id);
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Error al eliminar el docente: " + ex.getMessage(), ex);
        }
    }

    // Convierte una fila de la tabla en un objeto Docente (columna name -> nombre, specialty -> especialidad).
    private Docente mapearDocente(ResultSet rs) throws SQLException {
        Docente docente = new Docente();
        docente.setId(rs.getInt("id"));
        docente.setNombre(rs.getString("name"));
        docente.setEspecialidad(rs.getString("specialty"));
        docente.setEmail(rs.getString("email"));
        return docente;
    }
}