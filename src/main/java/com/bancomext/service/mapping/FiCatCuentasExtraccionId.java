package com.bancomext.service.mapping;

import lombok.Data;

import java.io.Serializable;

@Data
public class FiCatCuentasExtraccionId implements Serializable {

  private String cta;
  private String scta;
  private String sscta;
  private String ssscta;
  private String producto;

}
