/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2024 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.shared;

import org.pentaho.di.core.database.DatabaseMeta;

/**
 * This is the management interface used by the UI to perform CRUD operation. The implementors of this interface will
 * be scoped based on the bowl and can be retrieved using bowl's getManager()
 *
 */
public class ChangeTrackingDatabaseManager extends ChangeTrackingSharedObjectManager<DatabaseMeta> implements DatabaseManagementInterface {

  public ChangeTrackingDatabaseManager( SharedObjectsManagementInterface<DatabaseMeta> parent ) {
    super( parent );
  }
}
