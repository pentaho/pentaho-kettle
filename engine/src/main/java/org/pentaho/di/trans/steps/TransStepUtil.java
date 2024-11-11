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


package org.pentaho.di.trans.steps;

import javax.servlet.http.HttpServletResponse;

import org.pentaho.di.trans.Trans;

public class TransStepUtil {

  public static void initServletConfig( Trans srcTrans, Trans distTrans ) {
    // Also pass servlet information (if any)
    distTrans.setServletPrintWriter( srcTrans.getServletPrintWriter() );

    HttpServletResponse response = srcTrans.getServletResponse();
    if ( response != null ) {
      distTrans.setServletReponse( response );
    }

    distTrans.setServletRequest( srcTrans.getServletRequest() );
  }

}
