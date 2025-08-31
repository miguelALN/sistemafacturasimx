package com.bancomext.service.mapping;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class FiClientesSinCfdi implements Serializable {
  private long id;
  private String descripcionCliente;
  private String adicionadoPor;
  private Date fechaAdicion;
  private String modificadoPor;
  private Date fechaModificacion;

  public FiClientesSinCfdi() {
  }

  public FiClientesSinCfdi(long id, String descripcionCliente, String adicionadoPor, Date fechaAdicion, String modificadoPor, Date fechaModificacion) {
    this.id = id;
    this.descripcionCliente = descripcionCliente;
    this.adicionadoPor = adicionadoPor;
    this.fechaAdicion = fechaAdicion;
    this.modificadoPor = modificadoPor;
    this.fechaModificacion = fechaModificacion;
  }

  public FiClientesSinCfdi(long id, String descripcionCliente) {
    this.id = id;
    this.descripcionCliente = descripcionCliente;
  }

}
