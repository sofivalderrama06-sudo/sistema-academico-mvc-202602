package com.uniajc.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.uniajc.db.DatabaseConexion;
import com.uniajc.modelo.Usuario;
import com.uniajc.util.PasswordUtil;

/**
 * DAO: Responsable de las operaciones CRUD sobre la tabla "practica-mvc".usuarios.
 * La autenticación y almacenamiento de contraseñas se gestiona mediante BCrypt.
 */
public class UsuarioDao {

    private static final String TABLA = "\"practica-mvc\".usuarios";

    private static final String SQL_BUSCAR_POR_USERNAME =
            "SELECT id, username, password, nombre, correo, rol, activo "
            + "FROM " + TABLA + " "
            + "WHERE LOWER(username::text) = LOWER(?) "
            + "AND activo = true";

    private static final String SQL_OBTENER_TODOS =
            "SELECT id, username, password, nombre, correo, rol, activo "
            + "FROM " + TABLA + " "
            + "ORDER BY id ASC";

    private static final String SQL_BUSCAR_POR_ID =
            "SELECT id, username, password, nombre, correo, rol, activo "
            + "FROM " + TABLA + " "
            + "WHERE id = ?";

    private static final String SQL_GUARDAR =
            "INSERT INTO " + TABLA + " (username, password, nombre, correo, rol, activo) "
            + "VALUES (?, ?, ?, ?, ?, ?)";

    private static final String SQL_ACTUALIZAR_CON_PASSWORD =
            "UPDATE " + TABLA + " "
            + "SET username = ?, password = ?, nombre = ?, correo = ?, rol = ?, activo = ? "
            + "WHERE id = ?";

    private static final String SQL_ACTUALIZAR_SIN_PASSWORD =
            "UPDATE " + TABLA + " "
            + "SET username = ?, nombre = ?, correo = ?, rol = ?, activo = ? "
            + "WHERE id = ?";

    private static final String SQL_ELIMINAR =
            "DELETE FROM " + TABLA + " WHERE id = ?";

    private Connection obtenerConexion() {
        Connection conn = DatabaseConexion.getConnection();
        if (conn == null) {
            throw new RuntimeException("No hay conexion con la base de datos. Revisa config.properties.");
        }
        return conn;
    }

    /**
     * Autentica al usuario comparando la contraseña ingresada contra el hash BCrypt de la BD.
     */
    public Optional<Usuario> autenticar(String username, String password) {
        try (PreparedStatement ps = obtenerConexion().prepareStatement(SQL_BUSCAR_POR_USERNAME)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String hashAlmacenado = rs.getString("password");
                    if (PasswordUtil.verificar(password, hashAlmacenado)) {
                        return Optional.of(mapearUsuario(rs));
                    }
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Error al validar el usuario: " + ex.getMessage(), ex);
        }
        return Optional.empty();
    }

    /**
     * Obtiene la lista completa de usuarios registrados.
     */
    public List<Usuario> obtenerTodos() {
        List<Usuario> lista = new ArrayList<>();
        try (PreparedStatement ps = obtenerConexion().prepareStatement(SQL_OBTENER_TODOS);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapearUsuario(rs));
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Error al listar usuarios: " + ex.getMessage(), ex);
        }
        return lista;
    }

    /**
     * Busca un usuario por su ID primario.
     */
    public Optional<Usuario> buscarPorId(int id) {
        try (PreparedStatement ps = obtenerConexion().prepareStatement(SQL_BUSCAR_POR_ID)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearUsuario(rs));
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Error al buscar usuario por ID: " + ex.getMessage(), ex);
        }
        return Optional.empty();
    }

    /**
     * Guarda un nuevo usuario con su contraseña ya encriptada con BCrypt.
     */
    public void guardar(Usuario usuario, String passwordHasheado) {
        try (PreparedStatement ps = obtenerConexion().prepareStatement(SQL_GUARDAR)) {
            ps.setString(1, usuario.getUsername());
            ps.setString(2, passwordHasheado);
            ps.setString(3, usuario.getNombreCompleto());
            ps.setString(4, usuario.getCorreo());
            ps.setString(5, usuario.getRol() == null ? "USUARIO" : usuario.getRol());
            ps.setBoolean(6, usuario.isActivo());
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Error al registrar el usuario: " + ex.getMessage(), ex);
        }
    }

    /**
     * Actualiza los datos de un usuario. Si nuevaPasswordHasheada no es nula, también actualiza la clave.
     */
    public void actualizar(Usuario usuario, String nuevaPasswordHasheada) {
        boolean cambiaClave = nuevaPasswordHasheada != null && !nuevaPasswordHasheada.isBlank();
        String sql = cambiaClave ? SQL_ACTUALIZAR_CON_PASSWORD : SQL_ACTUALIZAR_SIN_PASSWORD;

        try (PreparedStatement ps = obtenerConexion().prepareStatement(sql)) {
            ps.setString(1, usuario.getUsername());
            int idx = 2;
            if (cambiaClave) {
                ps.setString(idx++, nuevaPasswordHasheada);
            }
            ps.setString(idx++, usuario.getNombreCompleto());
            ps.setString(idx++, usuario.getCorreo());
            ps.setString(idx++, usuario.getRol());
            ps.setBoolean(idx++, usuario.isActivo());
            ps.setInt(idx, Integer.parseInt(usuario.getId()));

            int filas = ps.executeUpdate();
            if (filas == 0) {
                throw new RuntimeException("Usuario no encontrado con ID: " + usuario.getId());
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Error al actualizar usuario: " + ex.getMessage(), ex);
        }
    }

    /**
     * Elimina físicamente un usuario por su ID.
     */
    public void eliminar(int id) {
        try (PreparedStatement ps = obtenerConexion().prepareStatement(SQL_ELIMINAR)) {
            ps.setInt(1, id);
            int filas = ps.executeUpdate();
            if (filas == 0) {
                throw new RuntimeException("Usuario no encontrado con ID: " + id);
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Error al eliminar usuario: " + ex.getMessage(), ex);
        }
    }

    private Usuario mapearUsuario(ResultSet rs) throws SQLException {
        return new Usuario(
                String.valueOf(rs.getInt("id")),
                rs.getString("username"),
                rs.getString("nombre"),
                rs.getString("correo"),
                rs.getString("rol"),
                rs.getBoolean("activo")
        );
    }
}
