package com.bancomext.service.mapping;

import lombok.Data;

import java.io.Serializable;

@Data
public class VoSaldosMoneda implements Serializable {

  private String saldoMoneda;
  private String descripcion;
}

