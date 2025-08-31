package com.bancomext.service.mapping;

import lombok.Data;

import java.io.Serializable;

@Data
public class FiFacturasDetalleId implements Serializable, Cloneable {

  private long secuencia;
  private String secuenciaimx;
  private String codigoconcepto;

  @Override
  public FiFacturasDetalleId clone() {
    try {
      return (FiFacturasDetalleId) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new AssertionError();
    }
  }
}


