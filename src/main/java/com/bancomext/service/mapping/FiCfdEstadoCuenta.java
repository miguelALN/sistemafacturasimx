package com.bancomext.service.mapping;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class FiCfdEstadoCuenta implements Serializable {

  private FiCfdEstadoCuentaId id;
  private BigDecimal cliente;
  private BigDecimal lc;
  private String moneda;
  private String producto;
  private BigDecimal orden;
  private Date fechaProceso;

}


