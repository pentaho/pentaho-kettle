/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.di.ui.trans.steps.sftpput;

import org.apache.commons.lang.reflect.FieldUtils;
import org.eclipse.swt.widgets.Shell;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.Props;
import org.pentaho.di.job.entries.sftp.SFTPClient;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.steps.sftpput.SFTPPutMeta;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.trans.steps.sftpput.SFTPPutDialog;

import java.lang.reflect.Field;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Created by Yury_Ilyukevich on 7/1/2015.
 */
public class SFTPPutDialogTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  private static boolean changedPropsUi;

  @BeforeClass
  public static void hackPropsUi() throws Exception {
    Field props = getPropsField();
    if ( props == null ) {
      throw new IllegalStateException( "Cannot find 'props' field in " + Props.class.getName() );
    }

    Object value = FieldUtils.readStaticField( props, true );
    if ( value == null ) {
      PropsUI mock = mock( PropsUI.class );
      FieldUtils.writeStaticField( props, mock, true );
      changedPropsUi = true;
    } else {
      changedPropsUi = false;
    }
  }

  @AfterClass
  public static void restoreNullInPropsUi() throws Exception {
    if ( changedPropsUi ) {
      Field props = getPropsField();
      FieldUtils.writeStaticField( props, null, true );
    }
  }

  private static Field getPropsField() {
    return FieldUtils.getDeclaredField( Props.class, "props", true );
  }


  @Test
  public void connectToSFTP_SeveralTimes_AlwaysReturnTrue() throws Exception {
    SFTPClient sftp = mock( SFTPClient.class );

    SFTPPutDialog sod =
      new SFTPPutDialog( mock( Shell.class ), new SFTPPutMeta(), mock( TransMeta.class ), "SFTPPutDialogTest" );
    SFTPPutDialog sodSpy = spy( sod );
    doReturn( sftp ).when( sodSpy ).createSFTPClient();

    assertTrue( sodSpy.connectToSFTP( false, null ) );
    assertTrue( sodSpy.connectToSFTP( false, null ) );
  }

  @Test
  public void connectToSFTP_CreateNewConnection_AfterChange() throws Exception {
    SFTPClient sftp = mock( SFTPClient.class );
    SFTPPutMeta sodMeta = new SFTPPutMeta();

    SFTPPutDialog sod =
      new SFTPPutDialog( mock( Shell.class ), sodMeta, mock( TransMeta.class ), "SFTPPutDialogTest" );
    SFTPPutDialog sodSpy = spy( sod );

    doReturn( sftp ).when( sodSpy ).createSFTPClient();

    assertTrue( sodSpy.connectToSFTP( false, null ) );
    sodMeta.setChanged( true );
    assertTrue( sodSpy.connectToSFTP( false, null ) );

    verify( sodSpy, times( 2 ) ).createSFTPClient();
  }
}
