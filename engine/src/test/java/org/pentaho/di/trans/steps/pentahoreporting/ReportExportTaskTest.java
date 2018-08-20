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

package org.pentaho.di.trans.steps.pentahoreporting;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertNotNull;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.layout.output.ReportProcessor;
import org.pentaho.reporting.engine.classic.core.modules.gui.common.StatusListener;
import org.pentaho.reporting.engine.classic.core.modules.gui.common.StatusType;

import org.pentaho.reporting.engine.classic.core.modules.output.pageable.base.PageableReportProcessor;
import org.pentaho.reporting.engine.classic.core.modules.output.pageable.pdf.PdfOutputProcessor;
import org.pentaho.reporting.libraries.base.config.Configuration;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;

import java.io.OutputStream;
import java.util.Locale;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ReportExportTaskTest {
  @ClassRule
  public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  private MasterReport masterReport;
  private PentahoReportingSwingGuiContext swingGuiContext;
  private String targetPath;
  private boolean createParentFolder;

  @Before
  public void setUp() {
    masterReport = mock( MasterReport.class );
    swingGuiContext = mock( PentahoReportingSwingGuiContext.class );
    targetPath = ReportExportTask.BASE_RESOURCE_CLASS;
    createParentFolder = false;
  }

  @Test( expected = NullPointerException.class )
  public void testExportReportWithNullReport() {
    masterReport = null;

    when( swingGuiContext.getLocale() ).thenReturn( Locale.US );
    when( swingGuiContext.getStatusListener() ).thenReturn( mock( StatusListener.class ) );

    Runnable exportTask = new ReportExportTask( masterReport, swingGuiContext, targetPath, createParentFolder ) {
      protected ReportProcessor createReportProcessor( OutputStream fout ) throws Exception {
        return null;
      }
    };
    assertNull( exportTask );
  }

  @Test
  public void testExportReportWithSupportedLocale() {
    when( masterReport.getConfiguration() ).thenReturn( mock( Configuration.class ) );
    when( masterReport.getResourceManager() ).thenReturn( new ResourceManager() );

    when( swingGuiContext.getLocale() ).thenReturn( Locale.US );
    when( swingGuiContext.getStatusListener() ).thenReturn( mock( StatusListener.class ) );

    Runnable exportTask = new ReportExportTask( masterReport, swingGuiContext, targetPath, createParentFolder ) {
      protected ReportProcessor createReportProcessor( OutputStream fout ) throws Exception {
        PdfOutputProcessor outputProcessor =
          new PdfOutputProcessor( masterReport.getConfiguration(), fout, masterReport.getResourceManager() );
        return new PageableReportProcessor( masterReport, outputProcessor );
      }
    };

    assertNotNull( exportTask );
    exportTask.run();
    assertThat( swingGuiContext.getStatusType(), not( StatusType.ERROR ) );
  }

  @Test
  public void testExportReportWithUnsupportedLocale() {
    when( masterReport.getConfiguration() ).thenReturn( mock( Configuration.class ) );
    when( masterReport.getResourceManager() ).thenReturn( new ResourceManager() );

    when( swingGuiContext.getLocale() ).thenReturn( Locale.UK );
    when( swingGuiContext.getStatusListener() ).thenReturn( mock( StatusListener.class ) );

    Runnable exportTask = new ReportExportTask( masterReport, swingGuiContext, targetPath, createParentFolder ) {
      protected ReportProcessor createReportProcessor( OutputStream fout ) throws Exception {
        PdfOutputProcessor outputProcessor =
          new PdfOutputProcessor( masterReport.getConfiguration(), fout, masterReport.getResourceManager() );
        return new PageableReportProcessor( masterReport, outputProcessor );
      }
    };

    assertNotNull( exportTask );
    exportTask.run();
    assertThat( swingGuiContext.getStatusType(), not( StatusType.ERROR ) );
  }
}
