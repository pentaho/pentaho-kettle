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

package org.pentaho.di.ui.trans.steps.simplemapping;

import org.junit.Test;
import org.pentaho.di.core.ObjectLocationSpecificationMethod;
import org.pentaho.di.core.exception.KettleException;

import static org.mockito.Mockito.*;

public class SimpleMappingDialogTest {

  @Test( expected = KettleException.class )
  public void loadTransformation() throws KettleException {

    SimpleMappingDialog simpleMappingDialog = mock( SimpleMappingDialog.class );

    doCallRealMethod().when( simpleMappingDialog ).loadTransformation();

    when( simpleMappingDialog.getReferenceObjectId() ).thenReturn( null );
    when( simpleMappingDialog.getSpecificationMethod() ).thenReturn(
      ObjectLocationSpecificationMethod.REPOSITORY_BY_REFERENCE );

    simpleMappingDialog.loadTransformation();

  }

}
