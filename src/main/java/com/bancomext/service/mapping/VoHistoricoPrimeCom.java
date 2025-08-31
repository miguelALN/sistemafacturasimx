package com.bancomext.service.mapping;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class VoHistoricoPrimeCom implements Serializable {

  private String numeroFolio;
  private Date fechaAsignacion;
  private int codigoCliente;
  private BigDecimal importe;

}
