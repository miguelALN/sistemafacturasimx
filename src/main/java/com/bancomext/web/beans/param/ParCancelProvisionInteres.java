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
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

@Data
@ManagedBean(name = "parCancelProvisionInteres")
@ViewScoped
public class ParCancelProvisionInteres implements Serializable {

  private static final Logger log = LogManager.getLogger(ParCancelProvisionInteres.class);
  private final GenericService genericService = ServiceLocator.getGenericService();
  // VARIABLE PARA PARAMETRIZACION DE LA CANCELACION DE LA PROVISION DE INTERESES
  private FiParametrizacionImx beanParametrizacion;
  private DataTable htmlDataTable;
  private List<FiParametrizacionImx> listaCancelacionProvision;
  private UsuarioDTO usuarioLogueado;
  private String tipoProceso;
  private UIComponent btnError;
  private UIComponent btnMensaje;
  private UIComponent uiMensaje;
  private boolean btnUpdateSave;
  private boolean paginatorVisible;
  private boolean ppCancelacion;
  private int defaultRows = Constantes.REGISTROS;
  private int id;
  private int rowIndex;
  private FiParametrizacionImx fiParametrizacionImx;
  private Boolean editar = false;

  public ParCancelProvisionInteres() {
    usuarioLogueado = ValidadorSesion.getUsuarioLogueado();
    if (usuarioLogueado != null) {
      ValidadorSesion.validarPermiso("ParameEjecucionProceso", usuarioLogueado.getRol());
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
    setListaCancelacionProvision(
        genericService.get("FiParametrizacionImx", " TIPO_PROCESO = 'CAN'", " order by 1"));

    for (FiParametrizacionImx fiParametrizacionImx : listaCancelacionProvision) {
      fiParametrizacionImx.setCtaOrigenStr(cerosIzquierda(fiParametrizacionImx.getCtaOrigen()));
      fiParametrizacionImx.setSctaOrigenStr(cerosIzquierda(fiParametrizacionImx.getSctaOrigen()));
      fiParametrizacionImx.setSsCtaOrigenStr(cerosIzquierda(fiParametrizacionImx.getSsCtaOrigen()));
      fiParametrizacionImx.setSssCtaOrigenStr(cerosIzquierda(fiParametrizacionImx.getSssCtaOrigen()));
      fiParametrizacionImx.setCtaCargoStr(cerosIzquierda(fiParametrizacionImx.getCtaCargo()));
      fiParametrizacionImx.setSctaCargoStr(cerosIzquierda(fiParametrizacionImx.getSctaCargo()));
      fiParametrizacionImx.setSsCtaCargoStr(cerosIzquierda(fiParametrizacionImx.getSsCtaCargo()));
      fiParametrizacionImx.setSssCtaCargoStr(cerosIzquierda(fiParametrizacionImx.getSssCtaCargo()));
      fiParametrizacionImx.setCtaAbonoStr(cerosIzquierda(fiParametrizacionImx.getCtaAbono()));
      fiParametrizacionImx.setSctaAbonoStr(cerosIzquierda(fiParametrizacionImx.getSctaAbono()));
      fiParametrizacionImx.setSsCtaAbonoStr(cerosIzquierda(fiParametrizacionImx.getSsCtaAbono()));
      fiParametrizacionImx.setSssCtaAbonoStr(cerosIzquierda(fiParametrizacionImx.getSssCtaAbono()));
    }
    setPaginatorVisible(listaCancelacionProvision.size() > 8);
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

  public void guardarCancelacion() {

    log.info("into guardarCancelacion() opCode " + fiParametrizacionImx.getOpCode());

    if (fiParametrizacionImx.getOpCode() != null && fiParametrizacionImx.getCtaOrigen() != null
                                  && fiParametrizacionImx.getSsCtaOrigen()  != null && fiParametrizacionImx.getCtaCargo()  != null
                                  && fiParametrizacionImx.getSctaCargo() != null && fiParametrizacionImx.getSssCtaCargo() != null
                                  && fiParametrizacionImx.getSsCtaCargo()  != null && fiParametrizacionImx.getCtaAbono() != null
                                  && fiParametrizacionImx.getSctaAbono() != null && fiParametrizacionImx.getSsCtaAbono()  != null
                                  && fiParametrizacionImx.getSssCtaAbono() != null) {

      tipoProceso = "CAN";
      BigDecimal secuencia;
      secuencia = ConsultasService.getMax("FI_PARAMETRIZACION_IMX", "ID");
      if (secuencia == null) {
        secuencia = new BigDecimal("0");
      }
      id = secuencia.intValue();
      id = id + 1;


      log.info("BEFORE SAVE ");
      // VAMOS AL SICAV PARA VALIDAR QUE EXISTAN LAS CUENTAS QUE SE ESTAN PARAMETRIZANDO
      final long ctaOrigenSSicav = ConsultasService.getCtaSicav(fiParametrizacionImx.getCtaOrigen(), fiParametrizacionImx.getSctaOrigen(), fiParametrizacionImx.getSsCtaOrigen(), fiParametrizacionImx.getSssCtaOrigen());
      final long ctaCargoSSicav =  ConsultasService.getCtaSicav(fiParametrizacionImx.getCtaCargo(), fiParametrizacionImx.getSctaCargo(), fiParametrizacionImx.getSsCtaCargo(), fiParametrizacionImx.getSssCtaCargo());

      // SI EXISTE SE GUARDA
      if (ctaOrigenSSicav > 0 && ctaCargoSSicav > 0) {

        try {
          log.info("gointo save ");
          fiParametrizacionImx.setId(id);
          fiParametrizacionImx.setOpCode(fiParametrizacionImx.getOpCode().toUpperCase());
          fiParametrizacionImx.setTipoProceso(tipoProceso);
          genericService.save(fiParametrizacionImx);
          log.info("was save ");
          setPpCancelacion(false);
          init();
          clear();
          mostrarMensaje("Se ha insertado satisfactoriamente el registro", false);
        } catch (Exception e) {
          setPpCancelacion(false);
          mostrarMensaje("Ha ocurrido un error a la hora de guardar el registro", true);
          log.error(e.getMessage(), e);
        }
      } else {
        log.info("no guarda");
        mostrarMensaje("La cuenta que desea parametrizar no existe. Por favor verifique e intente de nuevo", true);
      }
    } else {
      mostrarMensaje("Todos los campos son requeridos por favor verifique a intente de nuevo", true);
    }
  }

  public void muestraEdicion() throws Exception {

    log.info("INTO muestraEdicion " + rowIndex + " editar " + editar);
    setPpCancelacion(true);
    //setBtnUpdateSave(false);
    btnUpdateSave = false;

    try {

      if (editar) {
        fiParametrizacionImx = getListaCancelacionProvision().get(rowIndex);
        log.info("fiParametrizacionImx " + fiParametrizacionImx.getOpCode() + "--" + fiParametrizacionImx.getId());
      }
      editar = false;
    } catch (Exception ex) {
      setPpCancelacion(false);
      log.error(ex.getMessage(), ex);
    }
  }

  public void eliminarCancelacion() {

    FiParametrizacionImx beanParam;
    try {

      log.info("Eliminar Rol Factoraje IMX " + getId());

      Predicate<FiParametrizacionImx> parametrizacionImxPredicate = fiParametrizacionImx -> fiParametrizacionImx.getId().equals(getId());
      Optional<FiParametrizacionImx> optionalFiFechasEjecucionImx = getListaCancelacionProvision().stream().filter(parametrizacionImxPredicate).findFirst();
      if (optionalFiFechasEjecucionImx.isPresent()) {
        beanParam = optionalFiFechasEjecucionImx.get();
        setBeanParametrizacion(beanParam);
        genericService.delete(getBeanParametrizacion());
        init();
        mostrarMensaje("Registro eliminado satisfactorimente", false);
      }
    } catch (Exception ex) {
      log.error(ex.getMessage(), ex);
    }
  }

  public void modificaCancelacion() {

    log.info("INTO modificaCancelacion ");

    final long ctaOrigenSSicav = ConsultasService.getCtaSicav(fiParametrizacionImx.getCtaOrigen(), fiParametrizacionImx.getSctaOrigen(), fiParametrizacionImx.getSsCtaOrigen(), fiParametrizacionImx.getSssCtaOrigen());
    final long ctaCargoSSicav =  ConsultasService.getCtaSicav(fiParametrizacionImx.getCtaCargo(), fiParametrizacionImx.getSctaCargo(), fiParametrizacionImx.getSsCtaCargo(), fiParametrizacionImx.getSssCtaCargo());
    //SI EXISTE SE GUARDA
    if (ctaOrigenSSicav > 0 && ctaCargoSSicav > 0) {

      try {
        genericService.update(fiParametrizacionImx);
        clear();
        setPpCancelacion(false);
        init();
        mostrarMensaje("Registro actualizado satisfactoriamente", false);
      } catch (Exception e) {
        mostrarMensaje("Surgio un error al actualizar el registro", true);
      }
    } else {
      mostrarMensaje("La cuenta que desea parametrizar no existe. Por favor verifique e intente de nuevo.", true);
    }
  }

  public void cancelarCancelacion() {
    clear();
    setPpCancelacion(false);
  }

  public void agregarCancelacion() {

    log.info("INTO agregarCancelacion " + btnUpdateSave );
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
    log.info("AGREGAR CANCELACIO " + fiParametrizacionImx.getOpCode() + "--" + fiParametrizacionImx.getSsCtaCargo());

    setPpCancelacion(true);
    //setBtnUpdateSave(true);
    btnUpdateSave = true;
    log.info("INTO agregarCancelacion " + btnUpdateSave );
  }


  public void clear() {
    log.info("INTO CLEAR BEFORE");
    setTipoProceso(null);
    log.info("INTO CLEAR AFTER");
  }

}
