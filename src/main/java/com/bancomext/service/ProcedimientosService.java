package com.bancomext.service;

import com.bancomext.dao.ProcedimientosDAO;

import java.util.Date;

public class ProcedimientosService {

  // PROCEDIMIENTO PARA ENVIAR EL USUARIO DE JAVA A SESSION DE ORACLE
  public static void imxPRegistraUsuario(final String usuario) {
    ProcedimientosDAO.imxPRegistraUsuario(usuario);
  }

  public static String cfdiMain(final Date fechaEmision, final String tipoGeneracion) {
    return ProcedimientosDAO.cfdiMain(fechaEmision, tipoGeneracion);
  }

  public static String puStatusFolios(final String producto,
                                      final Date fechaEmision,
                                      final String status,
                                      final int numAcreditado,
                                      final String numContrato,
                                      final String secuencia) {
    return ProcedimientosDAO.puStatusFolios(producto, fechaEmision, status, numAcreditado, numContrato, secuencia);
  }

  public static String prFolioSatCredito(final String folio, final String producto, final Date fechaAsignacion) {
    return ProcedimientosDAO.prFolioSatCredito(folio, producto, fechaAsignacion);
  }

  public static String prEstatusCredito(final String folio,
                                        final String seEnvia,
                                        final Date fechaAsignacion,
                                        final Long codigoCliente,
                                        final String statusFolio,
                                        final String producto,
                                        final String codigoLineaCredito,
                                        final Long versionInfo) {
    return ProcedimientosDAO.prEstatusCredito(folio, seEnvia, fechaAsignacion, codigoCliente,
        statusFolio, producto, codigoLineaCredito, versionInfo);
  }

  public static String procesoComparativoSicav() {
    return ProcedimientosDAO.ejecutaSPSinParams("PKG_PROCESOS_REPORTES.PROCESO_COMPARATIVO_SICAV");
  }

  public static void cfdPTrama1H(final Date fechaEmision, final Date fechaProceso) {
    ProcedimientosDAO.cfdPTrama1H(fechaEmision, fechaProceso);
  }

  public static void organizaCifrasControl(final Date fechaProceso) {
    ProcedimientosDAO.organizaCifrasControl(fechaProceso);
  }


  // INSERTA O MODIFICA LOS COFACTORES
  public static String pModificaCofactor(final String campoB, final String nombreCof) {
    return ProcedimientosDAO.pModificaCofactor(campoB, nombreCof);
  }

  // GENERA CANCELACION DE FOLIOS CON CONCEPTOS INDEPENDIENTES
  public static String cancelaFolio(final String numeroFolio) {
    return ProcedimientosDAO.cancelaFolio(numeroFolio);
  }

  // GENERA VERIFICACION DE FOLIOS CON CONCEPTOS INDEPENDIENTES
  public static String pReprocesaInformacion(final Date fechaEmision, final String secuencia) {
    return ProcedimientosDAO.pReprocesaInformacion(fechaEmision, secuencia);
  }

  // PROCESO MANUAL DE CANCELACION DE LA PROVISION DE INTERESES
  public static String pCanProvIntManualCalcula() {
    return ProcedimientosDAO.ejecutaSPSinParams("PKG_ACCION_CONTABLE_MANUAL.P_CAN_PROV_INT_MANUAL_CALCULA");
  }

  public static String pCanProvIntManualCambios() {
    return ProcedimientosDAO.ejecutaSPSinParams("PKG_ACCION_CONTABLE_MANUAL.P_CAN_PROV_INT_MANUAL_CAMBIOS");
  }

  // PROCESO MANUAL DE CAPITALIZACION DE INTERESES
  public static String pCapIntManualCalcula() {
    return ProcedimientosDAO.ejecutaSPSinParams("PKG_ACCION_CONTABLE_MANUAL.P_CAP_INT_MANUAL_CALCULA");
  }

  public static String pCapIntManualCambios() {
    return ProcedimientosDAO.ejecutaSPSinParams("PKG_ACCION_CONTABLE_MANUAL.P_CAP_INT_MANUAL_CAMBIOS");
  }

  // INICIA PROCESOS RECLASIFICACION DE SALDO NEGATIVO
  public static String pFiuNegReclManualCalcula() {
    return ProcedimientosDAO.ejecutaSPSinParams("PKG_ACCION_CONTABLE_MANUAL.P_FIU_NEG_RECL_MANUAL_CALCULA");
  }

  public static String pFiuNegReclManualCambios() {
    return ProcedimientosDAO.ejecutaSPSinParams("PKG_ACCION_CONTABLE_MANUAL.P_FIU_NEG_RECL_MANUAL_CAMBIOS");
  }

  // INICIA PROCESOS RESTAURACION DE SALDO NEGATIVO
  public static String pFiuNegRestManualCalcula() {
    return ProcedimientosDAO.ejecutaSPSinParams("PKG_ACCION_CONTABLE_MANUAL.P_FIU_NEG_REST_MANUAL_CALCULA");
  }

  public static String pFiuNegRestManualCambios() {
    return ProcedimientosDAO.ejecutaSPSinParams("PKG_ACCION_CONTABLE_MANUAL.P_FIU_NEG_REST_MANUAL_CAMBIOS");
  }

  public static String pCofCampoBManualCalcula() {
    return ProcedimientosDAO.ejecutaSPSinParams("PKG_ACCION_CONTABLE_MANUAL.P_COF_CAMPOB_MANUAL_CALCULA");
  }

  public static String pCofCampoBManualCambios() {
    return ProcedimientosDAO.ejecutaSPSinParams("PKG_ACCION_CONTABLE_MANUAL.P_COF_CAMPOB_MANUAL_CAMBIOS");
  }

  public static String pAccionContableAuto() {
    return ProcedimientosDAO.ejecutaSPSinParams("P_ACCION_CONTABLE_AUTO");
  }

  // AGRUPACION DE POLIZAS
  public static String comparaMontosPorMoneda() {
    return ProcedimientosDAO.ejecutaSPSinParams("PKG_AGRUPACION_DE_POLIZAS.COMPARA_MONTOS_X_MONEDA");
  }

  // PROCESO AGRUPACION DE POLIZAS EXTEMPORANEO
  public static String pAgrupaPolExtempor() {
    return ProcedimientosDAO.ejecutaSPSinParams("PKG_AGRUPA_POLIZAS_EXTEMPOR.P_AGRUPA_POL_EXTEMPOR");
  }

  // PROCESO AGRUPACION DE POLIZAS EXTEMPORANEO CAMBIOS
  public static String pCambiosExtempor() {
    return ProcedimientosDAO.ejecutaSPSinParams("PKG_AGRUPA_POLIZAS_EXTEMPOR.P_CAMBIOS_EXTEMPOR");
  }


}