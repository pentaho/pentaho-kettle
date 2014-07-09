/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2014 by Pentaho : http://www.pentaho.com
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
package org.pentaho.di.ui.trans.steps.transexecutor;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.repository.AbstractRepository;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.kdr.KettleDatabaseRepositoryBase;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.steps.transexecutor.TransExecutorMeta;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.repository.dialog.SelectObjectDialog;

/**
 * @author Tatsiana_Kasiankova
 * 
 */
public class TransExecutorDialogTest {

  private Shell shellMock = mock( Shell.class );
  private TransMeta trMetaMock = mock( TransMeta.class );
  private TransExecutorMeta trExecutorMetaMock = mock( TransExecutorMeta.class );
  private AbstractRepository dbRepMock = mock( KettleDatabaseRepositoryBase.class );
  private TransExecutorDialog transExecutorDg;
  private SelectObjectDialog sod;
  SelectObjectDialog sodMock = mock( SelectObjectDialog.class );

  @BeforeClass
  public static void beforeClass() throws Exception {
    Display display = Display.getDefault();
    PropsUI.init( display, 1 );
  }

  @Before
  public void setUp() throws Exception {
    transExecutorDg = new TransExecutorDialog( shellMock, trExecutorMetaMock, trMetaMock, "TRANS_EXECUTOR" );
    transExecutorDg.setRepository( dbRepMock );
    sod = new SelectObjectDialog( transExecutorDg.getParent(), dbRepMock, true, false );

    RepositoryDirectoryInterface rpDirIMock = mock( RepositoryDirectory.class );
    doReturn( rpDirIMock ).when( dbRepMock ).loadRepositoryDirectoryTree();

  }

  @Test
  public void shouldBeShowTransformationsAsTrueAndShowJobsAsFalse_WhenSelectedTransByReference() throws Exception {

    TransExecutorDialog transExecutorDgSpy = spy( transExecutorDg );

    // The spy to avoid manipulation with dialog window
    SelectObjectDialog sodSpy = spy( sod );
    doReturn( "DONE" ).when( sodSpy ).open();
    doReturn( sodSpy ).when( transExecutorDgSpy ).getSelectObjectDialog( any( Shell.class ),
        any( AbstractRepository.class ), anyBoolean(), anyBoolean() );

    transExecutorDgSpy.selectTransByReference();

    verify( transExecutorDgSpy ).getSelectObjectDialog( any( Shell.class ), any( AbstractRepository.class ),
        eq( true ), eq( false ) );
  }

  @Test
  public void shouldBeShowTransformationsAsTrueAndShowJobsAsFalse_WhenSelectedRepositoryTrans() throws Exception {

    TransExecutorDialog transExecutorDgSpy = spy( transExecutorDg );

    // The spy to avoid manipulation with dialog window
    SelectObjectDialog sodSpy = spy( sod );
    doReturn( "DONE" ).when( sodSpy ).open();
    doReturn( sodSpy ).when( transExecutorDgSpy ).getSelectObjectDialog( any( Shell.class ),
        any( AbstractRepository.class ), anyBoolean(), anyBoolean() );

    transExecutorDgSpy.selectRepositoryTrans();

    verify( transExecutorDgSpy ).getSelectObjectDialog( any( Shell.class ), any( AbstractRepository.class ),
        eq( true ), eq( false ) );
  }

}
