package com.bancomext.service.mapping;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class FiCierreAntesDespues implements Serializable {

  private Integer id;
  private String opCode;
  private Integer moneda;
  private BigDecimal cargo;
  private BigDecimal abono;
  private BigDecimal diferencia;
  private String atributo1;
  private String atributo2;
  private Date fechaProceso;
  private String antesDespues;

}
