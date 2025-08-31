package com.bancomext.service.mapping;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class FiBitacoraExtemporImx implements Serializable {

  private Integer id;
  private String opCode;
  private String obsrv2;
  private Integer numPol;
  private Integer moneda;
  private BigDecimal cargo;
  private String cargoFor;
  private String abonoFor;
  private BigDecimal abono;
  private BigDecimal diferencia;
  private String diferenciaFor;
  private Integer fecaa;
  private Integer fecam;
  private Integer fecad;
  private String atrb1;
  private String atrb2;
  private Date fechaProceso;
  private String fechaProcesoFor;

}
