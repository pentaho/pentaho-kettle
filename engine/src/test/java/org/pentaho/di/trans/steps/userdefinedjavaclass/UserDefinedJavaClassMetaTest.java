/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

  @Test
  public void cookClassesCachingTest() throws Exception {
    String codeBlock1 = "public boolean processRow() {\n"
        + "    return true;\n"
        + "}\n\n";
    String codeBlock2 = "public boolean processRow() {\n"
        + "    // Random comment\n"
        + "    return true;\n"
        + "}\n\n";
    UserDefinedJavaClassMeta userDefinedJavaClassMeta1 = new UserDefinedJavaClassMeta();

    UserDefinedJavaClassDef userDefinedJavaClassDef1 = new UserDefinedJavaClassDef( UserDefinedJavaClassDef.ClassType.NORMAL_CLASS, "MainClass", codeBlock1 );

    StepMeta stepMeta = Mockito.mock( StepMeta.class );
    Mockito.when( stepMeta.getName() ).thenReturn( "User Defined Java Class" );
    userDefinedJavaClassMeta1.setParentStepMeta( stepMeta );

    UserDefinedJavaClassMeta userDefinedJavaClassMetaSpy = Mockito.spy( userDefinedJavaClassMeta1 );

    // Added classloader for https://jira.pentaho.com/browse/PDI-44134
    Class<?> clazz1 = userDefinedJavaClassMetaSpy.cookClass( userDefinedJavaClassDef1, null );
    Class<?> clazz2 = userDefinedJavaClassMetaSpy.cookClass( userDefinedJavaClassDef1, clazz1.getClassLoader() );
    Assert.assertTrue( clazz1 == clazz2 ); // Caching should work here and return exact same class

    UserDefinedJavaClassMeta userDefinedJavaClassMeta2 = new UserDefinedJavaClassMeta();
    UserDefinedJavaClassDef userDefinedJavaClassDef2 = new UserDefinedJavaClassDef( UserDefinedJavaClassDef.ClassType.NORMAL_CLASS, "AnotherClass", codeBlock2 );

    StepMeta stepMeta2 = Mockito.mock( StepMeta.class );
    Mockito.when( stepMeta2.getName() ).thenReturn( "Another UDJC" );
    userDefinedJavaClassMeta2.setParentStepMeta( stepMeta2 );
    UserDefinedJavaClassMeta userDefinedJavaClassMeta2Spy = Mockito.spy( userDefinedJavaClassMeta2 );

    Class<?> clazz3 = userDefinedJavaClassMeta2Spy.cookClass( userDefinedJavaClassDef2, clazz2.getClassLoader() );

    Assert.assertTrue( clazz3 != clazz1 ); // They should not be the exact same class
  }

  @Test
  public void oderDefinitionTest() throws Exception {
    String codeBlock1 = "public boolean processRow() {\n"
      + "    return true;\n"
      + "}\n\n";
    String codeBlock2 = "public boolean extraClassA() {\n"
      + "    // Random comment\n"
      + "    return true;\n"
      + "}\n\n";
    String codeBlock3 = "public boolean extraClassB() {\n"
      + "    // Random comment\n"
      + "    return true;\n"
      + "}\n\n";
    UserDefinedJavaClassMeta userDefinedJavaClassMeta = new UserDefinedJavaClassMeta();
    UserDefinedJavaClassDef processClassDef = new UserDefinedJavaClassDef( UserDefinedJavaClassDef.ClassType.TRANSFORM_CLASS, "Process", codeBlock1 );
    UserDefinedJavaClassDef processClassDefA = new UserDefinedJavaClassDef( UserDefinedJavaClassDef.ClassType.TRANSFORM_CLASS, "ProcessA", codeBlock1 );
    UserDefinedJavaClassDef normalClassADef = new UserDefinedJavaClassDef( UserDefinedJavaClassDef.ClassType.NORMAL_CLASS, "A", codeBlock1 );
    UserDefinedJavaClassDef normalClassBDef = new UserDefinedJavaClassDef( UserDefinedJavaClassDef.ClassType.NORMAL_CLASS, "B", codeBlock1 );
    UserDefinedJavaClassDef normalClassCDef = new UserDefinedJavaClassDef( UserDefinedJavaClassDef.ClassType.NORMAL_CLASS, "C", codeBlock1 );

    ArrayList<UserDefinedJavaClassDef> defs = new ArrayList<>(5);
    defs.add(processClassDefA);
    defs.add(processClassDef);
    defs.add(normalClassCDef);
    defs.add(normalClassBDef);
    defs.add(normalClassADef);

    StepMeta stepMeta = Mockito.mock( StepMeta.class );
    Mockito.when( stepMeta.getName() ).thenReturn( "User Defined Java Class" );
    userDefinedJavaClassMeta.setParentStepMeta( stepMeta );

    // Test reording the reverse order test
    List<UserDefinedJavaClassDef> orderDefs = userDefinedJavaClassMeta.orderDefinitions( defs );
    Assert.assertTrue( orderDefs.get(0).getClassName().equals( "A" ) );
    Assert.assertTrue( orderDefs.get(1).getClassName().equals( "B" ) );
    Assert.assertTrue( orderDefs.get(2).getClassName().equals( "C" ) );
    Assert.assertTrue( orderDefs.get(3).getClassName().equals( "Process" ) );
    Assert.assertTrue( orderDefs.get(4).getClassName().equals( "ProcessA" ) );


    // Random order test
    defs.clear();
    defs.add(normalClassADef);
    defs.add(normalClassCDef);
    defs.add(processClassDefA);
    defs.add(normalClassBDef);
    defs.add(processClassDef);
    orderDefs = userDefinedJavaClassMeta.orderDefinitions( defs );
    Assert.assertTrue( orderDefs.get(0).getClassName().equals( "A" ) );
    Assert.assertTrue( orderDefs.get(1).getClassName().equals( "B" ) );
    Assert.assertTrue( orderDefs.get(2).getClassName().equals( "C" ) );
    Assert.assertTrue( orderDefs.get(3).getClassName().equals( "Process" ) );
    Assert.assertTrue( orderDefs.get(4).getClassName().equals( "ProcessA" ) );
  }


}
