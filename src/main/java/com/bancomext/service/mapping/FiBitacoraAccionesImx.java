package com.bancomext.service.mapping;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class FiBitacoraAccionesImx implements Serializable {

  private BigDecimal id;
  private String folio;
  private String estatus;
  private String accion;
  private String adicionadoPor;
  private Date fechaAdicion;

}
