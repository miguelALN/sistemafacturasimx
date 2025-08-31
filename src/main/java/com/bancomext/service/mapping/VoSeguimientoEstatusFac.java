package com.bancomext.service.mapping;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class VoSeguimientoEstatusFac implements Serializable {

  private Date fechaValor;
  private String fechaValorFormat;
  private int codigoCliente;
  private String nombreCliente;
  private String contrato;
  private String moneda;
  private String producto;
  private String serieFolio;
  private BigDecimal total;
  private String statusVerificado;
  private String fechaVerificacion;
  private String fechaIngresoGacd;
  private String fechaSatGacd;
  private String fechaEnvioCliente;
  private String fechaStatus;
  private String status;
  private String accion;
  private String folioSAT;
  private boolean seleccionado;

}
