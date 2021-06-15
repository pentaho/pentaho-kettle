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
package org.eclipse.rap.rwt.dnd;

import org.eclipse.rap.rwt.SingletonUtil;
import org.eclipse.rap.rwt.client.ClientFile;
import org.eclipse.rap.rwt.client.service.ClientFileUploader;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;


/**
 * The class <code>ClientFileTransfer</code> allows the user to drop files from the user's file
 * system on a <code>DropTarget</code>. The DropEvent will contain data of the type
 * <code>ClientFile[]</code>.
 *
 * @see Transfer
 * @see ClientFile
 * @see ClientFileUploader
 * @since 2.3
 */
public class ClientFileTransfer extends Transfer {

  static final String TYPE_NAME = "ClientFile";
  static final int TYPE_ID = registerType( TYPE_NAME );

  private ClientFileTransfer() {
  }

  public static ClientFileTransfer getInstance() {
    return SingletonUtil.getSessionInstance( ClientFileTransfer.class );
  }

  @Override
  public TransferData[] getSupportedTypes() {
    int[] types = getTypeIds();
    TransferData[] data = new TransferData[ types.length ];
    for( int i = 0; i < types.length; i++ ) {
      data[ i ] = new TransferData();
      data[ i ].type = types[ i ];
    }
    return data;
  }

  @Override
  public boolean isSupportedType( TransferData transferData ) {
    if( transferData != null ) {
      for( int typeId : getTypeIds() ) {
        if( transferData.type == typeId ) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  protected int[] getTypeIds() {
    return new int[]{ TYPE_ID };
  }

  @Override
  protected String[] getTypeNames() {
    return new String[]{ TYPE_NAME };
  }


  @Override
  public void javaToNative( Object object, TransferData transferData ) {
  }

  @Override
  public Object nativeToJava( TransferData transferData ) {
    return null;
  }

}
