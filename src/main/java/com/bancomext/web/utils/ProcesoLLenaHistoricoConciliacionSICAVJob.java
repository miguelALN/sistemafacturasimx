package com.bancomext.web.utils;

import com.bancomext.service.ProcedimientosService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

public class ProcesoLLenaHistoricoConciliacionSICAVJob implements Job {
  private static final Logger log = LogManager.getLogger(ProcesoLLenaHistoricoConciliacionSICAVJob.class);

  @Override
  public void execute(final JobExecutionContext arg0) {
    log.info("Job Genera Historico Conciliacion SICAV ...");
    final String respuesta = ProcedimientosService.procesoComparativoSicav();
    log.info("proceso concluido con mensaje:" + respuesta);
  }
}