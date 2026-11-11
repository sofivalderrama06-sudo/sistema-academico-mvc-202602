package com.uniajc.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.uniajc.db.DatabaseConexion;
import com.uniajc.modelo.Materia;

/**
 * DAO encargado de ejecutar las consultas SQL para la tabla materias.
 */
public class MateriaDao {

    // Centraliza el nombre de la tabla y el esquema usados por las consultas.
    private static final String TABLA = "\"practica-mvc\".materias";

    // Recupera la conexión compartida del proyecto; los recursos de cada consulta
    // (PreparedStatement y ResultSet) se cierran dentro de cada método.
    private Connection obtenerConexion() {
        Connection conn = DatabaseConexion.getConnection();
        if (conn == null) {
            throw new RuntimeException("No hay conexion con la base de datos. Revisa config.properties.");
        }
        return conn;
    }

    // CREATE: inserta una materia; created_at queda a cargo del valor por defecto de la base de datos.
    public void guardar(Materia materia) {
        String sql = "INSERT INTO " + TABLA + " (name, credits) VALUES (?, ?)";
        try (PreparedStatement ps = obtenerConexion().prepareStatement(sql)) {
            ps.setString(1, materia.getName());
            ps.setInt(2, materia.getCredits());
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Error al guardar la materia: " + ex.getMessage(), ex);
        }
    }

    // READ - All: obtiene todas las materias ordenadas por su identificador.
    public List<Materia> obtenerTodos() {
        List<Materia> lista = new ArrayList<>();
        String sql = "SELECT id, name, credits, created_at FROM " + TABLA + " ORDER BY id";
        try (PreparedStatement ps = obtenerConexion().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapearMateria(rs));
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Error al listar las materias: " + ex.getMessage(), ex);
        }
        return lista;
    }

    // READ - By ID: busca una materia por su identificador y devuelve Optional.empty() si no existe.
    public Optional<Materia> buscarPorId(int id) {
        String sql = "SELECT id, name, credits, created_at FROM " + TABLA + " WHERE id = ?";
        try (PreparedStatement ps = obtenerConexion().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearMateria(rs));
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Error al buscar la materia: " + ex.getMessage(), ex);
        }
        return Optional.empty();
    }

    // READ - By Field: busca por nombre sin distinguir entre mayúsculas y minúsculas.
    public Optional<Materia> buscarPorCampo(String campo) {
        String sql = "SELECT id, name, credits, created_at FROM " + TABLA
                + " WHERE LOWER(name) = LOWER(?)";
        try (PreparedStatement ps = obtenerConexion().prepareStatement(sql)) {
            ps.setString(1, campo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearMateria(rs));
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Error al buscar la materia por nombre: " + ex.getMessage(), ex);
        }
        return Optional.empty();
    }

    // UPDATE: modifica el nombre y los créditos de la materia identificada por su ID.
    public void actualizar(Materia materia) {
        String sql = "UPDATE " + TABLA + " SET name = ?, credits = ? WHERE id = ?";
        try (PreparedStatement ps = obtenerConexion().prepareStatement(sql)) {
            ps.setString(1, materia.getName());
            ps.setInt(2, materia.getCredits());
            ps.setInt(3, materia.getId());
            int filas = ps.executeUpdate();
            if (filas == 0) {
                throw new RuntimeException("No se encontro la materia con ID: " + materia.getId());
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Error al actualizar la materia: " + ex.getMessage(), ex);
        }
    }

    // DELETE: elimina la materia que corresponde al ID recibido.
    public void eliminar(int id) {
        String sql = "DELETE FROM " + TABLA + " WHERE id = ?";
        try (PreparedStatement ps = obtenerConexion().prepareStatement(sql)) {
            ps.setInt(1, id);
            int filas = ps.executeUpdate();
            if (filas == 0) {
                throw new RuntimeException("No se encontro la materia con ID: " + id);
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Error al eliminar la materia: " + ex.getMessage(), ex);
        }
    }

    // Convierte las columnas de una fila SQL en una instancia del modelo Materia.
    private Materia mapearMateria(ResultSet rs) throws SQLException {
        return new Materia(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getInt("credits"),
                rs.getTimestamp("created_at"));
    }
}
