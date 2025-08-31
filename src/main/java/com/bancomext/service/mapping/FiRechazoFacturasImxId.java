package com.bancomext.service.mapping;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class FiRechazoFacturasImxId implements Serializable, Cloneable {

  private Date posAccDate;
  private String postPaperInvoiceNumber;
  private String posPostCcy;
  private String cliClientRefindividu;
  private String posPostNum;

  @Override
  public FiRechazoFacturasImxId clone() {
    try {
      return (FiRechazoFacturasImxId) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new AssertionError();
    }
  }
}


