package com.bancomext.service;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;

@ManagedBean(name = "serviceLocatorBean")
@ApplicationScoped
public class ServiceLocator implements ApplicationContextAware {

  private static ServletContext servletContext = null;

  private static ApplicationContext applicationContext = null;

  public ServiceLocator(final ServletContext context) {
    servletContext = context;
  }

  public static GenericService getGenericService() {
    initContext();
    return (GenericService) applicationContext.getBean("genericService");
  }

  private static void initContext() {
    if (servletContext == null) {
      servletContext = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
    }
    if (applicationContext == null) {
      applicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
    }
  }

  public static GenericService getGenericService(final ServletContext context) {
    servletContext = context;
    return getGenericService();
  }

  @Override
  public void setApplicationContext(ApplicationContext context) throws BeansException {
    applicationContext = context;
  }
}
