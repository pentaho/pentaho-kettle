/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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
package org.pentaho.di.trans;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Test;
import org.pentaho.di.core.gui.Point;
import org.pentaho.di.trans.step.StepMeta;

public class TransMetaTest {

  private final Point minimalCanvasPoint = new Point( 0, 0 );

  //for test goal should content coordinate more than NotePadMetaPoint
  private final Point stepPoint = new Point( 500, 500 );

  @Test
  public void testGetMinimum() {
    TransMeta transMeta = new TransMeta();

    //empty Trans return 0 coordinate point
    Point point = transMeta.getMinimum();
    assertEquals( minimalCanvasPoint.x, point.x );
    assertEquals( minimalCanvasPoint.y, point.y );

    //when Trans  content Step  than  trans should return minimal coordinate of step
    StepMeta stepMeta = mock( StepMeta.class );
    when( stepMeta.getLocation() ).thenReturn( stepPoint );
    transMeta.addStep( stepMeta );
    Point  actualStepPoint = transMeta.getMinimum();
    assertEquals( stepPoint.x - TransMeta.BORDER_INDENT, actualStepPoint.x );
    assertEquals( stepPoint.y - TransMeta.BORDER_INDENT, actualStepPoint.y );
  }

}
