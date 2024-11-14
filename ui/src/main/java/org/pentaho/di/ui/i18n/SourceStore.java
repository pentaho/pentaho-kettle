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


package org.pentaho.di.ui.i18n;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;

/**
 * Read the messages files for the source folders of the specified locale.
 *
 * @author matt
 *
 */
public class SourceStore {

  private Map<String, Map<String, List<KeyOccurrence>>> sourcePackageOccurrences;

  /** message package - MessageStore */
  private Map<String, MessagesStore> messagesMap;

  private String locale;
  protected LogChannelInterface log;

  private String sourceFolder;

  public SourceStore( LogChannelInterface log, String locale, String sourceFolder,
    Map<String, Map<String, List<KeyOccurrence>>> sourcePackageOccurrences ) {
    this.log = log;
    this.locale = locale;
    this.sourceFolder = sourceFolder;
    this.sourcePackageOccurrences = sourcePackageOccurrences;

    messagesMap = new HashMap<String, MessagesStore>();
  }

  public void read( List<String> directories ) throws KettleException {
    Map<String, List<KeyOccurrence>> po = sourcePackageOccurrences.get( sourceFolder );
    for ( String messagesPackage : po.keySet() ) {
      MessagesStore messagesStore =
        new MessagesStore( locale, sourceFolder, messagesPackage, sourcePackageOccurrences );
      try {
        messagesStore.read( directories );
        messagesMap.put( messagesPackage, messagesStore );
      } catch ( Exception e ) {
        // e.printStackTrace();
      }
    }
  }

  public Map<String, MessagesStore> getMessagesMap() {
    return messagesMap;
  }
}
