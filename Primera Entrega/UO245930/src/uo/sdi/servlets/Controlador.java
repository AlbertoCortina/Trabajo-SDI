package uo.sdi.servlets;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import alb.util.log.Log;
import uo.sdi.acciones.*;
import uo.sdi.model.User;
import uo.sdi.persistence.exception.PersistenceException;

public class Controlador extends javax.servlet.http.HttpServlet {

	private static final long serialVersionUID = 1L;
	private Map<String, Map<String, Accion>> mapaDeAcciones; // <rol, <opcion,
																// objeto
																// Accion>>
	private Map<String, Map<String, Map<String, String>>> mapaDeNavegacion; // <rol,
																			// <opcion,
																			// <resultado,
																			// JSP>>>

	public void init() throws ServletException {
		crearMapaAcciones();
		crearMapaDeNavegacion();
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

		String accionNavegadorUsuario, resultado, jspSiguiente;
		Accion objetoAccion;
		String rolAntes, rolDespues;

		try {
			accionNavegadorUsuario = request.getServletPath().replace("/", ""); // Obtener
																				// el
																				// string
																				// que
																				// hay
																				// a
																				// la
																				// derecha
																				// de
																				// la
																				// última
																				// /

			rolAntes = obtenerRolDeSesion(request);

			objetoAccion = buscarObjetoAccionParaAccionNavegador(rolAntes,
					accionNavegadorUsuario);

			request.removeAttribute("mensajeParaElUsuario");

			resultado = objetoAccion.execute(request, response);

			rolDespues = obtenerRolDeSesion(request);

			jspSiguiente = buscarJSPEnMapaNavegacionSegun(rolDespues,
					accionNavegadorUsuario, resultado);

			request.setAttribute("jspSiguiente", jspSiguiente);

		} catch (PersistenceException e) {

			request.getSession().invalidate();

			Log.error(
					"Se ha producido alguna excepción relacionada con la persistencia [%s]",
					e.getMessage());
			request.setAttribute("mensajeParaElUsuario",
					"Error irrecuperable: contacte con el responsable de la aplicación");
			jspSiguiente = "/login.jsp";

		} catch (Exception e) {

			request.getSession().invalidate();

			Log.error("Se ha producido alguna excepción no manejada [%s]",
					e.getMessage());
			Log.error(e);
			request.setAttribute("mensajeParaElUsuario",
					"Error irrecuperable: contacte con el responsable de la aplicación");
			jspSiguiente = "/login.jsp";
		}

		RequestDispatcher dispatcher = getServletContext()
				.getRequestDispatcher(jspSiguiente);

		dispatcher.forward(request, response);
	}

	private String obtenerRolDeSesion(HttpServletRequest req) {
		HttpSession sesion = req.getSession();
		if (sesion.getAttribute("user") == null)
			return "ANONIMO";
		else if (((User) sesion.getAttribute("user")).getIsAdmin())
			return "ADMIN";
		else
			return "USUARIO";
	}

	// Obtiene un objeto accion en funcion de la opcion
	// enviada desde el navegador
	private Accion buscarObjetoAccionParaAccionNavegador(String rol,
			String opcion) {

		Accion accion = mapaDeAcciones.get(rol).get(opcion);
		Log.debug("Elegida acción [%s] para opción [%s] y rol [%s]", accion,
				opcion, rol);
		return accion;
	}

	// Obtiene la p�gina JSP a la que habr� que entregar el
	// control el funci�n de la opci�n enviada desde el navegador
	// y el resultado de la ejecuci�n de la acci�n asociada
	private String buscarJSPEnMapaNavegacionSegun(String rol, String opcion,
			String resultado) {

		String jspSiguiente = mapaDeNavegacion.get(rol).get(opcion)
				.get(resultado);
		Log.debug(
				"Elegida página siguiente [%s] para el resultado [%s] tras realizar [%s] con rol [%s]",
				jspSiguiente, resultado, opcion, rol);
		return jspSiguiente;
	}	

	private void crearMapaAcciones() {

		mapaDeAcciones = new HashMap<String, Map<String, Accion>>();

		Map<String, Accion> mapaPublico = new HashMap<String, Accion>();
		mapaPublico.put("registrarse", new RegistrarseAction());
		mapaPublico.put("realizarRegistro", new RealizarRegistroAction());
		mapaPublico.put("validarse", new ValidarseAction());		
		mapaDeAcciones.put("ANONIMO", mapaPublico);

		Map<String, Accion> mapaRegistrado = new HashMap<String, Accion>();
		mapaRegistrado.put("modificarDatos", new ModificarDatosAction());		
		mapaRegistrado.put("listarCategorias", new ListarCategoriasAction());
		mapaRegistrado.put("añadirCategoria", new AñadirCategoriaAction());
		mapaRegistrado.put("listarTareasInbox", new ListarTareasInboxAction());
		mapaRegistrado.put("listarTareasHoy", new ListaTareasHoyAction());
		mapaRegistrado.put("cerrarSesion", new CerrarSesionAction());
		mapaRegistrado.put("finalizarTareaInbox", new FinalizarTareaInboxAction());
		mapaRegistrado.put("finalizarTareaHoy", new FinalizarTareaHoyAction());
		mapaRegistrado.put("añadirTareaInbox", new AñadirTareaAction());
		mapaRegistrado.put("añadirTareaHoy", new AñadirTareaAction());
		mapaDeAcciones.put("USUARIO", mapaRegistrado);
		
		Map<String, Accion> mapaAdministrador = new HashMap<String, Accion>();
		mapaAdministrador.put("modificarDatos", new ModificarDatosAction());
		mapaAdministrador.put("listarUsuarios", new ListarUsuariosAction());
		mapaAdministrador.put("cambiarEstado", new CambiarEstadoUsuarioAction());
		mapaAdministrador.put("cerrarSesion", new CerrarSesionAction());
		mapaDeAcciones.put("ADMIN", mapaAdministrador);
	}

	private void crearMapaDeNavegacion() {

		mapaDeNavegacion = new HashMap<String, Map<String, Map<String, String>>>();

		// Crear mapas auxiliares vacíos
		Map<String, Map<String, String>> opcionResultadoYJSP = new HashMap<String, Map<String, String>>();
		Map<String, String> resultadoYJSP = new HashMap<String, String>();

		// Mapa de navegación de anónimo
		resultadoYJSP = new HashMap<String, String>(); 
		resultadoYJSP.put("FRACASO", "/login.jsp");
		opcionResultadoYJSP.put("validarse", resultadoYJSP);		

		resultadoYJSP = new HashMap<String, String>();
		resultadoYJSP.put("EXITO", "/registrarse.jsp");
		opcionResultadoYJSP.put("registrarse", resultadoYJSP);
		
		resultadoYJSP = new HashMap<String, String>();
		resultadoYJSP.put("EXITO", "/login.jsp");
		resultadoYJSP.put("FRACASO", "/registrarse.jsp");
		opcionResultadoYJSP.put("realizarRegistro", resultadoYJSP);

		mapaDeNavegacion.put("ANONIMO", opcionResultadoYJSP);

		// Crear mapas auxiliares vacíos
		opcionResultadoYJSP = new HashMap<String, Map<String, String>>();
		resultadoYJSP = new HashMap<String, String>();

		// Mapa de navegación de usuarios normales
		resultadoYJSP = new HashMap<String, String>();
		resultadoYJSP.put("EXITO", "/principalUsuario.jsp");
		opcionResultadoYJSP.put("validarse", resultadoYJSP);
		
		resultadoYJSP = new HashMap<String, String>();
		resultadoYJSP.put("EXITO", "/principalUsuario.jsp");
		resultadoYJSP.put("FRACASO", "/principalUsuario.jsp");
		opcionResultadoYJSP.put("modificarDatos", resultadoYJSP);
		
		resultadoYJSP = new HashMap<String, String>();
		resultadoYJSP.put("EXITO", "/listarCategorias.jsp");
		opcionResultadoYJSP.put("listarCategorias", resultadoYJSP);		
		
		resultadoYJSP = new HashMap<String, String>();
		resultadoYJSP.put("EXITO", "/listarCategorias.jsp");
		resultadoYJSP.put("FRACASO", "/listarCategorias.jsp");
		opcionResultadoYJSP.put("añadirCategoria", resultadoYJSP);
		
		resultadoYJSP = new HashMap<String, String>();
		resultadoYJSP.put("EXITO", "/listarTareasInbox.jsp");
		resultadoYJSP.put("FRACASO", "/principalUsuario.jsp");
		opcionResultadoYJSP.put("listarTareasInbox", resultadoYJSP);
		
		resultadoYJSP = new HashMap<String, String>();
		resultadoYJSP.put("EXITO", "/listarTareasHoy.jsp");
		resultadoYJSP.put("FRACASO", "/principalUsuario.jsp");
		opcionResultadoYJSP.put("listarTareasHoy", resultadoYJSP);		
		
		resultadoYJSP = new HashMap<String, String>();
		resultadoYJSP.put("EXITO", "/listarTareasInbox");
		resultadoYJSP.put("FRACASO", "/listarTareasInbox");
		opcionResultadoYJSP.put("añadirTareaInbox", resultadoYJSP);
		
		resultadoYJSP = new HashMap<String, String>();
		resultadoYJSP.put("EXITO", "/listarTareasHoy");
		resultadoYJSP.put("FRACASO", "/listarTareasHoy");
		opcionResultadoYJSP.put("añadirTareaHoy", resultadoYJSP);
		
		resultadoYJSP = new HashMap<String, String>();
		resultadoYJSP.put("EXITO", "/listarTareasInbox");
		resultadoYJSP.put("FRACASO", "/listarTareasInbox");
		opcionResultadoYJSP.put("finalizarTareaInbox", resultadoYJSP);
		
		resultadoYJSP = new HashMap<String, String>();
		resultadoYJSP.put("EXITO", "/listarTareasHoy");
		resultadoYJSP.put("FRACASO", "/listarTareasHoy");
		opcionResultadoYJSP.put("finalizarTareaHoy", resultadoYJSP);
		
		resultadoYJSP = new HashMap<String, String>();
		resultadoYJSP.put("EXITO", "/login.jsp");
		opcionResultadoYJSP.put("cerrarSesion", resultadoYJSP);

		mapaDeNavegacion.put("USUARIO", opcionResultadoYJSP);

		// Crear mapas auxiliares vacíos
		opcionResultadoYJSP = new HashMap<String, Map<String, String>>();
		resultadoYJSP = new HashMap<String, String>();
				
		// Mapa de navegación del administrador
		resultadoYJSP = new HashMap<String, String>();
		resultadoYJSP.put("EXITO", "/principalAdministrador.jsp");
		opcionResultadoYJSP.put("validarse", resultadoYJSP);
		
		resultadoYJSP = new HashMap<String, String>();
		resultadoYJSP.put("EXITO", "/principalAdministrador.jsp");
		resultadoYJSP.put("FRACASO", "/principalAdministrador.jsp");
		opcionResultadoYJSP.put("modificarDatos", resultadoYJSP);
		
		resultadoYJSP = new HashMap<String, String>();
		resultadoYJSP.put("EXITO", "/listarUsuarios.jsp");
		resultadoYJSP.put("FRACASO", "/principalAdministrador.jsp");
		opcionResultadoYJSP.put("listarUsuarios", resultadoYJSP);		
		
		resultadoYJSP = new HashMap<String, String>();
		resultadoYJSP.put("EXITO", "/listarUsuarios.jsp");
		resultadoYJSP.put("FRACASO", "/listarUsuarios.jsp");
		opcionResultadoYJSP.put("cambiarEstado", resultadoYJSP);
		
		resultadoYJSP = new HashMap<String, String>();
		resultadoYJSP.put("EXITO", "/login.jsp");
		opcionResultadoYJSP.put("cerrarSesion", resultadoYJSP);
		
		mapaDeNavegacion.put("ADMIN", opcionResultadoYJSP);
	}

	public void doPost(HttpServletRequest req, HttpServletResponse res)
			throws IOException, ServletException {

		doGet(req, res);
	}
}