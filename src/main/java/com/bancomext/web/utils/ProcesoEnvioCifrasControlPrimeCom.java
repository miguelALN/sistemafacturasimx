package com.bancomext.web.utils;

import com.bancomext.service.ConsultasService;
import com.bancomext.service.mapping.VoCifrasPrimeComisiones;
import com.bancomext.service.mapping.VoCorreosCifrasPrimeComisiones;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.util.Date;
import java.util.List;

@Getter
@DisallowConcurrentExecution
public class ProcesoEnvioCifrasControlPrimeCom implements Job {

  private static final Logger log = LogManager.getLogger(ProcesoEnvioCifrasControlPrimeCom.class);

  private static void procesarProducto(final String producto) {
    log.info("LOOP ENVIO CIFRAS CONTROL: producto: " + producto);
    final Date ayer = new Date((new Date()).getTime() - 24 * 60 * 60 * 1000);
    final List<VoCifrasPrimeComisiones> listaStatus = ConsultasService.getCifrasStatusPrimeCom(ayer, producto, "S");

    if (listaStatus.isEmpty()) {
      final List<VoCifrasPrimeComisiones> listaCifrasPrimeCom = ConsultasService.getCifrasPrimeCom(ayer, producto);

      if (!listaCifrasPrimeCom.isEmpty()) {
        if (listaCifrasPrimeCom.size() == 2 &&
            listaCifrasPrimeCom.get(0).getTotal() != 0 && listaCifrasPrimeCom.get(1).getTotal() != 0 &&
            listaCifrasPrimeCom.get(0).getTotal() == listaCifrasPrimeCom.get(1).getTotal()) {
          enviarYUpdate(listaCifrasPrimeCom, "S", ayer, producto);
        }
        if (listaCifrasPrimeCom.size() == 1 && listaCifrasPrimeCom.get(0).getStatus() != null &&
            listaCifrasPrimeCom.get(0).getStatus().equals("GENERADO")) {
          enviarYUpdate(listaCifrasPrimeCom, "G", ayer, producto);
        }
      } else {
        enviarYUpdate(listaCifrasPrimeCom, "N", ayer, producto);
      }
    } else {
      log.info("YA SE HA REALIZADO EL ENVIO DE CORREO ELECTRONICO PARA LAS CIFRAS CONTROL");
    }
  }

  private static void enviarYUpdate(final List<VoCifrasPrimeComisiones> listaCifrasPrimeCom,
                                    final String match, final Date ayer, final String reg) {
    try {
      final String aux = ("REPF".equals(reg) ? "WHERE ACTIVO_REPF = 'S' " : "WHERE ACTIVO = 'S' ");
      final List<VoCorreosCifrasPrimeComisiones> listaCorreos = ConsultasService.getCorreosCifrasPrimeCom(aux);
      if (!listaCorreos.isEmpty()) {
        enviarCorreo(listaCorreos,
            UtileriaCorreo.contenidoProcesoEnvioCifrasPrimeComisiones(listaCifrasPrimeCom, match, ayer, reg));
        ConsultasService.updateCifrasStatusPrimeCom(ayer, reg, "S");
      }
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
  }

  private static void enviarCorreo(final List<VoCorreosCifrasPrimeComisiones> listaCorreos, final String contenido) {
    if (listaCorreos != null && !listaCorreos.isEmpty()) {
      final InternetAddress[] to = new InternetAddress[listaCorreos.size()];
      for (int i = 0; i < listaCorreos.size(); i++) {
        try {
          to[i] = new InternetAddress(listaCorreos.get(i).getCorreo());
        } catch (AddressException ae) {
          log.error(ae.getMessage(), ae);
        }
        log.info("Correo: " + listaCorreos.get(i).getCorreo());
      }
      UtileriaCorreo.enviarCorreo(to, null, "Cifras Control CFDI", contenido, "SUCRE", null);
    }
  }

  @Override
  public void execute(final JobExecutionContext paramJobExecutionContext) {
    log.info("Job Envio de Correo Cifras Control Prime - Comisiones - REPF");
    procesarProducto("PRIME");
    procesarProducto("COMISIONES");
    procesarProducto("REPF");
  }
}
