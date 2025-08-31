package com.bancomext.service.mapping;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class FiCorreosPorReporte implements Serializable, Cloneable {

  private FiCorreosPorReporteId id;
  private FiGposReporte fiGposReporte;
  private String adicionadoPor;
  private Date fechaAdicion;
  private String modificadoPor;
  private Date fechaModificacion;

  public FiCorreosPorReporte() {
    id = new FiCorreosPorReporteId();
    fiGposReporte = new FiGposReporte();
  }

  @Override
  public FiCorreosPorReporte clone() {
    try {
      FiCorreosPorReporte clone = (FiCorreosPorReporte) super.clone();
      clone.id = id.clone();
      clone.fiGposReporte = fiGposReporte;
      return clone;
    } catch (CloneNotSupportedException e) {
      throw new AssertionError();
    }
  }
}


