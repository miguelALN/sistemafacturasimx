package com.bancomext.service.mapping;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class FiLogErrores implements Serializable {

  private short idLog;
  private Date fechaCreacion;
  private String usuarioProceso;
  private String modulo;
  private String proceso;
  private String stacktrace;
  private String mensaje;
  private String adicionadoPor;
  private Date fechaAdicion;
  private String modificadoPor;
  private Date fechaModificacion;

}


