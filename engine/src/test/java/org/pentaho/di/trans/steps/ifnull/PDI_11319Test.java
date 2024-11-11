/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.di.trans.steps.ifnull;

import java.io.File;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.trans.steps.ifnull.IfNullMeta.ValueTypes;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;


public class PDI_11319Test {

  private Node xmlVersion4;
  private Node xmlVersion5;
  private IfNullMeta meta;


  @Before
  public void init() throws Exception {
    File v4 = new File( this.getClass().getResource( "v4.ktr" ).getFile() );
    File v5 = new File( this.getClass().getResource( "v5.ktr" ).getFile() );
    xmlVersion4 = XMLHandler.loadXMLFile( v4 );
    xmlVersion5 = XMLHandler.loadXMLFile( v5 );
    meta = new IfNullMeta();
  }

  /**
   * Test forward compatibility - transformation created in version 4 don't contain "set emtry" elements
   */
  @Test
  public void testLoadXMLVersion4() throws Exception {
    Node nullIfStep = getNullIfStep( xmlVersion4 );
    meta.loadXML( nullIfStep, null, (IMetaStore) null );

    Assert.assertFalse( "Set Empty String is true", meta.isSetEmptyStringAll() );
    boolean[] expected = { false };
    ValueTypes[] actual = meta.getValueTypes();
    Assert.assertEquals( expected.length, actual.length );
    for ( int i = 0; i < expected.length; i++ ) {
      Assert.assertEquals( "Set empty string value type works incorrectly", expected[i], actual[i]
          .isSetTypeEmptyString() );
    }
  }

  /**
   * Test transformation created in Kettle 5 - should work w/ and w/o the fix
   */
  @Test
  public void testLoadXMLVersion5() throws Exception {
    Node nullIfStep = getNullIfStep( xmlVersion5 );
    meta.loadXML( nullIfStep, null, (IMetaStore) null );

    Assert.assertFalse( "Set Empty String is true", meta.isSetEmptyStringAll() );
    boolean[] expected = { true, false, false };
    ValueTypes[] actual = meta.getValueTypes();
    Assert.assertEquals( expected.length, actual.length );
    for ( int i = 0; i < expected.length; i++ ) {
      Assert.assertEquals( "Set empty string value type works incorrectly", expected[i], actual[i]
          .isSetTypeEmptyString() );
    }
  }

  private Node getNullIfStep( Node doc ) {
    Node trans = XMLHandler.getSubNode( doc, "transformation" );
    List<Node> steps = XMLHandler.getNodes( trans, "step" );
    Node nullIfStep = null;
    for ( Node step : steps ) {
      if ( "IfNull".equals( XMLHandler.getNodeValue( XMLHandler.getSubNode( step, "type" ) ) ) ) {
        nullIfStep = step;
        break;
      }
    }

    return nullIfStep;
  }

}
