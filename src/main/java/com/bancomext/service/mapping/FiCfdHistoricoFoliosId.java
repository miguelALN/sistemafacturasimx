package com.bancomext.service.mapping;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FiCfdHistoricoFoliosId implements Serializable, Cloneable {

  private Date fechaAutorizacion;
  private String noAutorizacion;
  private BigDecimal numeroFolio;

  @Override
  public FiCfdHistoricoFoliosId clone() {
    try {
      return (FiCfdHistoricoFoliosId) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new AssertionError();
    }
  }
}


