/*
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.steps.file.BaseFileField;
import org.pentaho.di.trans.steps.file.BaseFileInputFiles;
import org.pentaho.di.trans.steps.fileinput.text.TextFileInputMeta;

import java.util.Collections;

import static java.util.Objects.requireNonNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class MetaXmlSerializerTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Before public void before() throws KettleException {
    KettleEnvironment.init();
  }

  @Test public void testRoundTrip() {
    StepMetaPropsTest.FooMeta fooMeta = StepMetaPropsTest.getTestFooMeta();
    StepMetaPropsTest.FooMeta deserializedMeta = new StepMetaPropsTest.FooMeta();

    requireNonNull( MetaXmlSerializer.deserialize(
      MetaXmlSerializer.serialize(
        StepMetaProps.from( fooMeta ) ) ) )
      .to( deserializedMeta );

    assertThat( fooMeta, equalTo( deserializedMeta ) );
  }


  @Test public void testRoundTripWithEmptyList() {
    StepMetaPropsTest.FooMeta fooMeta = StepMetaPropsTest.getTestFooMeta();
    StepMetaPropsTest.FooMeta deserializedMeta = new StepMetaPropsTest.FooMeta();

    fooMeta.alist = Collections.emptyList();

    requireNonNull( MetaXmlSerializer.deserialize(
      MetaXmlSerializer.serialize(
        StepMetaProps.from( fooMeta ) ) ) )
      .to( deserializedMeta );

    assertThat( deserializedMeta, equalTo( fooMeta ) );
  }


  @Test public void testTextFileInputRoundTrip() {
    // TextFileInputMeta is one of the most complex, including arrays under @InjectionDeep.
    TextFileInputMeta fileInputMeta = new TextFileInputMeta();
    fileInputMeta.inputFields = new BaseFileField[] {
      new BaseFileField( "foo", 0, 10 ),
      new BaseFileField( "bar", 0, 10 )
    };
    fileInputMeta.inputFiles = new BaseFileInputFiles();
    final String[] fileNames = { "file1.txt", "file2.tsv" };
    fileInputMeta.inputFiles.fileName = fileNames;

    TextFileInputMeta rehydratedMeta = new TextFileInputMeta();

    StepMetaProps props = StepMetaProps.from( fileInputMeta );
    MetaXmlSerializer.deserialize( MetaXmlSerializer.serialize( props ) )
      .to( rehydratedMeta );

    assertThat( rehydratedMeta.inputFields[ 0 ].getName(), equalTo( "foo" ) );
    assertThat( rehydratedMeta.inputFields[ 1 ].getName(), equalTo( "bar" ) );
    assertThat( rehydratedMeta.inputFiles.fileName, equalTo( fileNames ) );
  }

}
