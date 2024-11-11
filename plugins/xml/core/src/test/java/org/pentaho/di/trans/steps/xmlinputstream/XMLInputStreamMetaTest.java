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


package org.pentaho.di.trans.steps.xmlinputstream;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;

public class XMLInputStreamMetaTest {

  @Test
  public void testLoadSaveRoundTrip() throws KettleException {
    List<String> attributes =
      Arrays.asList( "filename", "addResultFile", "nrRowsToSkip", "rowLimit", "defaultStringLen", "encoding",
        "enableNamespaces", "enableTrim", "includeFilenameField", "filenameField", "includeRowNumberField",
        "rowNumberField", "includeXmlDataTypeNumericField", "xmlDataTypeNumericField",
        "includeXmlDataTypeDescriptionField", "xmlDataTypeDescriptionField", "includeXmlLocationLineField",
        "xmlLocationLineField", "includeXmlLocationColumnField", "xmlLocationColumnField",
        "includeXmlElementIDField", "xmlElementIDField", "includeXmlParentElementIDField",
        "xmlParentElementIDField", "includeXmlElementLevelField", "xmlElementLevelField",
        "includeXmlPathField", "xmlPathField", "includeXmlParentPathField", "xmlParentPathField",
        "includeXmlDataNameField", "xmlDataNameField", "includeXmlDataValueField", "xmlDataValueField" );

    LoadSaveTester<XMLInputStreamMeta> loadSaveTester =
      new LoadSaveTester<XMLInputStreamMeta>( XMLInputStreamMeta.class, attributes );

    loadSaveTester.testSerialization();
  }
}
