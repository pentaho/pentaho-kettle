/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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
