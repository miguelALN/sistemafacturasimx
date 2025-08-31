package com.bancomext.service.mapping;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class FiFechasEjecucionImx implements Serializable {

  private Integer id;
  private Date fecPrimerDiaHabilMes;
  private String fechaPrimerDia;
  private Integer dia;
  private Integer mes;
  private Integer anio;
  private Date fecUltimoDiaHabilMes;
  private String fechaUltimoDia;
  private Integer diaH;
  private Integer mesH;
  private Integer anioH;

}
