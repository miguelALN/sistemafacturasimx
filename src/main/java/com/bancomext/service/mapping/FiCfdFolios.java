package com.bancomext.service.mapping;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class FiCfdFolios implements Serializable {

  private FiCfdFoliosId id;
  private BigDecimal primerFolio;
  private BigDecimal ultimoFolio;
  private BigDecimal foliosUtilizados;
  private BigDecimal foliosDiponibles;
  private BigDecimal porcentajeFoliosDisponibles;
  private BigDecimal foliosCancelados;
  private String producto;
  private String serie;
  private Integer orden;

}
