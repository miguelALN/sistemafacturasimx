package com.bancomext.dao;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;

public class ProcedimientosDAO {

  private static final Logger log = LogManager.getLogger(ProcedimientosDAO.class);

  public static void imxPRegistraUsuario(final String usuario) {
    log.info("-----Ejecucion de PKG_FI_UTIL_IMX.IMX_P_REGISTRA_USUARIO-----");
    final String comando = "{call PKG_FI_UTIL_IMX.IMX_P_REGISTRA_USUARIO(?)}";
    try (final Connection con = DBConnection.getConnFactIMX();
         final CallableStatement cs = con.prepareCall(comando)) {
      cs.setString(1, usuario);
      cs.execute();
    } catch (SQLException sqle) {
      log.info("ocurrio un error:" + sqle.getMessage(), sqle);
    }
  }

  public static String puStatusFolios(final String producto,
                                      final Date fechaEmision,
                                      final String status,
                                      final int acreditado,
                                      final String contrato,
                                      final String secuencia) {
    String r = "";

    log.info("-----Ejecucion de PKG_FACTIMX_VERIFICACION.PU_STATUS_FOLIOS-----" +
        "\n-----Entradas-----\nstatus =" + status + "\nfechaEmision =" + fechaEmision +
        "\nacreditado =" + acreditado + "\ncontrato =" + contrato + "\nsecuencia =" + secuencia);

    final String comando = "{call PKG_FACTIMX_VERIFICACION.PU_STATUS_FOLIOS(?,?,?,?,?,?,?)}";

    try (final Connection con = DBConnection.getConnFactIMX();
         final CallableStatement cs = con.prepareCall(comando)) {
      cs.setDate(1, new java.sql.Date(fechaEmision.getTime()));
      cs.setString(2, status);
      cs.setInt(3, acreditado);
      cs.setString(4, contrato);
      cs.registerOutParameter(5, Types.INTEGER);
      cs.registerOutParameter(6, Types.VARCHAR);
      cs.setInt(7, Integer.parseInt(secuencia));
      cs.execute();
      r = cs.getString(6);
      log.info("-----Salidas-----\nNumero de salida es ::::" + cs.getInt(5) +
          "\n la salida de la verificacion para el producto::: " + producto + "::Secuncia :::: " +
          ":::fecha emision::::Mensaje Salida es:::::" + r);
    } catch (SQLException sqle) {
      log.error("ocurrio un error:" + sqle.getMessage(), sqle);
    }
    return r;
  }


  public static String cfdiMain(final Date fechaEmision, final String tipoGeneracion) {
    String r;
    log.info("-----Ejecucion de PKG_FI_FAC_CFDI.MAIN-----\n-----Entradas-----\nfecha =" + fechaEmision + " tipoGeneracion= " + tipoGeneracion);

    final String comando = "{call PKG_FI_FAC_CFDI.MAIN(?,?,?)}";

    try (final Connection con = DBConnection.getConnFactIMX();
         final CallableStatement cs = con.prepareCall(comando)) {
      cs.setDate(1, new java.sql.Date(fechaEmision.getTime()));
      cs.setString(2, tipoGeneracion);
      cs.registerOutParameter(3, Types.VARCHAR);
      cs.execute();
      r = cs.getString(3);
      log.info("-----Salidas----- " + r);
    } catch (SQLException sqle) {
      log.error("ocurrio un error:" + sqle.getMessage(), sqle);
      r = sqle.getMessage();
    }
    return r;
  }

  public static String prFolioSatCredito(final String folio, final String producto, final Date fechaAsignacion) {
    String r = "";
    log.info("-----Ejecucion de PKG_VERIFICA_ESTATUS.PR_FOLIO_SAT_CREDITO-----\n-----Entradas-----" +
        "\nserie = IMX\nFolio =" + folio + "\nproducto =" + producto +
        "\nFechaAsignacion =" + fechaAsignacion);

    final String comando = "{call PKG_VERIFICA_ESTATUS.PR_FOLIO_SAT_CREDITO( IMX, ?, ?, ?, ?)}";

    try (final Connection con = DBConnection.getConnFactIMX();
         final CallableStatement cs = con.prepareCall(comando)) {
      cs.setString(1, folio);
      cs.setString(2, producto);
      cs.setDate(3, new java.sql.Date(fechaAsignacion.getTime()));
      cs.registerOutParameter(4, Types.VARCHAR);
      cs.execute();
      r = cs.getString(5);
      log.info("-----Salidas----- " + r);
    } catch (SQLException sqle) {
      log.error("ocurrio un error:" + sqle.getMessage(), sqle);
    }
    return r;
  }

  public static String prEstatusCredito(final String folio,
                                        final String seEnvia,
                                        final Date fechaAsignacion,
                                        final Long codigoCliente,
                                        final String statusFolio,
                                        final String producto,
                                        final String codigoLineaCredito,
                                        final Long versionInfor) {
    String r = "";
    log.info("-----Ejecucion de PKG_VERIFICA_ESTATUS.PR_ESTATUS_CREDITO-----\n-----Entradas-----" +
        "\nserie =" + "IMX" + "\nFolio =" + folio + "\nseEnvia =" + seEnvia + "\nfechaAsignacion =" + fechaAsignacion +
        "\ncodigoCliente =" + codigoCliente + "\nstatusFolio =" + statusFolio + "\nproducto =" + producto +
        "\ncodigoLineaCredito =" + codigoLineaCredito + "\nversionInfor =" + versionInfor);

    final String comando = "{? = call PKG_VERIFICA_ESTATUS.PR_ESTATUS_CREDITO(?, IMX, ?, ?, ?, ?, ?, ?, ?)}";

    try (final Connection con = DBConnection.getConnFactIMX();
         final CallableStatement cs = con.prepareCall(comando)) {
      cs.registerOutParameter(1, Types.VARCHAR);
      cs.setString(2, folio);
      cs.setString(3, seEnvia);
      cs.setString(4, codigoCliente.toString());
      cs.setString(5, statusFolio);
      cs.setString(6, producto);
      cs.setDate(7, new java.sql.Date(fechaAsignacion.getTime()));
      cs.setString(8, codigoLineaCredito);
      cs.setInt(9, versionInfor.intValue());
      cs.executeUpdate();
      r = cs.getString(1);
      log.info("-----Salidas----- " + r);
    } catch (SQLException sqle) {
      log.error("ocurrio un error:" + sqle.getMessage(), sqle);
    }
    return r;
  }

  public static void cfdPTrama1H(final Date fechaEmision, final Date fechaProceso) {
    log.info("INTO Ejecucion de PKG_FACTIMX_VERIFICACION.cfd_p_Trama_1H-----\n-----Entradas-----" +
        "\nfechaEmision =" + fechaEmision + "\nfechaProceso =" + fechaProceso);

    final String comando = "{call PKG_FACTIMX_VERIFICACION.cfd_p_Trama_1H(?,?,?,?,?,?,?)}";

    try (final Connection con = DBConnection.getConnFactIMX();
         final CallableStatement cs = con.prepareCall(comando)) {
      cs.setDate(1, new java.sql.Date(fechaProceso.getTime()));
      cs.setDate(2, new java.sql.Date(fechaEmision.getTime()));
      cs.registerOutParameter(3, Types.VARCHAR);
      cs.setString(4,"");
      cs.setInt(5,0);
      cs.setInt(6,0);
      cs.setString(7,"");
      cs.execute();
      log.info("-----Salidas-----\n" + cs.getString(3));
    } catch (SQLException sqle) {
      log.error("ocurrio un error:" + sqle.getMessage(), sqle);
      sqle.printStackTrace();
    }
  }

  public static void organizaCifrasControl(final Date fechaProceso) {
    log.info("-----Ejecucion de PKG_FI_FAC_CFDI.ORGANIZACIFRASCONTROL-----\n-----Entradas-----" +
        "\nfechaEmision =" + fechaProceso);

    final String comando = "{call PKG_FI_FAC_CFDI.ORGANIZACIFRASCONTROL(?,?,?)}";

    try (final Connection con = DBConnection.getConnFactIMX();
         final CallableStatement cs = con.prepareCall(comando)) {
      cs.setDate(1, new java.sql.Date(fechaProceso.getTime()));
      cs.registerOutParameter(2, Types.INTEGER);
      cs.registerOutParameter(3, Types.VARCHAR);
      cs.execute();
      log.info("-----Salidas----- " + cs.getInt(2) + " " + cs.getString(3));
    } catch (SQLException sqle) {
      log.error("ocurrio un error:" + sqle.getMessage(), sqle);
    }
  }

  // PROCESO QUE INSERTA O MODIFICA LOS COFACTORES
  public static String pModificaCofactor(final String campoB, final String nombreCof) {
    String r = "";
    log.info("-----Ejecucion de PKG_FI_UTIL_IMX_P_MODIFICA_COFACTOR.P_MODIFICA_COFACTOR-----");

    final String comando = "{call PKG_FI_UTIL_IMX.P_MODIFICA_COFACTOR (?,?,?)}";

    try (final Connection con = DBConnection.getConnFactIMX();
         final CallableStatement cs = con.prepareCall(comando)) {
      cs.setString(1, campoB);
      cs.setString(2, nombreCof);
      cs.registerOutParameter(3, Types.VARCHAR);
      cs.execute();
      r = cs.getString(3);
      log.info("-----Salidas----- " + r);
    } catch (SQLException sqle) {
      log.error("ocurrio un error:" + sqle.getMessage(), sqle);
    }
    return r;
  }

  // GENERA CANCELACION
  public static String cancelaFolio(final String folio) {
    String r = "";
    log.info("-----Ejecucion de PKG_FACTIMX_VERIFICACION.CANCELA_FOLIO-----");

    final String comando = "{call PKG_FACTIMX_VERIFICACION.CANCELA_FOLIO (?,?)}";

    try (final Connection con = DBConnection.getConnFactIMX();
         final CallableStatement cs = con.prepareCall(comando)) {
      cs.setString(1, folio);
      cs.registerOutParameter(2, Types.VARCHAR);
      cs.execute();
      r = cs.getString(2);
      log.info("-----Salidas----- " + r);
    } catch (SQLException sqle) {
      log.error("ocurrio un error:" + sqle.getMessage(), sqle);
    }
    return r;
  }

  // GENERA VERIFICACION DE FOLIOS CON CONCEPTOS INDEPENDIENTES
  public static String pReprocesaInformacion(final Date fechaEmision, final String secuencia) {
    String r = "";
    log.info("-----Ejecucion de PKG_FACTIMX_VERIFICACION.CFD_P_REPROCESA_INFORMACION-----");
    final String comando = "{call PKG_FACTIMX_VERIFICACION.CFD_P_REPROCESA_INFORMACION (?,?,?)}";
    try (final Connection con = DBConnection.getConnFactIMX();
         final CallableStatement cs = con.prepareCall(comando)) {
      cs.setDate(1, new java.sql.Date(fechaEmision.getTime()));
      cs.setString(2, secuencia);
      cs.registerOutParameter(3, Types.VARCHAR);
      cs.execute();
      r = cs.getString(3);
      log.info("-----Salidas----- " + r);
    } catch (SQLException sqle) {
      log.error("ocurrio un error:" + sqle.getMessage(), sqle);
    }
    return r;
  }

  public static String ejecutaSPSinParams(final String procedure) {
    String r = "";
    log.info("-----Ejecucion de " + procedure + "-----");

    final String comando = "{call " + procedure + " (?)}";

    try (final Connection con = DBConnection.getConnFactIMX();
         final CallableStatement cs = con.prepareCall(comando)) {
      cs.registerOutParameter(1, Types.VARCHAR);
      cs.execute();
      r = cs.getString(1);
      log.info("-----Salidas----- " + r);
    } catch (SQLException sqle) {
      log.error("ocurrio un error:" + sqle.getMessage(), sqle);
    }
    return r;
  }

}
