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

package org.pentaho.di.core.logging.log4j;

import org.apache.logging.log4j.core.LogEvent;


public interface Log4jLayout {
    public String format( LogEvent event );

    public boolean ignoresThrowable();

    public void activateOptions();

    public boolean isTimeAdded();

    public void setTimeAdded( boolean addTime );
}

