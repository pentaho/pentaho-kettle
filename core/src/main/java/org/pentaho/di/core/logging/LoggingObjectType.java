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


package org.pentaho.di.core.logging;

public enum LoggingObjectType {
  // Runtime...
  //
    TRANS, STEP, JOB, JOBENTRY, DATABASE,

    // Metadata...
    //
    TRANSMETA, STEPMETA, JOBMETA,

    // User Interface...
    //
    SPOON, STEPDIALOG, JOBENTRYDIALOG,

    // Web server + HttpServlet...
    //
    CARTE, SERVLET,

    // Repository
    //
    REPOSITORY,

    // General
    //
    GENERAL,
}
