package com.bancomext.service.mapping;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class FiFacturasDetalle implements Serializable {

  private FiFacturasDetalleId id;
  private String concepto;
  private String codigodeudor;
  private String deudor;
  private short plazo;
  private String periodo;
  private BigDecimal tasa;
  private BigDecimal importe;
  private BigDecimal importeiva;
  private BigDecimal totalimporte;
  private BigDecimal totalvalorizado;
  private BigDecimal porcIva;

}


