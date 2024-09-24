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

package org.pentaho.di.ui.trans.steps.userdefinedjavaclass;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.ui.trans.steps.userdefinedjavaclass.UserDefinedJavaClassCodeSnippits.Snippit;

public class UserDefinedJavaClassCodeSnippitsTest {

  @Test
  public void testSnippitMainUseRightRowsize() throws Exception {
    String code = UserDefinedJavaClassCodeSnippits.getSnippitsHelper().getCode( "Main" );
    Assert.assertTrue( "Wrong row size variable is used", code.contains( "r = createOutputRow(r, data.outputRowMeta.size());" ) );
  }

  @Test
  public void testSnippitsWellDefined() throws KettleXMLException {
    List<String> snippitNames = new ArrayList<String>();
    for ( Snippit snippit : UserDefinedJavaClassCodeSnippits.getSnippitsHelper().getSnippits() ) {
      Assert.assertNotNull( snippit.category );
      Assert.assertFalse( Utils.isEmpty( snippit.name ) );
      Assert.assertFalse( Utils.isEmpty( snippit.code ) );
      Assert.assertFalse( Utils.isEmpty( snippit.sample ) );
      Assert.assertFalse( snippitNames.contains( snippit.name ) );
      snippitNames.add( snippit.name );
    }
  }
}
