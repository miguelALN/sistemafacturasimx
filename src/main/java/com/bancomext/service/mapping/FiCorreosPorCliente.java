package com.bancomext.service.mapping;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class FiCorreosPorCliente implements Serializable, Cloneable {

  private FiCorreosPorClienteId id;
  private String descripcionCliente;
  private String adicionadoPor;
  private Date fechaAdicion;
  private String modificadoPor;
  private Date fechaModificacion;

  public FiCorreosPorCliente() {
    id = new FiCorreosPorClienteId();
  }

  @Override
  public FiCorreosPorCliente clone() {
    try {
      FiCorreosPorCliente clone = (FiCorreosPorCliente) super.clone();
      clone.id = id.clone();
      return clone;
    } catch (CloneNotSupportedException e) {
      throw new AssertionError();
    }
  }
}