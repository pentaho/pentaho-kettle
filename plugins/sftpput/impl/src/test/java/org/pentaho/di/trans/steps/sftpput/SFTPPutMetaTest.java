/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2023 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.trans.steps.sftpput;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.IntLoadSaveValidator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SFTPPutMetaTest {
  LoadSaveTester loadSaveTester;
  Class<SFTPPutMeta> testMetaClass = SFTPPutMeta.class;
  ThreadLocal<SFTPPutMeta> testingMeta = new ThreadLocal<SFTPPutMeta>(); // here for validating afterFTPS
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Before
  public void setUpLoadSave() throws Exception {
    KettleEnvironment.init();
    PluginRegistry.init( false );
    List<String> attributes =
        Arrays.asList( "serverName", "serverPort", "userName", "password", "sourceFileFieldName", "remoteDirectoryFieldName",
            "addFilenameResut", "inputStream", "useKeyFile", "keyFilename", "keyPassPhrase", "compression", "createRemoteFolder",
            "proxyType", "proxyHost", "proxyPort", "proxyUsername", "proxyPassword", "destinationFolderFieldName", "createDestinationFolder",
            "afterFTPS", "remoteFilenameFieldName" );

    Map<String, String> getterMap = new HashMap<String, String>();
    Map<String, String> setterMap = new HashMap<String, String>();

    Map<String, FieldLoadSaveValidator<?>> attrValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    // See JobEntrySFTPPUT for the boundary ... chose not to create a test dependency between the Step and the JobEntry.
    attrValidatorMap.put( "afterFTPS", new IntLoadSaveValidator( 3 ) );
    Map<String, FieldLoadSaveValidator<?>> typeValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();

    loadSaveTester =
        new LoadSaveTester( testMetaClass, attributes, getterMap, setterMap, attrValidatorMap, typeValidatorMap );
  }

  @Test
  public void testSerialization() throws KettleException {
    loadSaveTester.testSerialization();
  }

  // Call the allocate method on the LoadSaveTester meta class
  public void modify( StepMetaInterface someMeta ) {
    if ( someMeta instanceof SFTPPutMeta ) {
      SFTPPutMeta curMeta = (SFTPPutMeta) someMeta;
      testingMeta.set( curMeta );
    }
  }
}
