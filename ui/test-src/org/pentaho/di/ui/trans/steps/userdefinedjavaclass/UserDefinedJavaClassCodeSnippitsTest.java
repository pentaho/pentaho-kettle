/*
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.ui.trans.steps.userdefinedjavaclass;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.ui.trans.steps.userdefinedjavaclass.UserDefinedJavaClassCodeSnippits.Snippit;

public class UserDefinedJavaClassCodeSnippitsTest {

  @Test
  public void testSnippitMainUseRightRowsize() throws Exception {
    String code = UserDefinedJavaClassCodeSnippits.getSnippitsHelper().getCode( "Main" );
    Assert.assertTrue( "Wrong row size variable is used", code.contains( "r = createOutputRow(r, data.outputRowMeta.size());" ) );
  }

  @Test
  public void testSnippitsWellDefined() throws KettleXMLException {
    List<String> snippitNames = new ArrayList<String>();
    for ( Snippit snippit : UserDefinedJavaClassCodeSnippits.getSnippitsHelper().getSnippits() ) {
      Assert.assertNotNull( snippit.category );
      Assert.assertFalse( Utils.isEmpty( snippit.name ) );
      Assert.assertFalse( Utils.isEmpty( snippit.code ) );
      Assert.assertFalse( Utils.isEmpty( snippit.sample ) );
      Assert.assertFalse( snippitNames.contains( snippit.name ) );
      snippitNames.add( snippit.name );
    }
  }
}
