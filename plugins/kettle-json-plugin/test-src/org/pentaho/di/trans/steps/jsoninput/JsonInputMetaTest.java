/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.jsoninput;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.metastore.api.IMetaStore;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by bmorrise on 3/22/16.
 */
@RunWith( MockitoJUnitRunner.class )
public class JsonInputMetaTest {

  public static final String DATA = "data";
  public static final String NAME = "name";

  JsonInputMeta jsonInputMeta;

  @Mock
  RowMetaInterface rowMeta;

  @Mock
  RowMetaInterface rowMetaInterfaceItem;

  @Mock
  StepMeta nextStep;

  @Mock
  VariableSpace space;

  @Mock
  Repository repository;

  @Mock
  IMetaStore metaStore;

  @Mock
  JsonInputMeta.InputFiles inputFiles;

  @Mock
  JsonInputField inputField;

  @Before
  public void setup() {
    jsonInputMeta = new JsonInputMeta();
    jsonInputMeta.setInputFiles( inputFiles );
    jsonInputMeta.setInputFields( new JsonInputField[] { inputField } );
  }

  @Test
  public void getFieldsRemoveSourceField() throws Exception {
    RowMetaInterface[] info = new RowMetaInterface[1];
    info[ 0 ] = rowMetaInterfaceItem;

    jsonInputMeta.setRemoveSourceField( true );
    jsonInputMeta.setFieldValue( DATA );
    jsonInputMeta.setInFields( true );

    when( rowMeta.indexOfValue( DATA ) ).thenReturn( 0 );

    jsonInputMeta.getFields( rowMeta, NAME, info, nextStep, space, repository, metaStore );

    verify( rowMeta ).removeValueMeta( 0 );
  }
}
