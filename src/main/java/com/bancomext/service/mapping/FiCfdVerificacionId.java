package com.bancomext.service.mapping;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class FiCfdVerificacionId implements Serializable, Cloneable {

  private Integer numAcreditado;
  private String contrato;
  private Date fechaEmision;

  @Override
  public FiCfdVerificacionId clone() {
    try {
      return (FiCfdVerificacionId) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new AssertionError();
    }
  }
}


