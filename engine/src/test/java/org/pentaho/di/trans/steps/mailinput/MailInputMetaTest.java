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

package org.pentaho.di.trans.steps.mailinput;

/**
 * Tests for MailInputMeta class
 *
 * @author Marc Batchelor - removed useless test case, added load/save tests
 * @see MailInputMeta
 */
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.job.entries.getpop.MailConnectionMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.initializer.InitializerInterface;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.IntLoadSaveValidator;

public class MailInputMetaTest implements InitializerInterface<StepMetaInterface> {
  LoadSaveTester loadSaveTester;
  Class<MailInputMeta> testMetaClass = MailInputMeta.class;

  @Before
  public void setUpLoadSave() throws Exception {
    KettleEnvironment.init();
    PluginRegistry.init( true );
    List<String> attributes =
        Arrays.asList( "conditionReceivedDate", "valueimaplist", "serverName", "userName", "password", "useSSL", "port",
            "firstMails", "retrievemails", "delete", "protocol", "firstIMAPMails", "IMAPFolder", "senderSearchTerm",
            "notTermSenderSearch", "recipientSearch", "subjectSearch", "receivedDate1", "receivedDate2",
            "notTermSubjectSearch", "notTermRecipientSearch", "notTermReceivedDateSearch", "includeSubFolders", "useProxy",
            "proxyUsername", "folderField", "dynamicFolder", "rowLimit", "useBatch", "start", "end", "batchSize",
            "stopOnError", "inputFields" );

    Map<String, String> getterMap = new HashMap<String, String>();
    Map<String, String> setterMap = new HashMap<String, String>();

    Map<String, FieldLoadSaveValidator<?>> attrValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    attrValidatorMap.put( "inputFields",
        new ArrayLoadSaveValidator<MailInputField>( new MailInputFieldLoadSaveValidator(), 5 ) );
    attrValidatorMap.put( "batchSize", new IntLoadSaveValidator( 1000 ) );
    attrValidatorMap.put( "conditionReceivedDate", new IntLoadSaveValidator( MailConnectionMeta.conditionDateCode.length ) );
    attrValidatorMap.put( "valueimaplist", new IntLoadSaveValidator( MailConnectionMeta.valueIMAPListCode.length ) );
    attrValidatorMap.put( "port", new StringIntLoadSaveValidator( 65534 ) );

    Map<String, FieldLoadSaveValidator<?>> typeValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();

    loadSaveTester =
        new LoadSaveTester( testMetaClass, attributes, new ArrayList<String>(), new ArrayList<String>(),
            getterMap, setterMap, attrValidatorMap, typeValidatorMap, this );
  }

  // Call the allocate method on the LoadSaveTester meta class
  @Override
  public void modify( StepMetaInterface someMeta ) {
    if ( someMeta instanceof MailInputMeta ) {
      ( (MailInputMeta) someMeta ).allocate( 5 );
    }
  }

  @Test
  public void testSerialization() throws KettleException {
    loadSaveTester.testSerialization();
  }

  public class MailInputFieldLoadSaveValidator implements FieldLoadSaveValidator<MailInputField> {
    final Random rand = new Random();
    @Override
    public MailInputField getTestObject() {
      MailInputField rtn = new MailInputField();
      rtn.setName( UUID.randomUUID().toString() );
      rtn.setColumn( rand.nextInt( MailInputField.ColumnDesc.length ) );
      return rtn;
    }

    @Override
    public boolean validateTestObject( MailInputField testObject, Object actual ) {
      if ( !( actual instanceof MailInputField ) ) {
        return false;
      }
      MailInputField another = (MailInputField) actual;
      return new EqualsBuilder()
          .append( testObject.getName(), another.getName() )
          .append( testObject.getColumn(), another.getColumn() )
      .isEquals();
    }
  }

  public class StringIntLoadSaveValidator implements FieldLoadSaveValidator<String> {
    final Random rand = new Random();
    int intBound;

    public StringIntLoadSaveValidator( ) {
      intBound = 0;
    }

    public StringIntLoadSaveValidator( int bounds ) {
      if ( bounds <= 0 ) {
        throw new IllegalArgumentException( "Bad boundary for StringIntLoadSaveValidator" );
      }
      this.intBound = bounds;
    }

    @Override
    public String getTestObject() {
      int someInt = 0;
      if ( intBound > 0 ) {
        someInt = rand.nextInt( intBound );
      } else {
        someInt = rand.nextInt();
      }
      return Integer.toString( someInt );
    }

    @Override
    public boolean validateTestObject( String testObject, Object actual ) {
      return ( actual.equals( testObject ) );
    }
  }
}
