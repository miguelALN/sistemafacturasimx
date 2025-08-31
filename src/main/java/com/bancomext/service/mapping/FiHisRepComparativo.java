package com.bancomext.service.mapping;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class FiHisRepComparativo implements Serializable {

  private FiHisRepComparativoId id;
  private BigDecimal montoImx;
  private BigDecimal montoSicav;
  private BigDecimal montoDif;
  private String nombreCliente;
  private String cuenta;

}


