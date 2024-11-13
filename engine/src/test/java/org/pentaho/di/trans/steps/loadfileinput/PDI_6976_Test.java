/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.trans.steps.loadfileinput;

import junit.framework.TestCase;
import org.junit.Test;
import org.mockito.stubbing.Answer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.metastore.api.IMetaStore;

import javax.tools.FileObject;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Tests for LoadFileInputMeta class
 *
 * @author Pavel Sakun
 * @see LoadFileInputMeta
 */
public class PDI_6976_Test {
  @Test
  public void testVerifyNoPreviousStep() {
    LoadFileInputMeta spy = spy( new LoadFileInputMeta() );

    FileInputList fileInputList = mock( FileInputList.class );
    List<FileObject> files = when( mock( List.class ).size() ).thenReturn( 1 ).getMock();
    doReturn( files ).when( fileInputList ).getFiles();
    doReturn( fileInputList ).when( spy ).getFiles( any( VariableSpace.class ) );

    @SuppressWarnings( "unchecked" )
    List<CheckResultInterface> validationResults = mock( List.class );

    // Check we do not get validation errors
    doAnswer( (Answer<Object>) invocation -> {
      if ( ( (CheckResultInterface) invocation.getArguments()[ 0 ] ).getType()
        != CheckResultInterface.TYPE_RESULT_OK ) {
        TestCase.fail( "We've got validation error" );
      }

      return null;
    } ).when( validationResults ).add( any( CheckResultInterface.class ) );

    spy.check( validationResults, mock( TransMeta.class ), mock( StepMeta.class ), mock( RowMetaInterface.class ),
      new String[] {}, new String[] { "File content", "File size" }, mock( RowMetaInterface.class ),
      mock( VariableSpace.class ), mock( Repository.class ), mock( IMetaStore.class ) );
  }
}
