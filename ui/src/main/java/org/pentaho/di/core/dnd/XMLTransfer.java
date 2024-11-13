/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.core.dnd;

import org.apache.commons.codec.binary.Base64;
import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;

public class XMLTransfer extends ByteArrayTransfer {
  private static final String MYTYPENAME = "KETTLE_XML_TRANSFER";

  private static final int MYTYPEID = registerType( MYTYPENAME );

  private static XMLTransfer _instance = new XMLTransfer();

  private LogChannelInterface log;

  private XMLTransfer() {
    this.log = new LogChannel( "XML DND Transfer" );
  }

  public static XMLTransfer getInstance() {
    return _instance;
  }

  public void javaToNative( Object object, TransferData transferData ) {
    if ( !checkMyType( object ) ) {
      return; // DND.error(DND.ERROR_INVALID_DATA);
    }

    try {
      byte[] buffer =
        Base64.encodeBase64( ( (DragAndDropContainer) object ).getXML().getBytes( Const.XML_ENCODING ) );

      super.javaToNative( buffer, transferData );
    } catch ( Exception e ) {
      log.logError( "Unexpected error trying to put a string onto the XML Transfer type: " + e.toString() );
      log.logError( Const.getStackTracker( e ) );
      return;
    }
  }

  boolean checkMyType( Object object ) {
    if ( object == null || !( object instanceof DragAndDropContainer ) ) {
      return false;
    }

    // System.out.println("Object class: "+object.getClass().toString());

    return true;
  }

  public Object nativeToJava( TransferData transferData ) {
    if ( isSupportedType( transferData ) ) {
      try {
        byte[] buffer = (byte[]) super.nativeToJava( transferData );
        String xml = new String( Base64.decodeBase64( new String( buffer ).getBytes() ) );
        return new DragAndDropContainer( xml );
      } catch ( Exception e ) {
        log.logError( "Unexpected error trying to read a drag and drop container from the XML Transfer type: "
          + e.toString() );
        log.logError( Const.getStackTracker( e ) );
        return null;
      }
    }
    return null;
  }

  protected String[] getTypeNames() {
    return new String[] { MYTYPENAME };
  }

  protected int[] getTypeIds() {
    return new int[] { MYTYPEID };
  }
}
