/*******************************************************************************
 * Copyright (c) 2014 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.testfixture.internal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.eclipse.swt.internal.widgets.IDisplayAdapter;
import org.eclipse.swt.widgets.Widget;


public class SerializationTestUtil {

  @SuppressWarnings("unchecked")
  public static <T> T serializeAndDeserialize( T instance ) throws Exception {
    byte[] bytes = serialize( instance );
    return ( T )deserialize( bytes );
  }

  @SuppressWarnings("unchecked")
  public static <T extends Widget> T serializeAndDeserialize( T instance ) throws Exception {
    byte[] bytes = serialize( instance );
    T result = ( T )deserialize( bytes );
    result.getDisplay().getAdapter( IDisplayAdapter.class ).attachThread();
    return result;
  }

  public static byte[] serialize( Object object ) throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    ObjectOutputStream objectOutputStream = new ObjectOutputStream( outputStream );
    objectOutputStream.writeObject( object );
    return outputStream.toByteArray();
  }

  public static Object deserialize( byte[] bytes ) throws IOException, ClassNotFoundException {
    ByteArrayInputStream inputStream = new ByteArrayInputStream( bytes );
    ObjectInputStream objectInputStream = new ObjectInputStream( inputStream );
    return objectInputStream.readObject();
  }

}
