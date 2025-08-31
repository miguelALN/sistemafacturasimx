package com.bancomext.web.beans.proc;

import com.bancomext.service.ConsultasService;
import com.bancomext.service.dto.UsuarioDTO;
import com.bancomext.service.mapping.VoLlaveValor;
import com.bancomext.service.mapping.VoReporteFacturasMensual;
import com.bancomext.web.utils.Constantes;
import com.bancomext.web.utils.Utilerias;
import com.bancomext.web.validator.ValidadorSesion;
import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@ManagedBean(name = "resumenFacturacionPorCliente")
@ViewScoped
public class ResumenFacturacionPorCliente implements Serializable {

  private static final Logger log = LogManager.getLogger(ResumenFacturacionPorCliente.class);
  private Date fechaFinal;
  private Date fechaInicial;
  private List<SelectItem> listaClientes = new ArrayList<>();
  private List<VoReporteFacturasMensual> listaMensual;
  private String codigoCliente;
  private String total;
  private UIComponent btnError;
  private UIComponent btnMensaje;
  private boolean paginatorVisible;
  private boolean totalVisible;
  private int defaultRows = Constantes.REGISTROS;
  private UsuarioDTO usuarioLogueado;

  public ResumenFacturacionPorCliente() {
    usuarioLogueado = ValidadorSesion.getUsuarioLogueado();
    if (usuarioLogueado != null) {
      ValidadorSesion.validarPermiso("ResumenFacturacionPorCliente", usuarioLogueado.getRol());
    }
    init();
  }

  private void init() {
    final List<VoLlaveValor> valores = ConsultasService.getComboDistinct("FI_CORREOS_POR_CLIENTE",
        "CODIGO_CLIENTE", "DESCRIPCION_CLIENTE", "",
        " ORDER BY DESCRIPCION_CLIENTE");
    listaClientes = Utilerias.creaSelectItem(valores);
  }

  public void generaResumen() {
    log.info("generaResumen " + fechaInicial + " " + fechaFinal + " " +  codigoCliente );
    if (fechaInicial != null && fechaFinal != null) {
      if (codigoCliente.equals("-1") || codigoCliente.isEmpty()) {
        codigoCliente = null;
      }
      listaMensual = ConsultasService.getReporteFacturas(fechaInicial, fechaFinal, codigoCliente);
      if (!listaMensual.isEmpty()) {
        setPaginatorVisible(listaMensual.size() > 16);
        if (listaMensual != null) {
          for (VoReporteFacturasMensual voReporteFacturasMensual : listaMensual) {
            if (voReporteFacturasMensual.getTotal() != null) {
              voReporteFacturasMensual.setTotalFor(
                  Utilerias.formatearNumero(voReporteFacturasMensual.getTotal(), ""));
            }
          }
        }
        setTotalVisible(true);
        sumaMontos(getListaMensual());
        mostrarMensaje("La busqueda se ha realizado Satisfactoriamente", "form1:btnMensaje");
      } else {
        mostrarMensaje("No se encontraron Registros para las fechas y cliente indicado.", "form1:btnError");
        setTotalVisible(false);
      }
    } else {
      mostrarMensaje("Todos los parametros son requeridos para realizar la busqueda.", "form1:btnError");
      setTotalVisible(false);
    }
  }

  public void sumaMontos(final List<VoReporteFacturasMensual> lst) {
    BigDecimal totalMontos;
    totalMontos = new BigDecimal("0");
    if (!lst.isEmpty()) {
      for (VoReporteFacturasMensual reg : lst) {
        totalMontos = totalMontos.add(reg.getTotal());
      }
      total = Utilerias.formatearNumero(totalMontos, "");
    }
  }

  public void mostrarMensaje(final String error, final String ui) {
    FacesMessage message = new FacesMessage();
    FacesContext context = FacesContext.getCurrentInstance();
    message.setDetail(error);
    message.setSummary(error);
    message.setSeverity(FacesMessage.SEVERITY_ERROR);
    context.addMessage(ui, message);
  }

  public void updateCalendar() {
    log.info("INICIAL " + fechaInicial +  " FINAL " + fechaFinal + " cliente " + codigoCliente);
  }

}
