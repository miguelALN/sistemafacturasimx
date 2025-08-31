package com.bancomext.service.mapping;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class FiParamGenerales implements Serializable, Cloneable {

  private boolean idparametro;
  private String adicionadoPor;
  private Date fechaAdicion;
  private String modificadoPor;
  private Date fechaModificacion;
  private String diasReproceso;
  private String ctaImp;
  private String sctaImp;
  private String ssctaImp;
  private String sssctaImp;
  private String ctaExp;
  private String sctaExp;
  private String ssctaExp;
  private String sssctaExp;
  private String ctaFiu;
  private String sctaFiu;
  private String ssctaFiu;
  private String sssctaFiu;
  private String importeeditable;
  private String diasCifrasCtrl;

  @Override
  public FiParamGenerales clone() {
    try {
      return (FiParamGenerales) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new AssertionError();
    }
  }
}


