package com.bancomext.service.mapping;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class VoCifrasTotalesEstatusFac implements Serializable {

  private int cifrasVerificado;
  private int cifrasGacd;
  private int cifrasFolioSat;
  private int cifrasEnviadoCliente;
  private int cifrasCancelado;
  private Date fechaValor;
  private String fechaValorFor;
  private int esperadasImx;

}
