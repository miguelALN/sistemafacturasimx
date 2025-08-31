package com.bancomext.service.mapping;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class FiAccesos implements Serializable {

  private String idPromotor;
  private String email;
  private String rol;
  private Date fechaEntrada;
  private Date fechaSalida;
  private boolean bloqueado;

  public void actualizaBloqueo() {
    bloqueado = (fechaEntrada != null && fechaSalida == null);
  }
}
