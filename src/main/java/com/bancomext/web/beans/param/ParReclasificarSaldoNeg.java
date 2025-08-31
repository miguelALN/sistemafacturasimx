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
@ManagedBean(name = "parReclasificarSaldoNeg")
@ViewScoped
public class ParReclasificarSaldoNeg implements Serializable {

  private static final Logger log = LogManager.getLogger(ParReclasificarSaldoNeg.class);
  private final GenericService genericService = ServiceLocator.getGenericService();
  private FiParametrizacionImx beanParametrizacion;
  private DataTable htmlDataTable;
  private Integer ctaOrigen;
  private Integer sctaOrigen;
  private Integer ssCtaOrigen;
  private Integer sssCtaOrigen;
  private Integer ctaCargo;
  private Integer sctaCargo;
  private Integer ssCtaCargo;
  private Integer sssCtaCargo;
  private Integer ctaAbono;
  private Integer sctaAbono;
  private Integer ssCtaAbono;
  private Integer sssCtaAbono;
  private List<FiParametrizacionImx> listaSaldoNegativo;
  private UsuarioDTO usuarioLogueado;
  private String opCode;
  private String tipoProceso;
  private UIComponent btnGuardar;
  private boolean btnUpdateSaveInt;
  private boolean paginatorVisible; // PARA PAGINADOR
  private boolean ppSaldoNegativo; // PARA POP UP ALTA Y MODIFICACION DE REGISTROS
  private int defaultRows = Constantes.REGISTROS;
  private int id;
  private FiParametrizacionImx fiParametrizacionImx;
  private int rowIndex;
  private Boolean editar = false;

  public ParReclasificarSaldoNeg() {
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
    fiParametrizacionImx = new FiParametrizacionImx();
    setListaSaldoNegativo(
        genericService.get("FiParametrizacionImx", " TIPO_PROCESO = 'FIU'", " order by 1"));

    for (FiParametrizacionImx fiParametrizacionImx : listaSaldoNegativo) {
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
    setPaginatorVisible(listaSaldoNegativo.size() > 8);
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

  public void guardaSaldoNegativo() {

    if (fiParametrizacionImx.getCtaOrigen() != null && fiParametrizacionImx.getSctaOrigen() != null && fiParametrizacionImx.getSsCtaOrigen()  != null && fiParametrizacionImx.getSssCtaOrigen() != null
        && fiParametrizacionImx.getCtaCargo() != null && fiParametrizacionImx.getSctaCargo() != null && fiParametrizacionImx.getSsCtaCargo() != null &&  fiParametrizacionImx.getSssCtaCargo() != null
        && fiParametrizacionImx.getCtaAbono() != null && fiParametrizacionImx.getSctaAbono() != null && fiParametrizacionImx.getSsCtaAbono()  != null && fiParametrizacionImx.getSssCtaAbono() != null) {

      String tipoProceso = "FIU";
      BigDecimal secuencia = ConsultasService.getMax("FI_PARAMETRIZACION_IMX", "ID");

      if (secuencia == null) {
        secuencia = new BigDecimal("0");
      }

      id = secuencia.intValue();
      id = id + 1;
      fiParametrizacionImx.setId(id);

      // VAMOS AL SICAV PARA VALIDAR QUE EXISTAN LAS CUENTAS QUE SE ESTAN PARAMETRIZANDO
      final long ctaOrigenSSicav = ConsultasService.getCtaSicav(fiParametrizacionImx.getCtaOrigen(), fiParametrizacionImx.getSctaOrigen(), fiParametrizacionImx.getSsCtaOrigen(), fiParametrizacionImx.getSssCtaOrigen());
      final long ctaCargoSSicav = ConsultasService.getCtaSicav(fiParametrizacionImx.getCtaCargo(), fiParametrizacionImx.getSctaCargo(), fiParametrizacionImx.getSsCtaCargo(), fiParametrizacionImx.getSssCtaCargo());

      // SI EXISTE SE GUARDA
      if (ctaOrigenSSicav > 0 & ctaCargoSSicav > 0) {

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
          setPpSaldoNegativo(false);//
          init();
          clear();
        } catch (Exception e) {
          log.error(e.getMessage(), e);
        }

      } else {
        mostrarMensaje("La cuenta que desea modificar no existe. Por favor verifique e intente de nuevo.", true);
      }

    } else {
      mostrarMensaje("Todos los campos son requeridos por favor verifique a intente de nuevo.", true);
    }
  }

  public void muestraEdicion() {

    setPpSaldoNegativo(true);
    setBtnUpdateSaveInt(false);

    try {

      fiParametrizacionImx = new FiParametrizacionImx();
      fiParametrizacionImx = getListaSaldoNegativo().get(rowIndex);

    } catch (Exception ex) {
      setPpSaldoNegativo(false);
      log.error(ex.getMessage(), ex);
    }
  }

  public void modificaSaldoNegativo() {

    final long ctaOrigenSSicav = ConsultasService.getCtaSicav(fiParametrizacionImx.getCtaOrigen(), fiParametrizacionImx.getSctaOrigen(), fiParametrizacionImx.getSsCtaOrigen(), fiParametrizacionImx.getSssCtaOrigen());
    final long ctaCargoSSicav = ConsultasService.getCtaSicav(fiParametrizacionImx.getCtaCargo(), fiParametrizacionImx.getSctaCargo(), fiParametrizacionImx.getSsCtaCargo(), fiParametrizacionImx.getSssCtaCargo());

    // SI EXISTE SE GUARDA
    if (ctaOrigenSSicav > 0 && ctaCargoSSicav > 0) {

      try {
        genericService.update(fiParametrizacionImx);
        clear();
        setPpSaldoNegativo(false);
        init();
      } catch (Exception e) {
        log.error(e.getMessage(), e);
        mostrarMensaje("Surgio un error al actualizar el registro", true);
      }
    } else {
      mostrarMensaje("La cuenta que desea modificar no existe. Por favor verifique e intente de nuevo.", true);
    }
  }

  public void eliminarSaldoNegativo() {

    FiParametrizacionImx beanParam;
    try {
      log.info("Eliminar registro Intereses " + getId());

      Predicate<FiParametrizacionImx> parametrizacionImxPredicate = parametro -> parametro.getId().equals(getId());
      Optional<FiParametrizacionImx> optionalFiParametrizacionImx = getListaSaldoNegativo().stream().filter(parametrizacionImxPredicate).findFirst();

      if (optionalFiParametrizacionImx.isPresent()) {
        beanParam = optionalFiParametrizacionImx.get();
        setBeanParametrizacion(beanParam);
        genericService.delete(getBeanParametrizacion());
        init();
      }

    } catch (Exception ex) {
      log.error(ex.getMessage(), ex);
    }
  }

  public void openPanel() {
    log.info("INTO openPanel");
    setPpSaldoNegativo(true);
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
    log.info("INTO openPanel opCode " + fiParametrizacionImx.getOpCode());
  }

  public void clear() {
    setId(0);
    setOpCode(null);
    setCtaOrigen(null);
    setSctaOrigen(null);
    setSsCtaOrigen(null);
    setSssCtaOrigen(null);
    setCtaCargo(null);
    setSctaCargo(null);
    setSsCtaCargo(null);
    setSssCtaCargo(null);
    setCtaAbono(null);
    setSctaAbono(null);
    setSsCtaAbono(null);
    setSssCtaAbono(null);
    setTipoProceso(null);
  }

  public void closePanel() {
    clear();
    setPpSaldoNegativo(false);
  }

}
