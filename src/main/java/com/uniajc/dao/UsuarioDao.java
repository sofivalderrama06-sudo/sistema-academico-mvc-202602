package com.uniajc.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import com.uniajc.db.DatabaseConexion;
import com.uniajc.modelo.Usuario;

/**
 * DAO: unica clase que escribe SQL para validar el login.
 * Usa las tablas del esquema "academico" (usuarios y personas).
 * La contrasena se compara con md5() en la base de datos, igual que se
 * guardo el usuario admin en estructura_sistema_academico.sql.
 */
public class UsuarioDao {

    private static final String SQL_LOGIN =
            "SELECT u.usuario_id, u.username, p.nombres, p.apellidos "
            + "FROM academico.usuarios u "
            + "JOIN academico.personas p ON p.persona_id = u.persona_id "
            + "WHERE LOWER(u.username::text) = LOWER(?) "
            + "AND u.password_hash = md5(?) "
            + "AND u.activo = true";

    // La conexion es unica (singleton): NO se cierra aqui.
    private Connection obtenerConexion() {
        Connection conn = DatabaseConexion.getConnection();
        if (conn == null) {
            throw new RuntimeException("No hay conexion con la base de datos. Revisa config.properties.");
        }
        return conn;
    }

    public Optional<Usuario> autenticar(String username, String password) {
        try (PreparedStatement ps = obtenerConexion().prepareStatement(SQL_LOGIN)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Usuario(
                            rs.getString("usuario_id"),
                            rs.getString("username"),
                            rs.getString("nombres"),
                            rs.getString("apellidos")));
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Error al validar el usuario: " + ex.getMessage(), ex);
        }
        return Optional.empty();
    }
}