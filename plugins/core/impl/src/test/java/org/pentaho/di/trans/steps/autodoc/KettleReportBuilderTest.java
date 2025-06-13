/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.di.trans.steps.autodoc;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.bowl.DefaultBowl;
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

    KettleReportBuilder builder = new KettleReportBuilder( DefaultBowl.getInstance(), log,
      Collections.<ReportSubjectLocation>emptyList(), "", options );
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
