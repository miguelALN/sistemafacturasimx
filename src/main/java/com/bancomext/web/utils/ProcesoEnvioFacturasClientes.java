package com.bancomext.web.utils;

import com.bancomext.service.GenericService;
import com.bancomext.service.ServiceLocator;
import com.bancomext.service.mapping.FiCfdHistoricoFolios;
import com.bancomext.service.mapping.FiCorreosPorCliente;
import com.bancomext.service.mapping.FiCuerpoCorreos;
import com.bancomext.service.mapping.FiFacturasEncabezado;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ProcesoEnvioFacturasClientes implements Job {

  private static final Logger log = LogManager.getLogger(ProcesoEnvioFacturasClientes.class);
  private final GenericService genericService = ServiceLocator.getGenericService();

  private static void enviarCorreo(final List<FiCorreosPorCliente> listaCorreos, final String asunto,
                                   final String contenido, final File adjunto) {
    if (listaCorreos != null && !listaCorreos.isEmpty()) {
      final InternetAddress[] to = new InternetAddress[listaCorreos.size()];
      for (int i = 0; i < listaCorreos.size(); i++) {
        try {
          if ("Desarrollo".equals(Constantes.AMBIENTE)) {
            to[i] = new InternetAddress(Constantes.MAIL_DESARROLLO);
          } else {
            to[i] = new InternetAddress(listaCorreos.get(i).getId().getEmail());
          }
        } catch (AddressException ae) {
          log.error(ae.getMessage(), ae);
        }
        log.info("Correo: " + listaCorreos.get(i).getId().getEmail());
      }
      UtileriaCorreo.enviarCorreo(to, null, asunto, contenido, "FACTORAJE", adjunto);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public void execute(final JobExecutionContext arg0) {

    log.info("Job Envio de Facturas a Clientes..");

    final SimpleDateFormat formatoddMMyyyy = new SimpleDateFormat("dd/MM/yyyy");
    final Locale espanol = new Locale("es", "ES");

    final SimpleDateFormat formatoMESdelAnio = new SimpleDateFormat("MMMMM 'del' yyyy", espanol);
    List<FiCfdHistoricoFolios> listaCliente;

    String carpetaIN = "IMX/IN/";
    String carpetaOUT = "IMX/OUT/";
    String carpetaOUT_ERR = "IMX/OUT_ERR/";
    String carpetaCancelacion = "IMX/cancelacion/";
    String carpetaExportar = "IMX/exportar/";
    String carpetaImportar = "IMX/importar/";
    String carpetaParametros = "IMX/parametros/";
    String carpetaValidacion = "IMX/validacion/";

    File archivosEntrada = new File(carpetaIN);
    if (!archivosEntrada.exists()) {
      log.info("Inicio creando directorios");
      boolean k;
      k = new File(carpetaIN).mkdirs();
      log.debug(k);
      k = new File(carpetaOUT).mkdirs();
      log.debug(k);
      k = new File(carpetaOUT_ERR).mkdirs();
      log.debug(k);
      k = new File(carpetaCancelacion).mkdirs();
      log.debug(k);
      k = new File(carpetaExportar).mkdirs();
      log.debug(k);
      k = new File(carpetaImportar).mkdirs();
      log.debug(k);
      k = new File(carpetaParametros).mkdirs();
      log.debug(k);
      k = new File(carpetaValidacion).mkdirs();
      log.debug(k);
      log.info("Fin creando directorios");
    }

    try {

      //se leen todos los archivos de la carpeta in
      if (Objects.requireNonNull(archivosEntrada.listFiles()).length > 0) {
        for (int i = 0; i < Objects.requireNonNull(archivosEntrada.listFiles()).length + 1; i++) {

          String nombreSinExtension = Objects.requireNonNull(archivosEntrada.listFiles())[i].getName().replaceAll(
              ".ZIP", "").replaceAll(".zip", "");
          String folio = "";
          boolean nofolio;
          try {
            folio = nombreSinExtension.split("_")[2].trim();
            nofolio = false;
          } catch (Exception ex) {
            nofolio = true;
          }

          if (!nofolio && !folio.isEmpty() && Utilerias.validaNumerico(folio)) {

            listaCliente = (List<FiCfdHistoricoFolios>) genericService.get("FiCfdHistoricoFolios",
                "id.numeroFolio =to_number('" + folio + "')", " Order by  id.numeroFolio asc ");

            if (listaCliente.size() == 1 && listaCliente.get(0).getCodigoCliente().longValue() != 0) {

              List<FiFacturasEncabezado> encabezado = (List<FiFacturasEncabezado>) genericService.get(
                  "FiFacturasEncabezado", " id.noAcreditado =" +
                      listaCliente.get(0).getCodigoCliente() + " and id.contrato ='" +
                      listaCliente.get(0).getContrato() + "' and trunc(id.fechavalor) = to_date('" +
                      formatoddMMyyyy.format(listaCliente.get(0).getFechaAsignacion()) + "','dd/mm/yyyy') ",
                  " Order by  id.noAcreditado asc ");

              log.info("Enviando Email a cliente " + listaCliente.get(0).getCodigoCliente());

              listaCliente.get(0).setImporte(encabezado.get(0).getImporte());

              //Se envian los archivos por correo
              String asunto = "";
              String leyenda = "";
              List<FiCuerpoCorreos> lst = genericService.get(
                  "FiCuerpoCorreos", " correo = 'CORREOS_CFDI'", "");
              if (!lst.isEmpty()) {
                asunto = lst.get(0).getAsunto();
                leyenda = lst.get(0).getCuerpo();
              }

              enviarCorreo(genericService.get(
                      "FiCorreosPorCliente", " id.codigoCliente='" +
                          listaCliente.get(0).getCodigoCliente() + "'", " Order by  id.email asc "),
                  asunto, UtileriaCorreo.contenidoProcesoGeneracionEnvioFacturas(
                      formatoMESdelAnio.format(encabezado.get(0).getId().getFechaValor()),
                      encabezado.get(0).getNombre(), listaCliente, leyenda),
                  Objects.requireNonNull(archivosEntrada.listFiles())[i]);

              //se envia el archivo en la carpteta OUT y se elimina de IN
              File outFile = new File(carpetaOUT + Objects.requireNonNull(
                  archivosEntrada.listFiles())[i].getName());
              FileInputStream in = new FileInputStream(Objects.requireNonNull(archivosEntrada.listFiles())[i]);
              FileOutputStream out = new FileOutputStream(outFile);
              int c;
              while ((c = in.read()) != -1) {
                out.write(c);
              }
              in.close();
              out.close();
              //se elimina archivo de carpeta IN
              boolean k = Objects.requireNonNull(archivosEntrada.listFiles())[i].delete();
              log.debug(k);
            }

          }

        }
      } else {
        log.info("no hay archivos por enviar");
      }

    } catch (IOException ioe) {
      log.error("Hubo un error de entrada/salida!!!" + ioe.getMessage(), ioe);
    } catch (Exception ex) {
      log.error(ex.getMessage(), ex);
    }

    log.info("Finish");
  }

}
