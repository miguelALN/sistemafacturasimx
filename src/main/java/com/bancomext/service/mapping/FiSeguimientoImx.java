package com.bancomext.service.mapping;

import lombok.Data;

import java.io.Serializable;

@Data
public class FiSeguimientoImx implements Serializable {

  private String fechaEmision;
  private String status;
  private String fechaGeneracion;
  private String rfc;
  private String moneda;
  private Double importe;
  private String campoA;
  private String folioImx;

}
