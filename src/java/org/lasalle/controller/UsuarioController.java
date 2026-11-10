package org.lasalle.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.lasalle.connection.ConnectionMySql;
import org.lasalle.model.Usuario;

/**
 * @author emili
 */
public class UsuarioController {

    public int insertUsuario(Usuario usuario) throws SQLException {
        String sql = "INSERT INTO users (nombre, primer_apellido, segundo_apellido, email) VALUES (?, ?, ?, ?)";
        try (Connection conn = new ConnectionMySql().open();
             PreparedStatement pstm = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstm.setString(1, usuario.getNombre());
            pstm.setString(2, usuario.getPrimerApellido());
            pstm.setString(3, usuario.getSegundoApellido());
            pstm.setString(4, usuario.getEmail());
            pstm.executeUpdate();
            try (ResultSet keys = pstm.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("No se obtuvo el ID del usuario creado");
                }
                return keys.getInt(1);
            }
        }
    }

    public List<Usuario> getUsuarios() throws SQLException {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT id_usuario, nombre, primer_apellido, segundo_apellido, email FROM users";

        try (Connection conn = new ConnectionMySql().open();
             PreparedStatement pstm = conn.prepareStatement(sql);
             ResultSet rs = pstm.executeQuery()) {

            while (rs.next()) {
                Usuario user = new Usuario();
                user.setIdUsuario(rs.getInt("id_usuario"));
                user.setNombre(rs.getString("nombre"));
                user.setPrimerApellido(rs.getString("primer_apellido"));
                user.setSegundoApellido(rs.getString("segundo_apellido"));
                user.setEmail(rs.getString("email"));
                lista.add(user);
            }
        }
        return lista;
    }

    public boolean existe(int idUsuario) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE id_usuario = ?";
        try (Connection conn = new ConnectionMySql().open();
             PreparedStatement pstm = conn.prepareStatement(sql)) {
            pstm.setInt(1, idUsuario);
            try (ResultSet rs = pstm.executeQuery()) {
                return rs.next();
            }
        }
    }
}
