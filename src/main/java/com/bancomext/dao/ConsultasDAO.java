package com.bancomext.dao;

import com.bancomext.service.mapping.*;
import com.bancomext.web.utils.Constantes;
import com.bancomext.web.utils.Utilerias;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ConsultasDAO {

  private static final Logger log = LogManager.getLogger(ConsultasDAO.class);

  private static final SimpleDateFormat formatoddMMyyyy = new SimpleDateFormat("dd/MM/yyyy");
  private static final SimpleDateFormat formatoyyyyMMdd = new SimpleDateFormat("yyyy-MM-dd");

  public static BigDecimal getMax(final String tabla, final String campo) {
    BigDecimal bd = new BigDecimal("0");
    final String query = "select nvl((max(" + campo + ")),0) as maximo FROM " + tabla;
    log.debug("query: " + query);
    try (final Connection conn = DBConnection.getConnFactIMX();
         final PreparedStatement ps = conn.prepareStatement(query);
         final ResultSet rs = ps.executeQuery()) {
      if (rs != null) {
        while (rs.next()) {
          bd = rs.getBigDecimal("maximo");
        }
        log.debug("\nresultado: " + bd);
      }
    } catch (SQLException sqle) {
      log.error(sqle.getMessage(), sqle);
    }
    return bd;
  }

  public static String getMaxString(final String tabla, final String campo) {
    String r = "";
    final String query = "select nvl((max(" + campo + ")),0) as maximo FROM " + tabla;
    log.debug("query: " + query);
    try (final Connection conn = DBConnection.getConnFactIMX();
         final PreparedStatement ps = conn.prepareStatement(query);
         final ResultSet rs = ps.executeQuery()) {
      if (rs != null) {
        while (rs.next()) {
          r = rs.getString("maximo");
        }
        log.debug("\nresultado: " + r);
      }
    } catch (SQLException sqle) {
      log.error(sqle.getMessage(), sqle);
    }
    return r;
  }

  public static Date getMaxDate(final String tabla, final String campo) {
    Date d = new Date();
    final String query = "SELECT max(" + campo + ") as maximo FROM " + tabla;
    log.debug("query: " + query);
    try (final Connection conn = DBConnection.getConnFactIMX();
         final PreparedStatement ps = conn.prepareStatement(query);
         final ResultSet rs = ps.executeQuery()) {
      if (rs != null) {
        while (rs.next()) {
          d = rs.getDate("maximo");
        }
        log.debug("\nresultado: " + d);
      }
    } catch (SQLException sqle) {
      log.error(sqle.getMessage(), sqle);
    }
    return d;
  }

  public static List<VoLlaveValor> getCombo(final String tabla,
                                            String campoLlave,
                                            String campoValor,
                                            final String condiciones,
                                            final String orden,
                                            final boolean distinct) {

    final List<VoLlaveValor> r = new ArrayList<>();
    final String order = orden != null && !orden.trim().isEmpty() ? " " + orden : " ORDER BY 1 ASC ";
    final String query;

    if (condiciones != null && !condiciones.trim().isEmpty()) {
      query = "SELECT " + (distinct ? "DISTINCT " : "") + campoLlave + ", " + campoValor + " FROM " + tabla +
          " WHERE " + condiciones + order;
    } else {
      query = "SELECT " + (distinct ? "DISTINCT " : "") + campoLlave + ", " + campoValor + " FROM " + tabla + order;
    }

    log.info("INTO getCombo " + query);
    if (campoLlave.indexOf("as") > 1) {
       campoLlave = campoLlave.substring(campoLlave.indexOf("as") +2).trim();
    }

    if (campoValor.indexOf("as")>1) {
      campoValor = campoValor.substring(campoValor.indexOf("as")+2).trim();
    }

    log.info("query: " + query);
    try (final Connection conn = DBConnection.getConnFactIMX();
         final PreparedStatement ps = conn.prepareStatement(query);
         final ResultSet rs = ps.executeQuery()) {
      if (rs != null) {
        while (rs.next()) {
          r.add(new VoLlaveValor(rs.getString(campoLlave), rs.getString(campoValor)));
        }
        log.debug("\nresultado: " + r.size());
      }
    } catch (SQLException sqle) {
      log.error(sqle.getMessage(), sqle);
    }
    return r;
  }

  public static String getGrupoAdministradores() {
    String r = "";
    final String query = "SELECT DESCRIPCION FROM FI_CAT_ROLES WHERE ID_GRUPO_CORREO = 1 ";
    log.debug("query: " + query);
    try (final Connection conn = DBConnection.getConnFactIMX();
         final PreparedStatement ps = conn.prepareStatement(query);
         final ResultSet rs = ps.executeQuery()) {
      if (rs != null) {
        while (rs.next()) {
          r = rs.getString("DESCRIPCION");
        }
        log.debug("\nresultado: " + r);
      }
    } catch (SQLException sqle) {
      log.error(sqle.getMessage(), sqle);
    }
    return r;
  }

  public static String getStringParametro(final String idparametro) {
    String r = "";
    final String query = "SELECT " + idparametro + " FROM FI_PARAM_GENERALES WHERE IDPARAMETRO = 1 ";
    log.info("query: " + query);
    try (final Connection conn = DBConnection.getConnFactIMX();
         final PreparedStatement ps = conn.prepareStatement(query);
         final ResultSet rs = ps.executeQuery()) {
      if (rs != null) {
        while (rs.next()) {
          r = rs.getString(1);
        }
        log.debug("\nresultado: " + r);
      }
    } catch (SQLException sqle) {
      log.error(sqle.getMessage(), sqle);
    }
    return r;
  }

  public static List<IcAdClientStatementHistory> getReporteSaldosFIU(String condicion) {
    final List<IcAdClientStatementHistory> r = new ArrayList<>();
    final String query = " SELECT to_char(CONTRACT_NUMBER) as CONTRACT_NUMBER, DT04_DT, " +
        " SUM(FUNDABLE_AMOUNT) as FUNDABLE_AMOUNT, " +
        " SUM(PURCHASED_AMOUNT) as PURCHASED_AMOUNT, " +
        " SUM(NOTCOVERED_AMOUNT) as NOTCOVERED_AMOUNT, " +
        " SUM(COVERED_AMOUNT) as COVERED_AMOUNT, " +
        " SUM(BLOCKED_AMOUNT) as BLOCKED_AMOUNT, " +
        " SUM(FINANCING_BASE) as FINANCING_BASE, " +
        " SUM(PORTFOLIO) as PORTFOLIO, " +
        " SUM(FIUCASH) as FIUCASH, " +
        " SUM(AVAILABILITY) as AVAILABILITY, " +
        " SUM(FIU) as FIU " +
        " FROM AD_CLIENT_STATEMENT_HISTORY aa " +
        " WHERE " + condicion + " AND fiu = (select max(fiu) " +
        " FROM AD_CLIENT_STATEMENT_HISTORY " +
        " WHERE " + condicion + " AND contract_number = aa.CONTRACT_NUMBER  ) " +
        " GROUP BY CONTRACT_NUMBER,  DT04_DT  ORDER BY DT04_DT DESC";

    log.debug("query: " + query);
    try (final Connection conn = DBConnection.getConnBanks();
         final PreparedStatement ps = conn.prepareStatement(query);
         final ResultSet rs = ps.executeQuery()) {
      if (rs != null) {
        while (rs.next()) {
          final IcAdClientStatementHistory tabla = new IcAdClientStatementHistory();
          tabla.setContractNumber(rs.getString("CONTRACT_NUMBER"));
          tabla.setDt04Dt(rs.getDate("DT04_DT"));
          tabla.setFundableAmount(rs.getBigDecimal("FUNDABLE_AMOUNT"));
          tabla.setPurchasedAmount(rs.getBigDecimal("PURCHASED_AMOUNT"));
          tabla.setNotcoveredAmount(rs.getBigDecimal("NOTCOVERED_AMOUNT"));
          tabla.setCoveredAmount(rs.getBigDecimal("COVERED_AMOUNT"));
          tabla.setBlockedAmount(rs.getBigDecimal("BLOCKED_AMOUNT"));
          tabla.setFinancingBase(rs.getBigDecimal("FINANCING_BASE"));
          tabla.setPortfolio(rs.getBigDecimal("PORTFOLIO"));
          tabla.setFiucash(rs.getBigDecimal("FIUCASH"));
          tabla.setAvailability(rs.getBigDecimal("AVAILABILITY"));
          tabla.setFiu(rs.getBigDecimal("FIU"));
          r.add(tabla);
        }
      }
      log.debug("\nresultado: " + r.size());
    } catch (SQLException sqle) {
      log.error(sqle.getMessage(), sqle);
    }
    return r;
  }

  public static List<FiCfdCifrasVerificacion> getCifrasControlVerificacion(final Date fechaInicio,
                                                                           final Date fechaFin) {

    final String fechaInicioStr = formatoyyyyMMdd.format(fechaInicio);
    final String fechaFinStr = formatoyyyyMMdd.format(fechaFin);

    final List<FiCfdCifrasVerificacion> r = new ArrayList<>();
    final String query = "select Sum(total) total, Sum(total_verificacion) total_verificacion, " +
        "Sum(total_esperado) total_esperado,  Sum(pendientes) total_pendientes, Sum(total_error) total_error " +
        " from fi_cfd_cifras_control Where trunc(fecha_proceso) " +
        " between to_date('" + fechaInicioStr + " 00:00:00', 'yyyy-MM-dd HH24:MI:SS') and " +
        " to_date('" + fechaFinStr + " 23:59:59', 'yyyy-MM-dd HH24:MI:SS')";

    log.debug("query: " + query);
    try (final Connection conn = DBConnection.getConnFactIMX();
         final PreparedStatement ps = conn.prepareStatement(query);
         final ResultSet rs = ps.executeQuery()) {
      if (rs != null) {
        while (rs.next()) {
          final FiCfdCifrasVerificacion cifrasControl = new FiCfdCifrasVerificacion();
          cifrasControl.setTotalFacturas(rs.getInt("total"));
          cifrasControl.setTotalVerificado(rs.getInt("total_verificacion"));
          cifrasControl.setTotalErrores(rs.getInt("total_error"));
          cifrasControl.setTotalEsperado(rs.getInt("total_esperado"));
          cifrasControl.setTotalPendientes(rs.getInt("total_pendientes"));
          cifrasControl.setDiferencia(rs.getInt("total") - rs.getInt("total_verificacion"));
          r.add(cifrasControl);
        }
      }
      log.debug("\nresultado: " + r.size());
    } catch (SQLException sqle) {
      log.error(sqle.getMessage(), sqle);
    }
    return r;
  }

  public static long getCtaSicav(final int cta, final int scta, final int sscta, final int ssscta) {
    long r = 0;
    final String query = "SELECT COUNT(*) AS SUMA FROM CGLIB.CGF001@AS400 WHERE CTA =" + cta + " AND " +
        "SCTA=" + scta + " AND SSCTA=" + sscta + " AND SSSCTA=" + ssscta;
    log.debug("query: " + query);
    try (final Connection conn = DBConnection.getConnBanks();
         final PreparedStatement ps = conn.prepareStatement(query);
         final ResultSet rs = ps.executeQuery()) {
      if (rs != null) {
        while (rs.next()) {
          r = rs.getLong("SUMA");
        }
        log.debug("\nresultado: " + r);
      }
    } catch (SQLException sqle) {
      log.error(sqle.getMessage(), sqle);
    }
    return r;
  }

  public static List<VoReporteFacturasMensual> getReporteFacturas(final Date fechaInicio,
                                                                  final Date fechaFin,
                                                                  final String codigoCliente) {
    final List<VoReporteFacturasMensual> r = new ArrayList<>();

    final String fechaInicioStr = formatoyyyyMMdd.format(fechaInicio);
    final String fechaFinStr = formatoyyyyMMdd.format(fechaFin);

    final String query = "SELECT FE.NOMBRE AS CLIENTE, T.NUMERO_FOLIO AS NO_FACTURA, FE.FECHAVALOR  " +
        " AS FECHA, imx_f_obtiene_concepto(FE.SECUENCIA) AS CONCEPTO, FE.IMPORTE AS TOTAL, T.CODIGO_CLIENTE " +
        " FROM FI_FACTURAS_ENCABEZADO FE, FI_CFD_HISTORICO_FOLIOS T, fi_bitacora_cfdi bit " +
        " WHERE FE.NO_ACREDITADO = T.CODIGO_CLIENTE and bit.CODIGO_CLIENTE=t.CODIGO_CLIENTE " +
        " AND bit.CONTRATO = t.CONTRATO " +
        " AND bit.SERIE_FOLIO=CONCAT( cast(t.serie as varchar(25)), cast( nvl(t.NUMERO_FOLIO,'') as varchar(25))) " +
        " AND FE.FECHAVALOR = T.FECHA_ASIGNACION AND FE.LINEA = T.CODIGO_LINEA_CREDITO AND FE.SECUENCIA = T.SECUENCIA " +
        " AND T.STATUS_FOLIO='VERIFICADO' AND bit.FECHA_ENVIO_CLIENTE is not null " +
        " AND FE.FECHAVALOR between to_date('" + fechaInicioStr + " 00:00:00', 'yyyy-MM-dd HH24:MI:SS') and " +
        " to_date('" + fechaFinStr + " 23:59:59', 'yyyy-MM-dd HH24:MI:SS') " +
        " AND FE.NO_ACREDITADO = NVL(" + codigoCliente + ", FE.NO_ACREDITADO) ORDER BY 1,2,3,4";

    log.info("query: " + query);
    try (final Connection conn = DBConnection.getConnFactIMX();
         final PreparedStatement ps = conn.prepareStatement(query);
         final ResultSet rs = ps.executeQuery()) {
      if (rs != null) {
        while (rs.next()) {
          final VoReporteFacturasMensual bean = new VoReporteFacturasMensual();
          bean.setCliente(rs.getString("CLIENTE"));
          bean.setNoFactura(rs.getInt("NO_FACTURA"));
          bean.setFechaFor(formatoddMMyyyy.format(rs.getDate("FECHA")));
          bean.setConcepto(rs.getString("CONCEPTO"));
          bean.setTotal(rs.getBigDecimal("TOTAL"));
          bean.setCodigoCliente(rs.getInt("CODIGO_CLIENTE"));
          r.add(bean);
        }
        log.info("\nresultado: " + r.size());
      }
    } catch (SQLException sqle) {
      log.error(sqle.getMessage(), sqle);
    }
    return r;
  }

  public static List<VoSaldosMoneda> getSaldosMoneda() {
    final List<VoSaldosMoneda> r = new ArrayList<>();
    final String query = " SELECT SUM(MONTO_ORIGEN) MONTO_X_MONEDA ,DESMON DESCRIPCION  " +
        " FROM FI_CAPITALIZACION_IMX_TMP  WHERE MONEDA <> 1 " +
        " GROUP BY DESMON  UNION  SELECT SUM(MONTO_SUST1) MONTO_X_MONEDA ,'PESO MEX' " +
        " FROM FI_CAPITALIZACION_IMX_TMP  WHERE  MONTO_SUST1 > 0  GROUP BY DESMON ";
    log.debug("query: " + query);
    try (final Connection conn = DBConnection.getConnFactIMX();
         final PreparedStatement ps = conn.prepareStatement(query);
         final ResultSet rs = ps.executeQuery()) {
      if (rs != null) {
        while (rs.next()) {
          final VoSaldosMoneda bean = new VoSaldosMoneda();
          bean.setSaldoMoneda(
              Utilerias.formatearNumero(rs.getBigDecimal("MONTO_X_MONEDA"), ""));
          bean.setDescripcion(rs.getString("DESCRIPCION"));
          r.add(bean);
        }
        log.debug("\nresultado: " + r.size());
      }
    } catch (SQLException sqle) {
      log.error(sqle.getMessage(), sqle);
    }
    return r;
  }

  // CONSULTA PARA LLENAR LA PANTALLA PRINCIPAL DEL PROCESO MANULA DE
  // COFACTORES
  public static List<VoCofactoresTmp> getClasificacionCofactor() {
    final List<VoCofactoresTmp> r = new ArrayList<>();
    final String query = " SELECT ENC.CAMPOA CAMPOA, ENC.NOM_CLIENTE CLIENTE," +
        "ENC.CUENTA_ORIGEN CUENTA_ORIGEN, ENC.SALDO_SICAV SALDO_SICAV," +
        "SUM(DECODE(DET.E102142_SUM,NULL,0,DET.E102142_SUM)) E102142, " +
        "COUNT(*) NUMCOF, DECODE(ENC.SALDO_SICAV,0,'B','A') ORDEN  " +
        " FROM FI_COFACTORES_IMX_ENC_TMP ENC, FI_COFACTORES_IMX_DET_TMP DET " +
        " WHERE ENC.CAMPOA = DET.CAMPOA(+) " +
        " GROUP BY  ENC.CAMPOA, ENC.NOM_CLIENTE, ENC.CUENTA_ORIGEN, ENC.SALDO_SICAV " +
        " ORDER BY ORDEN, ENC.CUENTA_ORIGEN ,ENC.CAMPOA ";
    log.debug("query: " + query);
    try (final Connection conn = DBConnection.getConnFactIMX();
         final PreparedStatement ps = conn.prepareStatement(query);
         final ResultSet rs = ps.executeQuery()) {
      if (rs != null) {
        while (rs.next()) {
          final VoCofactoresTmp bean = new VoCofactoresTmp();
          bean.setCampoA(rs.getString("CAMPOA"));
          bean.setCliente(rs.getString("CLIENTE"));
          bean.setCuentaOrigen(rs.getString("CUENTA_ORIGEN"));
          bean.setSaldoSicav(rs.getBigDecimal("SALDO_SICAV"));
          bean.setE102142(rs.getBigDecimal("E102142"));
          bean.setNumCof(rs.getInt("NUMCOF"));
          bean.setOrden(rs.getString("ORDEN"));
          bean.setE102142For("");
          bean.setSaldoSicavFor("");
          r.add(bean);
        }
        log.debug("\nresultado: " + r.size());
      }
    } catch (SQLException sqle) {
      log.error(sqle.getMessage(), sqle);
    }
    return r;
  }

  public static List<VoSeguimientoEstatusFac> getStatusFacturas(final Date fechaInicio, final Date fechaFin) {
    return (Constantes.PRODUCCION) ? getStatusFacturas_PROD(fechaInicio, fechaFin) : getStatusFacturas_DEV();
  }

  // CONSULTA PARA OBTENER EL ESTATUS DE LOS FOLIOS (TYPE)
  private static List<VoSeguimientoEstatusFac> getStatusFacturas_DEV() {
    final List<VoSeguimientoEstatusFac> r = new ArrayList<>();
    final String query = " SELECT * FROM fi_cfd_seg_stat_facturas ORDER BY STATUS_VERIFICADO DESC";
    log.debug("query: " + query);
    try (final Connection conn = DBConnection.getConnFactIMX();
         final PreparedStatement ps = conn.prepareStatement(query);
         final ResultSet rs = ps.executeQuery()) {
      if (rs != null) {
        while (rs.next()) {
          final VoSeguimientoEstatusFac bean = new VoSeguimientoEstatusFac();
          bean.setFechaValor(rs.getDate("FECHA_VALOR"));
          bean.setCodigoCliente(rs.getInt("CODIGO_CLIENTE"));
          bean.setNombreCliente(rs.getString("NOMBRE_CLIENTE"));
          bean.setContrato(rs.getString("CONTRATO"));
          bean.setMoneda(rs.getString("MONEDA"));
          bean.setProducto(rs.getString("PRODUCTO"));
          bean.setSerieFolio(rs.getString("SERIE_FOLIO"));
          bean.setTotal(rs.getBigDecimal("TOTAL"));
          bean.setStatusVerificado(rs.getString("STATUS_VERIFICADO"));
          bean.setFechaVerificacion(rs.getDate("FECHA_VERIFICACION") != null ?
              formatoddMMyyyy.format(rs.getDate("FECHA_VERIFICACION")) : null);
          bean.setFechaIngresoGacd(rs.getDate("FECHA_INGRESO_GACD") != null ?
              formatoddMMyyyy.format(rs.getDate("FECHA_INGRESO_GACD")) : null);
          bean.setFechaSatGacd(rs.getDate("FECHA_SAT_GACD") != null ?
              formatoddMMyyyy.format(rs.getDate("FECHA_SAT_GACD")) : null);
          bean.setFechaEnvioCliente(rs.getDate("FECHA_ENVIO_CLIENTE") != null ?
              formatoddMMyyyy.format(rs.getDate("FECHA_ENVIO_CLIENTE")) : null);
          bean.setFechaStatus(rs.getDate("FECHA_STATUS") != null ?
              formatoddMMyyyy.format(rs.getDate("FECHA_STATUS")) : null);
          bean.setFechaValorFormat(rs.getDate("FECHA_VALOR") != null ?
              formatoddMMyyyy.format(rs.getDate("FECHA_VALOR")) : null);
          bean.setFolioSAT(rs.getString("FOLIO_SAT"));
          bean.setStatus(rs.getString("STATUS"));
          bean.setAccion(rs.getString("ACCION"));
          r.add(bean);
        }
        log.debug("\nresultado: " + r.size());
      }
    } catch (SQLException sqle) {
      log.error(sqle.getMessage(), sqle);
    }
    return r;
  }

  private static List<VoSeguimientoEstatusFac> getStatusFacturas_PROD(final Date fechaInicio, final Date fechaFin) {

    final List<VoSeguimientoEstatusFac> r = new ArrayList<>();

    final String fechaInicioStr = (fechaInicio == null ? "" : formatoyyyyMMdd.format(fechaInicio));
    final String fechaFinStr = (fechaFin == null ? "" : formatoyyyyMMdd.format(fechaFin));

    final String query = " SELECT * FROM TABLE (IMX_F_SEG_STAT_FACTURAS(" +
        " TO_DATE('" + fechaInicioStr + "','yyyy-mm-dd'), TO_DATE('" + fechaFinStr + "', 'yyyy-mm-dd'))) " +
        "ORDER BY STATUS_VERIFICADO DESC";
    log.info("PROD query: " + query);
    try (final Connection conn = DBConnection.getConnFactIMX();
         final PreparedStatement ps = conn.prepareStatement(query);
         final ResultSet rs = ps.executeQuery()) {
      if (rs != null) {
        while (rs.next()) {
          final VoSeguimientoEstatusFac bean = new VoSeguimientoEstatusFac();
          bean.setFechaValor(rs.getDate("FECHA_VALOR"));
          bean.setCodigoCliente(rs.getInt("CODIGO_CLIENTE"));
          bean.setNombreCliente(rs.getString("NOMBRE_CLIENTE"));
          bean.setContrato(rs.getString("CONTRATO"));
          bean.setMoneda(rs.getString("MONEDA"));
          bean.setProducto(rs.getString("PRODUCTO"));
          bean.setSerieFolio(rs.getString("SERIE_FOLIO"));
          bean.setTotal(rs.getBigDecimal("TOTAL"));
          bean.setStatusVerificado(rs.getString("STATUS_VERIFICADO"));
          bean.setFechaVerificacion(rs.getDate("FECHA_VERIFICACION") != null ?
              formatoddMMyyyy.format(rs.getDate("FECHA_VERIFICACION")) : null);
          bean.setFechaIngresoGacd(rs.getDate("FECHA_INGRESO_GACD") != null ?
              formatoddMMyyyy.format(rs.getDate("FECHA_INGRESO_GACD")) : null);
          bean.setFechaSatGacd(rs.getDate("FECHA_SAT_GACD") != null ?
              formatoddMMyyyy.format(rs.getDate("FECHA_SAT_GACD")) : null);
          bean.setFechaEnvioCliente(rs.getDate("FECHA_ENVIO_CLIENTE") != null ?
              formatoddMMyyyy.format(rs.getDate("FECHA_ENVIO_CLIENTE")) : null);
          bean.setFechaStatus(rs.getDate("FECHA_STATUS") != null ?
              formatoddMMyyyy.format(rs.getDate("FECHA_STATUS")) : null);
          bean.setFechaValorFormat(rs.getDate("FECHA_VALOR") != null ?
              formatoddMMyyyy.format(rs.getDate("FECHA_VALOR")) : null);
          bean.setFolioSAT(rs.getString("FOLIO_SAT"));
          bean.setStatus(rs.getString("STATUS"));
          bean.setAccion(rs.getString("ACCION"));
          r.add(bean);
        }
        log.info("\nresultado: " + r.size());
      }
    } catch (SQLException sqle) {
      log.error(sqle.getMessage(), sqle);
    }
    return r;
  }


  // CONSULTA PARA OBTENER EL ESTATUS DE LOS FOLIOS (TYPE)
  public static List<FiSeguimientoImx> getSeguimientoIMX(final Date fechaInicio,
                                                         final Date fechaFin,
                                                         final String dias) {

    final List<FiSeguimientoImx> r = new ArrayList<>();

    final String fechaInicioStr = formatoyyyyMMdd.format(fechaInicio);
    final String fechaFinStr = formatoyyyyMMdd.format(fechaFin);

    final String query = " select to_char(fecha_asignacion,'dd/mm/yyyy') Fecha_Emision, " +
        " status_folio Status, to_char(fecha_modificacion,'dd/mm/yyyy') Fecha_generacion," +
        " fac.rfc, fol.Moneda, fac.Importe, fol.codigo_cliente Campo_A,  fol.serie||numero_folio   Folio_IMX " +
        " from fi_cfd_historico_folios fol, fi_facturas_encabezado fac " +
        " where fol.secuencia = fac.secuencia and trunc(fecha_asignacion) " +
        " between to_date('" + fechaInicioStr + " 00:00:00', 'yyyy-MM-dd HH24:MI:SS')-" + dias +
        " and to_date('" + fechaFinStr + " 23:59:59', 'yyyy-MM-dd HH24:MI:SS') " +
        " order by fecha_asignacion desc ";

    log.debug("query: " + query);
    try (final Connection conn = DBConnection.getConnFactIMX();
         final PreparedStatement ps = conn.prepareStatement(query);
         final ResultSet rs = ps.executeQuery()) {
      if (rs != null) {
        while (rs.next()) {
          final FiSeguimientoImx bean = new FiSeguimientoImx();
          bean.setFechaEmision(rs.getString("Fecha_Emision"));
          bean.setStatus(rs.getString("Status"));
          bean.setFechaGeneracion(rs.getString("Fecha_generacion"));
          bean.setRfc(rs.getString("rfc"));
          bean.setMoneda(rs.getString("Moneda"));
          bean.setImporte(rs.getDouble("Importe"));
          bean.setCampoA(rs.getString("Campo_A"));
          bean.setFolioImx(rs.getString("Folio_IMX"));
          r.add(bean);
        }
        log.debug("\nresultado: " + r.size());
      }
    } catch (SQLException sqle) {
      log.error(sqle.getMessage(), sqle);
    }
    return r;
  }

  // CONSULTA PARA OBTENER LOS CORREOS DE ENVIO CIFRAS CONTROL PARA PRIME Y
  // COMISIONES
  public static List<VoCorreosCifrasPrimeComisiones> getCorreosCifrasPrimeCom(final String condicion) {
    final List<VoCorreosCifrasPrimeComisiones> r = new ArrayList<>();
    final String query = " SELECT * FROM DD_CATALOGO_EMAIL_ADMON " + condicion;
    log.debug("query: " + query);
    try (final Connection conn = DBConnection.getConnBanks();
         final PreparedStatement ps = conn.prepareStatement(query);
         final ResultSet rs = ps.executeQuery()) {
      if (rs != null) {
        while (rs.next()) {
          final VoCorreosCifrasPrimeComisiones bean = new VoCorreosCifrasPrimeComisiones();
          bean.setUsuario(rs.getString("USUARIO"));
          bean.setCorreo(rs.getString("CORREO"));
          bean.setActivo(rs.getString("ACTIVO"));
          bean.setActivoCom(rs.getString("ACTIVO_COM"));
          bean.setActivoFac(rs.getString("ACTIVO_FAC"));
          r.add(bean);
        }
        log.debug("\nresultado: " + r.size());
      }
    } catch (SQLException sqle) {
      log.error(sqle.getMessage(), sqle);
    }
    return r;
  }

  // CONSULTA PARA DE HISTORICO DE FOLIOS ENCABEZADO PARA OBTENER IMPORTE Y
  // NOMBRE DE CLIENTE PARA ENVIO DE FACTURAS PRIMA Y COMISIONES
  public static List<VoHistoricoEncabezado> getHistoricoEncabezado(final int codigoCliente,
                                                                   final Date fechaValor,
                                                                   final int folio) {

    final String fechaStr = formatoyyyyMMdd.format(fechaValor);

    log.info("getHistorico_encabezado");
    final List<VoHistoricoEncabezado> r = new ArrayList<>();
    final String query = "SELECT PRODUCTO, NUMERO_FOLIO,CODIGO_CLIENTE,NOMBRE,MONEDA,FECHA_VALOR,IMPORTE " +
        " FROM CFDI_V_HISTORICO_ENCABEZADO WHERE CODIGO_CLIENTE = " + codigoCliente +
        " AND NUMERO_FOLIO = " + folio + " AND FECHA_VALOR =  TO_DATE('" + fechaStr + "','yyyy-MM-dd')";
    log.debug("query: " + query);
    try (final Connection conn = DBConnection.getConnBanks();
         final PreparedStatement ps = conn.prepareStatement(query);
         final ResultSet rs = ps.executeQuery()) {
      if (rs != null) {
        while (rs.next()) {
          final VoHistoricoEncabezado bean = new VoHistoricoEncabezado();
          bean.setProducto(rs.getString("PRODUCTO"));
          bean.setNumeroFolio(rs.getInt("NUMERO_FOLIO"));
          bean.setCodigoCliente(rs.getInt("CODIGO_CLIENTE"));
          bean.setNombre(rs.getString("NOMBRE_CLIENTE"));
          bean.setMoneda(rs.getString("MONEDA"));
          bean.setFechaValor(rs.getDate("FECHA_VALOR"));
          bean.setImporte(rs.getBigDecimal("IMPORTE"));
          r.add(bean);
        }
        log.debug("\nresultado: " + r.size());
      }
    } catch (SQLException sqle) {
      log.error(sqle.getMessage(), sqle);
    }
    return r;
  }

  // CONSULTA PARA DE HISTORICO DE FOLIOS ENCABEZADO PARA OBTENER IMPORTE Y
  // NOMBRE DE CLIENTE PARA ENVIO DE REPORTE DE RESUMEN DE FACTURACION
  public static List<VoHistoricoEncabezado> getResumenFacturacion(final Date fechaInicio,
                                                                  final Date fechaFin) {
    final List<VoHistoricoEncabezado> r = new ArrayList<>();

    final String fechaInicioStr = formatoyyyyMMdd.format(fechaInicio);
    final String fechaFinStr = formatoyyyyMMdd.format(fechaFin);

    final String query = " SELECT * FROM CFDI_V_HISTORICO_ENCABEZADO " +
        " WHERE FECHA_VALOR between to_date('" + fechaInicioStr + " 00:00:00', 'yyyy-MM-dd HH24:MI:SS') " +
        " and to_date('" + fechaFinStr + " 23:59:59', 'yyyy-MM-dd HH24:MI:SS') " +
        " AND PRODUCTO = 'PRIME REVENUE' ORDER BY NUMERO_FOLIO ASC";
    log.debug("query: " + query);
    try (final Connection conn = DBConnection.getConnBanks();
         final PreparedStatement ps = conn.prepareStatement(query);
         final ResultSet rs = ps.executeQuery()) {
      if (rs != null) {
        while (rs.next()) {
          final VoHistoricoEncabezado bean = new VoHistoricoEncabezado();
          bean.setProducto(rs.getString("PRODUCTO"));
          bean.setNumeroFolio(rs.getInt("NUMERO_FOLIO"));
          bean.setCodigoCliente(rs.getInt("CODIGO_CLIENTE"));
          bean.setNombre(rs.getString("NOMBRE_CLIENTE"));
          bean.setMoneda(rs.getString("MONEDA"));
          bean.setFechaValor(rs.getDate("FECHA_VALOR"));
          bean.setImporte(rs.getBigDecimal("IMPORTE"));
          r.add(bean);
        }
        log.debug("\nresultado: " + r.size());
      }
    } catch (SQLException sqle) {
      log.error(sqle.getMessage(), sqle);
    }
    return r;
  }

  // CONSULTA PARA DE HISTORICO DE FOLIOS ENCABEZADO PARA OBTENER IMPORTE Y
  // NOMBRE DE CLIENTE PARA ENVIO DE FACTURAS PRIMA Y COMISIONES
  public static List<VoHistoricoPrimeCom> getHistoricoPrimeCom(final String folio, final String serie,
                                                               final String consulta) {
    final List<VoHistoricoPrimeCom> r = new ArrayList<>();
    final String query;
    if (consulta.equals("CORREO")) {
      query = "SELECT NUMERO_FOLIO,FECHA_ASIGNACION,CODIGO_CLIENTE FROM CFD_HISTORICO_FOLIOS " +
          " WHERE NUMERO_FOLIO = " + folio + " AND SERIE = '" + serie + "' AND ENVIADO_CLIENTE IS NULL ";
    } else {
      query = "SELECT NUMERO_FOLIO,FECHA_ASIGNACION,CODIGO_CLIENTE FROM CFD_HISTORICO_FOLIOS " +
          " WHERE FECHA_ASIGNACION >= TO_DATE('05/05/2016','dd/mm/yyyy')  AND SERIE IN('COM','FAC') " +
          " AND ENVIADO_CLIENTE IS NULL  AND STATUS_FOLIO = 'VERIFICADO'";
    }
    log.debug("query: " + query);
    try (final Connection conn = DBConnection.getConnBanks();
         final PreparedStatement ps = conn.prepareStatement(query);
         final ResultSet rs = ps.executeQuery()) {
      if (rs != null) {
        while (rs.next()) {
          final VoHistoricoPrimeCom bean = new VoHistoricoPrimeCom();
          bean.setNumeroFolio(rs.getString("NUMERO_FOLIO"));
          bean.setFechaAsignacion(rs.getDate("FECHA_ASIGNACION"));
          bean.setCodigoCliente(rs.getInt("CODIGO_CLIENTE"));
          r.add(bean);
        }
        log.debug("\nresultado: " + r.size());
      }
    } catch (SQLException sqle) {
      log.error(sqle.getMessage(), sqle);
    }
    return r;
  }

  // CONSULTA PARA OBTENER CORREOS DE ENVIO CFDI (COMISIONES)
  public static List<VoTablaElemento> getCorreosCFDIComisiones(int codigoCliente) {
    final List<VoTablaElemento> r = new ArrayList<>();
    final String query = " SELECT EMAIL FROM PR_EMAIL_CLIENTE WHERE CODIGO_CLIENTE = " + codigoCliente;
    log.debug("query: " + query);
    try (final Connection conn = DBConnection.getConnBanks();
         final PreparedStatement ps = conn.prepareStatement(query);
         final ResultSet rs = ps.executeQuery()) {
      if (rs != null) {
        while (rs.next()) {
          final VoTablaElemento bean = new VoTablaElemento();
          bean.setElemento(rs.getString("EMAIL"));
          r.add(bean);
        }
        log.debug("\nresultado: " + r.size());
      }
    } catch (SQLException sqle) {
      log.error(sqle.getMessage(), sqle);
    }
    return r;
  }

  // CONSULTA PARA OBTENER CORREOS DE ENVIO CFDI (PRIME)
  public static List<VoTablaElemento> getCorreosCFDIPrime(final int codigoProveedor) {
    final List<VoTablaElemento> r = new ArrayList<>();
    log.info("codigo proveedor::::::::" + codigoProveedor);
    final String query =
        "SELECT EMAIL FROM DD_EMAIL_PROVEEDORES WHERE CODIGO_PROVEEDOR = " + codigoProveedor + " AND ACTIVO = 'S' ";
    log.debug("query: " + query);

    try (final Connection conn = DBConnection.getConnBanks();
         final PreparedStatement ps = conn.prepareStatement(query);
         final ResultSet rs = ps.executeQuery()) {
      if (rs != null) {
        while (rs.next()) {
          final VoTablaElemento bean = new VoTablaElemento();
          bean.setElemento(rs.getString("EMAIL"));
          r.add(bean);
        }
        log.debug("\nresultado: " + r.size());
      }
    } catch (SQLException sqle) {
      log.error(sqle.getMessage(), sqle);
    }
    return r;
  }

  // CONSULTA PARA OBTENER LAS CIFRAS CONTROL PARA PRODUCTO PRIME Y COMISIONES
  // (SUCRE)
  public static List<VoCifrasPrimeComisiones> getCifrasPrimeCom(final Date fechaEmision, final String producto) {
    final List<VoCifrasPrimeComisiones> r = new ArrayList<>();

    final String query = " SELECT cv.ORDEN,  cv.FECHA_EMISION,  cv.TOTAL,  cv.STATUS,  cv.MOTIVO,  " +
        " (select DECODE('Del '||to_char(min(numero_folio))||' Al " +
        " '||to_char(Max(numero_folio)),'Del  Al ','','Del '||to_char(min(numero_folio))||' Al " +
        " '||to_char(Max(numero_folio)))  from cfd_historico_folios  where " +
        " orden = cv.ORDEN  and fecha_asignacion = cv.FECHA_EMISION  and " +
        " producto = cv.PRODUCTO  and status_folio = 'VERIFICADO') FOLIOS,  " +
        " cv.FECHA,  cv.PRODUCTO,  cv.USUARIO_VERIFICA   from cfd_verificacion cv " +
        " where cv.producto in ('" + producto + "')  and " +
        " cv.fecha_emision = to_date ('" + fechaEmision + "','yyyy-mm-dd') order by producto, orden";
    log.debug("query: " + query);

    try (final Connection conn = DBConnection.getConnBanks();
         final PreparedStatement ps = conn.prepareStatement(query);
         final ResultSet rs = ps.executeQuery()) {
      if (rs != null) {
        while (rs.next()) {
          final VoCifrasPrimeComisiones bean = new VoCifrasPrimeComisiones();
          bean.setOrden(rs.getInt("ORDEN"));
          bean.setFechaEmision(rs.getDate("FECHA_EMISION"));
          bean.setTotal(rs.getInt("TOTAL"));
          bean.setStatus(rs.getString("STATUS"));
          bean.setMotivo(rs.getString("MOTIVO"));
          bean.setFolios(rs.getString("FOLIOS"));
          bean.setFecha(rs.getDate("FECHA"));
          bean.setProducto(rs.getString("PRODUCTO"));
          bean.setUsuarioVerifica(rs.getString("USUARIO_VERIFICA"));
          r.add(bean);
        }
        log.debug("\nresultado: " + r.size());
      }
    } catch (SQLException sqle) {
      log.error(sqle.getMessage(), sqle);
    }
    return r;
  }


  // CONSULTA PARA OBTENER EL ESTATUS DE LA CIFRAS PRIME - COMISIONES
  public static List<VoCifrasPrimeComisiones> getCifrasStatusPrimeCom(final Date fechaEmision,
                                                                      final String producto,
                                                                      final String status) {
    final String fechaStr = formatoyyyyMMdd.format(fechaEmision);

    final List<VoCifrasPrimeComisiones> r = new ArrayList<>();
    final String query = " SELECT * FROM DD_CIFRAS_CONTROL_PRIME_COM " +
        " WHERE FECHA_EMISION = to_date('" + fechaStr + "','yyyy-MM-dd') AND " +
        " PRODUCTO = '" + producto + "' AND STATUS = '" + status + "'";
    log.debug("query: " + query);
    try (final Connection conn = DBConnection.getConnBanks();
         final PreparedStatement ps = conn.prepareStatement(query);
         final ResultSet rs = ps.executeQuery()) {
      if (rs != null) {
        while (rs.next()) {
          final VoCifrasPrimeComisiones bean = new VoCifrasPrimeComisiones();
          bean.setOrden(rs.getInt("ORDEN"));
          bean.setFechaEmision(rs.getDate("FECHA_EMISION"));
          bean.setTotal(rs.getInt("TOTAL"));
          bean.setStatus(rs.getString("STATUS"));
          bean.setMotivo(rs.getString("MOTIVO"));
          bean.setFolios(rs.getString("FOLIOS"));
          bean.setFecha(rs.getDate("FECHA"));
          bean.setProducto(rs.getString("PRODUCTO"));
          bean.setUsuarioVerifica(rs.getString("USUARIO_VERIFICA"));
          r.add(bean);
        }
        log.debug("\nresultado: " + r.size());
      }
    } catch (SQLException sqle) {
      log.error(sqle.getMessage(), sqle);
    }
    return r;
  }

  // CONSULTA PARA OBTENER EL CUERPO DE LOS CORREOS PARA PRIME Y COMISIONES
  public static List<VoTablaElemento> getCuerpoCorreosPrimCom() {
    final List<VoTablaElemento> r = new ArrayList<>();
    final String query = " SELECT * FROM DD_EMAIL_CDFI ";
    log.debug("query: " + query);
    try (final Connection conn = DBConnection.getConnBanks();
         final PreparedStatement ps = conn.prepareStatement(query);
         final ResultSet rs = ps.executeQuery()) {
      if (rs != null) {
        while (rs.next()) {
          final VoTablaElemento bean = new VoTablaElemento();
          bean.setElemento(rs.getString("EMAIL_ASUNTO"));
          bean.setElemento2(rs.getString("EMAIL_CUERPO"));
          r.add(bean);
        }
        log.debug("\nresultado: " + r.size());
      }
    } catch (SQLException sqle) {
      log.error(sqle.getMessage(), sqle);
    }
    return r;
  }

  // CONSULTA PARA INSERTAR EL ESTATUS DE LA CIFRAS PRIME - COMISIONES
  public static void updateCifrasStatusPrimeCom(final Date fechaEmision,
                                                final String producto,
                                                final String status) {

    final String fechaStr = formatoyyyyMMdd.format(fechaEmision);

    final String update = " INSERT INTO DD_CIFRAS_CONTROL_PRIME_COM (FECHA_EMISION,PRODUCTO,STATUS) " +
        "VALUES(TO_DATE('" + fechaStr + "','yyyy-MM-dd'),'" + producto + "','" + status + "')";

    log.debug("update: " + update);

    try (final Connection conn = DBConnection.getConnBanks();
         final PreparedStatement ps = conn.prepareStatement(update)) {
      int r = ps.executeUpdate();
      log.debug("\nresultado: " + r);
    } catch (SQLException sqle) {
      log.error(sqle.getMessage(), sqle);
    }
  }

  // CONSULTA PARA INSERTAR EL ESTATUS DE LA CIFRAS PRIME - COMISIONES
  public static void updateHistoricoFoliosPrimeCom(final String numeroFolio,
                                                   final String serie) {
    log.info("Actualiza el estatus a S:: numFolio:: " + numeroFolio + "   serie:::: " + serie);

    final String update = " UPDATE CFD_HISTORICO_FOLIOS SET ENVIADO_CLIENTE = 'S' " +
        " WHERE NUMERO_FOLIO = " + numeroFolio + " AND SERIE = '" + serie + "'";

    log.debug("update: " + update);

    try (final Connection conn = DBConnection.getConnBanks();
         final PreparedStatement ps = conn.prepareStatement(update)) {
      int r = ps.executeUpdate();
      log.debug("\nresultado: " + r);
    } catch (SQLException sqle) {
      log.error(sqle.getMessage(), sqle);
    }
  }

  // CONSULTA PARA OBTENER LOS DATOS DE LAS POLIZAS EXTEMPORANEAS
  public static List<VoPolizasExtemporaneo> getPolizasExtemporaneo() {
    final List<VoPolizasExtemporaneo> r = new ArrayList<>();
    final String query = " SELECT ENC.NUMPOL                 POLIZA, " +
        " ENC.OBSRV3                        OPCODE, " +
        " ENC.OBSRV2                        DESCRIPCION," +
        " SUM(DET.CARGO)                    CARGO," +
        " SUM(DET.ABONO)                    ABONO " +
        " FROM   FI_EXTEMP_ENC_IMX_TMP      ENC  ," +
        " FI_EXTEMP_DET_IMX_TMP            DET " +
        " WHERE ENC.SUCCON      =           0 " +
        " AND   ENC.DEPTO       =           19 " +
        " AND   DET.SUCCON      =           ENC.SUCCON " +
        " AND   DET.DEPTO       =           ENC.DEPTO " +
        " AND   DET.NUMPOL      =           ENC.NUMPOL " +
        " GROUP BY ENC.OBSRV3, ENC.OBSRV2 , ENC.NUMPOL " +
        " ORDER BY OPCODE,DESCRIPCION,POLIZA";

    log.debug("query: " + query);
    try (final Connection conn = DBConnection.getConnFactIMX();
         final PreparedStatement ps = conn.prepareStatement(query);
         final ResultSet rs = ps.executeQuery()) {
      if (rs != null) {
        while (rs.next()) {
          final VoPolizasExtemporaneo bean = new VoPolizasExtemporaneo();
          bean.setPoliza(rs.getString("POLIZA"));
          bean.setOpCode(rs.getString("OPCODE"));
          bean.setDescripcion(rs.getString("DESCRIPCION"));
          bean.setCargo(rs.getBigDecimal("CARGO"));
          bean.setAbono(rs.getBigDecimal("ABONO"));
          r.add(bean);
        }
        log.debug("\nresultado: " + r.size());
      }
    } catch (SQLException sqle) {
      log.error(sqle.getMessage(), sqle);
    }
    return r;
  }

  public static List<VoPolizasExtemporaneoDetalle> getPolizasExtemporaneoDetalle(final String opCode,
                                                                                 final String descripcion,
                                                                                 final int poliza) {

    final List<VoPolizasExtemporaneoDetalle> r = new ArrayList<>();

    final String query = " SELECT ENC.NUMPOL                           POLIZA, " +
        " DET.CAMPOA                           CAMPOA, " +
        " DET.CAMPOB                           CAMPOB, " +
        " LPAD(TO_CHAR(DET.CTA)   ,4,'0')||'-'|| LPAD(TO_CHAR(DET.SCTA)  ,2,'0')||'-'|| " +
        " LPAD(TO_CHAR(DET.SSCTA) ,2,'0')||'-'|| LPAD(TO_CHAR(DET.SSSCTA),2,'0')      CTA_ORIGEN, " +
        " ENC.OBSRV3                           OPCODE, " +
        " ENC.OBSRV2                           DESCRIPCION, " +
        " DET.CARGO                            CARGO, " +
        " DET.ABONO                            ABONO " +
        " FROM   FI_EXTEMP_ENC_IMX_TMP    ENC  ," +
        " FI_EXTEMP_DET_IMX_TMP    DET " +
        " WHERE ENC.SUCCON      =         0 " +
        " AND   ENC.DEPTO       =         19 " +
        " AND   ENC.OBSRV3      =         '" + opCode + "' " +
        " AND   ENC.OBSRV2      =        '" + descripcion + "' " +
        " AND   ENC.NUMPOL      =         " + poliza + " " +
        " AND   DET.SUCCON      =         ENC.SUCCON " +
        " AND   DET.DEPTO       =         ENC.DEPTO " +
        " AND   DET.NUMPOL      =         ENC.NUMPOL " +
        " ORDER BY DET.CAMPOA,CARGO DESC, ABONO DESC";

    log.debug("query: " + query);
    try (final Connection conn = DBConnection.getConnFactIMX();
         final PreparedStatement ps = conn.prepareStatement(query);
         final ResultSet rs = ps.executeQuery()) {
      if (rs != null) {
        while (rs.next()) {
          final VoPolizasExtemporaneoDetalle bean = new VoPolizasExtemporaneoDetalle();
          bean.setPoliza(rs.getInt("POLIZA"));
          bean.setCampoA(rs.getString("CAMPOA"));
          bean.setCampoB(rs.getString("CAMPOB"));
          bean.setCuentaOrigen(rs.getString("CTA_ORIGEN"));
          bean.setOpCode(rs.getString("OPCODE"));
          bean.setDescripcion(rs.getString("DESCRIPCION"));
          bean.setCargo(rs.getBigDecimal("CARGO"));
          bean.setAbono(rs.getBigDecimal("ABONO"));
          r.add(bean);
        }
        log.debug("\nresultado: " + r.size());
      }
    } catch (SQLException sqle) {
      log.error(sqle.getMessage(), sqle);
    }
    return r;
  }

  // CONSULTA QUE NOS INDICA EL NOMBRE DE LOS COFACTORES
  public static List<VoTablaElemento> getCofactoresFaltantes() {

    final List<VoTablaElemento> r = new ArrayList<>();

    final String query = " SELECT A.NOM  NOM_COFACTOR  FROM " +
        " (SELECT  DISTINCT  O100057.NOM NOM " +
        " FROM ( SELECT SUM(O100140.MONTANT_DOS) FX100,  " +
        " O100140.REFELEM            FX101, " +
        " O100140.TYPELEM            FX102 " +
        " FROM       G_VENELEM     O100140 " +
        " GROUP BY   O100140.TYPELEM,  O100140.REFELEM  ), " +
        " AD_COFACTORS             O100057, " +
        " AD_CASE_CONTRAT          O100062, " +
        " AD_RECEIVABLE            O100081, " +
        " AD_ACCOUNT_DEBTOR_CLIENT O100087, " +
        " AD_DEBTORS               O100089, " +
        " T_INDIVIDU               REF " +
        " WHERE  O100081.FI_REFELEM     =           FX101(+) " +
        " AND 'fi'                      =           FX102(+) " +
        " AND O100057.REFINDIVIDU(+)    =           O100087.COFACTOR " +
        " AND O100087.DEBTOR            =           O100089.DEBTOR_REFINDIVIDU " +
        " AND O100087.REFDOSS           =           O100081.GP_REFDOSS " +
        " AND O100062.REFDOSS(+)        =           O100087.CONTRACT " +
        " AND O100081.FI_TYPE           =           'FACTURE' " +
        " AND REF.REFINDIVIDU           =           O100062.CL_REF " +
        " AND REF.SOCIETE               =           'SICAV' " +
        " and nvl(ltrim(rtrim(O100057.NOM)),'X') != 'X' " +
        " GROUP  BY     O100057.NOM " +
        " UNION " +
        " SELECT  DISTINCT  O100057.NOM                           NOM " +
        " FROM (    SELECT SUM(O100140.MONTANT_DOS)       FX100, " +
        " O100140.REFELEM                FX101, " +
        " O100140.TYPELEM                FX102 " +
        " FROM   G_VENELEM         O100140 " +
        " GROUP  BY     O100140.TYPELEM,  O100140.REFELEM  ), " +
        " AD_COFACTORS                O100057, " +
        " AD_CASE_CONTRAT             O100062, " +
        " AD_RECEIVABLE               O100081, " +
        " AD_ACCOUNT_DEBTOR_CLIENT    O100087, " +
        " AD_DEBTORS                  O100089, " +
        " T_INDIVIDU                  REF " +
        " WHERE  O100081.FI_REFELEM         =             FX101(+) " +
        " AND    'fi'                       =             FX102(+) " +
        " AND    O100057.REFINDIVIDU(+)     =             O100087.COFACTOR " +
        " AND    O100087.DEBTOR             =             O100089.DEBTOR_REFINDIVIDU " +
        " AND    O100087.REFDOSS            =             O100081.GP_REFDOSS " +
        " AND    O100062.REFDOSS(+)         =             O100087.CONTRACT " +
        " AND    O100081.FI_TYPE            =            'FACTURE' " +
        " AND    REF.REFINDIVIDU            =             O100089.DEBTOR_REFINDIVIDU " +
        " AND    REF.SOCIETE                =            'SICAV' " +
        " GROUP  BY     O100057.NOM  )   A " +
        " GROUP  BY     A.NOM  MINUS " +
        " SELECT NOMBRE NOM_COFACTOR  FROM       FI_COFACTORES_IMX ";

    log.debug("query: " + query);
    try (final Connection conn = DBConnection.getConnBanks();
         final PreparedStatement ps = conn.prepareStatement(query);
         final ResultSet rs = ps.executeQuery()) {
      if (rs != null) {
        while (rs.next()) {
          final VoTablaElemento e = new VoTablaElemento();
          e.setElemento(rs.getString("NOM_COFACTOR"));
          r.add(e);
        }
        log.debug("\nresultado: " + r.size());
      }
    } catch (SQLException sqle) {
      log.error(sqle.getMessage(), sqle);
    }
    return r;
  }

  // CONSULTA PARA MOSTRAR LAS CIFRAS TOTALES DE STATUS FACTURAS
  public static List<VoCifrasTotalesEstatusFac> getCifrasTotalesEstatusFac(final Date fechaInicio,
                                                                           final Date fechaFin) {
    final List<VoCifrasTotalesEstatusFac> r = new ArrayList<>();

    final String fechaInicioStr = formatoyyyyMMdd.format(fechaInicio);
    final String fechaFinStr = formatoyyyyMMdd.format(fechaFin);

    final String query = " select fecha_valor,sum(esperadas) esperadas, sum(verificadas) verificadas, sum(gacd) gacd, " +
        "sum(folio_sat) folio_sat, sum(enviado_cliente) enviado_cliente, sum(canceladas) canceladas " +
        " from ( select fecha_valor,0 esperadas, count(*) verificadas , 0 gacd, 0 folio_sat, 0 enviado_cliente, " +
        "0 canceladas  from fi_bitacora_cfdi  where status_verificado = 'VERIFICADO'  group by fecha_valor " +
        " union  select fecha_valor,0 esperadas, 0 verificadas , count(*) gacd, 0 folio_sat, 0 enviado_cliente," +
        "0 canceladas  from fi_bitacora_cfdi  where status_verificado = 'VERIFICADO'  and fecha_ingreso_gacd " +
        "is not null  group by fecha_valor  union  select fecha_valor,0 esperadas, 0 verificadas , 0 gacd, " +
        "count(*) folio_sat, 0 enviado_cliente,0 canceladas  from fi_bitacora_cfdi  where " +
        "status_verificado = 'VERIFICADO'  and folio_sat is not null  and fecha_sat_gacd is not null " +
        " group by fecha_valor  union  select fecha_valor,0 esperadas, 0 verificadas , 0 gacd, 0 folio_sat, " +
        " count(*) enviado_cliente,0 canceladas  from fi_bitacora_cfdi  where status_verificado = 'VERIFICADO' " +
        " and fecha_envio_cliente is not null  group by fecha_valor  union  select fecha_valor,0 esperadas," +
        " 0 verificadas , 0 gacd, 0 folio_sat,  0 enviado_cliente, count(*) canceladas  from fi_bitacora_cfdi " +
        " where status_verificado = 'CANCELADO'  group by fecha_valor  union   select fecha_emision fecha_valor," +
        " max(total_esperado) esperadas , 0 verificadas , 0 gacd, 0 folio_sat,  0 enviado_cliente, 0 canceladas " +
        " from FI_cfd_cifras_control  group by fecha_emision  )  where fecha_valor " +
        " between to_date('" + fechaInicioStr + " 00:00:00', 'yyyy-MM-dd HH24:MI:SS')" +
        " and to_date('" + fechaFinStr + " 23:59:59', 'yyyy-MM-dd HH24:MI:SS') " +
        " group by fecha_valor  order by fecha_valor desc ";

    log.info("query: " + query);
    try (final Connection conn = DBConnection.getConnFactIMX();
         final PreparedStatement ps = conn.prepareStatement(query);
         final ResultSet rs = ps.executeQuery()) {
      if (rs != null) {
        while (rs.next()) {
          final VoCifrasTotalesEstatusFac bean = new VoCifrasTotalesEstatusFac();
          bean.setCifrasVerificado(rs.getInt("VERIFICADAS"));
          bean.setEsperadasImx(Math.max(rs.getInt("ESPERADAS"), bean.getCifrasVerificado()));
          bean.setFechaValor(rs.getDate("FECHA_VALOR"));
          bean.setCifrasGacd(rs.getInt("GACD"));
          bean.setCifrasFolioSat(rs.getInt("FOLIO_SAT"));
          bean.setCifrasEnviadoCliente(rs.getInt("ENVIADO_CLIENTE"));
          bean.setCifrasCancelado(rs.getInt("CANCELADAS"));
          bean.setFechaValorFor(formatoddMMyyyy.format(rs.getDate("FECHA_VALOR")));
          r.add(bean);
        }
        log.debug("\nresultado: " + r.size());
      }
    } catch (SQLException sqle) {
      log.error(sqle.getMessage(), sqle);
    }
    return r;
  }

  public static List<FiCfdVerificacion> getVerificacion(final Date fecha, final boolean esAutomatico,
                                                        final int diasProceso) {

    final List<FiCfdVerificacion> r = new ArrayList<>();

    final String fechaStr = formatoyyyyMMdd.format(fecha);

    final String query;
    if (esAutomatico) {
      query = " SELECT ver.*, enc.secuencia FROM FI_CFD_VERIFICACION ver, fi_facturas_encabezado enc " +
          " where ver.FECHA_EMISION = TRUNC(enc.fechavalor) and enc.no_acreditado  = ver.no_acreditado " +
          " AND VER.CONTRATO = ENC.CONTRATO  AND enc.fechavalor " +
          " between to_date('" + fechaStr + " 00:00:00', 'yyyy-MM-dd HH24:MI:SS')-" + diasProceso +
          " and to_date('" + fechaStr + " 23:59:59', 'yyyy-MM-dd HH24:MI:SS') AND ver.STATUS = 'VERIFICADO' " +
          " AND enc.CONTRATO= ver.contrato AND MOTIVO='Generacion Automatica'  ORDER BY ver.FECHA_EMISION ";
    } else {
      query = " SELECT ver.*, enc.secuencia FROM FI_CFD_VERIFICACION ver, fi_facturas_encabezado enc " +
          " where ver.FECHA_EMISION = TRUNC(enc.fechavalor) and enc.no_acreditado  = ver.no_acreditado " +
          " AND enc.fechavalor between to_date('" + fechaStr + " 00:00:00', 'yyyy-MM-dd HH24:MI:SS')" +
          " and to_date('" + fechaStr + " 23:59:59', 'yyyy-MM-dd HH24:MI:SS') " +
          " AND ver.STATUS = 'VERIFICADO'  and enc.CONTRATO= ver.contrato " +
          " ORDER BY ver.FECHA_EMISION ";
    }
    log.debug("query: " + query);

    try (final Connection conn = DBConnection.getConnFactIMX();
         final PreparedStatement ps = conn.prepareStatement(query);
         final ResultSet rs = ps.executeQuery()) {
      if (rs != null) {
        while (rs.next()) {

          final FiCfdVerificacionId beanId = new FiCfdVerificacionId();
          beanId.setNumAcreditado(rs.getInt("NO_ACREDITADO"));
          beanId.setFechaEmision(rs.getDate("FECHA_EMISION"));
          beanId.setContrato(rs.getString("CONTRATO"));

          final FiCfdVerificacion bean = new FiCfdVerificacion();
          bean.setId(beanId);
          bean.setOrden(rs.getBigDecimal("ORDEN"));
          bean.setTotal(rs.getBigDecimal("TOTAL"));
          bean.setStatus(rs.getString("STATUS"));
          bean.setFecha(rs.getDate("FECHA"));
          bean.setProducto(rs.getString("PRODUCTO"));
          bean.setMotivo(rs.getString("MOTIVO"));
          bean.setAdicionadoPor(rs.getString("ADICIONADO_POR"));
          bean.setFechaAdicion(rs.getDate("FECHA_ADICION"));
          bean.setModificadoPor(rs.getString("MODIFICADO_POR"));
          bean.setFechaModificacion(rs.getDate("FECHA_MODIFICACION"));
          bean.setUsuarioVerifica(rs.getString("USUARIO_VERIFICA"));
          bean.setStatusConciliado(rs.getString("STATUS_CONCILIADO"));
          bean.setSecuencia(rs.getString("SECUENCIA"));

          r.add(bean);
        }
        log.debug("\nresultado: " + r.size());
      }
    } catch (SQLException sqle) {
      log.error(sqle.getMessage(), sqle);
    }
    return r;
  }

  // CONSULTA PARA OBTENER DATOS DE LA TABLA DE ENCABEZADO
  public static List<FiCfdVerificacion> getCancelacion(final Date fechaValorIni, final Date fechaValorFin,
                                                       final Date fechaGeneraIni, final Date fechaGeneraFin) {
    final List<FiCfdVerificacion> r = new ArrayList<>();

    String query = "SELECT C.NUMERO_FOLIO, A.FECHA_EMISION, C.STATUS_FOLIO, A.STATUS_CONCILIADO, A.FECHA, " +
        " A.PRODUCTO, A.NO_ACREDITADO, B.CODIGOMONEDA, SUM (B.IMPORTE) AS IMPORTE, B.CONTRATO, A.ORDEN, C.SECUENCIA " +
        " FROM FI_CFD_VERIFICACION A, FI_FACTURAS_ENCABEZADO B, FI_CFD_HISTORICO_FOLIOS C " +
        " WHERE C.CODIGO_CLIENTE = B.NO_ACREDITADO " +
        " AND C.FECHA_ASIGNACION = B.FECHAVALOR " +
        " AND C.CONTRATO         = B.CONTRATO " +
        " AND B.NO_ACREDITADO    = A.NO_ACREDITADO " +
        " AND B.FECHAVALOR       = A.FECHA_EMISION " +
        " AND B.CONTRATO         = A.CONTRATO ";
    if (fechaValorIni != null) {
      query += "AND A.FECHA_EMISION > TO_DATE('" + formatoyyyyMMdd.format(fechaValorIni) + " 00:00:00','yyyy-MM-dd HH24:MI:SS')";
    }
    if (fechaValorFin != null) {
      query += "AND A.FECHA_EMISION < TO_DATE('" + formatoyyyyMMdd.format(fechaValorFin) + " 23:59:59','yyyy-MM-dd HH24:MI:SS')";
    }
    if (fechaGeneraIni != null) {
      query += "AND A.FECHA > TO_DATE('" + formatoyyyyMMdd.format(fechaGeneraIni) + " 00:00:00','yyyy-MM-dd HH24:MI:SS')";
    }
    if (fechaGeneraFin != null) {
      query += "AND A.FECHA < TO_DATE('" + formatoyyyyMMdd.format(fechaGeneraFin) + " 23:59:59','yyyy-MM-dd HH24:MI:SS')";
    }
    query += " GROUP BY C.NUMERO_FOLIO , A.FECHA_EMISION, C.STATUS_FOLIO, A.STATUS_CONCILIADO, A.FECHA, A.PRODUCTO, " +
        " A.NO_ACREDITADO, B.CODIGOMONEDA, B.CONTRATO, A.ORDEN,  C.SECUENCIA  ORDER BY C.NUMERO_FOLIO";
    log.info("query: " + query);

    try (final Connection conn = DBConnection.getConnFactIMX();
         final PreparedStatement ps = conn.prepareStatement(query);
         final ResultSet rs = ps.executeQuery()) {
      if (rs != null) {
        while (rs.next()) {

          final FiCfdVerificacionId verificacionBeanId = new FiCfdVerificacionId();
          verificacionBeanId.setNumAcreditado(rs.getInt("NO_ACREDITADO"));
          verificacionBeanId.setFechaEmision(rs.getDate("FECHA_EMISION"));
          verificacionBeanId.setContrato(rs.getString("CONTRATO"));

          final FiFacturasEncabezado encabezadoBean = new FiFacturasEncabezado();
          encabezadoBean.setCodigoMoneda(rs.getString("CODIGOMONEDA"));
          encabezadoBean.setImporte(rs.getBigDecimal("IMPORTE"));

          final FiCfdHistoricoFoliosId historicoBeanId = new FiCfdHistoricoFoliosId();
          historicoBeanId.setNumeroFolio(rs.getBigDecimal("NUMERO_FOLIO"));

          final FiCfdHistoricoFolios historicoBean = new FiCfdHistoricoFolios();
          historicoBean.setId(historicoBeanId);
          historicoBean.setSecuencia(rs.getBigDecimal("SECUENCIA"));

          final FiCfdVerificacion verificacionBean = new FiCfdVerificacion();
          verificacionBean.setId(verificacionBeanId);
          verificacionBean.setFiFacturasEncabezado(encabezadoBean);
          verificacionBean.setFiCfdHistoricoFolios(historicoBean);
          verificacionBean.setStatus(rs.getString("STATUS_FOLIO"));
          verificacionBean.setStatusConciliado(rs.getString("STATUS_CONCILIADO"));
          verificacionBean.setFecha(rs.getDate("FECHA"));
          verificacionBean.setProducto(rs.getString("PRODUCTO"));
          verificacionBean.setOrden(rs.getBigDecimal("ORDEN"));

          r.add(verificacionBean);
        }
        log.debug("\nresultado: " + r.size());
      }
    } catch (SQLException sqle) {
      log.error(sqle.getMessage(), sqle);
    }
    return r;
  }

  public static int updateEncabezado(final FiFacturasEncabezado e) {
    int r = 0;
    final String update = "UPDATE FI_FACTURAS_ENCABEZADO SET " +
        " RFC = '" + e.getRfc() + "' ," +
        " NOMBRE = '" + e.getNombre() + "' ," +
        " CALLE = '" + e.getCalle() + "' ," +
        " NOEXTERIOR = " + e.getNumExterior() + " ," +
        " NOINTERIOR = " + e.getNumInterior() + " ," +
        " COLONIA = '" + e.getColonia() + "' ," +
        " LOCALIDAD = '" + e.getLocalidad() + "' ," +
        " REFERENCIA = '" + e.getReferencia() + "' ," +
        " MUNICIPIO = '" + e.getMunicipio() + "' ," +
        " ESTADO = '" + e.getEstado() + "' ," +
        " PAIS = '" + e.getPais() + "' ," +
        " CODIGOPOSTAL = '" + e.getCodigoPostal() + "' ," +
        " LINEA = '" + e.getLinea() + "' ," +
        " TIPOCREDITO = '" + e.getTipoCredito() + "' ," +
        " CODIGOMONEDA = '" + e.getCodigoMoneda() + "' ," +
        " MONEDA = '" + e.getMoneda() + "' " +
        " WHERE SECUENCIA = " + e.getSecuencia();

    log.debug("update: " + update);

    try (final Connection conn = DBConnection.getConnFactIMX();
         final PreparedStatement ps = conn.prepareStatement(update)) {
      r = ps.executeUpdate();
      log.debug("\nresultado: " + r);
    } catch (SQLException sqle) {
      log.error(sqle.getMessage(), sqle);
    }
    return r;
  }


  public static List<FiCfdCifrasCtrl> getCifrasCtrl(final Date fechaValor, final int dias) {
    log.info("getCifrasCtrl");
    final List<FiCfdCifrasCtrl> r = new ArrayList<>();

    final String fechaStr = formatoyyyyMMdd.format(fechaValor);

    final String query = "select fecha_valor, sum(esperadas) esperadas, sum(verificadas) verificadas , " +
        " sum(gacd) gacd, sum(folio_sat) folio_sat, sum(enviado_cliente) enviado_cliente, " +
        " sum(canceladas) canceladas " +
        " from ( " +
        " select fecha_valor, 0 esperadas, count(*) verificadas , 0 gacd, 0 folio_sat, 0 enviado_cliente,0 canceladas " +
        " from fi_bitacora_cfdi  where status_verificado = 'VERIFICADO' " +
        " group by fecha_valor  union " +
        " select fecha_valor, 0 esperadas, 0 verificadas , count(*) gacd, 0 folio_sat, 0 enviado_cliente,0 canceladas " +
        " from fi_bitacora_cfdi  where status_verificado = 'VERIFICADO'  and fecha_ingreso_gacd is not null " +
        " group by fecha_valor  union " +
        " select fecha_valor, 0 esperadas, 0 verificadas , 0 gacd, count(*) folio_sat, 0 enviado_cliente,0 canceladas " +
        " from fi_bitacora_cfdi  where status_verificado = 'VERIFICADO'  and folio_sat is not null  " +
        " and fecha_sat_gacd is not null  group by fecha_valor  union " +
        " select fecha_valor, 0 esperadas, 0 verificadas , 0 gacd, 0 folio_sat,  count(*) enviado_cliente,0 canceladas " +
        " from fi_bitacora_cfdi  where status_verificado = 'VERIFICADO'  and fecha_envio_cliente is not null " +
        " group by fecha_valor  union " +
        " select fecha_valor, 0 esperadas, 0 verificadas , 0 gacd, 0 folio_sat,  0 enviado_cliente, count(*) canceladas " +
        " from fi_bitacora_cfdi  where status_verificado = 'CANCELADO'  group by fecha_valor  union " +
        " select fecha_emision fecha_valor, max(total_esperado) esperadas , 0 verificadas , 0 gacd, 0 folio_sat, " +
        " 0 enviado_cliente, 0 canceladas  from FI_cfd_cifras_control  group by fecha_emision ) " +
        " where fecha_valor  between to_date('" + fechaStr + " 00:00:00', 'yyyy-MM-dd HH24:MI:SS')-" + dias + " and " +
        " to_date('" + fechaStr + " 23:59:59', 'yyyy-MM-dd HH24:MI:SS') group by fecha_valor order by fecha_valor desc ";

    log.debug("query: " + query);

    try (final Connection conn = DBConnection.getConnFactIMX();
         final PreparedStatement ps = conn.prepareStatement(query);
         final ResultSet rs = ps.executeQuery()) {
      if (rs != null) {
        while (rs.next()) {
          final FiCfdCifrasCtrl c = new FiCfdCifrasCtrl();
          c.setVerificadas(rs.getInt("verificadas"));
          c.setEsperadas(Math.max(rs.getInt("esperadas"), c.getVerificadas()));
          c.setFechaValor(formatoddMMyyyy.format(rs.getDate("fecha_valor")));
          c.setGacd(rs.getInt("gacd"));
          c.setFolioSat(rs.getInt("folio_sat"));
          c.setEnviadoCliente(rs.getInt("enviado_cliente"));
          c.setCanceladas(rs.getInt("canceladas"));
          r.add(c);
        }
        log.debug("\nresultado: " + r.size());
      }
    } catch (SQLException sqle) {
      log.error(sqle.getMessage(), sqle);
    }
    return r;
  }
}
