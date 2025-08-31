package com.bancomext.service.mapping;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@AllArgsConstructor
public class FiCfdFoliosId implements Serializable {

  private Date fechaAutorizacion;
  private String noAutorizacion;

}


