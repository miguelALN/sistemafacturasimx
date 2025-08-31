package com.bancomext.service.mapping;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class FiBitacoraCfdi implements Serializable {

  private String serieFolio;
  private Date fechaStatus;
  private Date fechaEnvioCliente;
  private String status;
  private String accion;
  private String statusVerificado;

}
