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
package org.pentaho.di.trans.steps.s3csvinput;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.encryption.TwoWayPasswordEncoderPluginType;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.TextFileInputFieldValidator;
import org.pentaho.di.trans.steps.textfileinput.TextFileInputField;

/**
 * @author Tatsiana_Kasiankova
 *
 */
@SuppressWarnings( "deprecation" )
public class S3CsvInputMetaTest {

  private static final String TEST_AWS_SECRET_KEY = "TestAwsSecretKey";
  private static final String TEST_ACCESS_KEY = "TestAccessKey";
  private static final String TEST_AWS_SECRET_KEY_ENCRYPTED = "Encrypted 2eafddcbc2bd081b7ae1abc75cab9aac3";
  private static final String TEST_ACCESS_KEY_ENCRYPTED = "Encrypted 2be98af9c0fd486a5a81aab63cdb9aac3";

  @BeforeClass
  public static void setUp() throws KettleException {
    PluginRegistry.addPluginType( TwoWayPasswordEncoderPluginType.getInstance() );
    PluginRegistry.init( true );
    String passwordEncoderPluginID =
      Const.NVL( EnvUtil.getSystemProperty( Const.KETTLE_PASSWORD_ENCODER_PLUGIN ), "Kettle" );
    Encr.init( passwordEncoderPluginID );
  }

  @Test
  public void testSerialization() throws KettleException {
    List<String> attributes = Arrays.asList( "AwsAccessKey", "AwsSecretKey", "Bucket", "Filename", "FilenameField",
      "RowNumField", "IncludingFilename", "Delimiter", "Enclosure", "HeaderPresent", "MaxLineSize",
      "LazyConversionActive", "RunningInParallel", "InputFields" );

    Map<String, FieldLoadSaveValidator<?>> typeMap = new HashMap<>();
    typeMap.put( TextFileInputField[].class.getCanonicalName(),
      new ArrayLoadSaveValidator<>( new TextFileInputFieldValidator() ) );
    Map<String, String> getterMap = new HashMap<>();
    Map<String, String> setterMap = new HashMap<>();
    LoadSaveTester<S3CsvInputMeta> tester = new LoadSaveTester<>( S3CsvInputMeta.class, attributes,
      getterMap, setterMap, new HashMap<String, FieldLoadSaveValidator<?>>(), typeMap );
    tester.testSerialization();
  }

  @Test
  public void testGetS3Service_notEncryptedKeys() {
    S3CsvInputMeta s3CvsInput = new S3CsvInputMeta();
    s3CvsInput.setAwsAccessKey( TEST_ACCESS_KEY );
    s3CvsInput.setAwsSecretKey( TEST_AWS_SECRET_KEY );

    try {
      S3Service s3Service = s3CvsInput.getS3Service( new Variables() );
      assertNotNull( s3Service );
      assertEquals( TEST_ACCESS_KEY, s3Service.getProviderCredentials().getAccessKey() );
      assertEquals( TEST_AWS_SECRET_KEY, s3Service.getProviderCredentials().getSecretKey() );
    } catch ( S3ServiceException e ) {
      fail( "No exception should be thrown. But it was:" + e.getLocalizedMessage() );
    }
  }

  @Test
  public void testGetS3Service_WithEncryptedKeys() {
    S3CsvInputMeta s3CvsInput = new S3CsvInputMeta();
    s3CvsInput.setAwsAccessKey( TEST_ACCESS_KEY_ENCRYPTED );
    s3CvsInput.setAwsSecretKey( TEST_AWS_SECRET_KEY_ENCRYPTED );

    try {
      S3Service s3Service = s3CvsInput.getS3Service( new Variables() );
      assertNotNull( s3Service );
      assertEquals( TEST_ACCESS_KEY, s3Service.getProviderCredentials().getAccessKey() );
      assertEquals( TEST_AWS_SECRET_KEY, s3Service.getProviderCredentials().getSecretKey() );
    } catch ( S3ServiceException e ) {
      fail( "No exception should be thrown. But it was:" + e.getLocalizedMessage() );
    }
  }

}
