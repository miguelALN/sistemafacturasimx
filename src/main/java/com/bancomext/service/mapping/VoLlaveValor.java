package com.bancomext.service.mapping;

import lombok.Data;

import java.io.Serializable;

@Data
public class VoLlaveValor implements Serializable {

  private String llave;
  private String valor;

  public VoLlaveValor(final String llave, final String valor) {
    this.llave = llave;
    this.valor = valor;
  }

}


