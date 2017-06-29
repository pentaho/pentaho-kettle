/*
 * *****************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2017 by Pentaho : http://www.pentaho.com
 *
 *  *******************************************************************************
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 *  this file except in compliance with the License. You may obtain a copy of the
 *  License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * *****************************************************************************
 *
 */

package org.pentaho.di.engine.configuration.impl.extension;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.core.attributes.metastore.EmbeddedMetaStore;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.engine.configuration.impl.RunConfigurationManager;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by bmorrise on 5/15/17.
 */
@RunWith( MockitoJUnitRunner.class )
public class RunConfigurationImportExtensionPointTest {

  private RunConfigurationImportExtensionPoint runConfigurationImportExtensionPoint;

  @Mock private RunConfigurationManager runConfigurationManager;
  @Mock private AbstractMeta abstractMeta;
  @Mock private LogChannelInterface log;
  @Mock private EmbeddedMetaStore embeddedMetaStore;

  @Before
  public void setup() {
    runConfigurationImportExtensionPoint = new RunConfigurationImportExtensionPoint( runConfigurationManager );

    when( abstractMeta.getEmbeddedMetaStore() ).thenReturn( embeddedMetaStore );
  }

  @Test
  public void testCallExtensionPoint() throws Exception {
    runConfigurationImportExtensionPoint.callExtensionPoint( log, abstractMeta );

    verify( abstractMeta ).getEmbeddedMetaStore();
  }

}
