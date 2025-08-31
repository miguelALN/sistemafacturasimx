package com.bancomext.service.mapping;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class FiCfdCifrasControlId implements Serializable {

  private Date fechaEmision;
  private String producto;
  private String tpocredito;

  @Override
  public FiCfdCifrasControlId clone() {
    try {
      return (FiCfdCifrasControlId) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new AssertionError();
    }
  }
}


