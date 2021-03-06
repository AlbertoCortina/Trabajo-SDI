package uo.sdi.acciones;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import uo.sdi.business.CategoryService;
import uo.sdi.business.Services;
import uo.sdi.business.exception.BusinessException;
import uo.sdi.model.Category;
import alb.util.log.Log;

public class ListarCategoriasAction implements Accion {

	@Override
	public String execute(HttpServletRequest request,
			HttpServletResponse response) {

		String resultado = "EXITO";
		Long id = Long.valueOf(request.getParameter("id"));
		List<Category> listaCategorias;
		CategoryService categoryService = Services.getCategoryService();
		try {
			listaCategorias = categoryService.findCategoriesByUserId(id);
			request.setAttribute("listaCategorias", listaCategorias);
			Log.debug(
					"Obtenida lista de categorías conteniendo [%d] categorías",
					listaCategorias.size());
		} catch (BusinessException b) {
			Log.debug("Algo ha ocurrido obteniendo lista de categorías: %s",
					b.getMessage());
			resultado = "FRACASO";
		}
		return resultado;
	}

	@Override
	public String toString() {
		return getClass().getName();
	}
}