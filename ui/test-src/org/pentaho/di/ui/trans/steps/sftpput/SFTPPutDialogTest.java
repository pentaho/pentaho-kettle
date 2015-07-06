/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2015 by Pentaho : http://www.pentaho.com
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
package org.pentaho.di.ui.trans.steps.sftpput;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleJobException;
import org.pentaho.di.job.entries.sftp.SFTPClient;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.steps.sftpput.SFTPPutMeta;
import org.pentaho.di.ui.core.PropsUI;

import java.net.UnknownHostException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

/**
 * Created by Yury_Ilyukevich on 7/1/2015.
 */
public class SFTPPutDialogTest {

  private Shell shellMock = mock( Shell.class );
  private TransMeta trMetaMock = mock( TransMeta.class );
  private SFTPClient sftp = mock( SFTPClient.class );
  private SFTPPutDialog sod;


  @BeforeClass
  public static void beforeClass() throws Exception {
    Display display = Display.getDefault();
    PropsUI.init( display, 1 );
  }

  @Test
  public void connectToSFTPTest() throws KettleJobException, UnknownHostException {
    SFTPPutMeta in = new SFTPPutMeta();
    sod = new SFTPPutDialog( shellMock, in, trMetaMock, "Name" );
    SFTPPutDialog sodSpy = spy( sod );
    doReturn( sftp ).when( sodSpy ).createSFTPClient();

    assertTrue( sodSpy.connectToSFTP( false, null ) );
    assertTrue( sodSpy.connectToSFTP( false, null ) );
  }
}
