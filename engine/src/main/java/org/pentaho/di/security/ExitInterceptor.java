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

package org.pentaho.di.security;

import java.util.concurrent.atomic.AtomicBoolean;

public class ExitInterceptor {
    private static final AtomicBoolean interceptEnabled = new AtomicBoolean(false);

    public static void enableIntercept() {
        interceptEnabled.set( true );
    }

    public static void disableIntercept() {
        interceptEnabled.set( false );
    }

    public static void exit( int status ) {
        if ( interceptEnabled.get() ) {
            throw new SecurityException( "System exit not allowed" );
        }
        System.exit( status );
    }
}
