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


package org.pentaho.di.www.service.zip;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.unzip.JobEntryUnZip;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.pentaho.di.core.util.Assert.assertNotNull;

public class ZipServiceKettleTest {

  protected ZipServiceKettle zipService;

  @Before
  public void setup() {
    zipService = new ZipServiceKettle();
  }

  @Test
  public void testSetValues() {

    String destinationDirectory = "/tmp/some/path";
    String zipFile = "/quotes/to/be/or/not/to/be/that/is/the/question.zip";
    String empty = "";
    JobEntryUnZip jobEntryUnZip = new JobEntryUnZip();


    zipService.setValues( jobEntryUnZip, zipFile, destinationDirectory);

    assertEquals( zipFile, jobEntryUnZip.getZipFilename() );
    assertEquals( empty, jobEntryUnZip.getWildcardSource() );
    assertEquals( empty, jobEntryUnZip.getWildcardExclude() );
    assertEquals( destinationDirectory, jobEntryUnZip.getSourceDirectory() );
    assertEquals( empty, jobEntryUnZip.getMoveToDirectory() );
  }

  @Test
  public void testExecute() {

    String destinationDirectory = "/tmp/some/path";
    String zipFile = "/quotes/to/be/or/not/to/be/that/is/the/question.zip";

    JobEntryUnZip jobEntryUnZip = mock( JobEntryUnZip.class );

    zipService.execute( jobEntryUnZip, zipFile, destinationDirectory );

    verify( jobEntryUnZip ).execute( any(), anyInt() );

  }

  @Test
  public void testInstantiateJobEntryUnZip() {

    JobEntryUnZip jobEntryUnZip = zipService.instantiateJobEntryUnZip();
    assertNotNull( jobEntryUnZip );
    assertNotNull( jobEntryUnZip.getParentJobMeta() );
    assertNotNull( jobEntryUnZip.getName() );

  }

  @Test
  public void testInstantiateJobMeta() {

    JobMeta jobMeta = zipService.instantiateJobMeta();
    assertNotNull( jobMeta );
  }

}
