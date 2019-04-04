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

package org.pentaho.di.core.dnd;

import org.junit.Test;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class DragAndDropContainerTest {

  @Test
  public void newDNDContainer() {
    DragAndDropContainer dnd = new DragAndDropContainer( DragAndDropContainer.TYPE_BASE_STEP_TYPE, "Step Name"  );

    assertNotNull( dnd );
    assertNull( dnd.getId() );
    assertEquals( DragAndDropContainer.TYPE_BASE_STEP_TYPE, dnd.getType() );
    assertEquals( "BaseStep", dnd.getTypeCode() );
    assertEquals( "Step Name", dnd.getData() );
  }

  @Test
  public void newDNDContainerWithId() {
    DragAndDropContainer dnd = new DragAndDropContainer( DragAndDropContainer.TYPE_BASE_STEP_TYPE, "Step Name", "StepID"  );

    assertNotNull( dnd );
    assertEquals( "StepID", dnd.getId() );
    assertEquals( DragAndDropContainer.TYPE_BASE_STEP_TYPE, dnd.getType() );
    assertEquals( "BaseStep", dnd.getTypeCode() );
    assertEquals( "Step Name", dnd.getData() );
  }

  @Test
  public void setId() {
    DragAndDropContainer dnd = new DragAndDropContainer( DragAndDropContainer.TYPE_BASE_STEP_TYPE, "Step Name"  );
    dnd.setId( "StepID" );

    assertEquals( "StepID", dnd.getId() );
  }

  @Test
  public void setData() {
    DragAndDropContainer dnd = new DragAndDropContainer( DragAndDropContainer.TYPE_BASE_STEP_TYPE, "Step Name"  );
    dnd.setData( "Another Step" );

    assertEquals( "Another Step", dnd.getData() );
  }

  @Test
  public void setType() {
    DragAndDropContainer dnd = new DragAndDropContainer( DragAndDropContainer.TYPE_BASE_JOB_ENTRY, "Step Name"  );
    dnd.setType( DragAndDropContainer.TYPE_BASE_STEP_TYPE );

    assertEquals( DragAndDropContainer.TYPE_BASE_STEP_TYPE, dnd.getType() );
  }

  @Test
  public void getTypeFromCode() {
    assertEquals( DragAndDropContainer.TYPE_BASE_STEP_TYPE, DragAndDropContainer.getType( "BaseStep" ) );
  }

  @Test
  public void getXML() {
    DragAndDropContainer dnd = new DragAndDropContainer( DragAndDropContainer.TYPE_BASE_STEP_TYPE, "Step Name"  );

    String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + Const.CR
               + "<DragAndDrop>" + Const.CR
               + "  <DragType>BaseStep</DragType>" + Const.CR
               + "  <Data>U3RlcCBOYW1l</Data>" + Const.CR
               + "</DragAndDrop>" + Const.CR;

    assertEquals( xml, dnd.getXML() );
  }

  @Test
  public void getXMLWithId() {
    DragAndDropContainer dnd = new DragAndDropContainer( DragAndDropContainer.TYPE_BASE_STEP_TYPE, "Step Name", "StepID"  );

    String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + Const.CR
                 + "<DragAndDrop>" + Const.CR
                 + "  <ID>StepID</ID>" + Const.CR
                 + "  <DragType>BaseStep</DragType>" + Const.CR
                 + "  <Data>U3RlcCBOYW1l</Data>" + Const.CR
                 + "</DragAndDrop>" + Const.CR;

    assertEquals( xml, dnd.getXML() );
  }

  @Test
  public void newFromXML() throws KettleException {
    String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + Const.CR
               + "<DragAndDrop>" + Const.CR
               + "  <DragType>BaseStep</DragType>" + Const.CR
               + "  <Data>U3RlcCBOYW1l</Data>" + Const.CR
               + "</DragAndDrop>" + Const.CR;

    DragAndDropContainer dnd = new DragAndDropContainer( xml );

    assertNotNull( dnd );
    assertNull( dnd.getId() );
    assertEquals( DragAndDropContainer.TYPE_BASE_STEP_TYPE, dnd.getType() );
    assertEquals( "BaseStep", dnd.getTypeCode() );
    assertEquals( "Step Name", dnd.getData() );
  }

  @Test
  public void newFromXMLWithId() throws KettleException {
    String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + Const.CR
               + "<DragAndDrop>" + Const.CR
               + "  <ID>StepID</ID>" + Const.CR
               + "  <DragType>BaseStep</DragType>" + Const.CR
               + "  <Data>U3RlcCBOYW1l</Data>" + Const.CR
               + "</DragAndDrop>" + Const.CR;

    DragAndDropContainer dnd = new DragAndDropContainer( xml );

    assertNotNull( dnd );
    assertEquals( "StepID", dnd.getId() );
    assertEquals( DragAndDropContainer.TYPE_BASE_STEP_TYPE, dnd.getType() );
    assertEquals( "BaseStep", dnd.getTypeCode() );
    assertEquals( "Step Name", dnd.getData() );
  }
}
