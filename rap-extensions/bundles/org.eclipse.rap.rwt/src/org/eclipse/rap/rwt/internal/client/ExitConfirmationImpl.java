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

import org.eclipse.rap.rwt.client.service.ExitConfirmation;


public class ExitConfirmationImpl implements ExitConfirmation {

  private String message;

  @Override
  public void setMessage( String message ) {
    this.message = message;
  }

  @Override
  public String getMessage() {
    return message;
  }

}
