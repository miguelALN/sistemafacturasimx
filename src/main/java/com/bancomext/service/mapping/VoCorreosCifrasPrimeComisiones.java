package com.bancomext.service.mapping;

import lombok.Data;

import java.io.Serializable;

@Data
public class VoCorreosCifrasPrimeComisiones implements Serializable {

  private String usuario;
  private String correo;
  private String activo;
  private String activoCom;
  private String activoFac;

}
