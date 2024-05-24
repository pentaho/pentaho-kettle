/*
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2024 by Hitachi Vantara : http://www.pentaho.com
 *
 * **************************************************************************
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
 */

package org.pentaho.di.core.util.serialization;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.pentaho.di.core.util.serialization.MetaXmlSerializer.serialize;
import static org.pentaho.di.core.util.serialization.StepMetaProps.from;

@RunWith ( MockitoJUnitRunner.class )
public class RepoSerializerTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Mock private Repository repo;
  @Mock private ObjectId transId, stepId;

  private StepMetaPropsTest.FooMeta stepMeta = StepMetaPropsTest.getTestFooMeta();

  @Before public void before() throws KettleException {
    KettleEnvironment.init();
  }

  @Test
  public void testSerialize() throws KettleException {
    String serialized = serialize( from( stepMeta ) );

    RepoSerializer
      .builder()
      .repo( repo )
      .stepId( stepId )
      .transId( transId )
      .stepMeta( stepMeta )
      .serialize();

    verify( repo, times( 1 ) )
      .saveStepAttribute( transId, stepId, "step-xml", serialized );
  }

  @Test
  public void testDeserialize() throws KettleException {
    StepMetaPropsTest.FooMeta blankMeta = new StepMetaPropsTest.FooMeta();
    String serialized = serialize( from( stepMeta ) );
    doReturn( serialized ).when( repo ).getStepAttributeString( stepId, "step-xml" );

    RepoSerializer
      .builder()
      .repo( repo )
      .stepId( stepId )
      .stepMeta( blankMeta )
      .deserialize();

    // blankMeta hydrated from the RepoSerializer should be the same as the serialized stepMeta
    assertThat( stepMeta, equalTo( blankMeta ) );
  }
}
