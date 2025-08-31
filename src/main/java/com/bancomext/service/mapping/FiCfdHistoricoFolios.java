package com.bancomext.service.mapping;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class FiCfdHistoricoFolios implements Serializable, Cloneable {

  private FiCfdHistoricoFoliosId id;
  private BigDecimal importe;
  private BigDecimal orden;
  private BigDecimal secuencia;
  private Date fechaAdicion;
  private Date fechaAsignacion;
  private Date fechaModificacion;
  private BigDecimal codigoCliente;
  private BigDecimal versionInfor;
  private String adicionadoPor;
  private String codigoLineaCredito;
  private String contrato;
  private String descripcionCliente;
  private String enviadoCliente;
  private String modificadoPor;
  private String moneda;
  private String monedaCuerpo;
  private String observacionesFolio;
  private String producto;
  private String serie;
  private String serieFolioSat;
  private String statusFolio;
  private String tipoCredito;

  @Override
  public FiCfdHistoricoFolios clone() {
    try {
      final FiCfdHistoricoFolios clone = (FiCfdHistoricoFolios) super.clone();
      clone.id = id.clone();
      return clone;
    } catch (CloneNotSupportedException e) {
      throw new AssertionError();
    }
  }

}


