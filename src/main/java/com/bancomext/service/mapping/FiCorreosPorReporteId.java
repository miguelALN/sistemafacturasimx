package com.bancomext.service.mapping;

import lombok.Data;

import java.io.Serializable;

@Data
public class FiCorreosPorReporteId implements Serializable, Cloneable {

  private short idGrupoReporte;
  private String email;

  @Override
  public FiCorreosPorReporteId clone() {
    try {
      return (FiCorreosPorReporteId) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new AssertionError();
    }
  }

}


