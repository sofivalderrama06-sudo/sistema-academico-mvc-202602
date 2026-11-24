package com.uniajc.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.uniajc.db.DatabaseConexion;
import com.uniajc.modelo.Periodo;

/**
 * DAO encargado de ejecutar las consultas SQL para la tabla periodo.
 */
public class PeriodoDao {

    // Mantiene centralizado el esquema y el nombre de la tabla para periodos.
    private static final String TABLA = "\"practica-mvc\".periodo";

    // Obtiene la conexión compartida del proyecto; cada operación cierra sus recursos.
    private Connection obtenerConexion() {
        Connection conn = DatabaseConexion.getConnection();
        if (conn == null) {
            throw new RuntimeException("No hay conexion con la base de datos. Revisa config.properties.");
        }
        return conn;
    }

    // CREATE: inserta un periodo y permite que la base de datos asigne el id.
    public void guardar(Periodo periodo) {
        String sql = "INSERT INTO " + TABLA + " (name, start_date, end_date, state) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = obtenerConexion().prepareStatement(sql)) {
            ps.setString(1, periodo.getName());
            ps.setTimestamp(2, periodo.getStartDate());
            ps.setTimestamp(3, periodo.getEndDate());
            ps.setBoolean(4, periodo.isState());
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Error al guardar el periodo: " + ex.getMessage(), ex);
        }
    }

    // READ - All: obtiene todos los periodos ordenados por su identificador.
    public List<Periodo> obtenerTodos() {
        List<Periodo> lista = new ArrayList<>();
        String sql = "SELECT id, name, start_date, end_date, state FROM " + TABLA + " ORDER BY id";
        try (PreparedStatement ps = obtenerConexion().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapearPeriodo(rs));
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Error al listar los periodos: " + ex.getMessage(), ex);
        }
        return lista;
    }

    // READ - By ID: busca un periodo por su clave primaria y devuelve Optional.empty() si no existe.
    public Optional<Periodo> buscarPorId(int id) {
        String sql = "SELECT id, name, start_date, end_date, state FROM " + TABLA + " WHERE id = ?";
        try (PreparedStatement ps = obtenerConexion().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearPeriodo(rs));
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Error al buscar el periodo: " + ex.getMessage(), ex);
        }
        return Optional.empty();
    }

    // READ - By Field: busca periodos por el nombre sin distinguir mayúsculas y minúsculas.
    public Optional<Periodo> buscarPorCampo(String campo) {
        String sql = "SELECT id, name, start_date, end_date, state FROM " + TABLA
                + " WHERE LOWER(name) = LOWER(?)";
        try (PreparedStatement ps = obtenerConexion().prepareStatement(sql)) {
            ps.setString(1, campo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearPeriodo(rs));
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Error al buscar el periodo por nombre: " + ex.getMessage(), ex);
        }
        return Optional.empty();
    }

    // UPDATE: modifica los datos principales del periodo identificado por su ID.
    public void actualizar(Periodo periodo) {
        String sql = "UPDATE " + TABLA + " SET name = ?, start_date = ?, end_date = ?, state = ? WHERE id = ?";
        try (PreparedStatement ps = obtenerConexion().prepareStatement(sql)) {
            ps.setString(1, periodo.getName());
            ps.setTimestamp(2, periodo.getStartDate());
            ps.setTimestamp(3, periodo.getEndDate());
            ps.setBoolean(4, periodo.isState());
            ps.setInt(5, periodo.getId());
            int filas = ps.executeUpdate();
            if (filas == 0) {
                throw new RuntimeException("No se encontro el periodo con ID: " + periodo.getId());
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Error al actualizar el periodo: " + ex.getMessage(), ex);
        }
    }

    // DELETE: elimina el periodo que coincide con el ID recibido.
    public void eliminar(int id) {
        String sql = "DELETE FROM " + TABLA + " WHERE id = ?";
        try (PreparedStatement ps = obtenerConexion().prepareStatement(sql)) {
            ps.setInt(1, id);
            int filas = ps.executeUpdate();
            if (filas == 0) {
                throw new RuntimeException("No se encontro el periodo con ID: " + id);
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Error al eliminar el periodo: " + ex.getMessage(), ex);
        }
    }

    // Convierte una fila SQL en una instancia del modelo Periodo.
    private Periodo mapearPeriodo(ResultSet rs) throws SQLException {
        Periodo periodo = new Periodo();
        periodo.setId(rs.getInt("id"));
        periodo.setName(rs.getString("name"));
        periodo.setStartDate(rs.getTimestamp("start_date"));
        periodo.setEndDate(rs.getTimestamp("end_date"));
        periodo.setState(rs.getBoolean("state"));
        return periodo;
    }
}
