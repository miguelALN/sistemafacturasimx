package com.bancomext.web.validator;

import com.bancomext.service.GenericService;
import com.bancomext.service.ServiceLocator;
import com.bancomext.service.dto.UsuarioDTO;
import com.bancomext.service.mapping.FiParamRoles;
import com.bancomext.web.utils.Constantes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class ValidadorSesion implements Serializable {

  private static final Logger log = LogManager.getLogger(ValidadorSesion.class);

  public static UsuarioDTO getUsuarioLogueado() {
    final ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
    final Map<String, Object> sessionMap = externalContext.getSessionMap();
    final UsuarioDTO usuarioLogueado = (UsuarioDTO) sessionMap.get("usuario");
    if (usuarioLogueado == null) {
      try {
        FacesContext.getCurrentInstance().responseComplete();
        externalContext.redirect(externalContext.getRequestContextPath());
      } catch (IOException ioe) {
        log.error(ioe.getMessage(), ioe);
      }
    }
    return usuarioLogueado;
  }

  public static void validarPermiso(final String pagina, final String rol) {
    if (Constantes.PRODUCCION) {
      validarPermiso_PROD(pagina, rol);
    }
  }

  private static void validarPermiso_PROD(final String pagina, final String rol) {
    final ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
    final boolean desarrollo = "Desarrollo".equals(Constantes.AMBIENTE);
    if (!desarrollo) {
      final GenericService genericService = ServiceLocator.getGenericService();
      @SuppressWarnings("unchecked") final List<FiParamRoles> listaPermisos = (List<FiParamRoles>) genericService.get(
          "FiParamRoles", " upper(fiCatRoles.descripcion)=upper('" + rol + "')", " ");
      boolean tienepermiso = false;
      for (FiParamRoles modulos : listaPermisos) {
        if (modulos.getFiModulosMenu().getDescripcion().equals(pagina)) {
          tienepermiso = true;
          break;
        }
      }
      log.info("tiene permiso " + tienepermiso);
      if (!tienepermiso) {
        log.info("AFTER CHECK IF tienepermiso " + tienepermiso);
       /*try {
          FacesContext.getCurrentInstance().responseComplete();
          externalContext.redirect(externalContext.getRequestContextPath());
        } catch (IOException ioe) {
          log.error(ioe.getMessage(), ioe);
        }*/
      }
    }
  }

}
