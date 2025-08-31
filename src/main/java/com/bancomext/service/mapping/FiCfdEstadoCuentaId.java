package com.bancomext.service.mapping;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class FiCfdEstadoCuentaId implements Serializable, Cloneable {

  private Date fecha;
  private BigDecimal folio;
  private String folioCfd;
  private String tramaId;
  private BigDecimal secuencia;
  private String trama;

  @Override
  public FiCfdEstadoCuentaId clone() {
    try {
      return (FiCfdEstadoCuentaId) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new AssertionError();
    }
  }
}
