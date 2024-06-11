/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2020-2024 by Hitachi Vantara : http://www.pentaho.com
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
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class GetXMLDataMetaTest {

  @Test
  public void testSaveRepOfAdditionalOutputFields() throws KettleException {
    GetXMLDataMeta getXMLDataMeta = new GetXMLDataMeta();
    String[] fileName = new String[] {};
    GetXMLDataField[] inputFields = new GetXMLDataField[] {};
    ReflectionTestUtils.setField( getXMLDataMeta, "fileName", fileName );
    ReflectionTestUtils.setField( getXMLDataMeta, "inputFields", inputFields );

    Repository rep = mock( Repository.class );
    IMetaStore metaStore = mock( IMetaStore.class );
    ObjectId idTransformation = mock( ObjectId.class );
    ObjectId idStep = mock( ObjectId.class );

    String shortFileFieldName = UUID.randomUUID().toString();
    ReflectionTestUtils.setField( getXMLDataMeta, "shortFileFieldName", shortFileFieldName );
    String extensionFieldName = UUID.randomUUID().toString();
    ReflectionTestUtils.setField( getXMLDataMeta, "extensionFieldName", extensionFieldName );
    String pathFieldName = UUID.randomUUID().toString();
    ReflectionTestUtils.setField( getXMLDataMeta, "pathFieldName", pathFieldName );
    String sizeFieldName = UUID.randomUUID().toString();
    ReflectionTestUtils.setField( getXMLDataMeta, "sizeFieldName", sizeFieldName );
    String hiddenFieldName = UUID.randomUUID().toString();
    ReflectionTestUtils.setField( getXMLDataMeta, "hiddenFieldName", hiddenFieldName );
    String lastModificationTimeFieldName = UUID.randomUUID().toString();
    ReflectionTestUtils.setField( getXMLDataMeta, "lastModificationTimeFieldName", lastModificationTimeFieldName );
    String uriNameFieldName = UUID.randomUUID().toString();
    ReflectionTestUtils.setField( getXMLDataMeta, "uriNameFieldName", uriNameFieldName );
    String rootUriNameFieldName = UUID.randomUUID().toString();
    ReflectionTestUtils.setField( getXMLDataMeta, "rootUriNameFieldName", rootUriNameFieldName );

    getXMLDataMeta.saveRep( rep, metaStore, idTransformation, idStep );

    verify( rep, times( 1 ) ).saveStepAttribute( idTransformation,
      idStep, "shortFileFieldName", shortFileFieldName );
    verify( rep, times( 1 ) ).saveStepAttribute( idTransformation,
      idStep, "extensionFieldName", extensionFieldName );
    verify( rep, times( 1 ) ).saveStepAttribute( idTransformation,
      idStep, "pathFieldName", pathFieldName );
    verify( rep, times( 1 ) ).saveStepAttribute( idTransformation,
      idStep, "sizeFieldName", sizeFieldName );
    verify( rep, times( 1 ) ).saveStepAttribute( idTransformation,
      idStep, "hiddenFieldName", hiddenFieldName );
    verify( rep, times( 1 ) ).saveStepAttribute( idTransformation,
      idStep, "lastModificationTimeFieldName", lastModificationTimeFieldName );
    verify( rep, times( 1 ) ).saveStepAttribute( idTransformation,
      idStep, "uriNameFieldName", uriNameFieldName );
    verify( rep, times( 1 ) ).saveStepAttribute( idTransformation,
      idStep, "rootUriNameFieldName", rootUriNameFieldName );

  }
}
