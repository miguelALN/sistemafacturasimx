package com.bancomext.web.utils;

import com.bancomext.service.mapping.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.faces.context.FacesContext;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;

public class UtileriaCorreo {

  private static final Logger log = LogManager.getLogger(UtileriaCorreo.class);

  public static String contenidoCorreosPorReporte() {
    String contenido = leerArchivo("correo_EnvioFactura.html");
    final String piepagina = "<br><table style='font: bold 11px Verdana, Arial, Helvetica, sans-serif;" +
        " background: #00928F;text-shadow: 0.1em 0.1em #000;color: #FFFFFF;width: 600px;" +
        " height:100px;'><tr><td><center>LA BANCA ELECTR&#211;NICA DE BANCOMEXT<br>" +
        " La seguridad y rapidez que necesitas.</center></td></tr></table>";
    contenido = contenido.replace("<piepagina>", piepagina);
    return contenido;
  }

  public static String contenidoProcesoGeneracionFactoraje(final String usuario,
                                                           final String fechasProceso,
                                                           final List<FiSeguimientoImx> listaSeguimientoImx,
                                                           final List<FiCfdCifrasCtrl> listaCifrasControl) {
    String contenido = leerArchivo("correo_ProcesoGeneracionFactoraje.html");

    final String piepagina = "<br><table style='font: bold 11px Verdana, Arial, Helvetica, sans-serif;" +
        " background: #00928F;text-shadow: 0.1em 0.1em #000;color: #FFFFFF;width: 600px;" +
        "height:100px;'><tr><td><center>LA BANCA ELECTR&#211;NICA DE BANCOMEXT<br>La seguridad y rapidez " +
        "que necesitas.</center></td></tr></table>";

    final StringBuilder tablaProcesados = new StringBuilder("<table border='2'><tr bgcolor='#088A68' " +
        " style='color: #FFFFFF;' ><td>Fecha Emisi&#243;n</td><td>Status</td>" +
        "<td>Fecha Generaci&#243;n</td><td>RFC</td><td>Moneda</td><td>Importe</td>" +
        "<td>Campo A</td><td>Folio imx</td></tr>");

    final DecimalFormat formato = new DecimalFormat("###,##0.00");

    for (FiSeguimientoImx procesado : listaSeguimientoImx) {
      tablaProcesados.append("<tr>")
          .append("<td>").append(procesado.getFechaEmision(), 0, 10).append("</td>")
          .append("<td>").append(procesado.getStatus()).append("</td>")
          .append("<td>").append(procesado.getFechaGeneracion(), 0, 10).append("</td>")
          .append("<td>").append(procesado.getRfc()).append("</td>")
          .append("<td>").append(procesado.getMoneda()).append("</td>")
          .append("<td align='right'>").append(formato.format(procesado.getImporte())).append("</td>")
          .append("<td>").append(procesado.getCampoA()).append("</td>")
          .append("<td>").append(procesado.getFolioImx()).append("</td>")
          .append("</tr>");
    }
    tablaProcesados.append("</table>");

    final StringBuilder tablaCifrasControl = new StringBuilder("<table border='2'><tr bgcolor='#088A68' " +
        " style='color: #FFFFFF;' ><td>Fecha Valor</td><td>Esperadas IMX</td><td>Verificado</td>" +
        "<td>Enviado GACD</td><td>Folio SAT</td><td>Enviado Cliente</td><td>Cancelado</td></tr>");

    for (FiCfdCifrasCtrl registro : listaCifrasControl) {
      tablaCifrasControl.append("<tr>")
          .append("<td align='center'>").append(registro.getFechaValor()).append("</td>")
          .append("<td align='right'>").append(registro.getEsperadas()).append("</td>")
          .append("<td align='right'>").append(registro.getVerificadas()).append("</td>")
          .append("<td align='right'>").append(registro.getGacd()).append("</td>")
          .append("<td align='right'>").append(registro.getFolioSat()).append("</td>")
          .append("<td align='right'>").append(registro.getEnviadoCliente()).append("</td>")
          .append("<td align='right'>").append(registro.getCanceladas()).append("</td>")
          .append("</tr>");
    }
    tablaCifrasControl.append("</table>");

    contenido = contenido.replace("<GeneradoPor>", "    Proceso generado por: " + usuario);
    contenido = contenido.replace("<FechasProceso>", fechasProceso);
    contenido = contenido.replace("<tablaProcesados>", tablaProcesados.toString());
    contenido = contenido.replace("<tablaCifrasControl>", tablaCifrasControl.toString());
    contenido = contenido.replace("<piepagina>", piepagina);

    return contenido;
  }

  public static String contenidoLogin(final String usuario, final String motivo) {
    String contenido = leerArchivo("correo_login.html");
    final String piePagina = "<br><table style='font: bold 11px Verdana, Arial, Helvetica, sans-serif;" +
        " background: #00928F;text-shadow: 0.1em 0.1em #000;color: #FFFFFF;width: 600px;" +
        " height:100px;'><tr><td><center>LA BANCA ELECTR&oacute;NICA DE BANCOMEXT<br>La seguridad y rapidez que " +
        " necesitas.</center></td></tr></table>";
    contenido = contenido.replace("<usuario>", usuario);
    contenido = contenido.replace("<motivo>", motivo);
    contenido = contenido.replace("<piepagina>", piePagina);
    return contenido;
  }

  public static String contenidoProcesoGeneracionEnvioFacturas(final String mesAnio,
                                                               final String nombreCliente,
                                                               final List<FiCfdHistoricoFolios> listaTabla,
                                                               final String leyenda) {
    String contenido = leerArchivo("correo_EnvioFactura.html");
    final StringBuilder tablaProcesados = new StringBuilder("<table border='2'><tr bgcolor='#008C82' " +
        " style='color: #FFFFFF;' ><td colspan=3 >" + nombreCliente + "</td></tr>" +
        "<tr bgcolor='#008C82'  style='color: #FFFFFF;' ><td align='center'>FACTURA</td>" +
        "<td align='center'>FECHA</td><td align='center'>TOTAL</td></tr>");
    for (FiCfdHistoricoFolios procesado : listaTabla) {
      tablaProcesados.append("<tr>")
          .append("<td align='center'> IMX").append(procesado.getId().getNumeroFolio()).append("</td>")
          .append("<td align='center'>").append(procesado.getFechaAsignacion()).append("</td>")
          .append("<td align='right'>").append(Utilerias.formatearNumero(procesado.getImporte(), ""))
          .append("</td></tr>");
    }
    tablaProcesados.append("</table>");

    contenido = contenido.replace("<mesAnio>", mesAnio);
    contenido = contenido.replace("<tabladocumentos>", tablaProcesados.toString());
    contenido = contenido.replace("<leyenda>", leyenda);

    return contenido;
  }

  public static String contenidoProcesoGeneracionEnvioFacturasPrimeCom(final String mesAnio,
                                                                       final String nombreCliente,
                                                                       final List<VoHistoricoPrimeCom> listaTabla,
                                                                       final String serie,
                                                                       final String leyenda) {
    String contenido = leerArchivo("correo_EnvioFacturaPrimCom.html");

    final StringBuilder tablaProcesados = new StringBuilder("<table border='2'><tr bgcolor='#008C82' " +
        " style='color: #FFFFFF;' ><td colspan=3 >" + nombreCliente + "</td></tr>" +
        "<tr bgcolor='#008C82'  style='color: #FFFFFF;' ><td align='center'>FACTURA</td>" +
        "<td align='center'>FECHA</td><td align='center'>TOTAL</td></tr>");

    final SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
    for (VoHistoricoPrimeCom procesado : listaTabla) {
      tablaProcesados.append("<tr>")
          .append("<td align='center'>").append(serie).append(procesado.getNumeroFolio()).append("</td>")
          .append("<td align='center'>").append(formatter.format(procesado.getFechaAsignacion())).append("</td>")
          .append("<td align='right'>").append(Utilerias.formatearNumero(procesado.getImporte(), ""))
          .append("</td></tr>");
    }
    tablaProcesados.append("</table>");
    contenido = contenido.replace("<mesAnio>", mesAnio);
    contenido = contenido.replace("<tabladocumentos>", tablaProcesados.toString());
    contenido = contenido.replace("<leyenda>", leyenda);

    return contenido;
  }

  public static String contenidoProcesoEnvioFacturasReporteMes(final List<VoReporteFacturasMensual> listaTabla,
                                                               final BigDecimal totalFacturas,
                                                               final String leyenda) {

    final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
    final Date fecha = listaTabla.get(0).getFecha();
    final String fechaDoc = formatter.format(fecha);
    final String mes = fechaDoc.substring(3, 5);
    final String anio = fechaDoc.substring(6, 10);

    String contenido = leerArchivo("correo_EnvioReporteFacturasMensual.html");

    final StringBuilder tablaProcesados = new StringBuilder("<table border='1'><tr bgcolor='#008C82' " +
        " style='color: #FFFFFF;' ><td colspan=3 align='center'>" + listaTabla.get(0).getCliente() +
        "</td></tr><tr bgcolor='#008C82'  style='color: #FFFFFF;' ><td align='center'>FACTURA</td>" +
        "<td align='center'>FECHA</td><td align='center'>TOTAL</td></tr>");

    for (VoReporteFacturasMensual procesado : listaTabla) {
      tablaProcesados.append("<tr>")
          .append("<td align='center'> IMX").append(procesado.getNoFactura()).append("</td>")
          .append("<td align='center'>").append(formatter.format(procesado.getFecha())).append("</td>")
          .append("<td align='right'>").append(Utilerias.formatearNumero(procesado.getTotal(), ""))
          .append("</td></tr>");
    }

    tablaProcesados.append("<tr>")
        .append("<td></td><td bgcolor='#C0C0C0' align='center'>TOTAL</td><td bgcolor='#C0C0C0' align='right'>")
        .append(Utilerias.formatearNumero(totalFacturas, "")).append("</td></tr></table>");

    contenido = contenido.replace("<leyenda>", leyenda);
    contenido = contenido.replace("<anio>", anio);
    contenido = contenido.replace("<mes>", mes);
    contenido = contenido.replace("<tabladocumentos>", tablaProcesados.toString());

    return contenido;
  }

  public static String contenidoProcesoEnvioFacturasReporteMesPrimeCom(final List<VoHistoricoEncabezado> listaTabla,
                                                                       final BigDecimal totalFacturas,
                                                                       final String leyenda) {
    final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
    final Date fecha = listaTabla.get(0).getFechaValor();
    final String fechaDoc = formatter.format(fecha);
    final String mes = fechaDoc.substring(3, 5);
    final String anio = fechaDoc.substring(6, 10);

    String contenido = leerArchivo("correo_EnvioReporteFacturasMensual.html");

    StringBuilder tablaProcesados = new StringBuilder("<table border='1'><tr bgcolor='#008C82' " +
        " style='color: #FFFFFF;' ><td colspan=3 align='center'>" + listaTabla.get(0).getNombre() + "</td>" +
        "</tr><tr bgcolor='#008C82'  style='color: #FFFFFF;' ><td align='center'>FACTURA</td>" +
        "<td align='center'>FECHA</td><td align='center'>TOTAL</td></tr>");

    for (VoHistoricoEncabezado procesado : listaTabla) {
      tablaProcesados.append("<tr>")
          .append("<td align='center'> FAC").append(procesado.getNumeroFolio()).append("</td>")
          .append("<td align='center'>").append(formatter.format(procesado.getFechaValor())).append("</td>")
          .append("<td align='right'>").append(Utilerias.formatearNumero(procesado.getImporte(), ""))
          .append("</td></tr>");
    }

    tablaProcesados.append("<tr><td></td><td bgcolor='#C0C0C0' align='center'>TOTAL</td>")
        .append("<td bgcolor='#C0C0C0' align='right'>")
        .append(Utilerias.formatearNumero(totalFacturas, "")).append("</td></tr></table>");

    contenido = contenido.replace("<leyenda>", leyenda);
    contenido = contenido.replace("<anio>", anio);
    contenido = contenido.replace("<mes>", mes);
    contenido = contenido.replace("<tabladocumentos>", tablaProcesados.toString());

    return contenido;
  }

  public static String contenidoProcesoEnvioCifrasPrimeComisiones(final List<VoCifrasPrimeComisiones> listaTabla,
                                                                  final String match,
                                                                  final Date fecha,
                                                                  final String prod) {
    String contenido = leerArchivo("correo_EnvioReporteCifrasPrimeComisiones.html");

    final String fechaStr = new SimpleDateFormat("dd-MM-yyyy").format(fecha);
    final String encabezado = "Reporte Cifras Control CFDI generado para fecha: " + fechaStr;

    log.info("ID Producto: " + prod);

    final String producto;
    switch (prod) {
      case "PRIME":
        producto = "Prime Revenue &#45; e_Factor";
        break;
      case "REPF":
        producto = "REPF";
        break;
      case "COMISIONES":
        producto = "Comisiones";
        break;
      default:
        producto = "";
    }
    log.info("Etiqueta producto: " + producto);

    final StringBuilder tablaProcesados = new StringBuilder("<table border='1'><tr bgcolor='#008C82' " +
        " style='color: #FFFFFF;' ><td colspan=8 align='center'> Cifras Control " + producto +
        "</td></tr><tr bgcolor='#008C82' style='color: #FFFFFF;' ><td>Fecha Emisi&oacute;n</td>" +
        "<td>Total Facturas</td><td>Status</td><td>Motivo</td><td>Folios Utilizados</td>" +
        "<td>Fecha</td><td>Producto</td><td>Usuario Verifica</td></tr>");

    if (match.equals("S") || match.equals("G")) {
      if (listaTabla != null) {
        final SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        for (VoCifrasPrimeComisiones procesado : listaTabla) {
          tablaProcesados.append("<tr>")
              .append("<td>").append(formatter.format(procesado.getFechaEmision())).append("</td>")
              .append("<td>").append(procesado.getTotal()).append("</td>")
              .append("<td>").append(procesado.getStatus()).append("</td>")
              .append("<td>").append(procesado.getMotivo()).append("</td>")
              .append("<td>").append(procesado.getFolios()).append("</td>")
              .append("<td>").append(formatter.format(procesado.getFecha())).append("</td>");

          if (procesado.getProducto() != null && !procesado.getProducto().isEmpty()) {

            if (procesado.getProducto().equals("PRIME")) {
              tablaProcesados.append("<td>").append(procesado.getProducto()).append("/E_FACTOR</td>");
            } else {
              tablaProcesados.append("<td>").append(procesado.getProducto()).append("</td>");
            }
          }
          tablaProcesados.append("<td>").append(procesado.getUsuarioVerifica()).append("</td></tr>");
        }
      }
    }
    tablaProcesados.append("</table>");

    final String leyenda;
    switch (match) {
      case "S":
        leyenda = "Cifras control conciliadas!!!";
        break;
      case "N":
        leyenda = "No existen movimientos a procesar!!";
        break;
      case "G":
        leyenda = "Proceso: Verificaci&oacute;n de facturas con errores!!!";
        break;
      default:
        leyenda = "";
    }
    contenido = contenido.replace("<encabezado>", encabezado);
    contenido = contenido.replace("<leyenda>", leyenda);
    final String auth = "Este proceso es de generaci&oacute;n autom&aacute;tica. Favor de no contestar este correo.";
    contenido = contenido.replace("<GeneradoPor>", auth);
    contenido = contenido.replace("<tablaCifrasControl>", tablaProcesados.toString());

    return contenido;
  }

  private static String leerArchivo(final String archivo) {
    final StringBuilder sb = new StringBuilder();
    try (final InputStream is = FacesContext.getCurrentInstance().getExternalContext().getResourceAsStream(archivo)) {
      if (is != null) {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        reader.lines().forEach(sb::append);
      }
    } catch (IOException ioe) {
      log.error(ioe.getMessage());
    }
    return sb.toString();
  }

  public static boolean enviarCorreo(final InternetAddress[] to,
                                     final InternetAddress[] cc,
                                     final String asunto,
                                     final String contenido,
                                     final String tipo,
                                     final File adjunto) {
    boolean exe = false;

    final String mail;
    switch (tipo) {
      case "SUCRE":
        mail = Constantes.MAIL_FACTURAS_SUCRE;
        break;
      case "CARTASCREDITO":
        mail = Constantes.MAIL_CARTAS;
        break;
      case "PRIME-COM":
        mail = Constantes.MAIL_PRIME;
        break;
      case "FACTORAJE":
      default:
        mail = Constantes.MAIL_FACTURAS;
        break;
    }

    Transport tr = null;
    InitialContext ic = null;
    try {
      if (to != null && to.length > 0) {
        ic = new InitialContext();
        final Session session = (Session) ic.lookup(Constantes.MAIL_JNDI);
        final Properties serverProp = session.getProperties();
        serverProp.setProperty("mail.smtp.user", mail);
        session.setDebug(true);

        final Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(mail));
        msg.setRecipients(Message.RecipientType.TO, to);
        if (cc != null) {
          msg.setRecipients(Message.RecipientType.CC, cc);
        }
        msg.setSubject(asunto);
        msg.setSentDate(new Date());

        final MimeBodyPart mbp = new MimeBodyPart();
        mbp.setContent(contenido, "text/html");

        final Multipart mp = new MimeMultipart();
        mp.addBodyPart(mbp);

        if (adjunto != null) {
          final MimeBodyPart mbpAdj = new MimeBodyPart();
          mbpAdj.setDataHandler(new DataHandler(new FileDataSource(adjunto)));
          mbpAdj.setFileName(adjunto.getName());
          mp.addBodyPart(mbpAdj);
        }
        msg.setContent(mp);
        tr = session.getTransport("smtp");
        tr.connect();
        tr.sendMessage(msg, msg.getAllRecipients());
        log.info("Envio de correo exitoso");
        exe = true;
      }
    } catch (MessagingException | NamingException ex) {
      log.error(ex.getMessage(), ex);
    } finally {
      if (tr != null) {
        try {
          tr.close();
        } catch (MessagingException ex) {
          log.error(ex.getMessage(), ex);
        }
      }
      if (ic != null) {
        try {
          ic.close();
        } catch (NamingException e) {
          log.error(e.getMessage(), e);
        }
      }
    }
    return exe;
  }
}