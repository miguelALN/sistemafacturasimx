package com.bancomext.service.mapping;

import lombok.Data;

import java.io.Serializable;

@Data
public class VoSumasMontoSsivac implements Serializable {

  private String montoOrigen;
  private String montoCargo;
  private String montoAbono;
  private String montoDestino;
  private String montoSustitucion1;
  private String montoSicav;
  private String montoCofactor;

}
