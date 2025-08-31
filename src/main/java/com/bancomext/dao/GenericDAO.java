package com.bancomext.dao;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import java.util.List;

public class GenericDAO extends HibernateDaoSupport {
  private static final Logger log = LogManager.getLogger(GenericDAO.class);

  public void save(final Object entity) throws DataIntegrityViolationException {
    getHibernateTemplate().save(entity);
    getHibernateTemplate().flush();
  }

  public void saveOrUpdate(final Object entity) {
    getHibernateTemplate().saveOrUpdate(entity);
    getHibernateTemplate().flush();
  }

  public void update(final Object entity) {
    getHibernateTemplate().update(entity);
  }

  public void delete(final Object entity) {
    getHibernateTemplate().delete(entity);
  }

  public List getAll(final String tipo) {
    return getHibernateTemplate().find("from " + tipo + " order by 1");
  }

  public List get(final String tipo, final String condiciones, final String order) {
    log.info("INTO GET  GENERIC DAO");
    final String orden = order != null && !order.trim().isEmpty() ? " " + order : " ORDER BY 1 ASC ";
    log.info("FROM " + tipo + " WHERE " + condiciones + " " + order);
    if (condiciones != null && !condiciones.trim().isEmpty()) {
      log.info("INTO condiciones NOT NULL");
      return getHibernateTemplate().find("FROM " + tipo + " WHERE " + condiciones + orden);
    } else {
      log.info("INTO condiciones NULL");
      return getHibernateTemplate().find("FROM " + tipo + orden);
    }
  }
}
