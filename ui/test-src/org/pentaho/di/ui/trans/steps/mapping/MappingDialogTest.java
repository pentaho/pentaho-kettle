package org.pentaho.di.ui.trans.steps.mapping;

import org.junit.Test;
import org.pentaho.di.core.ObjectLocationSpecificationMethod;
import org.pentaho.di.core.exception.KettleException;

import static org.mockito.Mockito.*;

public class MappingDialogTest {

  @Test( expected = KettleException.class )
  public void loadTransformation() throws KettleException {

    MappingDialog mappingDialog = mock( MappingDialog.class );

    doCallRealMethod().when( mappingDialog ).loadTransformation();

    when( mappingDialog.getReferenceObjectId() ).thenReturn( null );
    when( mappingDialog.getSpecificationMethod() ).thenReturn(
      ObjectLocationSpecificationMethod.REPOSITORY_BY_REFERENCE );

    mappingDialog.loadTransformation();

  }
}
