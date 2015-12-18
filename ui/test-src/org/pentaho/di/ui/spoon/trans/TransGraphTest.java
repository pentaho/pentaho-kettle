package org.pentaho.di.ui.spoon.trans;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.swt.events.MouseEvent;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.gui.Point;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.step.StepErrorMeta;
import org.pentaho.di.trans.step.StepMeta;

public class TransGraphTest {
    private static final boolean TRUE_RESULT = true;
    
    @Test
    public void testMouseUpHopGetsSelected() {
        try {
            MouseEvent event = mock( MouseEvent.class );
            int x = 0, y = 0;
            
            TransGraph transGraph = mock( TransGraph.class );
            TransHopMeta selectedHop = mock( TransHopMeta.class );
            StepMeta stepMeta = mock( StepMeta.class );
            StepErrorMeta errorMeta = mock( StepErrorMeta.class );
            
            when( selectedHop.isEnabled() ).thenReturn( TRUE_RESULT );
            when( stepMeta.getStepErrorMeta() ).thenReturn( errorMeta );
            when( selectedHop.getFromStep() ).thenReturn( stepMeta );
            when( transGraph.findHop( x, y ) ).thenReturn( selectedHop );
            when( transGraph.screen2real( any( Integer.class ), any( Integer.class ) ) ).thenReturn( new Point( x, y ) );
            
            Mockito.doCallRealMethod().when( transGraph ).mouseUp( event );
            transGraph.mouseUp( event );
            
            verify( errorMeta ).setEnabled( TRUE_RESULT );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
