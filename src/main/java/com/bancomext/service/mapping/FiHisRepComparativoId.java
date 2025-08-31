package com.bancomext.service.mapping;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class FiHisRepComparativoId implements Serializable, Cloneable {

  private Date fechaProceso;
  private String campoA;
  private String cta;
  private String scta;
  private String sscta;
  private String ssscta;
  private String contrato;

  @Override
  public FiHisRepComparativoId clone() {
    try {
      return (FiHisRepComparativoId) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new AssertionError();
    }
  }
}


