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
