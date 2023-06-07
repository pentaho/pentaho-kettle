/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2023 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.plugins.fileopensave.dragdrop;

import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.plugins.fileopensave.api.providers.EntityType;
import org.pentaho.di.plugins.fileopensave.providers.repository.RepositoryFileProvider;
import org.pentaho.di.plugins.fileopensave.providers.vfs.VFSFileProvider;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class ElementTransferTest {
  private static final String slash = "\\" + java.io.File.separator;
  private static final String NAME = "filename";
  private static final EntityType TYPE = EntityType.LOCAL_FILE;
  private static final String PATH = "/tmp/" + NAME;
  private static final String LOCAL_PROVIDER = "local";

  private VariableSpace space = new Variables();

  ElementTransfer elementTransfer;

  @Before
  public void setUp() throws Exception {
    elementTransfer = ElementTransfer.getInstance();
    ElementTransfer.testMode = true; //Set mode to test so it doesn't pass the data to the OS
  }

  @Test
  public void getTypeIds() {
    assertArrayEquals( new int[]{ ElementTransfer.TYPEID },  elementTransfer.getTypeIds() );
  }

  @Test
  public void getTypeNames() {
    assertArrayEquals( new String[] { ElementTransfer.TYPE_NAME }, elementTransfer.getTypeNames() );
  }

  @Test
  public void javaToNativeAndBack() {
    Element element = new Element( NAME, TYPE, adjustSlashes( PATH ), LOCAL_PROVIDER );
    TransferData transferData = new TransferData();
    elementTransfer.javaToNative( new Object[] { element.convertToFile( space ) }, transferData );
    Element[] elements = (Element[]) elementTransfer.nativeToJava( transferData );
    assertEquals( element, elements[0] );
  }

  private String adjustSlashes( String target ) {
    target = target.replaceAll( "/", slash );
    if ( target.startsWith( "\\" ) ) {
      target = "c:" + target;
    }
    return target;
  }
}