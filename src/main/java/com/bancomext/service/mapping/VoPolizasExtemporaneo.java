package com.bancomext.service.mapping;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class VoPolizasExtemporaneo implements Serializable {

  private String poliza;
  private String opCode;
  private String descripcion;
  private BigDecimal cargo;
  private BigDecimal abono;
  private String cargoFor;
  private String abonoFor;

}
