package com.bancomext.web.beans.oper;

import com.bancomext.service.ConsultasService;
import com.bancomext.service.GenericService;
import com.bancomext.service.ServiceLocator;
import com.bancomext.service.dto.UsuarioDTO;
import com.bancomext.service.mapping.VoHistoricoPrimeCom;
import com.bancomext.service.mapping.VoSeguimientoEstatusFac;
import com.bancomext.web.utils.EnvioCfdiCorreo;
import com.bancomext.web.utils.GeneradorZip;
import com.bancomext.web.utils.UtileriaConexion;
import com.bancomext.web.utils.Utilerias;
import com.bancomext.web.validator.ValidadorSesion;
import com.jcraft.jsch.ChannelSftp;
import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.PrimeFaces;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.io.File;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

@Data
@ManagedBean(name = "conexionSFTP")
@ViewScoped
public class ConexionSFTP implements Serializable {

  private static final Logger log = LogManager.getLogger(ConexionSFTP.class);
  private static final GenericService genericService = ServiceLocator.getGenericService();

  public ConexionSFTP() {
    final UsuarioDTO usuarioLogueado = ValidadorSesion.getUsuarioLogueado();
    ValidadorSesion.validarPermiso("pruebasSFTP", usuarioLogueado.getRol());
  }

  public static void conectaSFTP() {

    log.info("Entrando a conectaSFTP...");

    // METODO QUE LIMPIA LA CARPETA IN ES CASO DE EXISTIR ARCHIVOS
    borrarArchivosEnINyZIP();
    descargarEnDirectorioIN();

    // SE LEEN TODOS LOS ARCHIVOS DE LA CARPETA ZIP
    log.info("Leyendo Carpeta ZIP...");
    final File[] archivosEnDirectorioZIP = new File("IMX/ZIP/").listFiles();

    try {
      if (archivosEnDirectorioZIP != null && archivosEnDirectorioZIP.length > 0) {

        int cantidadCorreosFactoraje = 0;
        int cantidadCorreosPrime = 0;
        for (final File archivoZip : archivosEnDirectorioZIP) {

          final String nombreSinExtension =
              archivoZip.getName().replaceAll(".ZIP", "").replaceAll(".zip", "");
          String folio = "";
          String serie = "";
          boolean sinSerieFolio;
          try {
            folio = nombreSinExtension.split("_")[2].trim();
            serie = nombreSinExtension.split("_")[0].trim();
            sinSerieFolio = false;
          } catch (Exception ignored) {
            sinSerieFolio = true;
          }

          if (!sinSerieFolio && !folio.isEmpty() && Utilerias.validaNumerico(folio) && !serie.isEmpty()) {

            if (serie.equals("FACTORAJEIMX")) {
              if (EnvioCfdiCorreo.enviarFactoraje(folio, archivoZip)) {
                cantidadCorreosFactoraje++;
              }
            } else if (serie.equals("COMISIONES") || serie.equals("PRIME")) {
              if (EnvioCfdiCorreo.enviarPrimeCom(serie, folio, archivoZip)) {
                cantidadCorreosPrime++;
              }
            }
          }
        }
        log.info("Email enviados a clientes FACTORAJE_IMX ------------ >" + cantidadCorreosFactoraje + " < ------");
        log.info("Email enviados a clientes PRIME Y COMISIONES ------- >" + cantidadCorreosPrime + "< ------");
        borrarArchivosEnINyZIP();
        mostrarMensaje("Email enviados FACTORAJE_IMX " + cantidadCorreosFactoraje, false);
        mostrarMensaje("Email enviados PRIME Y COMISIONES " + cantidadCorreosPrime, false);

      }
      log.info("Proceso de Envio Terminado.");
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      mostrarMensaje("Error al procesar el envio manual de facturas", true);
    }

  }

  private static List<String> getHistoricoFolios() {
    log.info("Inicia getHistoricoFolios ...");
    final List<VoHistoricoPrimeCom> listaHistoricoPrimeCom =
        ConsultasService.getHistoricoPrimeCom("", "", "FOLIOS");

    final List<String> listaFoliosSFTP = listaHistoricoPrimeCom
        .stream()
        .map(VoHistoricoPrimeCom::getNumeroFolio)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());

    final List<VoSeguimientoEstatusFac> listaSeguimientoFacturas =
        ConsultasService.getStatusFacturas(null, null);
    for (VoSeguimientoEstatusFac reg : listaSeguimientoFacturas) {
      if (reg.getSerieFolio() != null &&
          reg.getFolioSAT() != null &&
          !reg.getFolioSAT().isEmpty() &&
          reg.getFechaEnvioCliente() == null &&
          reg.getStatusVerificado() != null &&
          reg.getStatusVerificado().equals("VERIFICADO")) {
        listaFoliosSFTP.add(reg.getSerieFolio().replace("IMX", ""));
      }
    }
    Collections.sort(listaFoliosSFTP);
    log.info("FOLIOS A OBTENER : " + listaFoliosSFTP);
    log.info("Termina getHistoricoFolios.");
    return listaFoliosSFTP;
  }

  private static void borrarArchivosEnINyZIP() {
    log.info("Inicia clean de archivos en IN...");
    final File directorioIn = new File("IMX/IN/");
    final File[] filesIn = directorioIn.listFiles();
    if (filesIn != null) {
      long k = Arrays.stream(filesIn).map(File::delete).count();
      log.info(k + "archivos borrados en IN");
    }
    log.info("Termina clean de archivos en IN... \nInicia clean de archivos en ZIP...");
    final File directorioZip = new File("IMX/ZIP/");
    final File[] filesZip = directorioZip.listFiles();
    if (filesZip != null) {
      long k = Arrays.stream(filesZip).map(File::delete).count();
      log.info(k + "archivos borrados en ZIP");
    }
    log.info("Termina clean de archivos en ZIP...");
  }

  @SuppressWarnings("unchecked")
  private static void descargarEnDirectorioIN() {

    //	LLENA LISTA DE FOLIOS PRIME, COMISIONES Y IMX PARA EXTRAER DEL SFTP
    final List<String> listaFoliosSFTP = getHistoricoFolios();

    final UtileriaConexion utilConexion = new UtileriaConexion();

    // CONECTAMOS AL SFTP
    final ChannelSftp channelSftp = utilConexion.getConnection();

    try {
      // Extrae archivos .PDF
      String ls = "*.pdf";
      Vector<ChannelSftp.LsEntry> listEntryPDF = channelSftp.ls(ls);

      for (ChannelSftp.LsEntry lsEntry : listEntryPDF) {
        log.info("listEntryPDF .... -> " + lsEntry.getFilename());
      }

      UtileriaConexion.extractFilesSFTP(listEntryPDF, listaFoliosSFTP, channelSftp);
      log.info("Finalizo el proceso de extraccion de archivos PDF satisfactoriamente");

      // Extrae archivos	.XML
      ls = "*.xml";
      Vector<ChannelSftp.LsEntry> listEntryXML = channelSftp.ls(ls);

      for (ChannelSftp.LsEntry lsEntry : listEntryXML) {
        log.info("listEntryXML .... -> " + lsEntry.getFilename());
      }

      UtileriaConexion.extractFilesSFTP(listEntryXML, listaFoliosSFTP, channelSftp);
      log.info("Finalizo el proceso de extraccion de archivos XML satisfactoriamente");

      // SE TRANSFIEREN ARCHIVOS AL REPOSITORIO
      utilConexion.uploadRep();

      // Comprimimos los archivos en la carpeta IN
      log.info("Comprimiendo Archivos...");
      GeneradorZip.generarArchivoZip();
      log.info("Termina Compresion de Archivos...");

    } catch (Exception e) {
      log.error("Error Tipo : " + e.getMessage(), e);
    } finally {
      utilConexion.closeConnection(channelSftp);
      log.info("Conexion Finalizada Exitosamente.!");
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
}
