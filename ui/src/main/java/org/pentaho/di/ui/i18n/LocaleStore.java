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

package org.pentaho.di.ui.i18n;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;

/**
 * This class stores all the messages for a locale for all the used packages...
 *
 * @author matt
 *
 */
public class LocaleStore {

  /** The locale to handle */
  private String locale;

  /**
   * source folder - SourceStore
   */
  private Map<String, SourceStore> sourceMap;

  private String mainLocale;

  private Map<String, Map<String, List<KeyOccurrence>>> sourcePackageOccurrences;

  private LogChannelInterface log;

  /**
   * Create a new LocaleStore
   *
   * @param locale
   *          The locale to handle
   * @param messagesPackages
   *          the packages to handle per source folder
   * @param packageOccurrences
   */
  public LocaleStore( LogChannelInterface log, String locale, String mainLocale,
    Map<String, Map<String, List<KeyOccurrence>>> sourcePackageOccurrences ) {
    this.log = log;
    this.locale = locale;
    this.mainLocale = mainLocale;
    this.sourceMap = new HashMap<String, SourceStore>();
    this.sourcePackageOccurrences = sourcePackageOccurrences;
  }

  /**
   * Read all the messages stores from the specified locale from all the specified packages
   *
   * @param directories
   *
   * @param directories
   *          The source directories to reference the packages from
   * @throws KettleException
   */
  public void read( List<String> directories ) throws KettleException {
    for ( String sourceFolder : sourcePackageOccurrences.keySet() ) {

      SourceStore sourceStore = new SourceStore( log, locale, sourceFolder, sourcePackageOccurrences );
      try {
        sourceStore.read( directories );
        sourceMap.put( sourceFolder, sourceStore );
      } catch ( Exception e ) {
        e.printStackTrace();
      }
    }
  }

  /**
   * @return the locale
   */
  public String getLocale() {
    return locale;
  }

  /**
   * @param locale
   *          the locale to set
   */
  public void setLocale( String locale ) {
    this.locale = locale;
  }

  /**
   * @return the mainLocale
   */
  public String getMainLocale() {
    return mainLocale;
  }

  /**
   * @param mainLocale
   *          the mainLocale to set
   */
  public void setMainLocale( String mainLocale ) {
    this.mainLocale = mainLocale;
  }

  public Map<String, SourceStore> getSourceMap() {
    return sourceMap;
  }

}
