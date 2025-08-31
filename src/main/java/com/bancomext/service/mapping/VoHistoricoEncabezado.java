package com.bancomext.service.mapping;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class VoHistoricoEncabezado implements Serializable {

  private String producto;
  private int codigoCliente;
  private int numeroFolio;
  private String nombre;
  private String moneda;
  private Date fechaValor;
  private BigDecimal importe;

}
