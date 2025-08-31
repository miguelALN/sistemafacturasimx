package com.bancomext.service.mapping;

import lombok.Data;

import java.io.Serializable;

@Data
public class FiCorreosPorClienteId implements Serializable, Cloneable {

  private long codigoCliente;
  private String email;

  @Override
  public FiCorreosPorClienteId clone() {
    try {
      return (FiCorreosPorClienteId) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new AssertionError();
    }
  }
}


