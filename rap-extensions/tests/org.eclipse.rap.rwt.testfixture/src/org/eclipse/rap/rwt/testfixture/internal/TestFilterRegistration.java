/*******************************************************************************
 * Copyright (c) 2011, 2014 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.testfixture.internal;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterRegistration;


class TestFilterRegistration implements FilterRegistration.Dynamic {
  private final String filterName;
  private Class<? extends Filter> filterClass;

  TestFilterRegistration( String filterName, Filter filter ) {
    this.filterName = filterName;
    this.filterClass = filter.getClass();
  }

  public String getName() {
    return filterName;
  }

  public String getClassName() {
    return filterClass.getName();
  }

  public boolean setInitParameter( String name, String value ) {
    return false;
  }

  public String getInitParameter( String name ) {
    return null;
  }

  public Set<String> setInitParameters( Map<String, String> initParameters ) {
    return null;
  }

  public Map<String, String> getInitParameters() {
    return null;
  }

  public void addMappingForServletNames( EnumSet<DispatcherType> dispatcherTypes,
                                         boolean isMatchAfter,
                                         String... servletNames )
  {
  }

  public Collection<String> getServletNameMappings() {
    return null;
  }

  public void addMappingForUrlPatterns( EnumSet<DispatcherType> dispatcherTypes,
                                        boolean isMatchAfter,
                                        String... urlPatterns )
  {
  }

  public Collection<String> getUrlPatternMappings() {
    return null;
  }

  public void setAsyncSupported( boolean isAsyncSupported ) {
  }
}
