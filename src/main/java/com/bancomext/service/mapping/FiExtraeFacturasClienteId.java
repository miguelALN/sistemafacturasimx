package com.bancomext.service.mapping;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class FiExtraeFacturasClienteId implements Serializable, Cloneable {

  private Date posAccDate;
  private String postPaperInvoiceNumber;
  private String posPostCcy;
  private String cliClientRefindividu;
  private String posPostNum;
  private Integer campoA;

  @Override
  public FiExtraeFacturasClienteId clone() {
    try {
      return (FiExtraeFacturasClienteId) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new AssertionError();
    }
  }
}

