package com.bancomext.service.mapping;

import lombok.Data;

import java.io.Serializable;

@Data
public class FiCfdCifrasCtrl implements Serializable {

  private String fechaValor;
  private Integer esperadas;
  private Integer verificadas;
  private Integer gacd;
  private Integer folioSat;
  private Integer enviadoCliente;
  private Integer canceladas;

}
