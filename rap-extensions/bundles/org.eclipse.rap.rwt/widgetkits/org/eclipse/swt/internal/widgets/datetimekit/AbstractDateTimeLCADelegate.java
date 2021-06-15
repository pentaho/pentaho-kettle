/*******************************************************************************
 * Copyright (c) 2008, 2013 Innoopract Informationssysteme GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Innoopract Informationssysteme GmbH - initial API and implementation
 *     EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.swt.internal.widgets.datetimekit;

import java.io.IOException;

import org.eclipse.swt.widgets.DateTime;

abstract class AbstractDateTimeLCADelegate {

  abstract void preserveValues( DateTime dateTime );
  abstract void renderInitialization( DateTime dateTime ) throws IOException;
  abstract void renderChanges( DateTime dateTime ) throws IOException;

}
