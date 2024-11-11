/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.di.repository.pur;

public class WebServiceSpecification {
  private String serviceName;
  private Class<?> serviceClass;
  private ServiceType serviceType;

  public enum ServiceType {
    JAX_RS, JAX_WS
  }

  private WebServiceSpecification() {
  }

  public static WebServiceSpecification getWsServiceSpecification( Class<?> serviceClass, String serviceName ) {
    WebServiceSpecification spec = new WebServiceSpecification();
    spec.serviceClass = serviceClass;
    spec.serviceName = serviceName;
    spec.serviceType = ServiceType.JAX_WS;
    return spec;
  }

  public static WebServiceSpecification getRestServiceSpecification( Class<?> serviceClass, String serviceName )
    throws NoSuchMethodException, SecurityException {
    WebServiceSpecification spec = new WebServiceSpecification();
    spec.serviceClass = serviceClass;
    spec.serviceName = serviceName;
    spec.serviceType = ServiceType.JAX_RS;
    return spec;
  }

  public String getServiceName() {
    return serviceName;
  }

  public Class<?> getServiceClass() {
    return serviceClass;
  }

  public ServiceType getServiceType() {
    return serviceType;
  }
}
