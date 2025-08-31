package com.bancomext.service.mapping;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class VoCifrasPrimeComisiones implements Serializable {

  private int orden;
  private Date fechaEmision;
  private int total;
  private String status;
  private String motivo;
  private String folios;
  private Date fecha;
  private String producto;
  private String usuarioVerifica;

}
