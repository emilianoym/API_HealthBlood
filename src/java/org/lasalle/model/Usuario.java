package org.lasalle.model;

import com.google.gson.annotations.SerializedName;

/**
 * @author emili
 */
public class Usuario {

    @SerializedName("id_usuario")
    private int idUsuario;
    private String nombre;
    @SerializedName("primer_apellido")
    private String primerApellido;
    @SerializedName("segundo_apellido")
    private String segundoApellido;
    private String email;

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getPrimerApellido() { return primerApellido; }
    public void setPrimerApellido(String primerApellido) { this.primerApellido = primerApellido; }

    public String getSegundoApellido() { return segundoApellido; }
    public void setSegundoApellido(String segundoApellido) { this.segundoApellido = segundoApellido; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
