/*
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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

import org.junit.Assert;
import org.junit.Test;

public class UserDefinedJavaClassCodeSnippitsTest {

  @Test
  public void testSnippitMainUseRightRowsize() throws Exception {

    String code = UserDefinedJavaClassCodeSnippits.getSnippitsHelper().getCode( "Main" );
    Assert.assertTrue( "Wrong row size variable is used", code.contains( "r = createOutputRow(r, data.outputRowMeta.size());" ) );

  }

}
