package com.bancomext.service.mapping;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class FiParamRoles implements Serializable {

  private FiParamRolesId id;
  private FiCatRoles fiCatRoles;
  private FiModulosMenu fiModulosMenu;
  private String adicionadoPor;
  private Date fechaAdicion;
  private String modificadoPor;
  private Date fechaModificacion;

}


