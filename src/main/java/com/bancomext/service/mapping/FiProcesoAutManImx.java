package com.bancomext.service.mapping;

import lombok.Data;

import java.io.Serializable;

@Data
public class FiProcesoAutManImx implements Serializable {

  private String id;
  private String nombreProceso;
  private String tipoProceso;
  private String ejecucion;
  private String hora;
  private String procesado;
}
