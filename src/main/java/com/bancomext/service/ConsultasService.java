package com.bancomext.service;

import com.bancomext.dao.ConsultasDAO;
import com.bancomext.service.mapping.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class ConsultasService {

  public static BigDecimal getMax(final String tabla, final String campo) {
    return ConsultasDAO.getMax(tabla, campo);
  }

  public static String getMaxString(final String tabla, final String campo) {
    return ConsultasDAO.getMaxString(tabla, campo);
  }

  public static Date getMaxDate(final String tabla, final String campo) {
    return ConsultasDAO.getMaxDate(tabla, campo);
  }

  public static List<VoLlaveValor> getCombo(final String tabla,
                                            final String campoLlave,
                                            final String campoValor,
                                            final String condiciones,
                                            final String orden) {
    return ConsultasDAO.getCombo(tabla, campoLlave, campoValor, condiciones, orden, false);
  }

  public static List<VoLlaveValor> getComboDistinct(final String tabla,
                                                    final String campoLlave,
                                                    final String campoValor,
                                                    final String condiciones,
                                                    final String orden) {
    return ConsultasDAO.getCombo(tabla, campoLlave, campoValor, condiciones, orden, true);
  }


  public static String getGrupoAdministradores() {
    return ConsultasDAO.getGrupoAdministradores();
  }

  public static String getStringParametro(final String idParametro) {
    return ConsultasDAO.getStringParametro(idParametro);
  }

  public static List<IcAdClientStatementHistory> getReporteSaldosFIU(final String condicion) {
    return ConsultasDAO.getReporteSaldosFIU(condicion);
  }

  public static List<FiCfdCifrasVerificacion> getCifrasControlVerificacion(final Date fechaIni, final Date fechaFin) {
    return ConsultasDAO.getCifrasControlVerificacion(fechaIni, fechaFin);
  }

  //  CONSULTA PARA OBTENER LAS CUENTAS DEL SICAV.
  public static long getCtaSicav(final int cta, final int scta, final int sscta, final int ssscta) {
    return ConsultasDAO.getCtaSicav(cta, scta, sscta, ssscta);
  }

  // CONSULTA PARA REPORTE DE FACTURAS MENSUAL
  public static List<VoReporteFacturasMensual> getReporteFacturas(final Date fechaInicio, final Date fechaFin,
                                                                  final String codigoCliente) {
    return ConsultasDAO.getReporteFacturas(fechaInicio, fechaFin, codigoCliente);
  }

  // QUERY PARA OBTENER EL MONTO POR MONEDA
  public static List<VoSaldosMoneda> getSaldosMoneda() {
    return ConsultasDAO.getSaldosMoneda();
  }

  // CONSULTA PARA LLENAR LA PANTALLA PRINCIPAL DEL PROCESO MANULA DE COFACTORES
  public static List<VoCofactoresTmp> getClasificacionCofactor() {
    return ConsultasDAO.getClasificacionCofactor();
  }

  // CONSULTA PARA OBTENER EL ESTATUS ACTUAL DE LAS FACTURAS
  public static List<VoSeguimientoEstatusFac> getStatusFacturas(final Date fechaInicio, final Date fechaFin) {
    return ConsultasDAO.getStatusFacturas(fechaInicio, fechaFin);
  }

  public static List<FiSeguimientoImx> getSeguimientoIMX(final Date fechaIni, final Date fechaFin, final String dias) {
    return ConsultasDAO.getSeguimientoIMX(fechaIni, fechaFin, dias);
  }


  // CONSULTA PARA OBTENER LOS CORREOS DE ENVIO CIFRAS CONTROL PARA PRIME Y COMISIONES
  public static List<VoCorreosCifrasPrimeComisiones> getCorreosCifrasPrimeCom(final String condicion) {
    return ConsultasDAO.getCorreosCifrasPrimeCom(condicion);
  }

  // CONSULTA PARA DE HISTORICO DE FOLIOS ENCABEZADO PARA OBTENER IMPORTE Y NOMBRE DE CLIENTE PARA ENVIO DE FACTURAS PRIMA Y COMISIONES
  public static List<VoHistoricoEncabezado> getHistoricoEncabezado(final int codigoCliente, final Date fechaValor,
                                                                   final int folio) {
    return ConsultasDAO.getHistoricoEncabezado(codigoCliente, fechaValor, folio);
  }

  // CONSULTA PARA DE HISTORICO DE FOLIOS ENCABEZADO PARA OBTENER IMPORTE Y NOMBRE DE CLIENTE PARA ENVIO DE REPORTE DE RESUMEN DE FACTURACION
  public static List<VoHistoricoEncabezado> getResumenFacturacion(final Date fechaInicio, final Date fechaFin) {
    return ConsultasDAO.getResumenFacturacion(fechaInicio, fechaFin);
  }

  // CONSULTA PARA OBTENER EL FOLIO, FECHA ASIGNACION Y CODIGO_CLIENTE DE PRIME Y COMISIONES
  public static List<VoHistoricoPrimeCom> getHistoricoPrimeCom(final String folio, final String serie,
                                                               final String consulta) {
    return ConsultasDAO.getHistoricoPrimeCom(folio, serie, consulta);
  }

  // CONSULTA PARA OBTENER CORREOS DE ENVIO CFDI (COMISIONES)
  public static List<VoTablaElemento> getCorreosCFDIComisiones(final int codigoCliente) {
    return ConsultasDAO.getCorreosCFDIComisiones(codigoCliente);
  }

  // CONSULTA PARA OBTENER CORREOS DE ENVIO CFDI (PRIME)
  public static List<VoTablaElemento> getCorreosCFDIPrime(final int codigoProveedor) {
    return ConsultasDAO.getCorreosCFDIPrime(codigoProveedor);
  }

  // CONSULTA PARA OBTENER LA CIFRAS CONTROL DE COMISIONES Y PRIME (SUCRE)
  public static List<VoCifrasPrimeComisiones> getCifrasPrimeCom(final Date fechaEmision, final String producto) {
    return ConsultasDAO.getCifrasPrimeCom(fechaEmision, producto);
  }


  // CONSULTA PARA OBTENER EL ESTATUS DE LA CIFRAS PRIME - COMISIONES
  public static List<VoCifrasPrimeComisiones> getCifrasStatusPrimeCom(final Date fechaEmision, final String producto,
                                                                      final String status) {
    return ConsultasDAO.getCifrasStatusPrimeCom(fechaEmision, producto, status);
  }

  // CONSULTA PARA OBTENER EL CUERPO DE LOS CORREOS PARA PRIME Y COMISIONES
  public static List<VoTablaElemento> getCuerpoCorreosPrimCom() {
    return ConsultasDAO.getCuerpoCorreosPrimCom();
  }

  public static void updateCifrasStatusPrimeCom(final Date fechaEmision, final String producto, final String status) {
    ConsultasDAO.updateCifrasStatusPrimeCom(fechaEmision, producto, status);
  }

  //	CONSULTA PARA ACTUALIZAR DEL HISTORICO DE FOLIOS LA BANDERA DE ENVIADO_CLIENTE
  public static void updateHistoricoFoliosPrimeCom(final String numeroFolio, final String serie) {
    ConsultasDAO.updateHistoricoFoliosPrimeCom(numeroFolio, serie);
  }

  //	CONSULTA PARA OBTENER LOS DATOS DE LAS POLIZAS EXTEMPORANEAS
  public static List<VoPolizasExtemporaneo> getPolizasExtemporaneo() {
    return ConsultasDAO.getPolizasExtemporaneo();
  }

  //	CONSULTA PARA OBTENER EL DETALLE DE LAS POLIZAS EXTEMPORANEAS
  public static List<VoPolizasExtemporaneoDetalle> getPolizasExtemporaneoDetalle(final String opCode,
                                                                                 final String descripcion,
                                                                                 final int poliza) {
    return ConsultasDAO.getPolizasExtemporaneoDetalle(opCode, descripcion, poliza);
  }

  // CONSULTA QUE NOS INDICA EL NOMBRE DE LOS COFACTORES FALTANTES
  public static List<VoTablaElemento> getCofactoresFaltantes() {
    return ConsultasDAO.getCofactoresFaltantes();
  }

  // CONSULTA PARA MOSTRAR LAS CIFRAS TOTALES DE STATUS FACTURAS
  public static List<VoCifrasTotalesEstatusFac> getCifrasTotalesEstatusFac(final Date fechaInicio, final Date fechaFin) {
    return ConsultasDAO.getCifrasTotalesEstatusFac(fechaInicio, fechaFin);
  }

  // CONSULTA PARA OBTENER DATOS DE LA TABLA DE VERIFICACION
  public static List<FiCfdVerificacion> getVerificacion(final Date fecha, final boolean esAutomatico,
                                                        final int diasProceso) {
    return ConsultasDAO.getVerificacion(fecha, esAutomatico, diasProceso);
  }

  // CONSULTA PARA OBTENER LOS DATOS PARA LA CANCELACION EN EL PROCESO MANUAL
  public static List<FiCfdVerificacion> getCancelacion(final Date fechaValorIni, final Date fechaValorFin,
                                                       final Date fechaGeneraIni, final Date fechaGeneraFin) {
    return ConsultasDAO.getCancelacion(fechaValorIni, fechaValorFin, fechaGeneraIni, fechaGeneraFin);
  }

  // UPDATE PARA ACTUALIZA LA TABLA DE ENCABEZADO PARA LA GENERACION DE FACTURAS
  public static int updateEncabezado(final FiFacturasEncabezado beanEncabezado) {
    return ConsultasDAO.updateEncabezado(beanEncabezado);
  }

  public static List<FiCfdCifrasCtrl> getListCifras(final Date fechaValor, final int dias) {
    return ConsultasDAO.getCifrasCtrl(fechaValor, dias);
  }

}
