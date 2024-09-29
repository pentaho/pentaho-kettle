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


package org.pentaho.di.plugins.fileopensave.api.providers;

/**
 * Created by bmorrise on 2/13/19.
 */
public interface File extends Entity, Providerable {
    public static final String TRANSFORMATION = "transformation";
    public static final String JOB = "job";

    public  static final String TYPE = "file";
    public static final String KTR = ".ktr";
    public static final String KJB = ".kjb";
}
