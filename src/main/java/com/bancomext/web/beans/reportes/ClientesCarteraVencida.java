package com.bancomext.web.beans.reportes;

import com.bancomext.service.ConsultasService;
import com.bancomext.service.GenericService;
import com.bancomext.service.ServiceLocator;
import com.bancomext.service.dto.UsuarioDTO;
import com.bancomext.service.mapping.FiClientesSinCfdi;
import com.bancomext.service.mapping.VoLlaveValor;
import com.bancomext.web.utils.Constantes;
import com.bancomext.web.utils.Utilerias;
import com.bancomext.web.utils.UtileriasReportesExcel;
import com.bancomext.web.validator.ValidadorSesion;
import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.primefaces.PrimeFaces;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.springframework.dao.DataIntegrityViolationException;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import com.bancomext.web.utils.*;

@Data
@ManagedBean(name = "clientesCarteraVencida")
@ViewScoped
public class ClientesCarteraVencida implements Serializable {

  private static final Logger log = LogManager.getLogger(ClientesCarteraVencida.class);
  private static final String filePath = "IMX/exportar/";
  private final GenericService genericService = ServiceLocator.getGenericService();
  private String nombreArchivo = "ClientesCarteraVencida";
  private Date fechaAdicion;
  private FiClientesSinCfdi fiGpoCorrreoAnt;
  private FiClientesSinCfdi registro;
  private DataTable htmlDataTable;
  private List<FiClientesSinCfdi> lst;
  private List<SelectItem> listaClavesCorreo = new ArrayList<>();
  private List<SelectItem> listaClientes = new ArrayList<>();
  private UsuarioDTO usuarioLogueado;
  private String adicionadoPor;
  private String cliente;
  private String clienteFiltro;
  private String descripcion;
  private String email;
  private String id;
  private String nombreArchivoV;
  private boolean btnFiltro;
  private boolean btnGuardar;
  private boolean confirmar;
  private boolean paginatorVisible;
  private boolean showCPData;
  private int defaultRows = Constantes.REGISTROS;
  private StreamedContent excelResourceV;
  private Utilerias utilerias;

  public ClientesCarteraVencida() {
    usuarioLogueado = ValidadorSesion.getUsuarioLogueado();
    if (usuarioLogueado != null) {
      ValidadorSesion.validarPermiso("ClientesCarteraVencida", usuarioLogueado.getRol());
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
    nombreArchivoV = "Nombre";
    excelResourceV = null;
    utilerias = new Utilerias();
    final List<VoLlaveValor> valores = ConsultasService.getComboDistinct("FI_CLIENTES_SIN_CFDI", "CODIGO_CLIENTE",
        "DESCRIPCION_CLIENTE", "", " ORDER BY DESCRIPCION_CLIENTE");
    listaClientes = Utilerias.creaSelectItem(valores);
    log.info("INTO init");
    setLst(genericService.get("FiClientesSinCfdi", "", " order by DESCRIPCION_CLIENTE"));
    log.info("INTO init AFTER FILL FiClientesSinCfdi");
    setPaginatorVisible(getLst().size() > 16);
    if (!getLst().isEmpty())
      exportarClientesCarteraVencida();
  }

  @SuppressWarnings("unchecked")
  public void filtroCliente() {

    try {

      log.info("INTO FILTROCLIENTE " + clienteFiltro);

      setLst(genericService.get("FiClientesSinCfdi", "CODIGO_CLIENTE LIKE ('%" + clienteFiltro.toUpperCase() + "%') ", " order by DESCRIPCION_CLIENTE"));

      log.info("numero de registros found " + getLst().size());

      setPaginatorVisible(getLst().size() > 16);
      if (!getLst().isEmpty())
        exportarClientesCarteraVencida();

    } catch (Exception e) {
      mostrarMensaje("Ha surgido un error a la hora de realizar el filtro por favor verifique sus parametros de busqueda", true);
      log.error(e.getMessage(), e);
    }
  }

  public String openPanel() {
    clear();
    setPaginatorVisible(true);
    setShowCPData(false);
    setBtnGuardar(true);
    return null;
  }

  public void muestraEdicion() throws Exception {
    int rowIndex;
    try {
      clear();

      rowIndex = Integer.parseInt(FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("rowIndex"));
      registro = getLst().get(rowIndex);

      //respaldo de la informacion
      setCliente(String.valueOf(registro.getId()));
      setDescripcion(registro.getDescripcionCliente());
      setEmail("correo");
      fiGpoCorrreoAnt = new FiClientesSinCfdi(new Long(getCliente()), getDescripcion());

      setBtnGuardar(false);
      setPaginatorVisible(true);
    } catch (Exception ex) {
      closePanel();
      log.error(ex.getMessage(), ex);
    }

  }

  public void guardaGpoClientesCarteraVencida() {

    try {
      log.info("Guardando  Cliente");
      if (!validaCampos()) {
        FiClientesSinCfdi bean = new FiClientesSinCfdi(new Long(getCliente()), getDescripcion());
        bean.setAdicionadoPor(usuarioLogueado.getUsuario());
        bean.setFechaAdicion(Calendar.getInstance().getTime());
        genericService.save(bean);
        init();
        closePanel();
      }
    } catch (DataIntegrityViolationException ex) {
      mostrarMensaje("El registro ya existe </br>", true);
      log.error(ex.getMessage(), ex);
    } catch (Exception ex) {
      mostrarMensaje("Error Guardando CClientesCarteraVencida", true);
      closePanel();
      log.error("Error Guardando Clientes Cartera Vencida" + ex.getMessage(), ex);
      Utilerias.guardarMensajeLog("ClientesCarteraVencida", "guarda Cliente Cartera Vencida", ex,
          "Error Guardando Clientes Cartera Vencida", usuarioLogueado);
    }
  }

  public void modificaGpoCorreo() {

    try {
      log.info("Modificando Correo Cliente");
      if (!validaCampos()) {
        registro.setId(new Long(getCliente()));
        registro.setDescripcionCliente(getDescripcion());
        registro.setModificadoPor(usuarioLogueado.getUsuario());
        registro.setFechaModificacion(Calendar.getInstance().getTime());
        genericService.delete(fiGpoCorrreoAnt);
        genericService.save(registro);
        init();
        closePanel();
      }
    } catch (Exception ex) {
      mostrarMensaje("Error Modificando ", true);
      closePanel();
      log.error("Error Modificando Correo Cliente" + ex.getMessage(), ex);
      Utilerias.guardarMensajeLog("CatCorreoClientes", "modificaGpoCorreo", ex,
          "Error Modificando Correo Cliente", usuarioLogueado);
    }

  }

  public void borraGpoCorreo(ActionEvent ae) {

    int rowIndex;
    try {
      log.info("Borrando Correo Cliente");
      rowIndex = Integer.parseInt(ae.getComponent().getAttributes().get("rowIndex").toString());
      registro = getLst().get(rowIndex);
      genericService.delete(registro);
      init();
    } catch (Exception ex) {
      mostrarMensaje("Error Borrando", true);
      closePanel();
      log.error("Error Borrando Correo Cliente" + ex.getMessage(), ex);
      Utilerias.guardarMensajeLog("CatCorreoClientes", "borraGpoCorreo", ex,
          "Error Borrando Correo Cliente", usuarioLogueado);
    }
  }

  public boolean validaCampos() {
    boolean isError = false;
    if (Utilerias.esInvalida(getCliente())) {
      mostrarMensaje("El valor es requerido", true);
      isError = true;
    }
    if (Utilerias.esInvalida(getDescripcion())) {
      mostrarMensaje("El valor es requerido", true);
      isError = true;
    }
    return isError;
  }

  public void clear() {
    setCliente(null);
    setDescripcion(null);
    setEmail(null);
  }

  public void closePanel() {
    setPaginatorVisible(false);
  }

  public void exportarClientesCarteraVencida() {

    log.info("Inicia Exportacion Archivo Excel");

    nombreArchivo = UtileriasReportesExcel.borrarExportsExistentes(filePath, nombreArchivo);
    final File archivo = new File(filePath + nombreArchivo + ".xls");

    log.info("ruta de archivo " + filePath + nombreArchivo + ".xls");

    archivo.setWritable(true);

    try (final HSSFWorkbook wb = new HSSFWorkbook()) {
      final HSSFFont font = wb.createFont();
      font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);

      final HSSFCellStyle style = wb.createCellStyle();
      style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
      style.setFont(font);

      final HSSFSheet sheet = wb.createSheet();
      final HSSFRow rowTit = sheet.createRow((short) 0);

      final HSSFCell cell = rowTit.createCell(0);
      sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 8));
      cell.setCellValue("CLIENTES CARTERA VENCIDA");
      cell.setCellStyle(style);

      final HSSFRow row = sheet.createRow((short) 1);
      row.createCell(0).setCellValue("Codigo Cliente");
      row.createCell(1).setCellValue("Descripcion Cliente");

      int k = lst.size() + 2;
      for (FiClientesSinCfdi reg : lst) {
        k--;
        final HSSFRow row2 = sheet.createRow((short) k);
        row2.createCell(0).setCellValue(reg.getId());
        row2.createCell(1).setCellValue(reg.getDescripcionCliente());
      }
      try (final FileOutputStream out = new FileOutputStream(archivo)) {
        log.info("Crea archivo correctamente BEFORE ");
        wb.write(out);
        log.info("Crea archivo correctamente AFTER ");

        excelResourceV = utilerias.downlodaFile(filePath + nombreArchivo + ".xls",nombreArchivo + ".xls");

      }

    } catch (Exception ex) {
      log.error("Error Creando Archivo Excel" + ex.getMessage(), ex);
      Utilerias.guardarMensajeLog("BitacoraCancelacionProvision", "exportarBitacoraCancelacion", ex,
          "Error Creando Archivo Excel", usuarioLogueado);
    }
    log.info("Termina Exportacion Archivo Excel");
  }

  public StreamedContent downlodaFile () throws IOException {
    excelResourceV = utilerias.downlodaFile("/Users/mac/Downloads/" + nombreArchivo + ".xls",nombreArchivo + ".xls");
    return  excelResourceV;
  }

}


