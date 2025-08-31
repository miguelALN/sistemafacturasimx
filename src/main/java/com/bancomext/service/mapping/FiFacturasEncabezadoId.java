package com.bancomext.service.mapping;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class FiFacturasEncabezadoId implements Serializable, Cloneable {

  private Integer numAcreditado;
  private String contrato;
  private Date fechaValor;

  @Override
  public FiFacturasEncabezadoId clone() {
    try {
      final FiFacturasEncabezadoId clone = (FiFacturasEncabezadoId) super.clone();
      if (clone.getContrato() == null) {
        clone.setContrato("");
      }
      return clone;
    } catch (CloneNotSupportedException e) {
      throw new AssertionError();
    }
  }

}


