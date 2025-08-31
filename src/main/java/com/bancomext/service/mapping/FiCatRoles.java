package com.bancomext.service.mapping;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class FiCatRoles implements Serializable {

  private Integer idRol;
  private String descripcion;
  private String adicionadoPor;
  private Date fechaAdicion;
  private String modificadoPor;
  private Date fechaModificacion;
}

