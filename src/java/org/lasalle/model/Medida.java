package org.lasalle.model;

import com.google.gson.annotations.SerializedName;

/**
 * Una lectura de glucosa de un usuario.
 *
 * @author emili
 */
public class Medida {

    @SerializedName("id_medida")
    private int idMedida;
    @SerializedName("id_usuario")
    private int idUsuario;
    @SerializedName("glucosa_mg_dl")
    private double glucosaMgDl;
    /** Formato ISO: 2026-10-26T08:30:00 (si no se envia, se usa la hora del servidor). */
    @SerializedName("fecha_hora")
    private String fechaHora;
    /** Opcional: ayuno, postprandial, etc. */
    private String contexto;

    public int getIdMedida() { return idMedida; }
    public void setIdMedida(int idMedida) { this.idMedida = idMedida; }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public double getGlucosaMgDl() { return glucosaMgDl; }
    public void setGlucosaMgDl(double glucosaMgDl) { this.glucosaMgDl = glucosaMgDl; }

    public String getFechaHora() { return fechaHora; }
    public void setFechaHora(String fechaHora) { this.fechaHora = fechaHora; }

    public String getContexto() { return contexto; }
    public void setContexto(String contexto) { this.contexto = contexto; }
}
