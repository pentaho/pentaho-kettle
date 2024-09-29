/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2024 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.core.util;

import org.junit.Before;
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

import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.test.util.XXEUtils;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class SerializationHelperTest {
  private static Repository repo;

  @Before
  public void setUp() throws Exception {
    repo = mock( Repository.class );
    doReturn( XXEUtils.MALICIOUS_XML ).when( repo ).getJobEntryAttributeString( any( ObjectId.class ), anyString() );
    doReturn( XXEUtils.MALICIOUS_XML ).when( repo ).getStepAttributeString( any( ObjectId.class ), anyString() );
  }

  @Test( expected = NullPointerException.class )
  public void readingJobRepoThrowsExceptionWhenParsingXmlWithBigAmountOfExternalEntities() throws Exception {
    SerializationHelper.readJobRep( null, repo, null, new ArrayList<>() );
  }

  @Test( expected = NullPointerException.class )
  public void readingStepRepoThrowsExceptionWhenParsingXmlWithBigAmountOfExternalEntities() throws Exception {
    SerializationHelper.readStepRep( null, repo, null, new ArrayList<>() );
  }
}
