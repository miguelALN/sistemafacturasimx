package com.bancomext.web.beans.param;

import com.bancomext.service.ConsultasService;
import com.bancomext.service.GenericService;
import com.bancomext.service.ServiceLocator;
import com.bancomext.service.dto.UsuarioDTO;
import com.bancomext.service.mapping.FiParametrizacionImx;
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
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

@Data
@ManagedBean(name = "parClasificacionCofactor")
@ViewScoped
public class ParClasificacionCofactor implements Serializable {

  private static final Logger log = LogManager.getLogger(ParClasificacionCofactor.class);
  private final GenericService genericService = ServiceLocator.getGenericService();
  private FiParametrizacionImx beanParametrizacion;
  private DataTable htmlDataTable;
  private List<FiParametrizacionImx> listaClasificacionCofactor;
  private UsuarioDTO usuarioLogueado;
  private String modalidad;
  private String tipoProceso;
  private boolean btnUpdateSaveInt;
  private boolean paginatorVisible; // PARA MOSTRAR EL PAGINADOR
  private boolean ppCofactor;
  private int defaultRows = Constantes.REGISTROS;
  private int id;
  private FiParametrizacionImx fiParametrizacionImx;
  private int rowIndex;
  private Boolean editar = false;

  public ParClasificacionCofactor() {
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
    fiParametrizacionImx = new FiParametrizacionImx();
    setListaClasificacionCofactor(
        genericService.get("FiParametrizacionImx", " TIPO_PROCESO = 'COF'", " order by 1"));
    for (FiParametrizacionImx fiParametrizacionImx : listaClasificacionCofactor) {
      fiParametrizacionImx.setCtaOrigenStr(cerosIzquierda(fiParametrizacionImx.getCtaOrigen()));
      fiParametrizacionImx.setSctaOrigenStr(cerosIzquierda(fiParametrizacionImx.getSctaOrigen()));
      fiParametrizacionImx.setSsCtaOrigenStr(cerosIzquierda(fiParametrizacionImx.getSsCtaOrigen()));
      fiParametrizacionImx.setSssCtaOrigenStr(cerosIzquierda(fiParametrizacionImx.getSssCtaOrigen()));
    }
    setPaginatorVisible(listaClasificacionCofactor.size() > 8);
  }

  public String cerosIzquierda(int valor) {
    int number = valor;
    int aux = number;
    String digit = "%02d";
    String digits;
    String formattedNumber = "";
    String count = String.valueOf(number);
    number = count.length();
    if (number == 1 || number == 2) {
      formattedNumber = String.format(digit, aux);
    } else if (number > 2) {
      digits = "%0" + 4 + "d";
      formattedNumber = String.format(digits, aux);
    }
    return formattedNumber;
  }

  public void agregarCofactor() {

    if (fiParametrizacionImx.getOpCode() != null && fiParametrizacionImx.getCtaOrigen() != null && fiParametrizacionImx.getSctaOrigen() != null
                                                 && fiParametrizacionImx.getSsCtaOrigen()  != null && fiParametrizacionImx.getSssCtaOrigen() != null) {
      String tipoProceso = "COF";
      BigDecimal secuencia;
      secuencia = ConsultasService.getMax("FI_PARAMETRIZACION_IMX", "ID");

      if (secuencia == null) {
        secuencia = new BigDecimal("0");
      }

      // VAMOS AL SICAV PARA VALIDAR QUE EXISTAN LAS CUENTAS QUE SE ESTAN PARAMETRIZANDO
      final long ctaOrigenSSicav = ConsultasService.getCtaSicav(fiParametrizacionImx.getCtaOrigen(), fiParametrizacionImx.getSctaOrigen(), fiParametrizacionImx.getSsCtaOrigen(), fiParametrizacionImx.getSssCtaOrigen());

      // SI EXISTE SE GUARDA
      if (ctaOrigenSSicav > 0) {

        id = secuencia.intValue();
        id = id + 1;
        fiParametrizacionImx.setId(id);
        fiParametrizacionImx.setOpCode(fiParametrizacionImx.getOpCode().toUpperCase());
        fiParametrizacionImx.setTipoProceso(tipoProceso);

        try {
          genericService.save(fiParametrizacionImx);
          setPpCofactor(false);//
          init();
          clear();
        } catch (Exception e) {
          mostrarMensaje("Ha ocurrido un error a la hora de guardar el registro", true);
          log.error(e.getMessage(), e);
        }
      } else {
        mostrarMensaje("La cuenta que desea parametrizar no existe. Por favor verifique e intente de nuevo", true);
      }
    } else {
      mostrarMensaje("Todos los campos son requeridos por favor verifique a intente de nuevo", true);
    }
  }

  public void modificaCofactor() {

    // VAMOS AL SICAV PARA VALIDAR QUE EXISTAN LAS CUENTAS QUE SE ESTAN PARAMETRIZANDO
    final long ctaOrigenSSicav = ConsultasService.getCtaSicav(fiParametrizacionImx.getCtaOrigen(), fiParametrizacionImx.getSctaOrigen(), fiParametrizacionImx.getSsCtaOrigen(), fiParametrizacionImx.getSssCtaOrigen());
    // SI EXISTE SE GUARDA
    if (ctaOrigenSSicav > 0) {

      try {
        genericService.update(fiParametrizacionImx);
        clear();
        setPpCofactor(false);
        init();
        mostrarMensaje("Registro actualizado satisfactoriamente", false);
      } catch (Exception e) {
        mostrarMensaje("Surgio un error al actualizar el registro", true);
      }
    } else {
      mostrarMensaje("La cuenta que desea modificar no existe. Por favor verifique e intente de nuevo.", true);
    }
  }

  public void eliminarCofactor() {

    FiParametrizacionImx beanParam;
    try {
      log.info("Eliminar registro Intereses " + getId());

      Predicate<FiParametrizacionImx> parametrizacionImxPredicate = predicate -> predicate.getId().equals(getId());
      Optional<FiParametrizacionImx> optionalFiParametrizacionImx = getListaClasificacionCofactor().stream().filter(parametrizacionImxPredicate).findFirst();
      if (optionalFiParametrizacionImx.isPresent()) {
        beanParam = optionalFiParametrizacionImx.get();
        setBeanParametrizacion(beanParam);
        genericService.delete(getBeanParametrizacion());
        mostrarMensaje("Registro eliminado satisfactorimente", false);
        init();
      }
    } catch (Exception ex) {
      log.error(ex.getMessage(), ex);
    }
  }

  public void muestraEdicion() {

    setPpCofactor(true);
    setBtnUpdateSaveInt(false);

    try {
      fiParametrizacionImx = getListaClasificacionCofactor().get(rowIndex);
    } catch (Exception ex) {
      setPpCofactor(true);
      log.error(ex.getMessage(), ex);
    }
  }

  public void openPanel() {
    setPpCofactor(true);//
    setBtnUpdateSaveInt(true);
    fiParametrizacionImx = new FiParametrizacionImx();
    fiParametrizacionImx.setOpCode("");
    fiParametrizacionImx.setCtaOrigen(null);
    fiParametrizacionImx.setSctaOrigen(null);
    fiParametrizacionImx.setSsCtaOrigen(null);
    fiParametrizacionImx.setSssCtaOrigen(null);

    fiParametrizacionImx.setCtaAbono(null);
    fiParametrizacionImx.setSctaAbono(null);
    fiParametrizacionImx.setSsCtaAbono(null);
    fiParametrizacionImx.setSssCtaAbono(null);

    fiParametrizacionImx.setCtaCargo(null);
    fiParametrizacionImx.setSctaCargo(null);
    fiParametrizacionImx.setSsCtaCargo(null);
    fiParametrizacionImx.setSssCtaCargo(null);
  }

  public void closePanel() {
    clear();
    setPpCofactor(false);
  }

  public void clear() {
    setId(0);
    setTipoProceso(null);
  }

}
