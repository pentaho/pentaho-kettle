/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.trans.steps.autodoc;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.libraries.base.util.ObjectUtilities;
import org.pentaho.reporting.libraries.fonts.LibFontBoot;
import org.pentaho.reporting.libraries.resourceloader.LibLoaderBoot;

import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class KettleReportBuilderTest {

  @Before
  public void initReportEnv() {
    if ( !ClassicEngineBoot.getInstance().isBootDone() ) {
      ObjectUtilities.setClassLoader( getClass().getClassLoader() );
      ObjectUtilities.setClassLoaderSource( ObjectUtilities.CLASS_CONTEXT );

      LibLoaderBoot.getInstance().start();
      LibFontBoot.getInstance().start();
      ClassicEngineBoot.getInstance().start();
    }
  }

  @Test
  public void createReport() throws Exception {
    LoggingObjectInterface log = mock( LoggingObjectInterface.class );

    AutoDocOptionsInterface options = mock( AutoDocOptionsInterface.class );
    when( options.isIncludingImage() ).thenReturn( Boolean.TRUE );

    KettleReportBuilder builder = new KettleReportBuilder( log, Collections.<ReportSubjectLocation>emptyList(), "", options );
    builder.createReport();

    assertNotNull( builder.getReport() );
    assertNotNull( builder.getReport().getDataFactory() );
    assertNotNull( builder.getReport().getReportHeader() );
    assertNotNull( builder.getReport().getReportFooter() );
    assertNotNull( builder.getReport().getRootGroup() );
    assertNotNull( builder.getReport().getPageDefinition() );
    assertTrue( builder.getReport().getExpressions().size() > 0 );
  }

}
