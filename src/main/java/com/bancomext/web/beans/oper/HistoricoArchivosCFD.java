package com.bancomext.web.beans.oper;

import com.bancomext.service.ConsultasService;
import com.bancomext.service.GenericService;
import com.bancomext.service.ServiceLocator;
import com.bancomext.service.dto.UsuarioDTO;
import com.bancomext.service.mapping.FiCfdEstadoCuenta;
import com.bancomext.service.mapping.VoLlaveValor;
import com.bancomext.web.utils.Utilerias;
import com.bancomext.web.utils.UtileriasReportesExcel;
import com.bancomext.web.validator.ValidadorSesion;
import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.PrimeFaces;
import org.primefaces.model.StreamedContent;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlSelectOneMenu;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@ManagedBean(name = "historicoArchivosCFD")
@ViewScoped
public class HistoricoArchivosCFD implements Serializable {

  private static final Logger log = LogManager.getLogger(HistoricoArchivosCFD.class);
  private static final String filePath = "IMX/exportar/";
  private final GenericService genericService = ServiceLocator.getGenericService();
  private String nombreArchivo = "FactorajeInternacional_";
  private HtmlSelectOneMenu selectfechaEmision;
  private List<SelectItem> listaFechaEmision = new ArrayList<>();
  private UsuarioDTO usuarioLogueado;
  private String fechaEmision;
  private UIComponent btnprocesar;
  private StreamedContent resource;
  private Utilerias utilerias;

  public HistoricoArchivosCFD() {
    resource = null;
    usuarioLogueado = ValidadorSesion.getUsuarioLogueado();
    if (usuarioLogueado != null) {
      ValidadorSesion.validarPermiso("HistoricoArchivosCFD", usuarioLogueado.getRol());
    }
    init();
  }

  private void init() {
    log.info("INTO INIT HistoricoArchivosCFD");
    final List<VoLlaveValor> valores = ConsultasService.getComboDistinct("fi_cfd_estado_cuenta",
        " to_char(FECHA_PROCESO,'dd/mm/yyyy') as FECHA ",
        " to_char(FECHA_PROCESO,'dd/mm/yyyy') as FECHA_EMISION ",
        " producto='FACTORAJE INT' ", " order by FECHA asc");
    listaFechaEmision = Utilerias.creaSelectItem(valores);
  }

  public void creaMensaje(String error, UIComponent btn) {
    FacesMessage message = new FacesMessage();
    FacesContext context = FacesContext.getCurrentInstance();
    message.setDetail(error);
    message.setSummary(error);
    message.setSeverity(FacesMessage.SEVERITY_ERROR);
    context.addMessage(btn.getClientId(context), message);
  }

  private static void mostrarMensaje(final String msg, final boolean esError) {
    final FacesMessage mensaje = new FacesMessage();
    mensaje.setSummary(msg);
    mensaje.setDetail("");
    mensaje.setSeverity(esError ? FacesMessage.SEVERITY_ERROR : FacesMessage.SEVERITY_INFO);
    FacesContext.getCurrentInstance().addMessage(null, mensaje);
    PrimeFaces.current().ajax().update(":form1:messages");
  }

  public void procesar() throws IOException {

    log.info("INTO procesar");

    if (fechaEmision == null || fechaEmision.equals("-1")) {
      creaMensaje("Debe ingresar una fecha de emision", btnprocesar);
      return;
    }

    Date date = new Date(); // Get the current date and time
    SimpleDateFormat formatter = new SimpleDateFormat("ddMMyyyy");
    String formattedDate = formatter.format(date);

    log.info("Procesando Archivo path " + filePath + nombreArchivo + formattedDate);
    nombreArchivo = UtileriasReportesExcel.borrarExportsExistentes(filePath, nombreArchivo + formattedDate);
    final File archivo = new File(filePath + nombreArchivo + formattedDate + ".xls");
    archivo.setWritable(true);

    @SuppressWarnings("unchecked") final List<FiCfdEstadoCuenta> lista = (List<FiCfdEstadoCuenta>)
        genericService.get("FiCfdEstadoCuenta",
            " trunc(fechaProceso) = to_date('" + getFechaEmision() + "','dd/mm/yyyy') ",
            " Order By id.folio,id.tramaId,orden ");

    log.info("FiCfdEstadoCuenta trunc(fechaProceso) = to_date('" + formattedDate +
        "','dd/mm/yyyy') , Order By id.folio,id.tramaId,orden ");

    try (final OutputStreamWriter writer =
             new OutputStreamWriter(Files.newOutputStream(archivo.toPath()), StandardCharsets.UTF_8);
         final BufferedWriter out = new BufferedWriter(writer)) {
      for (FiCfdEstadoCuenta fiedocta : lista) {
        if ("T4".equals(fiedocta.getId().getTramaId())) {
          log.info("**** IGUAL A TRAMA T4 *****");
        } else {
          log.info("**** DIFERENTE A TRAMA T4 *****");
          out.write(fiedocta.getId().getTrama());
          out.newLine();
        }
        log.info("ID TRAMA: " + fiedocta.getId().getTramaId());
        log.info(fiedocta.getId().getTrama());
      }

      utilerias = new Utilerias();
      resource = utilerias.downlodaFile("IMX/exportar/" + nombreArchivo + formattedDate + ".xls", nombreArchivo + formattedDate +  ".xls");
      mostrarMensaje ("Prceso terminado correctamente, ahora descargue el archivo dando click en el boton que se muestra abajo",false);

    } catch (Exception ex) {
      creaMensaje("Ocurrio un error al intentar ejecutar", btnprocesar);
      log.error("Error Procesando Archivo " + ex.getMessage(), ex);
      Utilerias.guardarMensajeLog("HistoricoArchivosCFD", "procesar", ex,
          "Error Procesando Archivo", usuarioLogueado);
    }
  }
}
