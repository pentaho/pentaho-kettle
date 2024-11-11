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


package org.pentaho.di.trans;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.base.PrivateDatabasesTestTemplate;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.shared.SharedObjects;
import org.w3c.dom.Document;

import java.io.ByteArrayInputStream;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;

/**
 * @author Andrey Khayrutdinov
 */
public class TransMetaPrivateDbTest extends PrivateDatabasesTestTemplate<TransMeta> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Override
  protected TransMeta createMeta() {
    return new TransMeta();
  }

  @Override
  protected TransMeta fromXml( String xml, final SharedObjects fakeSharedObjects ) throws Exception {
    TransMeta meta = spy( new TransMeta() );
    doAnswer( createInjectingAnswer( meta, fakeSharedObjects ) ).when( meta ).readSharedObjects();

    Document doc = XMLHandler.loadXMLFile( new ByteArrayInputStream( xml.getBytes() ), null, false, false );
    meta.loadXML( XMLHandler.getSubNode( doc, TransMeta.XML_TAG ), null, false );

    return meta;
  }

  @Override
  protected String toXml( TransMeta meta ) throws Exception {
    return meta.getXML();
  }

  @BeforeClass
  public static void initKettle() throws Exception {
    KettleEnvironment.init();
  }


  @Test
  public void onePrivate_TwoSharedWithProp() throws Exception {
    doTest_OnePrivate_TwoSharedOnlyUsed();
  }

  @Test
  public void onePrivate_TwoSharedNoProp() throws Exception {
    doTest_OnePrivate_TwoSharedAllExport();
  }


  @Test
  public void noPrivate() throws Exception {
    doTest_NoPrivate();
  }

  @Test
  public void onePrivate_NoSharedOnlyUsed() throws Exception {
    doTest_OnePrivate_NoSharedOnlyUsed();
  }
  
  @Test
  public void onePrivate_NoSharedExportAll() throws Exception {
    doTest_OnePrivate_NoSharedExportAll();
  }
}

