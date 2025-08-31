package com.bancomext.web.utils;

import com.bancomext.service.GenericService;
import com.bancomext.service.ServiceLocator;
import com.bancomext.service.mapping.FiLogErrores;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class ExpireQuotationJob implements Job {

  private static final Logger log = LogManager.getLogger(ExpireQuotationJob.class);

  /**
   * Ejecucion de actualizacion
   */
  @Override
  public void execute(final JobExecutionContext arg0) {

    log.info("Job Borrado directorios temporales..");
    final File exportar = new File("IMX/exportar/");
    final File importar = new File("IMX/importar/");

    final File in = new File("IMX/IN/");
    final File out = new File("IMX/OUT/");
    final File outErr = new File("IMX/OUT_ERR/");
    final SimpleDateFormat formatoddMMyyyy = new SimpleDateFormat("dd/MM/yyyy");

    try {
      boolean k = false;

      log.info("Borrando Carpetas :" +
          importar.getAbsolutePath() + " con untotal de archivos: " + Objects.requireNonNull(importar.listFiles()).length);
      while (Objects.requireNonNull(importar.listFiles()).length > 0) {
        for (int i = 0; i < Objects.requireNonNull(importar.listFiles()).length; i++) {
          k = Objects.requireNonNull(importar.listFiles())[i].delete();
        }
      }

      log.info("Borrando Carpetas :" +
          exportar.getAbsolutePath() + " con untotal de archivos: " + Objects.requireNonNull(exportar.listFiles()).length);
      while (Objects.requireNonNull(exportar.listFiles()).length > 0) {
        for (int i = 0; i < Objects.requireNonNull(exportar.listFiles()).length; i++) {
          k = Objects.requireNonNull(exportar.listFiles())[i].delete();
        }
      }

      log.info("Borrando Carpetas :" +
          in.getAbsolutePath() + " con untotal de archivos: " + Objects.requireNonNull(in.listFiles()).length);
      while (Objects.requireNonNull(in.listFiles()).length > 0) {
        for (int i = 0; i < Objects.requireNonNull(in.listFiles()).length; i++) {
          k = Objects.requireNonNull(in.listFiles())[i].delete();
        }
      }

      log.info("Borrando Carpetas :" +
          out.getAbsolutePath() + " con untotal de archivos: " + Objects.requireNonNull(out.listFiles()).length);
      while (Objects.requireNonNull(out.listFiles()).length > 0) {
        for (int i = 0; i < Objects.requireNonNull(out.listFiles()).length; i++) {
          k = Objects.requireNonNull(out.listFiles())[i].delete();
        }
      }

      log.info("Borrando Carpetas :" +
          outErr.getAbsolutePath() + " con untotal de archivos: " + Objects.requireNonNull(outErr.listFiles()).length);
      while (Objects.requireNonNull(outErr.listFiles()).length > 0) {
        for (int i = 0; i < Objects.requireNonNull(outErr.listFiles()).length; i++) {
          k = Objects.requireNonNull(outErr.listFiles())[i].delete();
        }
      }
      log.info(k);

      log.info("Elimina registros en FiLogErrores generados hasta 8 dias hacia atras");

      final GenericService genericService = ServiceLocator.getGenericService();

      @SuppressWarnings("unchecked")
      //Elimina registros generados hasta 8 dias hacia atras
      final List<FiLogErrores> logError =
          (List<FiLogErrores>) genericService.get("FiLogErrores", " trunc(fechaCreacion) <= to_date('" +
              formatoddMMyyyy.format(new Date()) + "','dd/mm/yyyy')-8 ", " Order by fechaCreacion asc ");
      for (FiLogErrores log : logError) {
        genericService.delete(log);
      }

    } catch (Exception ex) {
      log.error(ex.getMessage(), ex);
    }

    log.info("Termina ExpireQuotationJob..");
  }
}