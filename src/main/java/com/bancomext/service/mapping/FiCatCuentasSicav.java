package com.bancomext.service.mapping;

import lombok.Data;

import java.io.Serializable;

@Data
public class FiCatCuentasSicav implements Serializable {
  private FiCatCuentasSicavId id;

  public FiCatCuentasSicav() {
    id = new FiCatCuentasSicavId();
  }
}


