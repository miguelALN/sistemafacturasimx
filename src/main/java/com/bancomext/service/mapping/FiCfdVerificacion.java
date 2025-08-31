package com.bancomext.service.mapping;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class FiCfdVerificacion implements Serializable, Cloneable {
  private FiCfdVerificacionId id;
  private FiFacturasEncabezado fiFacturasEncabezado;
  private FiCfdHistoricoFolios fiCfdHistoricoFolios;
  private BigDecimal orden;
  private BigDecimal total;
  private Date fecha;
  private Date fechaAdicion;
  private Date fechaModificacion;
  private String adicionadoPor;
  private String folioIMX;
  private String modificadoPor;
  private String motivo;
  private String producto;
  private String secuencia;
  private String status;
  private String statusConciliado;
  private String usuarioVerifica;
  private boolean seleccionado;

  @Override
  public FiCfdVerificacion clone() {
    try {
      final FiCfdVerificacion clone = (FiCfdVerificacion) super.clone();
      clone.id = id.clone();
      clone.fiFacturasEncabezado = fiFacturasEncabezado.clone();
      clone.fiCfdHistoricoFolios = fiCfdHistoricoFolios.clone();
      return clone;
    } catch (CloneNotSupportedException e) {
      throw new AssertionError();
    }
  }
}
