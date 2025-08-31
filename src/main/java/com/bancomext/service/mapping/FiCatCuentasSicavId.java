package com.bancomext.service.mapping;

import lombok.Data;

import java.io.Serializable;

@Data
public class FiCatCuentasSicavId implements Serializable, Cloneable {

  private String cta;
  private String scta;
  private String sscta;
  private String ssscta;

  @Override
  public FiCatCuentasSicavId clone() {
    try {
      return (FiCatCuentasSicavId) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new AssertionError();
    }
  }

}

