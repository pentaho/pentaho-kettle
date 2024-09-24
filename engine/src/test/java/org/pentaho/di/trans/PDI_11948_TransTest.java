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

package org.pentaho.di.trans;

import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;

import org.junit.Test;

public class PDI_11948_TransTest {

  @Test( expected = IllegalArgumentException.class )
  public void setServletReponseTest() {
    Trans transMock = mock( Trans.class );

    doCallRealMethod().when( transMock ).setServletReponse( null );
    transMock.setServletReponse( null );
  }

}
