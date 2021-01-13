/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2020 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.getxmldata;

import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.metastore.api.IMetaStore;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.util.reflection.Whitebox.getInternalState;
import static org.mockito.internal.util.reflection.Whitebox.setInternalState;

public class GetXMLDataMetaTest {

  @Test
  public void testSaveRepOfAdditionalOutputFields() throws KettleException {
    GetXMLDataMeta getXMLDataMeta = new GetXMLDataMeta();
    String[] fileName = new String[] {};
    GetXMLDataField[] inputFields = new GetXMLDataField[] {};
    setInternalState( getXMLDataMeta, "fileName", fileName );
    setInternalState( getXMLDataMeta, "inputFields", inputFields );

    Repository rep = mock( Repository.class );
    IMetaStore metaStore = mock( IMetaStore.class );
    ObjectId idTransformation = mock( ObjectId.class );
    ObjectId idStep = mock( ObjectId.class );

    getXMLDataMeta.saveRep( rep, metaStore, idTransformation, idStep );

    verify( rep, times( 1 ) ).saveStepAttribute( idTransformation,
      idStep, "shortFileFieldName", (String) getInternalState( getXMLDataMeta, "shortFileFieldName" ) );
    verify( rep, times( 1 ) ).saveStepAttribute( idTransformation,
      idStep, "extensionFieldName", (String) getInternalState( getXMLDataMeta, "extensionFieldName" ) );
    verify( rep, times( 1 ) ).saveStepAttribute( idTransformation,
      idStep, "pathFieldName", (String) getInternalState( getXMLDataMeta, "pathFieldName" ) );
    verify( rep, times( 1 ) ).saveStepAttribute( idTransformation,
      idStep, "sizeFieldName", (String) getInternalState( getXMLDataMeta, "sizeFieldName" ) );
    verify( rep, times( 1 ) ).saveStepAttribute( idTransformation,
      idStep, "hiddenFieldName", (String) getInternalState( getXMLDataMeta, "hiddenFieldName" ) );
    verify( rep, times( 1 ) ).saveStepAttribute( idTransformation,
      idStep, "lastModificationTimeFieldName", (String) getInternalState( getXMLDataMeta, "lastModificationTimeFieldName" ) );
    verify( rep, times( 1 ) ).saveStepAttribute( idTransformation,
      idStep, "uriNameFieldName", (String) getInternalState( getXMLDataMeta, "uriNameFieldName" ) );
    verify( rep, times( 1 ) ).saveStepAttribute( idTransformation,
      idStep, "rootUriNameFieldName", (String) getInternalState( getXMLDataMeta, "rootUriNameFieldName" ) );

  }
}
