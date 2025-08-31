package com.bancomext.service.mapping;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class FiCapitalizacionImxTmp implements Serializable {

  private Integer id;
  private String opCode;
  private String campoA;
  private String nomCliente;

  private String cuentaOrigen;
  private Integer ctaOrigen;
  private Integer sctaOrigen;
  private Integer ssCtaOrigen;
  private Integer sssCtaOrigen;
  private BigDecimal montoOrigen;
  private String montoOriFor; // VALOR QUE SE PINTA EN LA VISTA FORMATEADO CORRESPONDE A montoOrigen

  private String cuentaDestino;
  private Integer ctaDestino;
  private Integer sctaDestino;
  private Integer ssCtaDestino;
  private Integer sssCtaDestino;
  private BigDecimal montoDestino;
  private String montoDesFor; // VALOR QUE SE PINTA EN LA VISTA FORMATEADO CORRESPONDE A montoDestino

  private String cuentaSustitucion; // VALOR QUE SE PINTA EN LA VISTA
  private BigDecimal montoSustitucion;
  private String montoSusFor; // VALOR QUE SE PINTA EN LA VISTA FORMATEADO CORRESPONDE A montoSustitucion

  private String cuentaSust1;
  private Integer ctaSust1;
  private Integer sCtaSust1;
  private Integer ssCtaSust1;
  private Integer sssCtaSust1;
  private BigDecimal montoSust1;

  private String cuentaSust2;
  private Integer ctaSust2;
  private Integer sCtaSust2;
  private Integer ssCtaSust2;
  private Integer sssCtaSust2;

  private BigDecimal montoSust2;
  private String montoSust2For;

  private Integer moneda;
  private String mensaje;
  private Integer suCcon;
  private Integer depto;
  private Integer numPol;
  private Integer feCad;
  private Integer feCam;
  private Integer feCaa;

  private boolean bandera;
  private String desMon;
  private BigDecimal tipoCambio;

}
