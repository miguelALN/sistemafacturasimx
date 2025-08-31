package com.bancomext.service.mapping;

import lombok.Data;

import java.io.Serializable;

@Data
public class FiCuerpoCorreos implements Serializable {

  private String correo;
  private String cuerpo;
  private String asunto;

}
