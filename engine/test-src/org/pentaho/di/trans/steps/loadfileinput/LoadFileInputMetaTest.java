/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.loadfileinput;

import org.junit.After;
import org.junit.Test;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;

import static org.junit.Assert.assertEquals;

/**
 * User: Dzmitry Stsiapanau Date: 12/17/13 Time: 3:11 PM
 */
public class LoadFileInputMetaTest {

  String xmlOrig = "    " + "<include>N</include>\n" + "    <include_field/>\n" + "    <rownum>N</rownum>\n"
      + "    <addresultfile>Y</addresultfile>\n" + "    <IsIgnoreEmptyFile>N</IsIgnoreEmptyFile>\n"
      + "    <rownum_field/>\n" + "    <encoding/>\n" + "    <file>\n" + "      <name>D&#x3a;&#x5c;DZMITRY</name>\n"
      + "      <filemask>&#x2a;&#x2f;</filemask>\n"
      + "      <exclude_filemask>&#x2f;&#x2a;&#x2a;&#x2a;</exclude_filemask>\n"
      + "      <file_required>N</file_required>\n" + "      <include_subfolders>N</include_subfolders>\n"
      + "      </file>\n" + "    <fields>\n" + "      </fields>\n" + "    <limit>0</limit>\n"
      + "    <IsInFields>N</IsInFields>\n" + "    <DynamicFilenameField/>\n" + "    <shortFileFieldName/>\n"
      + "    <pathFieldName/>\n" + "    <hiddenFieldName/>\n" + "    <lastModificationTimeFieldName/>\n"
      + "    <uriNameFieldName/>\n" + "    <rootUriNameFieldName/>\n" + "    <extensionFieldName/>\n";

  public LoadFileInputMeta createMeta() throws Exception {
    LoadFileInputMeta meta = new LoadFileInputMeta();
    meta.allocate( 1, 0 );
    meta.setIncludeFilename( false );
    meta.setFilenameField( null );
    meta.setAddResultFile( true );
    meta.setIgnoreEmptyFile( false );
    meta.setIncludeRowNumber( false );
    meta.setRowNumberField( null );
    meta.setEncoding( null );
    meta.setFileName( new String[] { "D:\\DZMITRY" } );
    meta.setFileMask( new String[] { "*/" } );
    meta.setExcludeFileMask( new String[] { "/***" } );
    meta.setFileRequired( new String[] { "N" } );
    meta.setIncludeSubFolders( new String[] { "N" } );
    meta.setRowLimit( 0 );
    meta.setIsInFields( false );
    meta.setDynamicFilenameField( null );
    meta.setShortFileNameField( null );
    meta.setPathField( null );
    meta.setIsHiddenField( null );
    meta.setLastModificationDateField( null );
    meta.setUriField( null );
    meta.setRootUriField( null );
    meta.setExtensionField( null );
    return meta;
  }

  @After
  public void tearDown() throws Exception {

  }

  @Test
  public void testGetXML() throws Exception {
    LoadFileInputMeta testMeta = createMeta();
    String xml = testMeta.getXML();
    assertEquals( xmlOrig.replaceAll( "\n", "" ).replaceAll( "\r", "" ), xml.replaceAll( "\n", "" ).replaceAll( "\r",
        "" ) );
  }

  @Test
  public void testLoadXML() throws Exception {
    LoadFileInputMeta origMeta = createMeta();
    LoadFileInputMeta testMeta = new LoadFileInputMeta();
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder db = dbf.newDocumentBuilder();
    Document doc = db.parse( new InputSource( new StringReader( "<step>" + xmlOrig + "</step>" ) ) );
    IMetaStore metaStore = null;
    testMeta.loadXML( doc.getFirstChild(), null, metaStore );
    assertEquals( origMeta, testMeta );
  }

}
