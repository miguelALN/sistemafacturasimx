package com.bancomext.service.mapping;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class FiCfdVerificacionBitaErr implements Serializable {

  private BigDecimal idFolio;
  private FiFacturasEncabezado fiFacturasEncabezado;
  private String status;
  private Date fecha;
  private String producto;
  private String motivo;
  private String adicionadoPor;
  private Date fechaAdicion;
  private String modificadoPor;
  private Date fechaModificacion;
  private String statusConciliado;

}


