/*!
* Copyright 2010 - 2015 Pentaho Corporation.  All rights reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
*/

package org.pentaho.di.repository.pur;

import java.util.Date;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.repository.RepositoryElementInterface;
import org.pentaho.platform.api.repository2.unified.data.node.DataNode;

public abstract class AbstractDelegate {

  protected static final String PROP_NAME = "NAME"; //$NON-NLS-1$

  protected static final String PROP_DESCRIPTION = "DESCRIPTION"; //$NON-NLS-1$

  protected LogChannelInterface log;
  
  public AbstractDelegate() {
    log = LogChannel.GENERAL;
  }

  protected String sanitizeNodeName(final String name) {
    StringBuffer result = new StringBuffer(30);

    for (char c : name.toCharArray()) {
      switch (c) {
        case ':':
        case '/':
          result.append('-');
          break;
        case '{':
        case '}':
        case '[':
        case ']':
        case ')':
        case '(':
        case '\\':
          result.append('_');
          break;
        default:
          if (Character.isLetterOrDigit(c)) {
            result.append(c);
          }
          break;
      }
    }

    return result.toString();
  }
  
  protected String getString(DataNode node, String name) {
    if (node.hasProperty(name)) {
      return node.getProperty(name).getString();
    } else {
      return null;
    }
  }
  
  protected long getLong(DataNode node, String name) {
    if (node.hasProperty(name)) {
      return node.getProperty(name).getLong();
    } else {
      return 0L;
    }
  }
  
  protected Date getDate(DataNode node, String name) {
    if (node.hasProperty(name)) {
      return node.getProperty(name).getDate();
    } else {
      return null;
    }
  }

  protected boolean getBoolean(DataNode node, String name, boolean defaultValue) {
    if (node.hasProperty(name)) {
      return node.getProperty(name).getBoolean();
    } else {
      return defaultValue;
    }
  }
  
  public abstract DataNode elementToDataNode(RepositoryElementInterface element) throws KettleException;
  
  public boolean equals( RepositoryElementInterface first, RepositoryElementInterface second ) {
    try {
      return elementToDataNode( first ).equals( elementToDataNode( second ) );
    } catch ( KettleException e ) {
      return false;
    }
  }
}
