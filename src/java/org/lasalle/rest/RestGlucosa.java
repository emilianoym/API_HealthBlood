package org.lasalle.rest;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lasalle.controller.MedidaController;
import org.lasalle.controller.UsuarioController;
import org.lasalle.model.Medida;
import org.lasalle.model.Usuario;

@Path("glucosa")
public class RestGlucosa {

    private static final Logger LOG = Logger.getLogger(RestGlucosa.class.getName());
    private static final Gson GSON = new Gson();

    /** Rango aceptado para una lectura, en mg/dL (descarta valores imposibles o errores de captura). */
    private static final double GLUCOSA_MIN = 10;
    private static final double GLUCOSA_MAX = 1000;

    @Path("saludar")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response saludar() {
        JsonObject out = new JsonObject();
        out.addProperty("Saludo", "Hola desde Java");
        return Response.ok(GSON.toJson(out)).build();
    }

    @Path("SaludarCumpleanios")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response saludarCumpleanios(@QueryParam("nombre") String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return error(Status.BAD_REQUEST, "Falta el parametro 'nombre'");
        }
        JsonObject out = new JsonObject();
        out.addProperty("response", "Feliz cumple " + nombre);
        return Response.ok(GSON.toJson(out)).build();
    }

    // ---------------------------------------------------------------- usuarios

    @Path("registrarUsuario")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registrarUsuario(@FormParam("nombre") String nombre,
            @FormParam("primer_apellido") String primerApellido,
            @FormParam("segundo_apellido") String segundoApellido,
            @FormParam("email") String email) {
        nombre = limpiar(nombre);
        primerApellido = limpiar(primerApellido);
        segundoApellido = limpiar(segundoApellido);
        email = limpiar(email);
        if (nombre == null) {
            return error(Status.BAD_REQUEST, "Falta 'nombre'");
        }
        if (excede(nombre, 80) || excede(primerApellido, 80) || excede(segundoApellido, 80)
                || excede(email, 120)) {
            return error(Status.BAD_REQUEST, "Nombre y apellidos admiten 80 caracteres; email admite 120");
        }
        if (email != null && !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            return error(Status.BAD_REQUEST, "El email no tiene un formato valido");
        }
        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setPrimerApellido(primerApellido);
        usuario.setSegundoApellido(segundoApellido);
        usuario.setEmail(email);
        try {
            usuario.setIdUsuario(new UsuarioController().insertUsuario(usuario));
            return Response.status(Status.CREATED).entity(GSON.toJson(usuario)).build();
        } catch (Exception e) {
            return errorServidor(e);
        }
    }

    @Path("getAllUsuarios")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllUsuarios() {
        try {
            List<Usuario> lista = new UsuarioController().getUsuarios();
            if (lista.isEmpty()) {
                return error(Status.NOT_FOUND, "Empty table");
            }
            return Response.ok(GSON.toJson(lista)).build();
        } catch (Exception e) {
            return errorServidor(e);
        }
    }

    // ----------------------------------------------------------------- glucosa

    /** GET /api/glucosa/getMedidas              -> todas las lecturas
     *  GET /api/glucosa/getMedidas?idUsuario=1  -> lecturas de ese usuario */
    @Path("getMedidas")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMedidas(@QueryParam("idUsuario") Integer idUsuario) {
        try {
            List<Medida> lista = new MedidaController().getMedidas(idUsuario);
            return Response.ok(GSON.toJson(lista)).build();
        } catch (Exception e) {
            return errorServidor(e);
        }
    }

    /** GET /api/glucosa/getUltimaMedida?idUsuario=1 */
    @Path("getUltimaMedida")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUltimaMedida(@QueryParam("idUsuario") Integer idUsuario) {
        if (idUsuario == null) {
            return error(Status.BAD_REQUEST, "Falta el parametro 'idUsuario'");
        }
        try {
            Medida m = new MedidaController().getUltimaMedida(idUsuario);
            if (m == null) {
                return error(Status.NOT_FOUND, "El usuario no tiene lecturas de glucosa");
            }
            return Response.ok(GSON.toJson(m)).build();
        } catch (Exception e) {
            return errorServidor(e);
        }
    }

    /**
     * POST /api/glucosa/registrarMedida
     * Formulario: id_usuario=1&glucosa_mg_dl=108&contexto=ayuno
     * (fecha_hora y contexto son opcionales)
     */
    @Path("registrarMedida")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registrarMedida(@FormParam("id_usuario") String idUsuario,
            @FormParam("glucosa_mg_dl") String glucosa,
            @FormParam("fecha_hora") String fechaHora,
            @FormParam("contexto") String contexto) {
        Medida m = new Medida();
        idUsuario = limpiar(idUsuario);
        glucosa = limpiar(glucosa);
        contexto = limpiar(contexto);
        if (idUsuario == null || glucosa == null) {
            return error(Status.BAD_REQUEST, "Se requieren 'id_usuario' y 'glucosa_mg_dl'");
        }
        try {
            m.setIdUsuario(Integer.parseInt(idUsuario));
            java.math.BigDecimal valor = new java.math.BigDecimal(glucosa);
            if (valor.stripTrailingZeros().scale() > 2) {
                return error(Status.BAD_REQUEST, "'glucosa_mg_dl' admite como maximo dos decimales");
            }
            m.setGlucosaMgDl(valor.doubleValue());
        } catch (NumberFormatException e) {
            return error(Status.BAD_REQUEST, "'id_usuario' debe ser entero y 'glucosa_mg_dl' debe ser decimal");
        }
        if (m.getIdUsuario() <= 0) {
            return error(Status.BAD_REQUEST, "'id_usuario' debe ser positivo");
        }
        if (m.getGlucosaMgDl() < GLUCOSA_MIN || m.getGlucosaMgDl() > GLUCOSA_MAX) {
            return error(Status.BAD_REQUEST,
                    "'glucosa_mg_dl' debe estar entre " + (int) GLUCOSA_MIN + " y " + (int) GLUCOSA_MAX);
        }
        if (excede(contexto, 40)) {
            return error(Status.BAD_REQUEST, "'contexto' admite como maximo 40 caracteres");
        }
        m.setFechaHora(limpiar(fechaHora));
        m.setContexto(contexto);
        try {
            if (!new UsuarioController().existe(m.getIdUsuario())) {
                return error(Status.NOT_FOUND, "El usuario no existe");
            }
            m.setIdMedida(new MedidaController().insertMedida(m));
            return Response.status(Status.CREATED).entity(GSON.toJson(m)).build();
        } catch (java.time.DateTimeException e) {
            return error(Status.BAD_REQUEST, "'fecha_hora' debe tener formato 2026-10-26T08:30:00");
        } catch (Exception e) {
            return errorServidor(e);
        }
    }

    // ----------------------------------------------------------------- helpers

    private static String limpiar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private static boolean excede(String valor, int limite) {
        return valor != null && valor.codePointCount(0, valor.length()) > limite;
    }

    private Response error(Status status, String mensaje) {
        JsonObject out = new JsonObject();
        out.addProperty("result", mensaje);
        return Response.status(status).entity(GSON.toJson(out)).type(MediaType.APPLICATION_JSON).build();
    }

    private Response errorServidor(Exception e) {
        LOG.log(Level.SEVERE, "Error en la API de glucosa", e);
        return error(Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
    }
}
