package org.lasalle.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import org.lasalle.connection.ConnectionMySql;
import org.lasalle.model.Medida;

/**
 * Lectura y registro de mediciones de glucosa.
 *
 * @author emili
 */
public class MedidaController {

    private static final String COLUMNAS
            = "id_medida, id_usuario, glucosa_mg_dl, fecha_hora, contexto";

    /** Todas las lecturas (idUsuario == null) o las de un usuario, de la mas reciente a la mas antigua. */
    public List<Medida> getMedidas(Integer idUsuario) throws SQLException {
        List<Medida> lista = new ArrayList<>();
        String sql = "SELECT " + COLUMNAS + " FROM medidas"
                + (idUsuario != null ? " WHERE id_usuario = ?" : "")
                + " ORDER BY fecha_hora DESC, id_medida DESC";

        try (Connection conn = new ConnectionMySql().open();
             PreparedStatement pstm = conn.prepareStatement(sql)) {
            if (idUsuario != null) {
                pstm.setInt(1, idUsuario);
            }
            try (ResultSet rs = pstm.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    /** Ultima lectura de un usuario, o null si todavia no tiene. */
    public Medida getUltimaMedida(int idUsuario) throws SQLException {
        String sql = "SELECT " + COLUMNAS + " FROM medidas WHERE id_usuario = ?"
                + " ORDER BY fecha_hora DESC, id_medida DESC LIMIT 1";

        try (Connection conn = new ConnectionMySql().open();
             PreparedStatement pstm = conn.prepareStatement(sql)) {
            pstm.setInt(1, idUsuario);
            try (ResultSet rs = pstm.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    /**
     * Guarda una lectura y devuelve su id.
     *
     * @throws DateTimeParseException si fecha_hora no tiene formato ISO
     */
    public int insertMedida(Medida m) throws SQLException {
        String sql = "INSERT INTO medidas (id_usuario, glucosa_mg_dl, fecha_hora, contexto)"
                + " VALUES (?, ?, ?, ?)";

        Timestamp cuando = (m.getFechaHora() == null || m.getFechaHora().isBlank())
                ? Timestamp.valueOf(LocalDateTime.now())
                : Timestamp.valueOf(LocalDateTime.parse(m.getFechaHora().trim().replace(' ', 'T')));
        int anio = cuando.toLocalDateTime().getYear();
        if (anio < 1000 || anio > 9999) {
            throw new java.time.DateTimeException("Fecha fuera del rango de MySQL DATETIME");
        }
        cuando.setNanos(0);
        m.setFechaHora(cuando.toLocalDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        try (Connection conn = new ConnectionMySql().open();
             PreparedStatement pstm = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstm.setInt(1, m.getIdUsuario());
            pstm.setDouble(2, m.getGlucosaMgDl());
            pstm.setTimestamp(3, cuando);
            pstm.setString(4, m.getContexto());
            pstm.executeUpdate();

            try (ResultSet keys = pstm.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("No se obtuvo el ID de la medida creada");
                }
                return keys.getInt(1);
            }
        }
    }

    private Medida mapear(ResultSet rs) throws SQLException {
        Medida m = new Medida();
        m.setIdMedida(rs.getInt("id_medida"));
        m.setIdUsuario(rs.getInt("id_usuario"));
        m.setGlucosaMgDl(rs.getDouble("glucosa_mg_dl"));
        Timestamp ts = rs.getTimestamp("fecha_hora");
        m.setFechaHora(ts == null ? null
                : ts.toLocalDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        m.setContexto(rs.getString("contexto"));
        return m;
    }
}
