/*******************************************************************************
 * Copyright (c) 2013 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.template;

import static org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory.getRemoteObject;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.template.Template;
import org.eclipse.swt.widgets.Widget;


public class TemplateLCAUtil {

  private static final String PROP_ROW_TEMPLATE = "rowTemplate";

  public static void renderRowTemplate( Widget widget ) {
    Object data = widget.getData( RWT.ROW_TEMPLATE );
    if( data instanceof Template ) {
      TemplateSerializer serializer = ( ( Template )data ).getAdapter( TemplateSerializer.class );
      getRemoteObject( widget ).set( PROP_ROW_TEMPLATE, serializer.toJson() );
    }
  }

  private TemplateLCAUtil() {
    // prevent instantiation
  }

}
