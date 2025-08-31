package com.bancomext.service.mapping;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class VoCofactoresTmp implements Serializable {

  private String campoA;
  private String cliente;
  private String cuentaOrigen;
  private BigDecimal saldoSicav;
  private String saldoSicavFor;
  private BigDecimal e102142;
  private String e102142For;
  private int numCof;
  private String orden;
  private boolean diferencia;

}
