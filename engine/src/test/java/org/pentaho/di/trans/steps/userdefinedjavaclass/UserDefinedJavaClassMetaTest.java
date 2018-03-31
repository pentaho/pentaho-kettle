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

package org.pentaho.di.trans.steps.userdefinedjavaclass;


import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.trans.step.StepMeta;

import java.util.Collections;

public class UserDefinedJavaClassMetaTest {

  @Test
  public void cookClassErrorCompilationTest() throws Exception {
    String wrongCode = "public boolean processRow() {\n"
      + "   return true;\n"
      + "}\n"
      + "\n"
      + "public boolean processRow() {\n"
      + "   return true;\n"
      + "}\n";


    UserDefinedJavaClassMeta userDefinedJavaClassMeta = new UserDefinedJavaClassMeta();

    UserDefinedJavaClassDef userDefinedJavaClassDef = Mockito.mock( UserDefinedJavaClassDef.class );
    Mockito.when( userDefinedJavaClassDef.isTransformClass() ).thenReturn( false );
    Mockito.when( userDefinedJavaClassDef.getSource() ).thenReturn( wrongCode );
    Mockito.when( userDefinedJavaClassDef.getClassName() ).thenReturn(  "MainClass" );
    Mockito.when( userDefinedJavaClassDef.isActive() ).thenReturn( true );

    StepMeta stepMeta = Mockito.mock( StepMeta.class );
    Mockito.when( stepMeta.getName() ).thenReturn( "User Defined Java Class" );
    userDefinedJavaClassMeta.setParentStepMeta( stepMeta );

    UserDefinedJavaClassMeta userDefinedJavaClassMetaSpy = Mockito.spy( userDefinedJavaClassMeta );
    Mockito.when( userDefinedJavaClassMetaSpy.getDefinitions( ) ).thenReturn( Collections.singletonList( userDefinedJavaClassDef ) );

    userDefinedJavaClassMetaSpy.cookClasses();
    Assert.assertEquals( 1, userDefinedJavaClassMeta.cookErrors.size() );
  }
}
