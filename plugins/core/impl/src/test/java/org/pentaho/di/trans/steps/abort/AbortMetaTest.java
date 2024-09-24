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
