/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Pentaho : http://www.pentaho.com
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
package org.pentaho.di.trans.steps.propertyinput;

import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.trans.step.BaseStep;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.Charset;

public class PropertyInputMetaTest {

  @Test
  public void testOpenNextFile() throws Exception {

    PropertyInputMeta propertyInputMeta = Mockito.mock( PropertyInputMeta.class );
    PropertyInputData propertyInputData = new PropertyInputData();
    FileInputList fileInputList = new FileInputList();
    FileObject fileObject = Mockito.mock( FileObject.class );
    FileName fileName = Mockito.mock( FileName.class );
    Mockito.when( fileName.getRootURI() ).thenReturn( "testFolder" );
    Mockito.when( fileName.getURI() ).thenReturn( "testFileName.ini" );

    String header = "test ini data with umlauts";
    String key = "key";
    String testValue = "value-with-äöü";
    String testData = "[" + header + "]\r\n"
            + key + "=" + testValue;
    String charsetEncode = "Windows-1252";

    InputStream inputStream = new ByteArrayInputStream( testData.getBytes(
            Charset.forName( charsetEncode ) ) );
    FileContent fileContent = Mockito.mock( FileContent.class );
    Mockito.when( fileObject.getContent() ).thenReturn( fileContent );
    Mockito.when( fileContent.getInputStream() ).thenReturn( inputStream );
    Mockito.when( fileObject.getName() ).thenReturn( fileName );
    fileInputList.addFile( fileObject );

    propertyInputData.files = fileInputList;
    propertyInputData.propfiles = false;
    propertyInputData.realEncoding = charsetEncode;

    PropertyInput propertyInput = Mockito.mock( PropertyInput.class );

    Field logField = BaseStep.class.getDeclaredField( "log" );
    logField.setAccessible( true );
    logField.set( propertyInput, Mockito.mock( LogChannelInterface.class ) );

    Mockito.doCallRealMethod().when( propertyInput ).dispose( propertyInputMeta, propertyInputData );

    propertyInput.dispose( propertyInputMeta, propertyInputData );

    Method method = PropertyInput.class.getDeclaredMethod( "openNextFile" );
    method.setAccessible( true );
    method.invoke( propertyInput );

    Assert.assertEquals( testValue, propertyInputData.wini.get( header ).get( key ) );
  }

}
