package com.bancomext.service.mapping;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class VoPolizasExtemporaneoDetalle implements Serializable {

  private int poliza;
  private String campoA;
  private String campoB;
  private String cuentaOrigen;
  private String opCode;
  private String descripcion;
  private BigDecimal cargo;
  private BigDecimal abono;
  private String cargoFor;
  private String abonoFor;

}
