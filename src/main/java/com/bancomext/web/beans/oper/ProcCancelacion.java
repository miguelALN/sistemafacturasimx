package com.bancomext.web.beans.oper;

import com.bancomext.service.ConsultasService;
import com.bancomext.service.GenericService;
import com.bancomext.service.ProcedimientosService;
import com.bancomext.service.ServiceLocator;
import com.bancomext.service.dto.UsuarioDTO;
import com.bancomext.service.mapping.FiBitacoraAccionesImx;
import com.bancomext.service.mapping.FiCfdVerificacion;
import com.bancomext.service.mapping.FiFacturasEncabezado;
import com.bancomext.web.utils.Constantes;
import com.bancomext.web.validator.ValidadorSesion;
import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.PrimeFaces;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@ManagedBean(name = "procCancelacion")
@ViewScoped
public class ProcCancelacion implements Serializable {

  private static final Logger log = LogManager.getLogger(ProcCancelacion.class);
  private final GenericService genericService = ServiceLocator.getGenericService();
  private final int defaultRows = Constantes.REGISTROS;
  private Date fechaEmisionIni;
  private Date fechaEmisionFin;
  private Date fechaGeneraIni;
  private Date fechaGeneraFin;
  private List<FiCfdVerificacion> listaCfds = new ArrayList<>();
  private List<FiCfdVerificacion> cfdsSeleccionados = new ArrayList<>();
  private FiCfdVerificacion cfdAEditar;
  private FiFacturasEncabezado encabezado = new FiFacturasEncabezado();
  private String labelBotonVerificar;
  private String labelBotonCancelar;
  private String usuario;
  private String password;
  private String motivo;
  private boolean esCancelacion;
  private boolean paginadorVisible;
  private UsuarioDTO usuarioLogueado;

  public ProcCancelacion() {
    usuarioLogueado = ValidadorSesion.getUsuarioLogueado();
    if (usuarioLogueado != null) {
      ValidadorSesion.validarPermiso("ProcesoCancelacion", usuarioLogueado.getRol());
    }
    init();
    listaCfds = ConsultasService.getCancelacion(fechaEmisionIni, fechaEmisionFin, fechaGeneraIni, fechaGeneraFin);
    paginadorVisible = (listaCfds.size() > 16);
  }

  private static void mostrarMensaje(final String msg, final boolean esError) {
    final FacesMessage mensaje = new FacesMessage();
    mensaje.setSummary(msg);
    mensaje.setDetail("");
    mensaje.setSeverity(esError ? FacesMessage.SEVERITY_ERROR : FacesMessage.SEVERITY_INFO);
    FacesContext.getCurrentInstance().addMessage(null, mensaje);
    PrimeFaces.current().ajax().update(":form1:messages");
  }

  private void init() {
    fechaEmisionIni = new Date();
    fechaEmisionFin = new Date();
    fechaGeneraIni = new Date();
    fechaGeneraFin = new Date();
    labelBotonVerificar = "Verificar";
    labelBotonCancelar = "Cancelar";
    cfdsSeleccionados.clear();
  }

  public void limpiarTabla() {
    init();
    listaCfds.clear();
    paginadorVisible = false;
  }

  public void filtrarTableCFDVerificacion() {
    listaCfds = ConsultasService.getCancelacion(fechaEmisionIni, fechaEmisionFin, fechaGeneraIni, fechaGeneraFin);
    paginadorVisible = (listaCfds.size() > 16);
    cfdsSeleccionados.clear();
    if (listaCfds.isEmpty()) {
      mostrarMensaje("No se encontró ningun registro para las fechas especificadas", false);
    }
    PrimeFaces.current().ajax().update(":form1:dt-cfdis");
  }

  public void procesarSeleccion() {
    log.info("cfdsSeleccionados.size() =" + cfdsSeleccionados.size());
    final String num;
    if (hayCFDSseleccionados()) {
      num = (cfdsSeleccionados.size() > 1 ? " " + cfdsSeleccionados.size() + " CFDs seleccionados" :
          " 1 CFD seleccionado");
    } else {
      num = "";
    }
    labelBotonVerificar = "Verificar" + num;
    labelBotonCancelar = "Cancelar" + num;
  }

  public boolean hayCFDSseleccionados() {
    return cfdsSeleccionados != null && !cfdsSeleccionados.isEmpty();
  }

  public void validarVerificacion() {
    esCancelacion = false;
    log.debug("Entramos a validaVerificacion...");
    final long porVerificar = cfdsSeleccionados.stream().filter(c -> c.getStatus().equals("CANCELADO")).count();
    log.info("porVerificar=" + porVerificar);
    if (porVerificar == cfdsSeleccionados.size()) {
      usuario = usuarioLogueado.getUsuario();
      log.info("usuario=" + usuario);
      PrimeFaces.current().ajax().update(":dialogs:confirma-usuario-content");
      PrimeFaces.current().executeScript("PF('usuarioDialog').show()");
    } else {
      mostrarMensaje("Uno o más registros seleccionados no han sido cancelados, revise el estatus y " +
          "vuelva a intentar", true);
    }
  }

  public void validarCancelacion() {
    esCancelacion = true;
    final long porCancelar = cfdsSeleccionados.stream().filter(c -> c.getStatus().equals("VERIFICADO")).count();
    if (porCancelar == cfdsSeleccionados.size()) {
      usuario = usuarioLogueado.getUsuario();
      PrimeFaces.current().ajax().update(":dialogs:confirma-usuario-content");
      PrimeFaces.current().executeScript("PF('usuarioDialog').show()");
    } else {
      mostrarMensaje("Uno o más registros seleccionados no han sido verificados, revise el estatus y " +
          "vuelva a intentar", true);
    }
  }

  public void cancelarCFD() {
    int contadorErrores = 0;
    for (final FiCfdVerificacion registro : cfdsSeleccionados) {
      try {
        final String folio = registro.getFiCfdHistoricoFolios().getId().getNumeroFolio().toPlainString();
        final String error = ProcedimientosService.cancelaFolio(folio);
        if (error != null) {
          contadorErrores++;
          continue;
        }
        // ACTUALIZAMOS BITACORA DE ACCIONES
        actualizarBitacoraAcciones("CANCELAR", folio, registro.getStatus());
      } catch (Exception e) {
        mostrarMensaje("Error al realizar la cancelación.\n" + e.getMessage(), true);
        log.error(e.getMessage());
      }
    }
    if (contadorErrores == 0) {
      mostrarMensaje("Cancelación correcta de todos los registros", false);
    } else {
      mostrarMensaje("No fue posible Cancelar " + contadorErrores + " registros", true);
    }
  }

  public void verificarCFD() {
    int contadorErrores = 0;
    for (final FiCfdVerificacion registro : cfdsSeleccionados) {
      try {
        final String folio = registro.getFiCfdHistoricoFolios().getId().getNumeroFolio().toPlainString();
        log.info("FOLIO a Verificar: " + registro.getFiCfdHistoricoFolios().getSecuencia());
        final String error = ProcedimientosService.pReprocesaInformacion(registro.getId().getFechaEmision(),
            registro.getFiCfdHistoricoFolios().getSecuencia().toPlainString());
        if (error != null) {
          contadorErrores++;
          continue;
        }
        // ACTUALIZAMOS BITACORA DE ACCIONES
        actualizarBitacoraAcciones("REPROCESO", folio, registro.getStatus());
      } catch (Exception e) {
        mostrarMensaje("Error al Verificar.\n" + e.getMessage(), true);
        log.error(e.getMessage());
      }
    }
    if (contadorErrores == 0) {
      mostrarMensaje("Verificación correcta de todos los registros", false);
    } else {
      mostrarMensaje("No fue posible Verificar " + contadorErrores + " registros", true);
    }
  }

  private void actualizarBitacoraAcciones(final String accion, final String folio, final String status) {
    final BigDecimal secuencia = ConsultasService.getMax("FI_BITACORA_ACCIONES_IMX", "ID");
    final BigDecimal id = (secuencia == null ? new BigDecimal(1) : secuencia.add(new BigDecimal(1)));
    final FiBitacoraAccionesImx beanBitacora = new FiBitacoraAccionesImx();
    beanBitacora.setId(id);
    beanBitacora.setAccion(accion);
    beanBitacora.setFolio(folio);
    beanBitacora.setEstatus(status);
    genericService.save(beanBitacora);
  }

  @SuppressWarnings("unchecked")
  public void mostrarEditarEncabezado() {
    if (cfdAEditar.getStatus() != null && cfdAEditar.getStatus().equals("CANCELADO")) {
      final SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      final String sentenciaWhere = " NO_ACREDITADO = " + cfdAEditar.getId().getNumAcreditado() + " AND " +
          "CONTRATO = '" + cfdAEditar.getId().getContrato() + "' AND FECHAVALOR = TO_DATE('" +
          formato.format(cfdAEditar.getId().getFechaEmision()) + "', 'yyyy-MM-dd HH24:MI:ss')";
      final List<FiFacturasEncabezado> listaEncabezado =
          genericService.get("FiFacturasEncabezado", sentenciaWhere, " order by 1");
      if (listaEncabezado != null && !listaEncabezado.isEmpty()) {
        encabezado = listaEncabezado.get(0);
      }
      PrimeFaces.current().executeScript("PF('modificacionDialog').show()");
    } else {
      mostrarMensaje("Solo se pueden modificar encabezados con status CANCELADO.", false);
    }
  }

  public void comprobarUsuario() {
    if (!password.equals(usuarioLogueado.getClave())) {
      mostrarMensaje("Password incorrecto", true);
    } else {
      PrimeFaces.current().executeScript("PF('usuarioDialog').hide()");
      if (esCancelacion) {
        PrimeFaces.current().executeScript("PF('alertaCancela').show()");
      } else {
        PrimeFaces.current().executeScript("PF('alertaVerifica').show()");
      }
    }
  }

  public void actualizarEncabezado() {
    int result = ConsultasService.updateEncabezado(encabezado);
    PrimeFaces.current().executeScript("PF('modificacionDialog').hide()");
    if (result == 1) {
      mostrarMensaje("El registro se actualizó satisfactoriamente", false);
    } else {
      mostrarMensaje("El registro no se actualizó, verifique los datos", true);
    }
  }
}
