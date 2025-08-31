package com.bancomext.service.mapping;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class FiCofactoresImxDetTmp implements Serializable {

  private String clientRefInd;
  private String campoA;
  private String contrato;
  private String nomCliente;
  private String nomCofactor;
  private BigDecimal e102142Sum;
  private String e102142SumFormat;
  private BigDecimal e102128Sum;
  private String e102128SumFormat;
  private String atributo1;
  private String atributo2;
  private BigDecimal saldoAux1;
  private BigDecimal saldoAux2;
  private Integer id;

}
