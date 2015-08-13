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

package org.pentaho.di.ui.trans.steps.elasticsearchbulk;

import org.elasticsearch.env.Environment;
import org.elasticsearch.env.FailedToResolveConfigException;
import org.junit.Before;
import org.junit.Test;

import java.net.URL;

import static org.junit.Assert.assertNotNull;

/**
 * Tests for functionality inside the Elasticsearch Bulk Insert dialog. Note that these tests may not be exercising the
 * dialog class/methods explicitly, but rather the code inside those methods.
 */
public class ElasticSearchBulkDialogTest {

  @Before
  public void setUp() throws Exception {
  }

  @Test( expected = FailedToResolveConfigException.class )
  public void testEnvironmentResolveError() {
    // This test determines whether Elasticsearch will be able to find the default names.txt file given the current
    // environment. One case where this might not happen is if the thread's current classloader does not know about
    // Elasticsearch classes (which can happen in PDI). This test exercises such a path.
    Thread.currentThread().setContextClassLoader( this.getClass().getClassLoader().getParent() );
    Environment env = new Environment();
    env.resolveConfig( "names.txt" );
  }

  @Test
  public void testEnvironmentResolve() {
    // This test determines whether Elasticsearch will be able to find the default names.txt file given the current
    // environment. One case where this might not happen is if the thread's current classloader does not know about
    // Elasticsearch classes (which can happen in PDI). This test exercises the "correct" path
    Thread.currentThread().setContextClassLoader( this.getClass().getClassLoader() );
    Environment env = new Environment();
    URL url = env.resolveConfig( "names.txt" );
    assertNotNull( url );
  }
}
