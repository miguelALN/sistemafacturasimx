package com.bancomext.service.mapping;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class FiModulosMenu implements Serializable {

  private short idModulo;
  private String descripcion;
  private String adicionadoPor;
  private Date fechaAdicion;
  private String modificadoPor;
  private Date fechaModificacion;

}


