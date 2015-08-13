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

package org.pentaho.di.job;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.job.entries.empty.JobEntryEmpty;
import org.pentaho.di.job.entry.JobEntryCopy;

public class JobMetaTest {
  private JobMeta jm;
  private JobEntryEmpty je1;
  private JobEntryEmpty je2;
  private JobEntryEmpty je4;

  @Before
  public void setUp() {
    jm = new JobMeta();

    je1 = new JobEntryEmpty();
    je1.setName( "je1" );
    JobEntryCopy copy1 = new JobEntryCopy( je1 );

    je2 = new JobEntryEmpty();
    je2.setName( "je2" );
    JobEntryCopy copy2 = new JobEntryCopy( je2 );
    JobHopMeta hop = new JobHopMeta( copy1, copy2 );
    jm.addJobHop( hop );

    JobEntryEmpty je3 = new JobEntryEmpty();
    je3.setName( "je3" );
    copy2 = new JobEntryCopy( je3 );
    hop = new JobHopMeta( copy1, copy2 );
    jm.addJobHop( hop );

    je4 = new JobEntryEmpty();
    je4.setName( "je4" );
    copy1 = new JobEntryCopy( je3 );
    copy2 = new JobEntryCopy( je4 );
    hop = new JobHopMeta( copy1, copy2 );
    jm.addJobHop( hop );

  }

  @Test
  public void testPathExist() throws KettleXMLException, IOException, URISyntaxException {
    Assert.assertTrue( jm.isPathExist( je1, je4 ) );
  }

  @Test
  public void testPathNotExist() throws KettleXMLException, IOException, URISyntaxException {
    Assert.assertFalse( jm.isPathExist( je2, je4 ) );
  }
}