package com.bancomext.service.mapping;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class FiCofactoresImxTmp implements Serializable {

  private String campoA;
  private String campoAcof;
  private String campoB;
  private String contratoImx;
  private Integer ctaOrigen;
  private String cuentaOrigen;
  private Integer depto;
  private BigDecimal e102128;
  private BigDecimal e102142;
  private String e102142For; // VALOR QUE SE MUESTRA EN LA VISTA CORRESPONDE A e102142
  private BigDecimal saldoSicav;
  private String saldoSicavFor; // VALOR QUE SE MUESTRA EN LA VISTA CORRESPONDE A saldoSicav
  private BigDecimal e103447;
  private BigDecimal e103450;
  private BigDecimal e103453;
  private Integer fecAA;
  private Integer feCad;
  private Integer feCam;
  private Integer id;
  private String mensaje;
  private Integer moneda;
  private String monedaCof;
  private Integer montoAbonoOriginal;
  private Integer montoCargoOriginal;
  private String nomCliente;
  private String nomClienteCof;
  private String nomCofactor;
  private Integer numPol;
  private String opCode;
  private Integer sctaOrigen;
  private Integer ssCtaOrigen;
  private Integer sssCtaOrigen;
  private Integer sucCon;
  private boolean diferencia;
  private String atributo1;
  private String atributo2;

}
