package org.pentaho.di.ui.spoon.trans;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import junit.framework.Assert;

import org.eclipse.swt.events.MouseEvent;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.gui.Point;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepErrorMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.ui.spoon.Spoon;

public class TransGraphTest {
    private static final boolean TRUE_RESULT = true;
    
    @Test
    public void testMouseUpHopGetsSelected() {
        try {
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
            
            Mockito.doCallRealMethod().when( transGraph ).mouseUp( event );
            transGraph.mouseUp( event );
            
            Assert.assertTrue( errorMeta.isEnabled() );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void testEnableHopGetsSelected() {
        try {
            TransGraph transGraph = mock( TransGraph.class );
            Mockito.doCallRealMethod().when( transGraph ).setTransMeta( any( TransMeta.class ) );
            Mockito.doCallRealMethod().when( transGraph ).setSpoon( any( Spoon.class ) );
            transGraph.setTransMeta( new TransMeta() );
            transGraph.setSpoon( mock( Spoon.class ) );
            StepMeta stepMeta = mock( StepMeta.class );
            StepErrorMeta errorMeta = new StepErrorMeta( null, null );
            TransHopMeta selectedHop = new TransHopMeta();
            selectedHop.setErrorHop( true );
            selectedHop.setEnabled( false );
            selectedHop.setFromStep( stepMeta );
            
            when( stepMeta.getStepErrorMeta() ).thenReturn( errorMeta );
            selectedHop.setToStep( new StepMeta() );
            when( transGraph.getCurrentHop() ).thenReturn( selectedHop );

            Mockito.doCallRealMethod().when( transGraph ).enableHop();
            transGraph.enableHop();
            
            Assert.assertTrue( errorMeta.isEnabled() );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
