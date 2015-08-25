/*!
* Copyright 2010 - 2015 Pentaho Corporation.  All rights reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
*/

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
