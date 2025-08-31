package com.bancomext.web.srvlt;

import com.bancomext.web.utils.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class QuartzInitializerServlet extends HttpServlet {

  private static final Logger log = LogManager.getLogger(Constantes.class);

  @Override
  protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
    if (response != null) {
      response.setContentType("text/html;charset=UTF-8");
      final PrintWriter out = response.getWriter();
      out.close();
    }
  }

  @Override
  protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
    if (response != null) {
      response.setContentType("text/html;charset=UTF-8");
      final PrintWriter out = response.getWriter();
      out.close();
    }
  }

  /**
   * Ejecucion del Job ExpireQuotationJob el cual actualiza el status de las
   * cotizaciones que han sido expiradas por vigencia.
   */
  @Override
  public void init(final ServletConfig config) throws ServletException {
    final SchedulerFactory schedFact = new StdSchedulerFactory();

    final JobDataMap mapJob = new JobDataMap();

    // Borrado de Temporales, Depura tabla logs
    final JobDetail job1 = new JobDetail("BorraTemporales_2359", ExpireQuotationJob.class);
    job1.setJobDataMap(mapJob);
    final Trigger trigger1 = TriggerUtils.makeDailyTrigger(23, 59);
    trigger1.setName("Cron_BorraTemporales_2359");

    // Proceso de Generacion de Informacion
    final JobDetail job2 = new JobDetail("ProcesoExtraccion_2359", ProcesoExtraccionJob.class);
    job2.setJobDataMap(mapJob);
    final Trigger trigger2 = TriggerUtils.makeDailyTrigger(14, 30);
    trigger2.setName("Cron_ProcesoExtraccion_2359");

    // Proceso de Generacion de Historico Reporte SICAV
    final JobDetail job3 = new JobDetail(
        "ProcesoExtraccionHistoricoComparativo_0601", ProcesoLLenaHistoricoConciliacionSICAVJob.class);
    job3.setJobDataMap(mapJob);
    final Trigger trigger3 = TriggerUtils.makeDailyTrigger(10, 0);
    trigger3.setName("ProcesoExtraccionHistoricoComparativo_0601");

    // Genera Reportes SICAV y Saldos FIU
    final JobDetail job4 = new JobDetail("ProcesoGeneraReportes_0701", ProcesoGeneraReportesJob.class);
    job4.setJobDataMap(mapJob);
    final Trigger trigger4 = TriggerUtils.makeDailyTrigger(13, 10);
    trigger4.setName("Cron_ProcesoGeneraReportes_0701");

    // Envio PDF (facturas Digitales) a Clientes
    final JobDetail job5 =
        new JobDetail("ProcesoEnvioFacturasClientes_PorHora", ProcesoEnvioFacturasClientes.class);
    job5.setJobDataMap(mapJob);
    final Trigger trigger5 = TriggerUtils.makeHourlyTrigger(1);
    trigger5.setName("Cron_ProcesoEnvioFacturasClientes_PorHora");

    // PROCESO DE ENVIO DE CIFRAS CONTROL PRIME Y COMISIONES
    final JobDetail job6 =
        new JobDetail("ProcesoEnvioCifrasControl_Prime_Com", ProcesoEnvioCifrasControlPrimeCom.class);
    job6.setJobDataMap(mapJob);
    final Trigger trigger6 = TriggerUtils.makeDailyTrigger(10, 0);
    trigger6.setName("Cron_ProcesoEnvioCifrasControlPrimeCom");

    try {
      final Scheduler sched = schedFact.getScheduler();
      sched.start();

      if ("Desarrollo".equals(Constantes.AMBIENTE)) {
        log.debug("Ambiente de desarrollo - Timers desactivados");
      } else {
        sched.scheduleJob(job1, trigger1);
        sched.scheduleJob(job2, trigger2);
        sched.scheduleJob(job3, trigger3);
        sched.scheduleJob(job4, trigger4);
        sched.scheduleJob(job5, trigger5);
        sched.scheduleJob(job6, trigger6);
      }

    } catch (final SchedulerException se) {
      log.error(se.getMessage(), se);
    }
  }
}