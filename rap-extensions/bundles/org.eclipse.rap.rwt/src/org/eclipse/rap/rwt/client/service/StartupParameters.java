/*******************************************************************************
 * Copyright (c) 2015 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.client.service;

import java.util.Collection;
import java.util.List;



/**
 * The startup parameters service allows accessing startup parameters of an entry point. In the
 * default web client, these parameters can be passed as HTTP request parameters in the initial
 * request.
 *
 * @since 3.0
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface StartupParameters extends ClientService {

  /**
   * Returns the names of the entrypoint startup parameters.
   *
   * @return a (possibly empty) collection of parameter names
   */
  Collection<String> getParameterNames();

  /**
   * Returns the value of a named entrypoint startup parameter. You should only use this method
   * when you are sure the parameter has only one value. If the parameter might have more than one
   * value, use {@link #getParameterValues}.
   *
   * If you use this method with a multivalued parameter, the value returned is equal to the first
   * value in the list returned by <code>getParameterValues</code>.
   *
   * @param name the name of the parameter
   * @return the value of the parameter, or <code>null</code> if the parameter does not exist
   */
  String getParameter( String name );

  /**
   * Returns a list with values of a named entrypoint startup parameter.
   *
   * If the parameter has a single value, the list has a size of 1.
   *
   * @param name the name of the parameter
   * @return the values of the parameter, or <code>null</code> if the parameter does not exist
   */
  List<String> getParameterValues( String name );

}
