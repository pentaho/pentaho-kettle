package org.pentaho.di.ui.trans.steps.simplemapping;

import org.junit.Test;
import org.pentaho.di.core.ObjectLocationSpecificationMethod;
import org.pentaho.di.core.exception.KettleException;

import static org.mockito.Mockito.*;

public class SimpleMappingDialogTest {

  @Test( expected = KettleException.class )
  public void loadTransformation() throws KettleException {

    SimpleMappingDialog simpleMappingDialog = mock( SimpleMappingDialog.class );

    doCallRealMethod().when( simpleMappingDialog ).loadTransformation();

    when( simpleMappingDialog.getReferenceObjectId() ).thenReturn( null );
    when( simpleMappingDialog.getSpecificationMethod() ).thenReturn(
      ObjectLocationSpecificationMethod.REPOSITORY_BY_REFERENCE );

    simpleMappingDialog.loadTransformation();

  }

}
