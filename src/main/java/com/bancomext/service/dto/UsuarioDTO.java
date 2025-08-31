package com.bancomext.service.dto;

import com.bancomext.service.ServiceLocator;
import com.bancomext.service.mapping.FiAccesos;
import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

@Data
public class UsuarioDTO implements Serializable, HttpSessionBindingListener {
  private static final Logger log = LogManager.getLogger(UsuarioDTO.class);
  private Date fechaEntrada;
  private String email;
  private String nombre;
  private String usuario;
  private String clave;
  private String rol;

  public void valueBound(final HttpSessionBindingEvent event) {
    try {
      fechaEntrada = Calendar.getInstance().getTime();
      final FiAccesos tgParamAccesos = new FiAccesos();
      tgParamAccesos.setIdPromotor(usuario);
      tgParamAccesos.setFechaEntrada(fechaEntrada);
      tgParamAccesos.setFechaSalida(null);
      tgParamAccesos.setEmail(email);
      tgParamAccesos.setRol(rol);

      //Guardamos el registro en la tabla de accesos
      ServiceLocator.getGenericService(event.getSession().getServletContext()).saveOrUpdate(tgParamAccesos);
    } catch (Exception ex) {
      log.error(ex.getMessage(), ex);
    }
  }

  public void valueUnbound(final HttpSessionBindingEvent event) {
    try {
      final FiAccesos tgParamAccesos = new FiAccesos();
      tgParamAccesos.setIdPromotor(usuario);
      tgParamAccesos.setFechaEntrada(fechaEntrada);
      tgParamAccesos.setFechaSalida(Calendar.getInstance().getTime());
      tgParamAccesos.setEmail(email);
      tgParamAccesos.setRol(rol);

      //Actualizamos el registro en la tabla de accesos
      ServiceLocator.getGenericService(event.getSession().getServletContext()).saveOrUpdate(tgParamAccesos);
    } catch (Exception ex) {
      log.error(ex.getMessage(), ex);
    }
  }

}
