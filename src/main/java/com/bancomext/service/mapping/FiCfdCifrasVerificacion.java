package com.bancomext.service.mapping;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class FiCfdCifrasVerificacion implements Serializable {

  private Date fechaVerificado;
  private Integer totalFacturas;
  private Integer totalVerificado;
  private Integer totalPendientes;
  private Integer totalErrores;
  private Integer totalEsperado;
  private Integer diferencia;

}
