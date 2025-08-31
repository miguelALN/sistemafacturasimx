package com.bancomext.web.beans.param;

import com.bancomext.service.ConsultasService;
import com.bancomext.service.GenericService;
import com.bancomext.service.ServiceLocator;
import com.bancomext.service.dto.UsuarioDTO;
import com.bancomext.service.mapping.FiCofactoresImx;
import com.bancomext.web.utils.Constantes;
import com.bancomext.web.validator.ValidadorSesion;
import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.PrimeFaces;
import org.primefaces.component.datatable.DataTable;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

@Data
@ManagedBean(name = "parCofactores")
@ViewScoped
public class ParCofactores implements Serializable {

  private static final Logger log = LogManager.getLogger(ParCofactores.class);
  private final GenericService genericService = ServiceLocator.getGenericService();
  private FiCofactoresImx bean;
  private DataTable htmlDataTable;
  private List<FiCofactoresImx> listaCofactores;
  private UsuarioDTO usuarioLogueado;
  private String campoB;
  private String nombre;
  private UIComponent btnActualiza; // boton para mostrar mensaje que se ha actualizado correctamente la tabla
  private UIComponent btnAgregar; // boton para mostrar mensaje que se ha insertado satisfactorimente
  private UIComponent btnElimina; // boton para mostrar que se eliminado correctamente el registro
  private boolean btnAltaEdita;
  private boolean paginatorVisible;
  private boolean popUpAltaEdita;
  private int defaultRows = Constantes.REGISTROS;
  private int id;
  private int rowIndex;

  public ParCofactores() {
    usuarioLogueado = ValidadorSesion.getUsuarioLogueado();
    if (usuarioLogueado != null) {
      ValidadorSesion.validarPermiso("ParameCofactor", usuarioLogueado.getRol());
    }
    init();
  }

  private static void mostrarMensaje(final String msg, final boolean esError) {
    final FacesMessage mensaje = new FacesMessage();
    mensaje.setSummary(msg);
    mensaje.setDetail("");
    mensaje.setSeverity(esError ? FacesMessage.SEVERITY_ERROR : FacesMessage.SEVERITY_INFO);
    FacesContext.getCurrentInstance().addMessage(null, mensaje);
    PrimeFaces.current().ajax().update(":form1:messages");
  }

  @SuppressWarnings("unchecked")
  private void init() {
    listaCofactores = null;
    setListaCofactores(genericService.get("FiCofactoresImx", "", " order by 1"));
    for (FiCofactoresImx listCofactore : listaCofactores) {
      listCofactore.setCampoB(String.format("%08d", Integer.parseInt(listCofactore.getCampoB())));
    }
    setPaginatorVisible(listaCofactores.size() > 16);
  }

  public void muestraEdicion() {

    setPopUpAltaEdita(true);
    setBtnAltaEdita(false);

    try {

      FiCofactoresImx beanResp = getListaCofactores().get(rowIndex);
      setId(beanResp.getId());
      setCampoB(beanResp.getCampoB());
      setNombre(beanResp.getNombre());

    } catch (Exception ex) {
      setPopUpAltaEdita(false);
      log.error(ex.getMessage(), ex);
    }

  }

  public void eliminarAjuste() {
    try {
      log.info("INTO ELIMINA AJUSTE " + getId());
      Predicate<FiCofactoresImx> cofactoresImxPredicate = cofactor -> cofactor.getId().equals(getId());
      Optional<FiCofactoresImx> optionalFiCofactoresImx = getListaCofactores().stream().filter(cofactoresImxPredicate).findFirst();
      if (optionalFiCofactoresImx.isPresent()) {
        FiCofactoresImx beanCofactores = optionalFiCofactoresImx.get();
        genericService.delete(beanCofactores);
        clear();
        init();
        mostrarMensaje("Registro eliminado satisfactorimente", false);
      }
    } catch (Exception ex) {
      log.error(ex.getMessage(), ex);
    }
  }

  public void agregarAjuste() {
    bean = new FiCofactoresImx();
    if (nombre != null && campoB != null) {
      BigDecimal secuencia = ConsultasService.getMax("FI_COFACTORES_IMX", "ID");
      if (secuencia == null) {
        secuencia = new BigDecimal("0");
      }
      id = secuencia.intValue();
      id = id + 1;
      bean.setId(id);
      bean.setNombre(nombre);
      campoB = String.format("%08d", Integer.parseInt(campoB));
      bean.setCampoB(campoB);
      try {
        genericService.save(bean);
        mostrarMensaje("Se ha insertado satisfactorimente el registro", false);
        closePanel();
        clear();
        init();
      } catch (Exception ex) {
        mostrarMensaje("El registro ya existe.", true);
        log.error(ex.getMessage(), ex);
      }
    } else {
      mostrarMensaje("Todos los campos son requeridos por favor verifique a intente de nuevo", true);
    }
  }

  public void modificaAjuste() {
    log.info("MODIFICA AJUSTE");
    bean = new FiCofactoresImx();
    bean.setId(getId());
    bean.setNombre(getNombre());
    setCampoB(String.format("%08d", Integer.parseInt(getCampoB())));
    bean.setCampoB(getCampoB());
    try {
      genericService.update(bean);
      closePanel();
      clear();
      init();
      mostrarMensaje("Registro actualizado satisfactoriamente", false);
    } catch (Exception ex) {
      mostrarMensaje("El registro ya existe.", true);
      log.error(ex.getMessage(), ex);
    }
  }

  public void clear() {
    setId(0);
    setNombre(null);
    setCampoB(null);
  }

  public void closePanel() {
    setPopUpAltaEdita(false);
    clear();
  }

  public void openPanel() {
    clear();
    setPopUpAltaEdita(true);//
    setBtnAltaEdita(true);
  }
}
