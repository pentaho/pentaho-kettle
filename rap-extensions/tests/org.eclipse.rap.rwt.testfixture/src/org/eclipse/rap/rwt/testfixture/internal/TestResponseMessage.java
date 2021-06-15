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

import java.util.ArrayList;

import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.rwt.internal.protocol.Operation;
import org.eclipse.rap.rwt.internal.protocol.ResponseMessage;


/**
 * <strong>IMPORTANT:</strong> This class is <em>not</em> part the public RAP
 * API. It may change or disappear without further notice. Use this class at
 * your own risk.
 */
public class TestResponseMessage extends ResponseMessage {

  public TestResponseMessage() {
    super( new JsonObject(), new ArrayList<Operation>() );
  }

  public TestResponseMessage( JsonObject json ) {
    super( json );
  }

}
