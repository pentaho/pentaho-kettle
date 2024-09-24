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

package org.pentaho.di.job;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.base.PrivateDatabasesTestTemplate;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.Props;
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
public class JobMetaPrivateDbTest extends PrivateDatabasesTestTemplate<JobMeta>  {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Override
  public JobMeta createMeta() {
    return new JobMeta();
  }

  @Override
  public JobMeta fromXml( String xml, final SharedObjects fakeSharedObjects ) throws Exception {
    JobMeta meta = spy( new JobMeta() );
    doAnswer( createInjectingAnswer( meta, fakeSharedObjects ) ).when( meta ).readSharedObjects();

    Document doc = XMLHandler.loadXMLFile( new ByteArrayInputStream( xml.getBytes() ), null, false, false );
    meta.loadXML( XMLHandler.getSubNode( doc, JobMeta.XML_TAG ), null, null );

    return meta;
  }

  @Override
  public String toXml( JobMeta meta ) {
    return meta.getXML();
  }


  @BeforeClass
  public static void initKettle() throws Exception {
    if ( Props.isInitialized() ) {
      Props.getInstance().setOnlyUsedConnectionsSavedToXML( false );
    }
    KettleEnvironment.init();
  }


  @Test
  public void onePrivate_TwoSharedOnlyUsed() throws Exception {
    doTest_OnePrivate_TwoSharedOnlyUsed();
  }


  @Test
  public void onePrivate_TwoSharedAllUsed() throws Exception {
    doTest_OnePrivate_TwoSharedAllExport();
  }

  @Test
  public void noPrivate() throws Exception {
    doTest_NoPrivate();
  }

  @Test
  public void onePrivate_NoSharedExportAll() throws Exception {
    doTest_OnePrivate_NoSharedExportAll();
  }

  @Test
  public void onePrivate_NoSharedOnlyUsed() throws Exception {
    doTest_OnePrivate_NoSharedOnlyUsed();
  }


}
