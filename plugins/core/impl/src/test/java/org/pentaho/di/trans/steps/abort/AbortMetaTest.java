/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
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
 *
 ******************************************************************************/

package org.pentaho.di.trans.steps.abort;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.validator.EnumLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class AbortMetaTest {

  @Test
  public void testRoundTrip() throws KettleException {
    List<String> attributes = Arrays.asList( "row_threshold", "message", "always_log_rows", "abort_option" );

    Map<String, String> getterMap = new HashMap<String, String>();
    getterMap.put( "row_threshold", "getRowThreshold" );
    getterMap.put( "message", "getMessage" );
    getterMap.put( "always_log_rows", "isAlwaysLogRows" );
    getterMap.put( "abort_option", "getAbortOption" );

    Map<String, String> setterMap = new HashMap<String, String>();
    setterMap.put( "row_threshold", "setRowThreshold" );
    setterMap.put( "message", "setMessage" );
    setterMap.put( "always_log_rows", "setAlwaysLogRows" );
    setterMap.put( "abort_option", "setAbortOption" );

    Map<String, FieldLoadSaveValidator<?>> attributeValidators = Collections.emptyMap();

    Map<String, FieldLoadSaveValidator<?>> typeValidators = new HashMap<>();
    typeValidators.put( AbortMeta.AbortOption.class.getCanonicalName(),
      new EnumLoadSaveValidator<>( AbortMeta.AbortOption.ABORT ) );

    LoadSaveTester loadSaveTester = new LoadSaveTester( AbortMeta.class, attributes, getterMap, setterMap,
      attributeValidators, typeValidators );
    loadSaveTester.testSerialization();
  }

  @Test
  public void testBackwardsCapatibilityAbortWithError() throws KettleXMLException {
    IMetaStore metaStore = mock( IMetaStore.class );
    AbortMeta meta = new AbortMeta();

    // Abort with error
    String inputXml = "  <step>\n"
      + "    <name>Abort</name>\n"
      + "    <type>Abort</type>\n"
      + "    <abort_with_error>Y</abort_with_error>\n"
      + "  </step>";
    Node node = XMLHandler.loadXMLString( inputXml ).getFirstChild();
    meta.loadXML( node, Collections.emptyList(), metaStore );
    assertTrue( meta.isAbortWithError() );

    // Don't abort with error
    inputXml = "  <step>\n"
      + "    <name>Abort</name>\n"
      + "    <type>Abort</type>\n"
      + "    <abort_with_error>N</abort_with_error>\n"
      + "  </step>";
    node = XMLHandler.loadXMLString( inputXml ).getFirstChild();
    meta.loadXML( node, Collections.emptyList(), metaStore );
    assertTrue( meta.isAbort() );

    // Don't abort with error
    inputXml = "  <step>\n"
      + "    <name>Abort</name>\n"
      + "    <type>Abort</type>\n"
      + "  </step>";
    node = XMLHandler.loadXMLString( inputXml ).getFirstChild();
    meta.loadXML( node, Collections.emptyList(), metaStore );
    assertTrue( meta.isAbortWithError() );
  }
}
