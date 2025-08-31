package com.bancomext.web.utils;

import com.bancomext.service.ConsultasService;
import com.bancomext.service.GenericService;
import com.bancomext.service.ServiceLocator;
import com.bancomext.service.mapping.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class EnvioCfdiCorreo {
  private static final Logger log = LogManager.getLogger(EnvioCfdiCorreo.class);
  private final static GenericService genericService = ServiceLocator.getGenericService();

  @SuppressWarnings("unchecked")
  public static boolean enviarFactoraje(final String folio, final File archivoZip) {
    boolean r = true;

    final List<FiCfdHistoricoFolios> listaCliente =
        (List<FiCfdHistoricoFolios>) genericService.get("FiCfdHistoricoFolios",
            "id.numeroFolio =to_number('" + folio + "')", " Order by  id.numeroFolio asc ");

    if (listaCliente.size() == 1 && listaCliente.get(0).getCodigoCliente().longValue() != 0) {

      final SimpleDateFormat formatoddMMyyyy = new SimpleDateFormat("dd/MM/yyyy");
      final List<FiFacturasEncabezado> encabezado =
          (List<FiFacturasEncabezado>) genericService.get("FiFacturasEncabezado",
              " id.noAcreditado =" + listaCliente.get(0).getCodigoCliente() +
                  " and id.contrato ='" + listaCliente.get(0).getContrato() + "'" +
                  " and trunc(id.fechavalor) = to_date('" +
                  formatoddMMyyyy.format(listaCliente.get(0).getFechaAsignacion()) + "','dd/mm/yyyy') ",
              " order by  id.noAcreditado asc ");

      log.info("Enviando Email a cliente " + listaCliente.get(0).getCodigoCliente());

      listaCliente.get(0).setImporte(encabezado.get(0).getImporte());

      //Se envian los archivos por correo
      String asunto = "";
      String leyenda = "";
      final List<FiCuerpoCorreos> cuerposCorreo = (List<FiCuerpoCorreos>)
          genericService.get("FiCuerpoCorreos", " correo = 'CORREOS_CFDI'", "");
      for (final FiCuerpoCorreos fiCuerpoCorreos : cuerposCorreo) {
        if (fiCuerpoCorreos.getCorreo() != null && fiCuerpoCorreos.getCuerpo() != null) {
          asunto = fiCuerpoCorreos.getAsunto();
          leyenda = fiCuerpoCorreos.getCuerpo();
        }
      }

      final Locale locale = new Locale("es", "ES");
      final SimpleDateFormat formatoMESdelAnio = new SimpleDateFormat("MMMMM 'del' yyyy", locale);

      final String contenido = UtileriaCorreo.contenidoProcesoGeneracionEnvioFacturas(
          formatoMESdelAnio.format(encabezado.get(0).getId().getFechaValor()),
          encabezado.get(0).getNombre(), listaCliente, leyenda);

      try {
        enviarCorreo(genericService.get("FiCorreosPorCliente",
            " id.codigoCliente='" + listaCliente.get(0).getCodigoCliente() + "'",
            " order by  id.email asc "), asunto, contenido, archivoZip);

        // SE ACTUALIZA TABLA DE BITACORA DE SEGUIMIENTO
        BitacoraAcciones.actualizarBitacoraCFDI("Enviado al cliente", folio, "VERIFICADO");

        // ACTUALIZAMOS BITACORA DE ACCIONES
        BitacoraAcciones.actualizarAcciones("ENVIO A CLIENTE", folio, "VERIFICADO");
      } catch (Exception e) {
        log.error(e.getMessage(), e);
        BitacoraAcciones.actualizarBitacoraCFDI("Envio fallido al cliente", folio, "");
        r = false;
      }
      boolean k = archivoZip.delete();
      log.debug(k);
    }
    return r;
  }

  public static boolean enviarPrimeCom(final String serie, final String folio, final File archivoZip) {

    boolean r = true;
    final String ser = ((serie.equals("COMISIONES") ? "COM" : "FAC"));

    final List<VoHistoricoPrimeCom> listaHistoricoPrimeCom =
        ConsultasService.getHistoricoPrimeCom(folio, ser, "CORREO");

    if (listaHistoricoPrimeCom.size() == 1 &&
        listaHistoricoPrimeCom.get(0).getCodigoCliente() != 0 &&
        listaHistoricoPrimeCom.get(0).getFechaAsignacion() != null) {

      final List<VoHistoricoEncabezado> listaHistoricoEncabezado = ConsultasService.getHistoricoEncabezado(
          listaHistoricoPrimeCom.get(0).getCodigoCliente(),
          listaHistoricoPrimeCom.get(0).getFechaAsignacion(),
          Integer.parseInt(folio));

      log.info("Enviando Email a cliente: " + serie + listaHistoricoPrimeCom.get(0).getCodigoCliente());

      if (!listaHistoricoEncabezado.isEmpty()) {
        listaHistoricoPrimeCom.get(0).setImporte(listaHistoricoEncabezado.get(0).getImporte());
      }

      try {

        final List<VoCorreosCifrasPrimeComisiones> listaCorreos;
        final List<VoTablaElemento> listaCorreosPrimeCom;
        if (serie.equals("COMISIONES")) {
          listaCorreosPrimeCom =
              ConsultasService.getCorreosCFDIComisiones(listaHistoricoPrimeCom.get(0).getCodigoCliente());
          // LISTA DE CORREOS COPIA OCULTA
          listaCorreos = ConsultasService.getCorreosCifrasPrimeCom("WHERE ACTIVO_COM = 'S'");
        } else {
          listaCorreosPrimeCom =
              ConsultasService.getCorreosCFDIPrime(listaHistoricoPrimeCom.get(0).getCodigoCliente());
          // LISTA DE CORREOS COPIA OCULTA
          listaCorreos = ConsultasService.getCorreosCifrasPrimeCom("WHERE ACTIVO_FAC = 'S'");
        }

        // OBTENEMOS EL ASUNTO Y EL CUERPO DEL CORREO DE PRIME Y COMISIONES
        final List<VoTablaElemento> listaCuerpoCorreosPrimeCom = ConsultasService.getCuerpoCorreosPrimCom();

        String asunto = "";
        String cuerpo = "";

        if (!listaCuerpoCorreosPrimeCom.isEmpty()) {
          if (listaCuerpoCorreosPrimeCom.get(0).getElemento() != null &&
              listaCuerpoCorreosPrimeCom.get(0).getElemento2() != null) {
            asunto = listaCuerpoCorreosPrimeCom.get(0).getElemento();
            cuerpo = listaCuerpoCorreosPrimeCom.get(0).getElemento2();
          }
        }

        final SimpleDateFormat formatoMESdelAnio =
            new SimpleDateFormat("MMMMM 'del' yyyy", new Locale("es", "ES"));

        if (!listaCorreosPrimeCom.isEmpty()) {
          enviarCorreo(listaCorreosPrimeCom, listaCorreos,
              asunto, UtileriaCorreo.contenidoProcesoGeneracionEnvioFacturasPrimeCom(
                  formatoMESdelAnio.format(listaHistoricoPrimeCom.get(0).getFechaAsignacion()),
                  listaHistoricoEncabezado.get(0).getNombre(),
                  listaHistoricoPrimeCom, ser, cuerpo),
              archivoZip);
          boolean k = archivoZip.delete();
          log.debug(k);
          log.info("UPDATING HISTORICO FOLIOS PRIME - COM ------ SERIE : -----> " + ser + " FOLIO : --- > " + folio);
          ConsultasService.updateHistoricoFoliosPrimeCom(folio, ser);

        } else {
          log.info("No existe correo. No se envio correo a..." + listaHistoricoPrimeCom.get(0).getCodigoCliente());
        }

      } catch (Exception e) {
        log.error(e.getMessage(), e);
        r = false;
      }
    } else {
      log.info("No existen datos en Historico de Folios. No se envio Correo...");
    }
    return r;
  }


  // ENVIO DE CORREO CFDI FACTORAJE-IMX
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
      }
      UtileriaCorreo.enviarCorreo(to, null, asunto, contenido, "FACTORAJE", adjunto);
    }
  }

  // ENVIO DE CORREO PRIME COMISIONES
  private static void enviarCorreo(final List<VoTablaElemento> listaCorreos,
                                   final List<VoCorreosCifrasPrimeComisiones> listaCorreosCc,
                                   final String asunto, final String contenido, final File adjunto) {
    final InternetAddress[] cc;
    if (listaCorreosCc != null && !listaCorreosCc.isEmpty()) {
      cc = new InternetAddress[listaCorreosCc.size()];
      for (int i = 0; i < listaCorreosCc.size(); i++) {
        try {
          if ("Desarrollo".equals(Constantes.AMBIENTE)) {
            cc[i] = new InternetAddress(Constantes.MAIL_DESARROLLO);
          } else {
            cc[i] = new InternetAddress(listaCorreosCc.get(i).getCorreo());
          }
        } catch (AddressException ae) {
          log.error(ae.getMessage(), ae);
        }
      }
    } else {
      cc = new InternetAddress[0];
    }

    InternetAddress[] to;
    if (listaCorreos != null && !listaCorreos.isEmpty()) {
      to = new InternetAddress[listaCorreos.size()];
      for (int i = 0; i < listaCorreos.size(); i++) {
        try {
          if ("Desarrollo".equals(Constantes.AMBIENTE)) {
            to[i] = new InternetAddress(Constantes.MAIL_DESARROLLO);
          } else {
            to[i] = new InternetAddress(listaCorreos.get(i).getElemento());
          }
        } catch (AddressException ae) {
          log.error(ae.getMessage(), ae);
        }
      }
      UtileriaCorreo.enviarCorreo(to, cc, asunto, contenido, "PRIME-COM", adjunto);
    }
  }


}
