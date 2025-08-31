package com.bancomext.web.utils;

import com.bancomext.service.ConsultasService;
import com.bancomext.service.GenericService;
import com.bancomext.service.ServiceLocator;
import com.bancomext.service.mapping.FiCatCuentasSicav;
import com.bancomext.service.mapping.FiCorreosPorReporte;
import com.bancomext.service.mapping.IcAdClientStatementHistory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class ProcesoGeneraReportesJob implements Job {
  private static final Logger log = LogManager.getLogger(ProcesoGeneraReportesJob.class);

  @SuppressWarnings("unchecked")
  private static void generaSaldosFIU() {
    UtileriasReportesExcel generaExcel = new UtileriasReportesExcel();
    File archivo;

    final SimpleDateFormat formatoddMMyyyy = new SimpleDateFormat("dd/MM/yyyy");
    try {
      log.info("Inicia Generacion Reporte Saldos FIU");

      List<String> headers = Arrays.asList("Numero Contrato", "Fecha", "Importe Cubierto", "FIU Efectivo",
          "Importe Financiable Max", "Portafolio", "Disponible");
      List<String> listaNombresColumnas = Arrays.asList("contractNumber", "dt04Dt", "coveredAmount", "fiucash",
          "fundableAmount", "portfolio", "availability");

      String condicion = " TRUNC(DT04_DT) =to_date('" + formatoddMMyyyy.format(new Date()) + "','dd/mm/yyyy')-1 ";

      final List<IcAdClientStatementHistory> listaSaldos = ConsultasService.getReporteSaldosFIU(condicion);
      final List<Object> listaSaldosO = new ArrayList<>(listaSaldos);
      archivo = generaExcel.generaTablaExcelArchivo("IMX/exportar/", "ReporteSaldosFIU",
          "Saldos", 0, headers, listaNombresColumnas, listaSaldosO);

      final GenericService genericService = ServiceLocator.getGenericService();
      enviarCorreo(genericService.get("FiCorreosPorReporte",
              " id.idGrupoReporte=2 ", " Order by id.email desc "),
          "Proceso Reporte Saldos FIU " + formatoddMMyyyy.format(new Date()), UtileriaCorreo.contenidoCorreosPorReporte(),
          archivo);

      log.info("Termina Enviar Correo");

    } catch (Exception ex) {
      log.error("Error Creando Reporte Saldos FIU" + ex.getMessage(), ex);
      Utilerias.guardarMensajeLog("ProcesoGeneraReportesJob", "generaSaldosFIU", ex,
          "Error Creando Reporte Saldos FIU", null);
    }
  }

  @SuppressWarnings("unchecked")
  private static void generaComparativo() {

    final String filePath = "IMX/exportar/";
    String nombreArchivo = "ReporteComparativo";
    nombreArchivo = UtileriasReportesExcel.borrarExportsExistentes(filePath, nombreArchivo);
    final File archivo = new File(filePath + nombreArchivo + ".xls");
    final boolean k = archivo.setWritable(true);
    log.debug(k);

    final UtileriasReportesExcel generaExcel = new UtileriasReportesExcel();
    final GenericService genericService = ServiceLocator.getGenericService();

    try (final HSSFWorkbook wb = new HSSFWorkbook()) {
      final HSSFSheet sheet = wb.createSheet();
      final SimpleDateFormat formatoddMMyyyy = new SimpleDateFormat("dd/MM/yyyy");

      log.info("Inicia Exportacion Archivo Excel");

      final String ctaImp = ConsultasService.getStringParametro("cta_Imp");
      final String sctaImp = ConsultasService.getStringParametro("scta_Imp");
      final String ssctaImp = ConsultasService.getStringParametro("sscta_Imp");
      final String sssctaImp = ConsultasService.getStringParametro("ssscta_Imp");
      final String ctaExp = ConsultasService.getStringParametro("cta_Exp");
      final String sctaExp = ConsultasService.getStringParametro("scta_Exp");
      final String ssctaExp = ConsultasService.getStringParametro("sscta_Exp");
      final String sssctaExp = ConsultasService.getStringParametro("ssscta_Exp");
      final String ctaFiu = ConsultasService.getStringParametro("cta_Fiu");
      final String sctaFiu = ConsultasService.getStringParametro("scta_Fiu");
      final String ssctaFiu = ConsultasService.getStringParametro("sscta_Fiu");
      final String sssctaFiu = ConsultasService.getStringParametro("ssscta_Fiu");

      log.info("El archivo fue generado y su nombre es :::: " + archivo.getName());

      //Comparativo SICAV AD
      final HSSFRow currentRow = sheet.createRow((short) 1);
      currentRow.createCell(0).setCellType(Cell.CELL_TYPE_STRING);
      currentRow.createCell(0).setCellValue("Fecha :" + formatoddMMyyyy.format(new Date()));
      log.info("ProcesoGeneraReportesJob, exportarExcel_Comparativo, Fecha" + formatoddMMyyyy.format(new Date()));

      final HSSFRow rowCuentaImp = sheet.createRow((short) 2);
      rowCuentaImp.createCell(0).setCellType(Cell.CELL_TYPE_STRING);
      rowCuentaImp.createCell(0).setCellValue(
          "Cuenta :" + ctaImp + "-" + sctaImp + "-" + ssctaImp + "-" + sssctaImp);

      log.info("ProcesoGeneraReportesJob, exportarExcel_Comparativo, CuentaIMP" +
          ctaImp + "-" + sctaImp + "-" + ssctaImp + "-" + sssctaImp);

      final List<String> headers =
          Arrays.asList("Nombre Cliente", "Campo A", "Contrato", "Cuenta", "IMX", "Contabilidad", "Diferencia");
      final List<String> listaNombresColumnas =
          Arrays.asList("nombreCliente", "id.campoA", "id.contrato", "cuenta", "montoImx", "montoSicav", "montoDif");

      final List<Object> listacontenido = genericService.get("FiHisRepComparativo", " cta='" +
          ctaImp + "' And scta='" + sctaImp + "' and sscta='" + ssctaImp + "' and ssscta='" + sssctaImp +
          "' and Trunc(id.fechaProceso)=trunc(SYSDATE) ", "order by cuenta, nombreCliente, contrato ");

      generaExcel.generaTablaExcel(wb, sheet, 3, headers, listaNombresColumnas, listacontenido);

      final List<Object> listacontenido2 = genericService.get("FiHisRepComparativo",
          " cta='" + ctaExp + "' And scta='" + sctaExp + "' and sscta='" + ssctaExp + "' and ssscta='" +
              sssctaExp + "' and Trunc(id.fechaProceso)=trunc(SYSDATE) ", "order by id.fechaProceso desc ");

      final HSSFRow rowCuentaExp = sheet.createRow((short) listacontenido.size() + 5);
      rowCuentaExp.createCell(0).setCellType(Cell.CELL_TYPE_STRING);
      rowCuentaExp.createCell(0).setCellValue(
          "Cuenta :" + ctaExp + "-" + sctaExp + "-" + ssctaExp + "-" + sssctaExp);
      log.info("ProcesoGeneraReportesJob, exportarExcel_Comparativo, CuentaEXP" + ctaExp + "-" +
          sctaExp + "-" + ssctaExp + "-" + sssctaExp);

      generaExcel.generaTablaExcel(
          wb, sheet, listacontenido.size() + 6, headers, listaNombresColumnas, listacontenido2);
      final List<Object> listacontenido3 = genericService.get("FiHisRepComparativo", " cta='" + ctaFiu +
          "' and scta='" + sctaFiu + "' and sscta='" + ssctaFiu + "' and ssscta='" + sssctaFiu +
          "' and Trunc(id.fechaProceso)=trunc(SYSDATE) ", "order by cuenta, nombreCliente, contrato ");

      final HSSFRow rowCuentaFiu = sheet.createRow(
          (short) listacontenido.size() + 5 + listacontenido2.size() + 3);
      rowCuentaFiu.createCell(0).setCellType(Cell.CELL_TYPE_STRING);
      rowCuentaFiu.createCell(0).setCellValue(
          "Cuenta :" + ctaFiu + "-" + sctaFiu + "-" + ssctaFiu + "-" + sssctaFiu);

      generaExcel.generaTablaExcel(wb, sheet, listacontenido.size() + 5 + listacontenido2.size() + 4,
          headers, listaNombresColumnas, listacontenido3);
      int inicio = listacontenido.size() + 5 + listacontenido2.size() + 3 + listacontenido3.size() + 3;

      for (FiCatCuentasSicav cuentassicav : (List<FiCatCuentasSicav>)
          genericService.get("FiCatCuentasSicav", "", " order by id.cta desc")) {

        final HSSFRow rowCuenta = sheet.createRow((short) inicio);
        rowCuenta.createCell(0).setCellType(Cell.CELL_TYPE_STRING);
        rowCuenta.createCell(0).setCellValue("Cuenta :" + cuentassicav.getId().getCta() + "-" +
            cuentassicav.getId().getScta() + "-" + cuentassicav.getId().getSscta() + "-" +
            cuentassicav.getId().getSsscta());

        final List<Object> listacontenido4 = genericService.get("FiHisRepComparativo",
            " cta='" + cuentassicav.getId().getCta() + "' and scta='" + cuentassicav.getId().getScta() +
                "' and sscta='" + cuentassicav.getId().getSscta() + "' and ssscta='" + cuentassicav.getId().getSsscta() +
                "' and Trunc(id.fechaProceso)=trunc(SYSDATE)", "order by cuenta, nombreCliente, contrato ");
        generaExcel.generaTablaExcel(wb, sheet, inicio + 1, headers, listaNombresColumnas, listacontenido4);
        inicio = inicio + 4 + listacontenido4.size();
      }

      try (final FileOutputStream out = new FileOutputStream(archivo)) {
        wb.write(out);
      }
      log.info("Termina Exportacion Archivo Excel");

      enviarCorreo(
          genericService.get("FiCorreosPorReporte", " id.idGrupoReporte=1 ", " Order by id.email desc "),
          "Proceso Reporte Comparativo " + formatoddMMyyyy.format(new Date()), UtileriaCorreo.contenidoCorreosPorReporte(),
          archivo
      );
      log.info("Termina Enviar Correo");

    } catch (Exception ex) {
      log.error("Error Creando Archivo Excel" + ex.getMessage(), ex);
      Utilerias.guardarMensajeLog("ProcesoGeneraReportesJob", "exportarExcel_Comparativo", ex,
          "Error Creando Archivo Excel", null);
    }
  }

  private static void enviarCorreo(final List<FiCorreosPorReporte> listaCorreos, final String asunto,
                                   final String contenido, final File adjunto) {
    if (listaCorreos != null && !listaCorreos.isEmpty()) {
      final InternetAddress[] to = new InternetAddress[listaCorreos.size()];
      for (int i = 0; i < listaCorreos.size(); i++) {
        try {
          if ("Desarrollo".equals(Constantes.AMBIENTE)) {
            to[i] = new InternetAddress(Constantes.MAIL_DESARROLLO);
          } else {
            to[i] = new InternetAddress(listaCorreos.get(i).getId().getEmail());
            log.info("Correo: " + listaCorreos.get(i).getId().getEmail());
          }
        } catch (AddressException ae) {
          log.error(ae.getMessage(), ae);
        }
        UtileriaCorreo.enviarCorreo(to, null, asunto, contenido, "FACTORAJE", adjunto);
      }
    }
  }

  @Override
  public void execute(final JobExecutionContext arg0) {
    log.info("Job Proceso Genera Reportes .." +
        "\nLa ruta del contexto es: /" +
        "\nruta directorio reportes: exportar/");
    generaComparativo();
    generaSaldosFIU();
    log.info("Job Proceso Genera Reportes concluido...");
  }

}
