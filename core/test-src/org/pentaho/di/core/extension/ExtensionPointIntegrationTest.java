/*
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.core.extension;

import javassist.CannotCompileException;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtNewMethod;
import javassist.NotFoundException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.plugins.PluginRegistry;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;


public class ExtensionPointIntegrationTest {
  public static final String EXECUTED_FIELD_NAME = "executed";
  private static ClassPool pool;

  @BeforeClass
  public static void setupBeforeClass() throws Exception {
    pool = ClassPool.getDefault();
    pool.insertClassPath( new ClassClassPath( ExtensionPointIntegrationTest.class ) );
    for ( KettleExtensionPoint ep : KettleExtensionPoint.values() ) {
      ExtensionPointPluginType.getInstance().registerCustom( createClassRuntime( ep ),
          "custom", "id" + ep.id, ep.id, "no description", null );
    }

    KettleClientEnvironment.init();
  }

  @Test
  public void test() throws Exception {
    // check that all extension points are added to the map
    assertEquals( KettleExtensionPoint.values().length, ExtensionPointMap.getInstance().getMap().size() );

    // check that all extension points are executed
    final LogChannelInterface log = mock( LogChannelInterface.class );
    for ( KettleExtensionPoint ep : KettleExtensionPoint.values() ) {
      final ExtensionPointInterface currentEP = ExtensionPointMap.getInstance().get( ep.id ).get( "id" + ep.id );
      assertFalse( currentEP.getClass().getField( EXECUTED_FIELD_NAME ).getBoolean( currentEP ) );
      ExtensionPointHandler.callExtensionPoint( log, ep.id, null );
      assertTrue( currentEP.getClass().getField( EXECUTED_FIELD_NAME ).getBoolean( currentEP ) );
    }

    // check modification of extension point
    final KettleExtensionPoint jobAfterOpen = KettleExtensionPoint.JobAfterOpen;
    final ExtensionPointInterface int1 = ExtensionPointMap.getInstance().get( jobAfterOpen.id ).get( "id" + jobAfterOpen.id );
    ExtensionPointPluginType.getInstance().registerCustom( createClassRuntime( jobAfterOpen, "Edited" ), "custom", "id"
            + jobAfterOpen.id, jobAfterOpen.id,
        "no description", null );
    assertNotSame( int1, ExtensionPointMap.getInstance().get( jobAfterOpen.id ) );
    assertEquals( KettleExtensionPoint.values().length, ExtensionPointMap.getInstance().getMap().size() );

    // check removal of extension point
    PluginRegistry.getInstance().removePlugin( ExtensionPointPluginType.class, PluginRegistry.getInstance().getPlugin(
        ExtensionPointPluginType.class, "id" + jobAfterOpen.id ) );
    assertTrue( ExtensionPointMap.getInstance().get( jobAfterOpen.id ).isEmpty() );
    assertEquals( KettleExtensionPoint.values().length - 1, ExtensionPointMap.getInstance().getMap().size() );
  }

  private static Class createClassRuntime( KettleExtensionPoint ep ) throws NotFoundException, CannotCompileException {
    return createClassRuntime( ep, "" );
  }

  /**
   * Create ExtensionPointInterface subclass in runtime
   *
   * @param ep extension point id
   * @param addition addition to class name to avoid duplicate classes
   * @return class
   * @throws NotFoundException
   * @throws CannotCompileException
   */
  private static Class createClassRuntime( KettleExtensionPoint ep, String addition )
      throws NotFoundException, CannotCompileException {
    final CtClass ctClass = pool.makeClass( "Plugin" + ep.id + addition );
    ctClass.addInterface( pool.get( ExtensionPointInterface.class.getCanonicalName() ) );
    ctClass.addField( CtField.make( "public boolean " + EXECUTED_FIELD_NAME + ";", ctClass ) );
    ctClass.addMethod( CtNewMethod.make(
        "public void callExtensionPoint( org.pentaho.di.core.logging.LogChannelInterface log, Object object ) "
            + "throws org.pentaho.di.core.exception.KettleException { " + EXECUTED_FIELD_NAME + " = true; }",
        ctClass ) );
    return ctClass.toClass();
  }
}
