/*
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 * **************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
