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

package org.pentaho.di.trans.steps.mailvalidator;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;

public class MailValidatorMetaTest {

  @Test
  public void testLoadSave() throws KettleException {
    List<String> attributes = Arrays.asList( "EmailField", "ResultFieldName", "ResultAsString", "SMTPCheck",
      "EmailValideMsg", "EmailNotValideMsg", "ErrorsField", "TimeOut", "DefaultSMTP", "EmailSender",
      "DefaultSMTPField", "DynamicDefaultSMTP" );

    LoadSaveTester<MailValidatorMeta> loadSaveTester =
      new LoadSaveTester<MailValidatorMeta>( MailValidatorMeta.class, attributes );

    loadSaveTester.testSerialization();
  }
}
