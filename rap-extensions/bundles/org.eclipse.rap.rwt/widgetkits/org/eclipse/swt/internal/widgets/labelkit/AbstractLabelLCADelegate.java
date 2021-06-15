/*******************************************************************************
 * Copyright (c) 2002, 2013 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.swt.internal.widgets.labelkit;

import java.io.IOException;

import org.eclipse.swt.widgets.Label;


abstract class AbstractLabelLCADelegate {

  abstract void preserveValues( Label label );
  abstract void renderInitialization( Label label ) throws IOException;
  abstract void renderChanges( Label label ) throws IOException;

}
