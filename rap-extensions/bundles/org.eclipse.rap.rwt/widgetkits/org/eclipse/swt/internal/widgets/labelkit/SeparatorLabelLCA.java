/*******************************************************************************
 * Copyright (c) 2002, 2015 Innoopract Informationssysteme GmbH and others.
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

import static org.eclipse.rap.rwt.internal.protocol.JsonUtil.createJsonArray;
import static org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory.createRemoteObject;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.getStyles;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil.getId;

import java.io.IOException;

import org.eclipse.rap.rwt.internal.lifecycle.ControlLCAUtil;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil;
import org.eclipse.rap.rwt.remote.RemoteObject;
import org.eclipse.swt.widgets.Label;


final class SeparatorLabelLCA extends AbstractLabelLCADelegate {

  static final SeparatorLabelLCA INSTANCE = new SeparatorLabelLCA();

  private static final String TYPE = "rwt.widgets.Separator";
  private static final String[] ALLOWED_STYLES = {
    "SEPARATOR", "HORIZONTAL", "VERTICAL", "SHADOW_IN", "SHADOW_OUT", "SHADOW_NONE", "BORDER"
  };

  @Override
  void preserveValues( Label label ) {
  }

  @Override
  void renderInitialization( Label label ) throws IOException {
    RemoteObject remoteObject = createRemoteObject( label, TYPE );
    remoteObject.setHandler( new LabelOperationHandler( label ) );
    remoteObject.set( "parent", getId( label.getParent() ) );
    remoteObject.set( "style", createJsonArray( getStyles( label, ALLOWED_STYLES ) ) );
  }

  @Override
  void renderChanges( Label label ) throws IOException {
    ControlLCAUtil.renderChanges( label );
    WidgetLCAUtil.renderCustomVariant( label );
  }

  private SeparatorLabelLCA() {
    // prevent instantiation
  }

}
