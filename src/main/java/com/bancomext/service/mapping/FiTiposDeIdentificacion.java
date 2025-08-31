package com.bancomext.service.mapping;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class FiTiposDeIdentificacion implements Serializable {

  private short codigoTipoIdentificacion;
  private String descripcion;
  private String formato;
  private String documentacionDeFormato;
  private String personaNatural;
  private String adicionadoPor;
  private Date fechaAdicion;
  private String modificadoPor;
  private Date fechaModificacion;

}


