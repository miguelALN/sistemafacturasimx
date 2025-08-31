package com.bancomext.web.beans.reportes;

import com.bancomext.service.ConsultasService;
import com.bancomext.service.dto.UsuarioDTO;
import com.bancomext.service.mapping.IcAdClientStatementHistory;
import com.bancomext.web.utils.Constantes;
import com.bancomext.web.utils.Utilerias;
import com.bancomext.web.utils.UtileriasReportesExcel;
import com.bancomext.web.validator.ValidadorSesion;
import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.PrimeFaces;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.model.StreamedContent;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

@Data
@ManagedBean(name = "reporteSaldosFIU")
@ViewScoped
public class ReporteSaldosFIU implements Serializable {

  private static final Logger log = LogManager.getLogger(ReporteSaldosFIU.class);
  private Date fechaConsulta;
  private DataTable htmlDataTable;
  private List<IcAdClientStatementHistory> listaSaldos;
  private List<Object> listaSaldosO;
  private UsuarioDTO usuarioLogueado;
  private String cliente;
  private String contrato;
  private String mensaje;
  private String nombreArchivoFacturas;
  private UsuarioDTO usuario;
  private boolean paginatorVisible;
  private int defaultRows = Constantes.REGISTROS;
  private int placeholder;
  private StreamedContent excelResource;
  private Utilerias utilerias;

  public ReporteSaldosFIU() {
    excelResource = null;
    usuarioLogueado = ValidadorSesion.getUsuarioLogueado();
    if (usuarioLogueado != null) {
      ValidadorSesion.validarPermiso("ReporteSaldosFIU", usuarioLogueado.getRol());
    }
  }

  private static void mostrarMensaje(final String msg, final boolean esError) {
    final FacesMessage mensaje = new FacesMessage();
    mensaje.setSummary(msg);
    mensaje.setDetail("");
    mensaje.setSeverity(esError ? FacesMessage.SEVERITY_ERROR : FacesMessage.SEVERITY_INFO);
    FacesContext.getCurrentInstance().addMessage(null, mensaje);
    PrimeFaces.current().ajax().update(":form1:messages");
  }

  public void consultar() throws IOException {
    final SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
    listaSaldos = new ArrayList<>();
    listaSaldosO = new ArrayList<>();
    if (!validaCampos()) {
      final Calendar cal = Calendar.getInstance();
      cal.setTime(fechaConsulta);
      cal.add(Calendar.DAY_OF_MONTH, 1);
      StringBuilder condicion = new StringBuilder();
      condicion.append(fechaConsulta != null ?
          " TRUNC(DT04_DT) =to_date('" + formato.format(fechaConsulta) + "','dd/mm/yyyy') @" : "");
      condicion.append(cliente != null && !cliente.isEmpty() ? " CL_REFINDIVIDU ='" + cliente + "'@" : "");
      condicion.append(contrato != null && !contrato.isEmpty() ? " CONTRACT_NUMBER ='" + contrato + "'@" : "");
      String[] con = condicion.toString().split("@");
      condicion = new StringBuilder();
      for (int i = 0; i < con.length; i++) {
        condicion.append(i == 0 ? con[i] : " and " + con[i]);
      }
      listaSaldos = ConsultasService.getReporteSaldosFIU(condicion.toString());
      listaSaldosO.addAll(listaSaldos);
      exportarExcel();
      paginatorVisible = (listaSaldos.size() > 6);
      cal.add(Calendar.DAY_OF_MONTH, -1);
    }
  }

  public boolean validaCampos() {
    boolean isError = false;
    String error;
    String ui;
    if (cliente == null && contrato == null && fechaConsulta == null) {
      error = "Campo necesario";
      ui = "form1:cliente";
      isError = true;
      mostrarMensaje(error, true);
      mensaje = "Al menos un campo es necesario.";
    }
    return isError;
  }

  public void exportarExcel() throws IOException {
    final List<String> headers = Arrays.asList("Numero Contrato", "Fecha", "Importe Cubierto", "FIU Efectivo",
        "Importe Financiable Max", "Portafolio", "Disponible");
    final List<String> listaNombresColumnas = Arrays.asList("contractNumber", "dt04Dt", "coveredAmount", "fiucash",
        "fundableAmount", "portfolio", "availability");
    final UtileriasReportesExcel generaExcel = new UtileriasReportesExcel();
    final File archivo =
        generaExcel.generaTablaExcelArchivo("IMX/exportar/", "ReporteSaldosFIU", "Saldos", 0, headers, listaNombresColumnas, listaSaldosO);
    nombreArchivoFacturas = archivo.getName();
    utilerias = new Utilerias();
    excelResource = utilerias.downlodaFile("IMX/exportar/ReporteSaldosFIU.xls", "ReporteSaldosFIU.xls");
  }

  public void updateCalendar() {
    log.info("INICIAL " + fechaConsulta + " cliente " + cliente);
  }

}
