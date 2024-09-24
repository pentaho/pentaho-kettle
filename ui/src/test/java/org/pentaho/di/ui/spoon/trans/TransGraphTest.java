/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2017 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.ui.spoon.trans;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;

import org.eclipse.swt.events.MouseEvent;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.gui.Point;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepErrorMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.ui.xul.components.XulMenuitem;
import org.pentaho.ui.xul.containers.XulMenu;
import org.pentaho.ui.xul.dom.Document;

public class TransGraphTest {
  private static final boolean TRUE_RESULT = true;

  @Test
  public void testMouseUpHopGetsSelected() {
    MouseEvent event = mock( MouseEvent.class );
    int x = 0, y = 0;

    TransGraph transGraph = mock( TransGraph.class );
    StepMeta stepMeta = mock( StepMeta.class );
    StepErrorMeta errorMeta = new StepErrorMeta( null, null );
    TransHopMeta selectedHop = new TransHopMeta();
    selectedHop.setErrorHop( true );
    selectedHop.setEnabled( TRUE_RESULT );
    selectedHop.setFromStep( stepMeta );

    when( stepMeta.getStepErrorMeta() ).thenReturn( errorMeta );
    when( transGraph.findHop( x, y ) ).thenReturn( selectedHop );
    when( transGraph.screen2real( any( Integer.class ), any( Integer.class ) ) ).thenReturn( new Point( x, y ) );

    doCallRealMethod().when( transGraph ).mouseUp( event );
    transGraph.mouseUp( event );

    Assert.assertTrue( errorMeta.isEnabled() );
  }

  @Test
  public void testEnableHopGetsSelected() {
    TransGraph transGraph = mock( TransGraph.class );
    doCallRealMethod().when( transGraph ).setTransMeta( any( TransMeta.class ) );
    doCallRealMethod().when( transGraph ).setSpoon( any( Spoon.class ) );
    transGraph.setTransMeta( new TransMeta() );
    transGraph.setSpoon( mock( Spoon.class ) );
    StepMeta stepMeta = mock( StepMeta.class );
    StepErrorMeta errorMeta = new StepErrorMeta( null, null );
    TransHopMeta selectedHop = new TransHopMeta();
    selectedHop.setErrorHop( true );
    selectedHop.setEnabled( false );
    selectedHop.setFromStep( stepMeta );

    when( stepMeta.getStepErrorMeta() ).thenReturn( errorMeta );
    StepMeta toStep = new StepMeta();
    toStep.setName( "toStep" );
    selectedHop.setToStep( toStep );
    when( transGraph.getCurrentHop() ).thenReturn( selectedHop );

    doCallRealMethod().when( transGraph ).enableHop();
    transGraph.enableHop();

    Assert.assertTrue( errorMeta.isEnabled() );
  }

  @SuppressWarnings( "unchecked" )
  @Test
  public void testInitializeXulMenu() throws KettleException {
    StepMeta stepMeta = mock( StepMeta.class );
    TransGraph transGraph = mock( TransGraph.class );
    TransMeta transMeta = mock( TransMeta.class );
    Document document = mock( Document.class );
    XulMenuitem xulItem = mock( XulMenuitem.class );
    XulMenu xulMenu = mock( XulMenu.class );
    StepErrorMeta stepErrorMeta = mock( StepErrorMeta.class );
    Spoon spoon = mock( Spoon.class );
    List<StepMeta> selection = Arrays.asList( new StepMeta(), stepMeta, new StepMeta() );

    doCallRealMethod().when( transGraph ).setTransMeta( any( TransMeta.class ) );
    doCallRealMethod().when( transGraph ).setSpoon( any( Spoon.class ) );
    transGraph.setTransMeta( transMeta );
    transGraph.setSpoon( spoon );

    when( stepMeta.getStepErrorMeta() ).thenReturn( stepErrorMeta );
    when( stepMeta.isDrawn() ).thenReturn( true );
    when( document.getElementById( any( String.class ) ) ).thenReturn( xulItem );
    when( document.getElementById( TransGraph.TRANS_GRAPH_ENTRY_AGAIN ) ).thenReturn( xulMenu );
    when( document.getElementById( TransGraph.TRANS_GRAPH_ENTRY_SNIFF ) ).thenReturn( xulMenu );

    doCallRealMethod().when( transGraph ).initializeXulMenu( any( Document.class ),
      any( List.class ), any( StepMeta.class ) );

    transGraph.initializeXulMenu( document, selection, stepMeta );
    verify( transMeta ).isAnySelectedStepUsedInTransHops();
  }
}
