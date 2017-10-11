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

package org.pentaho.di.repository.kdr.delegates;

import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.repository.kdr.KettleDatabaseRepository;

public class KettleDatabaseRepositoryBaseDelegate {

  protected KettleDatabaseRepository repository;
  protected LogChannelInterface log;

  public KettleDatabaseRepositoryBaseDelegate( KettleDatabaseRepository repository ) {
    this.repository = repository;
    this.log = repository.getLog();
  }

  public String quote( String identifier ) {
    return repository.connectionDelegate.getDatabaseMeta().quoteField( identifier );
  }

  public String quoteTable( String table ) {
    return repository.connectionDelegate.getDatabaseMeta().getQuotedSchemaTableCombination( null, table );
  }
}
