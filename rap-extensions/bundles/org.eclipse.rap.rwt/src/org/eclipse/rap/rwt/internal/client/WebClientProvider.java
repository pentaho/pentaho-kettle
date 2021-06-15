/*******************************************************************************
 * Copyright (c) 2012, 2015 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.client;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.rap.rwt.client.Client;
import org.eclipse.rap.rwt.client.WebClient;


public class WebClientProvider implements ClientProvider {

  @Override
  public boolean accept( HttpServletRequest request ) {
    return true;
  }

  @Override
  public Client getClient() {
    return new WebClient();
  }

}
