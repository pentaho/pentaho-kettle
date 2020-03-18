/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2019 by Hitachi Vantara : http://www.pentaho.com
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
