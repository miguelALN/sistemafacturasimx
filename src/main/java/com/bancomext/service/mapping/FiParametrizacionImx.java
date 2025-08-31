package com.bancomext.service.mapping;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
public class FiParametrizacionImx implements Serializable {

  private Integer id;
  private String opCode;
  private String tipoProceso;

  private Integer ctaOrigen;
  private Integer sctaOrigen;
  private Integer ssCtaOrigen;
  private Integer sssCtaOrigen;

  private Integer ctaCargo;
  private Integer sctaCargo;
  private Integer ssCtaCargo;
  private Integer sssCtaCargo;

  private Integer ctaAbono;
  private Integer sctaAbono;
  private Integer ssCtaAbono;
  private Integer sssCtaAbono;

  private Integer ctaSust;
  private Integer sCtaSust;
  private Integer ssCtaSust;
  private Integer sssCtaSust;
  private String modalidad;

  // VARIABLES PARA MOSTRAR EN PANTALLA CON CEROS

  private String ctaOrigenStr;
  private String sctaOrigenStr;
  private String ssCtaOrigenStr;
  private String sssCtaOrigenStr;

  private String ctaCargoStr;
  private String sctaCargoStr;
  private String ssCtaCargoStr;
  private String sssCtaCargoStr;

  private String ctaAbonoStr;
  private String sctaAbonoStr;
  private String ssCtaAbonoStr;
  private String sssCtaAbonoStr;

  private String ctaSustStr;
  private String sctaSustStr;
  private String ssCtaSustStr;
  private String sssCtaSustStr;

}
