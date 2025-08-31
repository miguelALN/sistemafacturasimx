package com.bancomext.service.mapping;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class VoReporteFacturasMensual implements Serializable {

  private String cliente;
  private int noFactura;
  private Date fecha;
  private String fechaFor;
  private String concepto;
  private BigDecimal total;
  private String totalFor;
  private int codigoCliente;

}
