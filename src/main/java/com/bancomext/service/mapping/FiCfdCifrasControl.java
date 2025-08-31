package com.bancomext.service.mapping;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class FiCfdCifrasControl implements Serializable {

  private FiCfdCifrasControlId id;
  private String tipoCartera;
  private BigDecimal total;
  private BigDecimal totalInformativo;
  private BigDecimal totalFiscal;
  private BigDecimal cfdTotal;
  private BigDecimal cfdTotalInforma;
  private BigDecimal cfdTotalFiscal;
  private BigDecimal orden;
  private Date fechaProceso;
  private BigDecimal totalVerificacion;
  private BigDecimal totalEsperado;
  private BigDecimal pendientes;
  private String motivoError;
  private BigDecimal totalError;

}


