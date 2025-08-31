package com.bancomext.web.beans.oper;

import com.bancomext.service.GenericService;
import com.bancomext.service.ServiceLocator;
import com.bancomext.service.mapping.FiCfdHistoricoFolios;
import com.bancomext.service.mapping.FiCorreosPorCliente;
import com.bancomext.service.mapping.VoTablaElemento;
import com.bancomext.web.utils.Utilerias;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.PrimeFaces;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.model.file.UploadedFile;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Data
@NoArgsConstructor
@ManagedBean(name = "cargaFacturas")
@ViewScoped
public class CargaFacturas implements Serializable {

  private static final Logger log = LogManager.getLogger(CargaFacturas.class);
  private List<VoTablaElemento> listaTablaError = new ArrayList<>();
  private List<VoTablaElemento> listaTablaIn = new ArrayList<>();
  private List<VoTablaElemento> listaTablaOut = new ArrayList<>();
  private boolean paginatorVisibleError;
  private boolean paginatorVisibleIn;
  private boolean paginatorVisibleOut;
  private UploadedFile uploadedFile;

  private static void mostrarMensaje(final String msg, final boolean esError) {
    final FacesMessage mensaje = new FacesMessage();
    mensaje.setSummary(msg);
    mensaje.setDetail("");
    mensaje.setSeverity(esError ? FacesMessage.SEVERITY_ERROR : FacesMessage.SEVERITY_INFO);
    FacesContext.getCurrentInstance().addMessage(null, mensaje);
    PrimeFaces.current().ajax().update(":form1:messages");
  }

  public void validarInformacionTab(final TabChangeEvent<String> event) {
    switch (event.getTab().getId()) {
      case "panelTab2":
        filtrarIn();
        break;
      case "panelTab3":
        filtrarOut();
        break;
      case "panelTab4":
        filtrarError();
        break;
      default:
        break;
    }
  }

  public void filtrarIn() {
    final List<String> listaTablaInTmp = new ArrayList<>();
    listaTablaIn = new ArrayList<>();
    final File archivos = new File("IMX/IN/");
    for (int i = 0; i < Objects.requireNonNull(archivos.listFiles()).length; i++) {
      listaTablaInTmp.add(Objects.requireNonNull(archivos.listFiles())[i].getName());
    }
    Collections.sort(listaTablaInTmp);
    for (final String s : listaTablaInTmp) {
      final VoTablaElemento vo = new VoTablaElemento();
      vo.setElemento(s);
      listaTablaIn.add(vo);
    }
    paginatorVisibleIn = (listaTablaIn.size() > 16);
  }

  public void filtrarOut() {
    final List<String> listaTablaInTmp = new ArrayList<>();
    listaTablaOut = new ArrayList<>();
    final File archivos = new File("IMX/OUT/");
    for (int i = 0; i < Objects.requireNonNull(archivos.listFiles()).length; i++) {
      listaTablaInTmp.add(Objects.requireNonNull(archivos.listFiles())[i].getName());
    }
    Collections.sort(listaTablaInTmp);
    for (final String s : listaTablaInTmp) {
      final VoTablaElemento vo = new VoTablaElemento();
      vo.setElemento(s);
      listaTablaOut.add(vo);
    }
    paginatorVisibleOut = (listaTablaOut.size() > 16);
  }

  public void filtrarError() {
    final List<String> listaTablaInTmp = new ArrayList<>();
    listaTablaError = new ArrayList<>();
    final File archivos = new File("IMX/OUT_ERR/");
    for (int i = 0; i < Objects.requireNonNull(archivos.listFiles()).length; i++) {
      listaTablaInTmp.add(Objects.requireNonNull(archivos.listFiles())[i].getName());
    }
    Collections.sort(listaTablaInTmp);
    for (final String s : listaTablaInTmp) {
      final VoTablaElemento vo = new VoTablaElemento();
      vo.setElemento(s);
      listaTablaError.add(vo);
    }
    paginatorVisibleError = (listaTablaError.size() > 16);
  }

  public void fileUploadListener(final FileUploadEvent e) {
    log.info("fileUploadListener con:" + e.getFile());
    uploadedFile = e.getFile();
  }

  public void subirArchivo() {
    log.info("Cargando archivo: " + uploadedFile.getFileName());

    if (uploadedFile.getSize() > 0 && uploadedFile.getFileName().toLowerCase().endsWith(".zip")) {
      final String nombreSinExtension =
          uploadedFile.getFileName().replaceAll(".ZIP", "").replaceAll(".zip", "");
      String folio = "";
      boolean sinFolio;
      try {
        folio = nombreSinExtension.split("_")[2].trim();
        sinFolio = false;
      } catch (Exception ex) {
        sinFolio = true;
      }

      log.info("FOLIO " + folio + " sinFolio " + sinFolio);

      if (!sinFolio && !folio.isEmpty() && Utilerias.validaNumerico(folio)) {

        final GenericService genericService = ServiceLocator.getGenericService();
        @SuppressWarnings("unchecked") final List<FiCfdHistoricoFolios> listaCliente = (List<FiCfdHistoricoFolios>)
            genericService.get("FiCfdHistoricoFolios"
                , "id.numeroFolio =to_number('" + folio + "')", " Order by  id.numeroFolio asc ");

        if (listaCliente.size() == 1 && listaCliente.get(0).getCodigoCliente().longValue() != 0) {
          @SuppressWarnings("unchecked") final List<FiCorreosPorCliente> listaExiste = (List<FiCorreosPorCliente>)
              genericService.get("FiCorreosPorCliente"
                  , "id.codigoCliente=to_number('" + listaCliente.get(0).getCodigoCliente() + "')",
                  " Order by  id.email asc ");

          if (listaExiste.isEmpty()) {
            mostrarMensaje("No existe el cliente " + listaCliente.get(0).getCodigoCliente() +
                " en el catalogo de correos por cliente." +
                "\nArchivo enviado a la carpeta de Archivos no Cargados ", true);

            escribirArchivo("IMX/OUT_ERR/");
          } else {
            escribirArchivo("IMX/IN/");
            mostrarMensaje("Archivo Cargado en carpeta de Archivos Cargados", false);
          }
        } else {
          mostrarMensaje("No existe codigo de cliente  para el folio:" + folio, false);
          escribirArchivo("IMX/OUT_ERR/");
        }

      } else {
        //Nombre de archivo no cumple con el formato idCliente.pdf o idcliente.XML
        //se envia archivo de carpeta 'importar' a carpeta 'OUT_ERR'
        mostrarMensaje("El nombre del Archivo no cumple formato  FacturaFactorajeIMX_NOFOLIO_IMXFecha.... " +
            " Archivo enviado a la carpeta de Archivos no Cargados", true);
        escribirArchivo("IMX/OUT_ERR/");
      }
      //se borra la carpeta 'importar'
      final File importar = new File("IMX/importar/");
      while (Objects.requireNonNull(importar.listFiles()).length > 0) {
        boolean k = false;
        for (int i = 0; i < Objects.requireNonNull(importar.listFiles()).length; i++) {
          k = Objects.requireNonNull(importar.listFiles())[i].delete();
        }
        log.debug(k);
      }
    } else {
      mostrarMensaje("Formato del archivo invalido. Archivo enviado a la carpeta ERROR ", true);
      escribirArchivo("IMX/OUT_ERR/");
    }
  }

  private void escribirArchivo(final String directorio) {
    final File outFile = new File(directorio + uploadedFile.getFileName());
    try (final InputStream in = uploadedFile.getInputStream();
         final FileOutputStream out = new FileOutputStream(outFile)) {
      int c;
      while ((c = in.read()) != -1) {
        out.write(c);
      }
    } catch (IOException ioe) {
      log.error(ioe.getMessage(), ioe);
    }
  }
}

