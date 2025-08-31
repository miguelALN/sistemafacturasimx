package com.bancomext.web.utils;

import com.bancomext.service.ConsultasService;
import com.bancomext.service.GenericService;
import com.bancomext.service.ServiceLocator;
import com.bancomext.service.mapping.FiBitacoraAccionesImx;
import com.bancomext.service.mapping.FiBitacoraCfdi;

import java.math.BigDecimal;
import java.util.Date;

public class BitacoraAcciones {
  private final static GenericService genericService = ServiceLocator.getGenericService();

  public static void actualizarAcciones(final String accion, final String folio, final String status) {
    final BigDecimal secuencia = ConsultasService.getMax("FI_BITACORA_ACCIONES_IMX", "ID");
    final BigDecimal id = (secuencia == null ? new BigDecimal(1) : secuencia.add(new BigDecimal(1)));
    final FiBitacoraAccionesImx beanBitacora = new FiBitacoraAccionesImx();
    beanBitacora.setId(id);
    beanBitacora.setAccion(accion);
    beanBitacora.setFolio(folio);
    beanBitacora.setEstatus(status);
    genericService.save(beanBitacora);
  }


  public static void actualizarBitacoraCFDI(final String accion, final String folio, final String status) {
    final FiBitacoraCfdi beanBitacoraCfdi = new FiBitacoraCfdi();
    beanBitacoraCfdi.setAccion(accion);
    beanBitacoraCfdi.setSerieFolio("IMX" + folio);
    beanBitacoraCfdi.setFechaStatus(new Date());
    beanBitacoraCfdi.setFechaEnvioCliente(new Date());
    beanBitacoraCfdi.setStatusVerificado(status);
    genericService.update(beanBitacoraCfdi);
  }

}
