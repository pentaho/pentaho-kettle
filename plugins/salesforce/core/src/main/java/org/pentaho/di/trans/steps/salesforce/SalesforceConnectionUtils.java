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

package org.pentaho.di.trans.steps.salesforce;

import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.steps.salesforceinput.SalesforceInputMeta;

public class SalesforceConnectionUtils {

  public static final int MAX_UPDATED_OBJECTS_IDS = 2000;

  private static Class<?> PKG = SalesforceInputMeta.class; // for i18n purposes, needed by Translator2!!

  public static final String TARGET_DEFAULT_URL = "https://login.salesforce.com/services/Soap/u/47.0";

  public static final String DEFAULT_TIMEOUT = "60000";

  /**
   * The records filter description
   */
  public static final String[] recordsFilterDesc = {
    BaseMessages.getString( PKG, "SalesforceInputMeta.recordsFilter.All" ),
    BaseMessages.getString( PKG, "SalesforceInputMeta.recordsFilter.Updated" ),
    BaseMessages.getString( PKG, "SalesforceInputMeta.recordsFilter.Deleted" ) };

  /**
   * The records filter type codes
   */
  public static final String[] recordsFilterCode = { "all", "updated", "deleted" };

  public static final int RECORDS_FILTER_ALL = 0;

  public static final int RECORDS_FILTER_UPDATED = 1;

  public static final int RECORDS_FILTER_DELETED = 2;

  public static String getRecordsFilterDesc( int i ) {
    if ( i < 0 || i >= recordsFilterDesc.length ) {
      return recordsFilterDesc[0];
    }
    return recordsFilterDesc[i];
  }

  public static int getRecordsFilterByDesc( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < recordsFilterDesc.length; i++ ) {
      if ( recordsFilterDesc[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }
    // If this fails, try to match using the code.
    return getRecordsFilterByCode( tt );
  }

  public static int getRecordsFilterByCode( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < recordsFilterCode.length; i++ ) {
      if ( recordsFilterCode[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }
    return 0;
  }

  public static String getRecordsFilterCode( int i ) {
    if ( i < 0 || i >= recordsFilterCode.length ) {
      return recordsFilterCode[0];
    }
    return recordsFilterCode[i];
  }

}
