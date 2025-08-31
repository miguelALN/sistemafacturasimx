package com.bancomext.service.mapping;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class FiCancelacionImxTmp implements Serializable {

  private Integer id;
  private String opCode;
  private String campoA;
  private String nomCliente;
  // CUENTA ORIGEN
  private String cuentaOrigen;
  private Integer ctaOrigen;
  private Integer sctaOrigen;
  private Integer ssctaOrigen;
  private Integer sssctaOrigen;
  private BigDecimal montoOrigen;
  private BigDecimal montoOrigenSSivac;
  private String montoOriFor; // VALOR QUE SE PINTA EN LA VISTA FORMATEADO CORRESPONDE A montoOrigenSSivac
  // CUENTA CARGO
  private String cuentaCargo;
  private Integer ctaCargo;
  private Integer sctaCargo;
  private Integer ssctaCargo;
  private Integer sssctaCargo;
  private BigDecimal montoCargo; // MONTO_CARGO_ORIGINAL
  private BigDecimal montoCargoSSivac;
  private String montoCarFor;  // VALOR QUE SE PINTA EN LA VISTA FORMATEADO CORRESPONDE A montoCargoSSivac
  // CUENTA ABONO
  private String cuentaAbono;
  private Integer ctaAbono;
  private Integer sctaAbono;
  private Integer ssctaAbono;
  private Integer sssctaAbono;
  private BigDecimal montoAbono; // MONTO_ABONO_ORIGINAL
  private String montoAbonoOrigFor;
  private BigDecimal montoAbonoSSivac;
  private String montoAbonoSicavFor; // VALOR QUE SE PINTA EN LA VISTA FORMATEADO CORRESPONDE A montoAbonoSSivac
  private BigDecimal saldoSivac;
  private Integer moneda;
  private String mensaje;
  private Integer suCcon;
  private Integer depto;
  private Integer numPol;
  private Integer feCad;
  private Integer feCam;
  private Integer feCaa;
  private boolean bandera;
  private Integer flag;
  private String atributo1;

}