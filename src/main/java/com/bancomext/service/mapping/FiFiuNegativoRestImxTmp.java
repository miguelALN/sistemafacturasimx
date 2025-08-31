package com.bancomext.service.mapping;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class FiFiuNegativoRestImxTmp implements Serializable {

  private Integer sucCon;
  private Integer depto;
  private String opCode;
  private String campoA;
  private Integer numPol;
  private String nomCliente;

  private String cuentaOrigen;
  private Integer ctaOrigen;
  private Integer sctaOrigen;
  private Integer ssCtaOrigen;
  private Integer sssCtaOrigen;
  private BigDecimal montoOrigensSivac;
  private String montoOriFor;  // VALOR QUE SE MUESTRA EN LA VISTA QUE CORRESPONDE AL CAMPO montoOrigensSivac

  private String cuentaCargo;
  private Integer ctaCargo;
  private Integer sctaCargo;
  private Integer ssCtaCargo;
  private Integer sssCtaCargo;
  private BigDecimal montoCargosSivac;
  private String montoCarFor; // VALOR QUE SE MUESTRA EN LA VISTA QUE CORRESPONDE AL CAMPO montoCargosSivac

  private String cuentaAbono;
  private Integer ctaAbono;
  private Integer sctaAbono;
  private Integer ssCtaAbono;
  private Integer sssCtaAbono;
  private BigDecimal montoAbonosSivac;
  private String montoAboFor; // VALOR QUE SE MUESTRA EN LA VISTA QUE CORRESPONDE AL CAMPO montoAbonosSivac

  private BigDecimal saldoSivac;
  private Integer moneda;
  private BigDecimal montoCargoOriginal;
  private BigDecimal montoAbonoOriginal;
  private Integer fecAd;
  private Integer fecAm;
  private Integer fecAa;
  private String mensaje;
  private Integer id;
  private String atributo1;
  private String atributo2;
  private boolean bandera;

}
