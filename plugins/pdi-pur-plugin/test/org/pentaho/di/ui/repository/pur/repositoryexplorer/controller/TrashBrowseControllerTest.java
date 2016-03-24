/*!
 * Copyright 2010 - 2015 Pentaho Corporation.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.pentaho.di.ui.repository.pur.repositoryexplorer.controller;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIRepositoryDirectory;

/**
 * @author Tatsiana_Kasiankova
 * 
 */
public class TrashBrowseControllerTest {

  private TrashBrowseControllerSpy trBrController;
  private UIRepositoryDirectory repoDirMock;

  @Before
  public void setUp() throws Exception {
    repoDirMock = mock( UIRepositoryDirectory.class );
    trBrController = new TrashBrowseControllerSpy();
  }

  @Test
  public void testShouldRefreshRepoDir_IfKettleExceptionOnRepoDirDeletion() throws Exception {
    KettleException ke = new KettleException( "TEST MESSAGE" );
    doThrow( ke ).when( repoDirMock ).delete();
    try {
      trBrController.deleteFolder( repoDirMock );
      fail( "Expected appearance KettleException: " + ke.getMessage() );
    } catch ( KettleException e ) {
      assertTrue( ke.getMessage().equals( e.getMessage() ) );
    }
    verify( repoDirMock, times( 1 ) ).refresh();
  }

  public class TrashBrowseControllerSpy extends TrashBrowseController {

    private static final long serialVersionUID = 1L;

    TrashBrowseControllerSpy() {
      super();
      this.repoDir = repoDirMock;
    }
  }

}
