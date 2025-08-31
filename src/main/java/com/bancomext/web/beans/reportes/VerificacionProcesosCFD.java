package com.bancomext.web.beans.reportes;

import com.bancomext.service.ConsultasService;
import com.bancomext.service.GenericService;
import com.bancomext.service.ServiceLocator;
import com.bancomext.service.dto.UsuarioDTO;
import com.bancomext.service.mapping.*;
import com.bancomext.web.utils.Constantes;
import com.bancomext.web.utils.Utilerias;
import com.bancomext.web.utils.UtileriasReportesExcel;
import com.bancomext.web.validator.ValidadorSesion;
import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.primefaces.PrimeFaces;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.StreamedContent;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlSelectOneMenu;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.naming.AuthenticationException;
import javax.naming.NamingException;
import javax.naming.directory.InitialDirContext;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Data
@ManagedBean(name = "verificacionProcesosCFD")
@ViewScoped
public class VerificacionProcesosCFD implements Serializable {

  private static final Logger log = LogManager.getLogger(VerificacionProcesosCFD.class);
  private static final String filePath = "IMX/exportar/";
  private final GenericService genericService = ServiceLocator.getGenericService();
  private final SimpleDateFormat formatoDelTexto = new SimpleDateFormat("yyyy-MM-dd");
  private final SimpleDateFormat formatoddMMyyyy = new SimpleDateFormat("dd/MM/yyyy");
  //falg tabs
  int flagtab = 1;
  /*Pestana 1 Cifras Control*/
  FiCfdCifrasControl regCifrasControl;
  Date filtroFechaProcesoIni;
  Date filtroFechaProcesoFin;
  String filtroFechaProcesoIniStr;
  String filtroFechaProcesoFinStr;
  /*Pestana 2 CfdFacturasEncabezado*/
  UIComponent btnFiltrarDetalleFacturas;
  List<String> listaIndices = new ArrayList<>();
  FiCfdVerificacion regVerificaAGuardar;
  FiCfdVerificacion regVerificacionImporte;
  String statusRegistro = "";
  boolean flagVerificar;
  boolean flagCancelar;
  boolean ppDetalleVerificacion;
  boolean importeEditable;
  boolean showDialogImporte;
  /*Pestana Cfras Verificacion*/
  UIComponent btnFiltrarCifrasVerificacion;
  private String nombreArchivo = "CifrasControl";
  private UIComponent btnFiltrarCifrasControl;
  private UIComponent btnVerificar;
  private UIComponent btnCancelar;
  private UIComponent btncomprobarUsuario;
  private UIComponent btnFiltrar;
  private UIComponent btnOk;
  private UIComponent btnGuardarImporte;
  private String usuarioVerifica;
  private UsuarioDTO usuarioLogueado;
  private String productoFacturasIMX = "FACTORAJE INT";
  private int defaultRows = Constantes.REGISTROS;
  /*Sort FiCfdCifrasControl*/
  private boolean paginatorVisibleCFDCifrasControl;
  private DataTable htmlDataTableCFDCifrasControl;
  private List<FiCfdCifrasControl> listaTablaCFDCifrasControl;
  /*Sort FiFacturasEncabezado*/
  private boolean paginatorVisibleDetalleFacturas;
  private DataTable htmlDataTableDetalleFacturas;
  private List<FiFacturasEncabezado> listaTablaDetalleFacturas;
  /*Sort FiCfdVerificacion*/
  //Verificacion de Facturas
  private boolean paginatorVisibleCFDVerificacion;
  private DataTable htmlDataTableCFDVerificacion;
  private List<FiCfdVerificacion> listaTablaCFDVerificacion;
  private List<FiCfdHistoricoFolios> listaTablaCFDHistoricoFolios;
  /*Sort CFDConciliaVerificacion*/
  private boolean paginatorVisibleCFDConciliaVerificacion;
  private DataTable htmlDataTableCFDConciliaVerificacion;
  private List<FiCfdCifrasVerificacion> listaTablaCFDConciliaVerificacion;
  private int selectIndex = 0;/////Tab Inicial
  private Integer foliosDisponibles = 0;
  private String nombreArchivoCC;
  private String campoA;
  private HtmlSelectOneMenu selectCampoA;
  private List<SelectItem> listaCampoA = new ArrayList<>();
  private String tipoCredito;
  private HtmlSelectOneMenu selectTipoCredito;
  private List<SelectItem> listaTipoCredito = new ArrayList<>();
  private String status;
  private HtmlSelectOneMenu selectStatus;
  private List<SelectItem> listaStatus = new ArrayList<>();
  private String nombreArchivoDF;
  /*Pestana CFD_Verificacion */
  private boolean ppUsuario;
  private String usuario;
  private String password;
  private FiCfdVerificacion registroVerificar;
  private boolean ppAlerta;
  private String mensajeAlerta;
  private String motivo;
  private boolean headerSelectFlag;
  private String filtroEstatusVerificacion;
  private HtmlSelectOneMenu selectFiltroEstatusVerificacion;
  private List<SelectItem> listaFiltroEstatusVerificacion = new ArrayList<>();
  private Date filtroFechaEmision;
  private Date filtroFechaGeneracion;
  private Date filtroFechaEmisionFin;
  private Date filtroFechaGeneracionFin;
  private String campoAF;
  private String filtroProducto;
  private HtmlSelectOneMenu selectFiltroProducto;
  private List<SelectItem> listaFiltroProducto = new ArrayList<>();
  private String nombreArchivoV;
  private BigDecimal importeCambio;
  /*Campos Detalle Verificacion - Encabezado */
  private Integer noAcreditado;
  private String rfc;
  private String nombre;
  private String contrato;
  private String linea;
  private String tipoDeCredito;
  private String moneda;
  private BigDecimal tipoDeCambio;
  private BigDecimal importe;
  private String fechaValor;
  private String secuenciaIMX;
  private String campoB;
  private BigDecimal importeCont;
  private Date filtroFechaVerificacion;
  private StreamedContent excelResourceCC;
  private Utilerias utilerias;

  public VerificacionProcesosCFD() {
    excelResourceCC = null;
    usuarioLogueado = ValidadorSesion.getUsuarioLogueado();
    if (usuarioLogueado != null) {
      ValidadorSesion.validarPermiso("VerificaProcesosCFDI", usuarioLogueado.getRol());
    }
    init();
  }

  private void init() {

    filtroFechaEmision = new Date();
    filtroFechaGeneracion = new Date();

    filtrarTableCifrasControl();

    if (!listaFiltroEstatusVerificacion.isEmpty()) {
      listaFiltroEstatusVerificacion.clear();
    }

    listaFiltroEstatusVerificacion.add(new SelectItem("TODOS", "TODOS"));
    listaFiltroEstatusVerificacion.add(new SelectItem("VERIFICADO", "VERIFICADO"));
    listaFiltroEstatusVerificacion.add(new SelectItem("CANCELADO", "CANCELADO"));
    listaFiltroEstatusVerificacion.add(new SelectItem("ERROR", "ERROR"));
    filtroEstatusVerificacion = "GENERADO";
    filtroFechaVerificacion = new Date();

    status = "TODOS";

    if (ConsultasService.getStringParametro("importeeditable").equals("SI")) {
      importeEditable = true;
    }

    if (!listaFiltroProducto.isEmpty()) {
      listaFiltroProducto.clear();
    }
    listaFiltroProducto.add(new SelectItem("-1", "SELECCIONAR"));
    listaFiltroProducto.add(new SelectItem("EXPORT FCI", "EXPORT FCI"));
    listaFiltroProducto.add(new SelectItem("EXPORT FCI SOLO COBRANZA", "EXPORT FCI SOLO COBRANZA"));
    listaFiltroProducto.add(new SelectItem("IMPORT FCI SOLO COBRANZA", "IMPORT FCI SOLO COBRANZA"));
    listaFiltroProducto.add(new SelectItem("IMPORT FCI", "IMPORT FCI"));
    filtroProducto = "-1";
  }


  /* pestana 1 Cifras Control */
  @SuppressWarnings("unchecked")
  public void filtrarTableCifrasControl() {
    log.info("INTO filtrarTableCifrasControl");
    try {
      if (filtroFechaProcesoIni != null && filtroFechaProcesoFin != null) {
        log.info("INTO filtrarTableCifrasControl fechaas not null " + filtroFechaProcesoIni + "--" + filtroFechaProcesoFin  );
        listaTablaCFDCifrasControl = genericService.get("FiCfdCifrasControl",
            " trunc(fechaProceso) between to_date('" + formatoddMMyyyy.format(filtroFechaProcesoIni) +
                "','dd/mm/yyyy') " + " and to_date('" + formatoddMMyyyy.format(filtroFechaProcesoFin) +
                "','dd/mm/yyyy') ", " order by id.fechaEmision asc ");
        paginatorVisibleCFDCifrasControl = (listaTablaCFDCifrasControl.size() > 8);
        if (!listaTablaCFDCifrasControl.isEmpty()) {
          exportarExcelCifrasControl();
        }

      } else {
        log.info("SHOULD CREARMENSAJE");
        mostrarMensaje("Debe seleccionar una fecha de Generacion", true);
      }

      filtrarTableCFDConciliaVerificacion();

    } catch (Exception e) {
      mostrarMensaje(e.getMessage(), true);
      log.error(e.getMessage(), e);
    }
  }


  public void poUpVerificar() {

    FiCfdVerificacion registro;
    int contPorVerificar = 0;
    if (!listaIndices.isEmpty()) {
      try {
        for (String rowIndex : listaIndices) {
          log.info("Proceso Manual de Verificacion indice:" + rowIndex);
          registro = listaTablaCFDVerificacion.get(Integer.parseInt(rowIndex));
          log.info("Status Actual del Registro:" + registro.getStatus());
          contPorVerificar++;
        }
        if (contPorVerificar == listaIndices.size()) {
          flagVerificar = true;
          flagCancelar = false;
          ppUsuario = true;
        } else {
          creaMensaje("Uno o mas registros seleccionados no tienen el estatus correcto para Verificar ", btnVerificar);
        }
        usuario = null;
        motivo = null;
        password = null;
      } catch (Exception ex) {
        log.error(ex.getMessage(), ex);
      }
    } else {
      creaMensaje("No se ha seleccionado ningun registro", btnVerificar);
    }
  }

  public void closePopUpDialogImporte() {
    showDialogImporte = false;
  }

  public void popupImporte() {
    showDialogImporte = true;
    try {
      int rowIndex;
      rowIndex = Integer.parseInt(FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("rowIndex"));
      regVerificacionImporte = listaTablaCFDVerificacion.get(rowIndex);
      importeCambio = regVerificacionImporte.getFiFacturasEncabezado().getImporte();
    } catch (Exception ex) {
      log.error(ex.getMessage(), ex);
    }

  }

  public void comprobarUsuario() {
    if (usuario == null || password == null) {
      return;
    }

    final Hashtable<String, String> env = new Hashtable<>();
    env.put("java.naming.factory.initial", "com.sun.jndi.ldap.LdapCtxFactory");
    env.put("java.naming.provider.url", "ldap://" + Constantes.URL_LDAP + ":" + Constantes.PORT_LDAP + "/o=bancomext");
    env.put("java.naming.security.authentication", "simple");
    env.put("java.naming.security.credentials", password);

    boolean usuPassValido;
    try {
      try {
        env.put("java.naming.security.principal",
            "cn=" + usuario.trim().toLowerCase() + ",ou=Empleados,ou=Personas,o=BANCOMEXT");
        new InitialDirContext(env);
        usuPassValido = true;
      } catch (AuthenticationException er) {
        try {
          env.put("java.naming.security.principal",
              "cn=" + usuario.trim().toUpperCase() + ",ou=Clientes,ou=Personas,o=BANCOMEXT");
          new InitialDirContext(env);
          usuPassValido = true;
        } catch (AuthenticationException err) {
          usuPassValido = false;
        }
      }
    } catch (NamingException ne) {
      usuPassValido = false;
    }
    if (usuPassValido) {
      ppUsuario = false;
      if (flagCancelar) {
        mensajeAlerta = "Los folios que estan verificados para esta fecha de emision seran cancelados y no " +
            "se podran utilizar de nuevo. Desea Continuar?";
      } else {
        mensajeAlerta = "Va Comenzar el Proceso de Generacion de Informacion del Txt para CFD. Desea Continuar?";
      }
      ppAlerta = true;
    } else {
      creaMensaje("Usuario o Password invalidos", btncomprobarUsuario);
    }
  }

  public void cancelarppDetalleVerificacion() {
    ppDetalleVerificacion = false;
  }

  public void cancelarppUsuario() {
    ppUsuario = false;
  }

  public void cancelarppAlerta() {
    ppAlerta = false;
  }

  public void filtrarTableCFDConciliaVerificacion() {

    log.info("INTO filtrarTableCFDConciliaVerificacion");

    try {

      if (filtroFechaProcesoIni != null && filtroFechaProcesoFin != null) {
        log.info("INTO filtrarTableCFDConciliaVerificacion fechas not null");
        listaTablaCFDConciliaVerificacion =
                ConsultasService.getCifrasControlVerificacion(filtroFechaProcesoIni, filtroFechaProcesoFin);
        for (final FiCfdCifrasVerificacion c : listaTablaCFDConciliaVerificacion) {
          c.setFechaVerificado(filtroFechaProcesoIni);
        }
        paginatorVisibleCFDConciliaVerificacion = (listaTablaCFDConciliaVerificacion.size() > 5);
      } else {
        log.info("SHOULD MENSAJE filtrarTableCFDConciliaVerificacion");
        mostrarMensaje("Debe seleccionar una fecha de Generacion", true);
      }

    } catch (Exception ex) {
      log.error(ex.getMessage(), ex);
      mostrarMensaje(ex.getMessage(), true);
    }
  }

  public void creaMensaje(String error, UIComponent btn) {
    FacesMessage message = new FacesMessage();
    FacesContext context = FacesContext.getCurrentInstance();
    message.setDetail(error);
    message.setSummary(error);
    message.setSeverity(FacesMessage.SEVERITY_ERROR);
    context.addMessage(btn.getClientId(context), message);
  }

  public void redirectSeguimientoFacturas() throws IOException {
    FacesContext facesContext = FacesContext.getCurrentInstance();
    log.info(filtroFechaProcesoIni.toString());
    facesContext.getExternalContext().redirect("seguimientoFacturas.xhtml?fechaInicial="+ filtroFechaProcesoIni.toString() + "&fechaFinal=" + filtroFechaProcesoFin.toString());
  }

  public void exportarExcelCifrasControl() {
    log.info("Inicia Exportacion Archivo Excel");

    nombreArchivo = UtileriasReportesExcel.borrarExportsExistentes(filePath, nombreArchivo);
    final File archivo = new File(filePath + nombreArchivo + ".xls");

    UtileriasReportesExcel generaExcel = new UtileriasReportesExcel();

    try (final HSSFWorkbook wb = new HSSFWorkbook()) {
      final List<String> headers = Arrays.asList("Fecha Valor", "Producto", "Total Generacion", "Total Verificacion",
          "Total Esperado", "Pendientes", "Total Error", "Motivo");
      final List<String> listaNombresColumnas = Arrays.asList("id.fechaEmision", "id.tpocredito", "total",
          "totalVerificacion", "totalEsperado", "pendientes", "totalError", "motivoError");
      final List<Object> listacontenido = new ArrayList<>(listaTablaCFDCifrasControl);
      final HSSFSheet sheet = wb.createSheet();
      generaExcel.generaTablaExcel(wb, sheet, 0, headers, listaNombresColumnas, listacontenido);
      try (final FileOutputStream out = new FileOutputStream(archivo)) {
        wb.write(out);
      }

      utilerias = new Utilerias();
      excelResourceCC = utilerias.downlodaFile("IMX/exportar/" + nombreArchivo + ".xls", nombreArchivo + ".xls");

    } catch (Exception ex) {
      creaMensaje("Error cerrando Archivo Excel", btnFiltrarCifrasControl);
      log.error("Error Creando Archivo Excel" + ex.getMessage(), ex);
      Utilerias.guardarMensajeLog("VerificaProcesosCFDI", "exportarExcelCifrasControl", ex,
          "Error Creando Archivo Excel", usuarioLogueado);
    }
    log.info("Termina Exportacion Archivo Excel");
  }

  public void updateCalendar() {
    log.info("INICIAL " + filtroFechaProcesoIni +  " FINAL " + filtroFechaProcesoFin);
  }

  private static void mostrarMensaje(final String msg, final boolean esError) {
    final FacesMessage mensaje = new FacesMessage();
    mensaje.setSummary(msg);
    mensaje.setDetail("");
    mensaje.setSeverity(esError ? FacesMessage.SEVERITY_ERROR : FacesMessage.SEVERITY_INFO);
    FacesContext.getCurrentInstance().addMessage(null, mensaje);
    PrimeFaces.current().ajax().update(":form1:messages");
  }

}
