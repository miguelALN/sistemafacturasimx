package com.bancomext.service.mapping;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class FiBitacoraImx implements Serializable {

  private long id;
  private String atributo3;
  private String atributo4;
  private String campoA;
  private String opeCode;
  private String nomCliente;
  private String cuentaOrigen;
  private String cuentaCargo;
  private String cuentaAbono;
  private String cuentaDestino;
  private String cuentaSust1;
  private String cuentaSust2;

  private BigDecimal montoCtaOrigen;
  private String montoCtaOriFor;

  private BigDecimal montoCtaCargo;
  private String montoCtaCarFor;

  private BigDecimal montoCtaAbono;
  private String montoCtaAboFor;

  private BigDecimal montoDestino;
  private String montoDesFor;

  private BigDecimal montoSustitucion;
  private String montoSusFor;

  private BigDecimal montoSicav;
  private String montoSicavFor;

  private BigDecimal montoCofactores;
  private String montoCofFor;

  private BigDecimal montoSust2;
  private String montoSust2For;

  private BigDecimal montoCargoAnt;
  private BigDecimal montoAbonoAnt;
  private BigDecimal montoCofactores2;
  private BigDecimal montoSust1;

  private BigDecimal tipoCambio;
  private String moneda;
  private String monedaCof;
  private String nomCofactor;
  private String monedaSust;
  private String contrato;
  private String campoB;
  private Date fechaProceso;
  private String tipoProceso;
  private String ejecucion;
  private String operacionFiu;
  private Date fechaAdicion;
  private Date fechaModificacion;
  private String adicionadoPor;
  private String cuentaSustitucion;
  private String desMon;
  private String modificadoPor;
  private String usuario;
  private Integer ctaAbono;
  private Integer ctaCargo;
  private Integer ctaDestino;
  private Integer ctaOrigen;
  private Integer ctaSust1;
  private Integer ctaSust2;
  private Integer depto;
  private Integer feCaa;
  private Integer feCad;
  private Integer feCam;
  private Integer flag;
  private Integer numPol;
  private Integer sctaAbono;
  private Integer sctaCargo;
  private Integer sctaDestino;
  private Integer sctaOrigen;
  private Integer sctaSust1;
  private Integer sctaSust2;
  private Integer ssCtaAbono;
  private Integer ssCtaCargo;
  private Integer ssCtaDestino;
  private Integer ssCtaOrigen;
  private Integer ssctaSust1;
  private Integer ssctaSust2;
  private Integer sssCtaAbono;
  private Integer sssCtaCargo;
  private Integer sssCtaDestino;
  private Integer sssCtaOrigen;
  private Integer sssctaSust1;
  private Integer sssctaSust2;
  private Integer suCcon;

}
