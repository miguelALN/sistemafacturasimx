package com.bancomext.web.utils;

import com.bancomext.service.ConsultasService;
import com.bancomext.service.GenericService;
import com.bancomext.service.ProcedimientosService;
import com.bancomext.service.ServiceLocator;
import com.bancomext.service.mapping.*;
import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Data
public class ProcesoExtraccionJob implements Job {

  private static final Logger log = LogManager.getLogger(ProcesoExtraccionJob.class);

  @SuppressWarnings("unchecked")
  private static void extraccion(final int diasProceso, final String diasCifrasControl) {
    log.info("Job Proceso Extraccion..");

    final GenericService genericService = ServiceLocator.getGenericService();
    final Date fechaInicial;
    final Calendar cal = Calendar.getInstance();
    final SimpleDateFormat formatoddMMyyyy = new SimpleDateFormat("dd/MM/yyyy");

    final String mensajefechas;

    try {

      log.info("Respuestas ");
      String respuesta = ProcedimientosService.cfdiMain(new Date(), "Generacion Automatica");

      String respuestaVerificacion;
      log.info("respuesta PKG_FI_FAC_CFDI_MAIN :" + respuesta);

      if (respuesta == null) {
        log.info("RESPUESTA NULL");

        boolean contadorVerificado = false;
        final List<FiCfdVerificacion> listaTablaVerificacion =
            ConsultasService.getVerificacion(new Date(), true, diasProceso);

        if (listaTablaVerificacion.isEmpty()) {
          log.info("No hay facturas por processar con estatus GENERADO y CONCILIADOS");
          mensajefechas = "No hay facturas por processar con estatus GENERADO y CONCILIADOS";
        } else {

          for (FiCfdVerificacion registro : listaTablaVerificacion) {
            log.info("El estatus es:::  " + registro.getStatus());
            respuestaVerificacion = ProcedimientosService.puStatusFolios(registro.getProducto(),
                registro.getId().getFechaEmision(), registro.getStatus(), registro.getId().getNumAcreditado(),
                registro.getId().getContrato(), registro.getSecuencia());

            log.info("respuestaVerificacion:" + respuestaVerificacion);
            if (respuestaVerificacion != null) {
              log.info("Se otuvo respuesta diferente de nulo :producto::: " + registro.getProducto() +
                  " ::fecha emision::   " + registro.getId().getFechaEmision() + " ::Estatus:   " +
                  registro.getStatus() + ":::::" + registro.getId().getNumAcreditado() + ":::contrato::" +
                  registro.getId().getContrato() + "::secuencia::" + registro.getSecuencia());
            } else {

              try {
                registro.setFolioIMX("IMX"
                    + ((List<FiCfdHistoricoFolios>) genericService.get("FiCfdHistoricoFolios",
                    " Trunc(fechaAsignacion) = to_date('" + formatoddMMyyyy.format(
                        registro.getId().getFechaEmision()) + "','dd/mm/yyyy')"
                        + " And codigoCliente =" + registro.getId().getNumAcreditado()
                        + " And contrato ='" + registro.getId().getContrato() + "'",
                    " order by id.numeroFolio asc ")).get(0).getId().getNumeroFolio()
                );
              } catch (Exception ex) {
                log.info("Error al asignar folio IMX de fatura Verificada fechaasignacion:" +
                    registro.getId().getFechaEmision() + " codigoCliente:" + registro.getId().getNumAcreditado() +
                    " contrato:" + registro.getId().getContrato());
              }

              contadorVerificado = true;
              registro.setStatus("VERIFICADO");
              registro.setModificadoPor("Automatico");
              registro.setFechaModificacion(Calendar.getInstance().getTime());

            }
          }

          log.info("Generando H1  hay almenos uno verificado: " + contadorVerificado);
          if (contadorVerificado && genericService.get("FiCfdEstadoCuenta", " trunc(fechaProceso)= to_date('" +
              formatoddMMyyyy.format(new Date()) + "','dd/mm/yyyy') and id.tramaId='T01H' ", "").isEmpty()) {
            ProcedimientosService.cfdPTrama1H(new Date(), new Date());
          }

          cal.setTime(new Date());
          cal.add(Calendar.DATE, -Integer.parseInt(ConsultasService.getStringParametro("dias_Reproceso")));

          fechaInicial = cal.getTime();
          cal.add(Calendar.DATE, +Integer.parseInt(ConsultasService.getStringParametro("dias_Reproceso")));

          mensajefechas = "Se procesaron las facturas en el rango de fechas del " +
              formatoddMMyyyy.format(fechaInicial) + " al " + formatoddMMyyyy.format(new Date());

          log.info(mensajefechas);
        }

        ProcedimientosService.organizaCifrasControl(new Date());

        String grupoAdministradores = ConsultasService.getGrupoAdministradores();

        final int dias = Integer.parseInt(diasCifrasControl);

        final List<FiCfdCifrasCtrl> listaCifrasControl = ConsultasService.getListCifras(new Date(), dias);

        final List<FiSeguimientoImx> listaSeguimientoImx =
            ConsultasService.getSeguimientoIMX(new Date(), new Date(), diasCifrasControl);

        enviarCorreo(
            genericService.get("FiAccesos", " upper(rol)=upper('" + grupoAdministradores + "')", ""),
            "Proceso Generacion de Facturas IMX " + formatoddMMyyyy.format(new Date()), UtileriaCorreo.contenidoProcesoGeneracionFactoraje(
                "Proceso Automatico", mensajefechas, listaSeguimientoImx, listaCifrasControl)
        );

      } else {
        log.info("Ocurrio un error al procesar los registros.");
      }

    } catch (Exception ex) {
      log.error(ex.getMessage(), ex);
    }
  }

  private static void enviarCorreo(final List<FiAccesos> listaCorreos, final String asunto, final String contenido) {
    if ((listaCorreos != null) && (!listaCorreos.isEmpty())) {
      final InternetAddress[] to = new InternetAddress[listaCorreos.size()];
      for (int i = 0; i < listaCorreos.size(); ++i) {
        try {
          if ("Desarrollo".equals(Constantes.AMBIENTE)) {
            to[i] = new InternetAddress(Constantes.MAIL_DESARROLLO);
          } else {
            to[i] = new InternetAddress(listaCorreos.get(i).getEmail());
            log.info("Correo: " + to[i]);
          }
        } catch (AddressException ae) {
          log.error(ae.getMessage(), ae);
        }
      }
      UtileriaCorreo.enviarCorreo(to, null, asunto, contenido, "FACTORAJE", null);
    }
  }

  @Override
  public void execute(final JobExecutionContext arg0) {

    int diasReproceso = Integer.parseInt(ConsultasService.getStringParametro("dias_Reproceso"));
    final String diasCifrasControl =
        ConsultasService.getStringParametro("DIAS_CIFRAS_CONTROL") != null &&
            !ConsultasService.getStringParametro("DIAS_CIFRAS_CONTROL").trim().isEmpty() ?
            ConsultasService.getStringParametro("DIAS_CIFRAS_CONTROL") : "1";

    log.info("dias Reproceso :" + diasReproceso);
    if (diasReproceso > 0) {
      log.info("Se ejecutara el procesos ProcesoExtraccionJob 2 con " + diasReproceso + " dias de reproceso");
      extraccion(diasReproceso, diasCifrasControl);
    } else {
      log.info("No se ejecutara el procesos " + diasReproceso + " dias de reproceso");
    }
  }
}
