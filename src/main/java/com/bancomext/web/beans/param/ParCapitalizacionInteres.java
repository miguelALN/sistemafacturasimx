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
@ManagedBean(name = "parCapitalizacionInteres")
@ViewScoped
public class ParCapitalizacionInteres implements Serializable {

  private static final Logger log = LogManager.getLogger(ParCapitalizacionInteres.class);
  private final GenericService genericService = ServiceLocator.getGenericService();
  private FiParametrizacionImx beanParametrizacion;
  private DataTable htmlDataTable;
  private Integer sctaSust;
  private List<FiParametrizacionImx> listaCapitalizacionInt;
  private UsuarioDTO usuarioLogueado;
  private String opCode;
  private String tipoProceso;
  private boolean btnUpdateSaveInt;
  private boolean paginatorVisible;
  private boolean ppIntereses;
  private int defaultRows = Constantes.REGISTROS;
  private int id;
  private FiParametrizacionImx fiParametrizacionImx;
  private Boolean editar = false;
  private int rowIndex;

  public ParCapitalizacionInteres() {
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

    log.info("INTO INIT CAPITALIZACION");
    fiParametrizacionImx = new FiParametrizacionImx();
    setListaCapitalizacionInt(genericService.get(
        "FiParametrizacionImx", "OPCODE = 'VF_KL_FUC' AND TIPO_PROCESO = 'CAP'", " order by 1"));
    for (FiParametrizacionImx fiParametrizacionImx : listaCapitalizacionInt) {
      log.info("id " + fiParametrizacionImx.getId());
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
      fiParametrizacionImx.setCtaSustStr(cerosIzquierda(fiParametrizacionImx.getCtaSust()));
      fiParametrizacionImx.setSctaSustStr(cerosIzquierda(fiParametrizacionImx.getSCtaSust()));
      fiParametrizacionImx.setSsCtaSustStr(cerosIzquierda(fiParametrizacionImx.getSsCtaSust()));
      fiParametrizacionImx.setSssCtaSustStr(cerosIzquierda(fiParametrizacionImx.getSssCtaSust()));
    }
    setPaginatorVisible(listaCapitalizacionInt.size() > 8);
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

  public void agregarIntereses() {

    log.info("INTO SAVE VALUES opCode" + fiParametrizacionImx.getOpCode() + "- ctaOrigen " + fiParametrizacionImx.getCtaOrigen() + "-sctaOrigen " + fiParametrizacionImx.getSctaOrigen()  + "- ssCtaOrigen " + fiParametrizacionImx.getSsCtaOrigen() + "-sssCtaOrigen " + fiParametrizacionImx.getSssCtaOrigen() + "-ctaCargo " + fiParametrizacionImx.getCtaCargo());
    log.info("INTO SAVE VALUES sctaCargo " +  fiParametrizacionImx.getSctaCargo() + "-ssCtaCargo " + fiParametrizacionImx.getSsCtaCargo() + "-sssCtaCargo " + fiParametrizacionImx.getSssCtaSust() + "-ctaAbono " + fiParametrizacionImx.getCtaAbono() + "-sctaAbono " + fiParametrizacionImx.getSctaAbono() + "-ssCtaAbono " + fiParametrizacionImx.getSsCtaAbono() + "-sssCtaAbono" + fiParametrizacionImx.getSssCtaCargo());
    fiParametrizacionImx.setSCtaSust(sctaSust);
    if (fiParametrizacionImx.getOpCode() != null
        && fiParametrizacionImx.getCtaOrigen() != null && fiParametrizacionImx.getSctaOrigen() != null && fiParametrizacionImx.getSsCtaOrigen() != null && fiParametrizacionImx.getSssCtaOrigen() != null
        && fiParametrizacionImx.getCtaCargo() != null && fiParametrizacionImx.getSctaCargo() != null && fiParametrizacionImx.getSsCtaCargo() != null && fiParametrizacionImx.getSssCtaCargo() != null
        && fiParametrizacionImx.getCtaSust()!= null && fiParametrizacionImx.getSCtaSust() != null && fiParametrizacionImx.getSsCtaSust() != null && fiParametrizacionImx.getSssCtaSust()  != null
        && fiParametrizacionImx.getCtaAbono() != null && fiParametrizacionImx.getSctaAbono() != null && fiParametrizacionImx.getSsCtaAbono() != null && fiParametrizacionImx.getSssCtaAbono()  != null) {

      String tipoProceso = "CAP";
      FiParametrizacionImx beanCancelacion = new FiParametrizacionImx();

      BigDecimal secuencia;
      secuencia = ConsultasService.getMax("FI_PARAMETRIZACION_IMX", "ID");

      if (secuencia == null) {
        secuencia = new BigDecimal("0");
      }

      id = secuencia.intValue();
      id = id + 1;
      fiParametrizacionImx.setId(id);
      fiParametrizacionImx.setOpCode(fiParametrizacionImx.getOpCode().toUpperCase());

      // VAMOS AL SICAV PARA VALIDAR QUE EXISTAN LAS CUENTAS QUE SE ESTAN PARAMETRIZANDO
      log.info("valores ctaOrigenSSicav " + fiParametrizacionImx.getCtaOrigen() + "-" + fiParametrizacionImx.getSctaOrigen() + "-" + fiParametrizacionImx.getSsCtaOrigen() + "-" + fiParametrizacionImx.getSssCtaOrigen());
      final long ctaOrigenSSicav = ConsultasService.getCtaSicav(fiParametrizacionImx.getCtaOrigen(), fiParametrizacionImx.getSctaOrigen(), fiParametrizacionImx.getSsCtaOrigen(), fiParametrizacionImx.getSssCtaOrigen());
      log.info("varlores ctaCargoSSicav " + fiParametrizacionImx.getCtaCargo() + "-"  + fiParametrizacionImx.getSctaCargo() + "-" + fiParametrizacionImx.getSsCtaCargo() + "-" + fiParametrizacionImx.getSssCtaCargo());
      final long ctaCargoSSicav = ConsultasService.getCtaSicav(fiParametrizacionImx.getCtaCargo(), fiParametrizacionImx.getSctaCargo(), fiParametrizacionImx.getSsCtaCargo(), fiParametrizacionImx.getSssCtaCargo());

      // SI EXISTE SE GUARDA
      if (ctaOrigenSSicav > 0 && ctaCargoSSicav > 0) {

        if (fiParametrizacionImx.getCtaAbono() == null && fiParametrizacionImx.getSctaAbono() == null && fiParametrizacionImx.getSsCtaAbono() == null && fiParametrizacionImx.getSssCtaAbono()  == null) {
          fiParametrizacionImx.setCtaAbono(0);
          fiParametrizacionImx.setSctaAbono(0);
          fiParametrizacionImx.setSsCtaAbono(0);
          fiParametrizacionImx.setSssCtaAbono(0);
        }

        fiParametrizacionImx.setTipoProceso(tipoProceso);
        try {
          genericService.save(fiParametrizacionImx);
          log.info("Provision guardada satisfactoriamente");
          setPpIntereses(false);//
          init();
          clear();
          mostrarMensaje("Se ha insertado satisfactoriamente el registro", false);
        } catch (Exception e) {
          log.error(e.getMessage(), e);
          mostrarMensaje("Ha ocurrido un error a la hora de guardar el registro", true);
        }
      } else {
        mostrarMensaje("La cuenta que desea parametrizar no existe. Por favor verifique e intente de nuevo.", false);
      }
    } else {
      mostrarMensaje("Todos los campos son requeridos por favor verifique a intente de nuevo.", false);
    }
  }

  public void muestraEdicion() {

    setPpIntereses(true);
    setBtnUpdateSaveInt(false);

    try {
      fiParametrizacionImx =  getListaCapitalizacionInt().get(rowIndex);
      sctaSust = fiParametrizacionImx.getSCtaSust();
    } catch (Exception ex) {
      setPpIntereses(true);
      log.error(ex.getMessage(), ex);
    }
  }

  public void modificaIntereses() {
    beanParametrizacion = new FiParametrizacionImx();

    final long ctaOrigenSSicav = ConsultasService.getCtaSicav(fiParametrizacionImx.getCtaOrigen(), fiParametrizacionImx.getSctaOrigen(), fiParametrizacionImx.getSsCtaOrigen(), fiParametrizacionImx.getSssCtaOrigen());
    final long ctaCargoSSicav = ConsultasService.getCtaSicav(fiParametrizacionImx.getCtaCargo(), fiParametrizacionImx.getSctaCargo(), fiParametrizacionImx.getSsCtaCargo(), fiParametrizacionImx.getSssCtaCargo());
    //SI EXISTE SE GUARDA
    if (ctaOrigenSSicav > 0 && ctaCargoSSicav > 0) {
      fiParametrizacionImx.setSCtaSust(sctaSust);
      try {
        genericService.update(fiParametrizacionImx);
        clear();
        setPpIntereses(false);
        init();
      } catch (Exception e) {
        log.error(e.getMessage(), e);
      }
    } else {
      mostrarMensaje("La cuenta que desea modificar no existe. Por favor verifique e intente de nuevo.", false);
    }
  }

  public void eliminarIntereses() {

    FiParametrizacionImx beanParam;
    try {

      log.info("Eliminar Ajuste id " + getId());

      Predicate<FiParametrizacionImx> parametrizacionImxPredicate = fiParametrizacionImx -> fiParametrizacionImx.getId().equals(getId());
      Optional<FiParametrizacionImx> optionalFiFechasEjecucionImx = getListaCapitalizacionInt().stream().filter(parametrizacionImxPredicate).findFirst();
      if (optionalFiFechasEjecucionImx.isPresent()) {
        beanParam = optionalFiFechasEjecucionImx.get();
        setBeanParametrizacion(beanParam);
        genericService.delete(getBeanParametrizacion());
        mostrarMensaje("Registro eliminado satisfactorimente", false);
        init();
      }
    } catch (Exception ex) {
      log.error(ex.getMessage(), ex);
    }
  }

  public void openPanel() {

    setPpIntereses(true);
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
    setPpIntereses(false);
  }

  public void clear() {

    setId(0);
    setOpCode(null);
    setTipoProceso(null);
    setSctaSust(null);

  }

  public void creaMensaje(String error, UIComponent btn) {
    FacesMessage message = new FacesMessage();
    FacesContext context = FacesContext.getCurrentInstance();
    message.setDetail(error);
    message.setSummary(error);
    message.setSeverity(FacesMessage.SEVERITY_ERROR);
    context.addMessage(btn.getClientId(context), message);
  }
}


