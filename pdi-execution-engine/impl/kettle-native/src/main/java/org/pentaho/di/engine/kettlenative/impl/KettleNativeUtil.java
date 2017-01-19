package org.pentaho.di.engine.kettlenative.impl;

import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.exception.KettleMissingPluginsException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.engine.api.ITransformation;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class KettleNativeUtil {

  public static TransMeta getTransMeta( ITransformation transformation ) {
    String config = transformation.getConfig( "TransMeta.xml", String.class )
      .orElseThrow( IllegalStateException::new );

    Document doc;
    try {
      doc = XMLHandler.loadXMLString( config );
      Node stepNode = XMLHandler.getSubNode( doc, "transformation" );
      return new TransMeta( stepNode, null );
    } catch ( KettleXMLException | KettleMissingPluginsException e ) {
      throw new RuntimeException( e );
    }
  }

  /**
   * Temp hack to set rowsets w/o modifying kettle Trans
   */
  public static Trans createTrans() {
    Trans trans = new Trans();
    trans.setLog( LogChannel.GENERAL );
    try {
      Field rowsets = Trans.class.getDeclaredField( "rowsets" );
      rowsets.setAccessible( true );
      rowsets.set( trans, new ArrayList<RowSet>() );
      return trans;
    } catch ( NoSuchFieldException | IllegalAccessException e ) {
      throw new RuntimeException( e );
    }
  }

}
