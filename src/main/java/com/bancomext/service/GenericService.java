package com.bancomext.service;

import com.bancomext.dao.GenericDAO;
import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

@Data
public class GenericService {

  private static final Logger log = LogManager.getLogger(GenericService.class);
  private GenericDAO genericDAO;

  public void save(final Object entity) {
    try {
      genericDAO.save(entity);
    } catch (DataIntegrityViolationException de) {
      String msg = "Error al guardar informacion: " + de.getMessage();
      log.error(de.getMessage(), de);
      throw new DataIntegrityViolationException(msg);
    }
  }

  public void saveOrUpdate(final Object entity) {
    genericDAO.saveOrUpdate(entity);
  }

  public void delete(final Object entity) {
    genericDAO.delete(entity);
  }

  public void update(final Object entity) {
    genericDAO.update(entity);
  }

  public List getAll(final String tipo) {
    return genericDAO.getAll(tipo);
  }

  public List get(final String tipo, final String condiciones, final String Order) {
    return genericDAO.get(tipo, condiciones, Order);
  }
}
