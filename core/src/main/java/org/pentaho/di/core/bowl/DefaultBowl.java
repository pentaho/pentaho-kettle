/*!
 * Copyright 2024 Hitachi Vantara.  All rights reserved.
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
package org.pentaho.di.core.bowl;

import org.pentaho.di.connections.ConnectionManager;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.metastore.MetaStoreConst;
import org.pentaho.di.shared.SharedObjectsIO;
import org.pentaho.di.shared.VfsSharedObjectsIO;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.api.IMetaStore;

import com.google.common.annotations.VisibleForTesting;
import java.util.function.Supplier;

/**
 * The default/global Bowl. A singleton for standard behavior when there is no custom Bowl.
 *
 */
public class DefaultBowl extends BaseBowl {
  private static final DefaultBowl INSTANCE = new DefaultBowl();

  // for testing
  private Supplier<IMetaStore> metastoreSupplier = MetaStoreConst.getDefaultMetastoreSupplier();
  private boolean customSupplier = false;

  private SharedObjectsIO sharedObjectsIO;

  private DefaultBowl() {
  }

  public static DefaultBowl getInstance() {
    return INSTANCE;
  }

  @Override
  public IMetaStore getMetastore() throws MetaStoreException {
    return metastoreSupplier.get();
  }

  @Override
  public <T> T getManager( Class<T> managerClass) throws KettleException {
    // need to override getManager so this instance of DefaultBowl shares the same ConnectionManager
    // instance with ConnectionManager.getInstance()
    if ( managerClass == ConnectionManager.class && !customSupplier ) {
      return managerClass.cast( ConnectionManager.getInstance() );
    } else {
      return super.getManager( managerClass );
    }
  }

  @Override
  public VariableSpace getADefaultVariableSpace() {
    VariableSpace space = new Variables();
    space.initializeVariablesFrom( null );
    return space;
  }

  /**
   * Set a specific metastore supplier for use by later calls to this class. Note that this will cause the
   * ConnectionManager from this class and from ConnectionManager.getInstance() to return different instances.
   */
  @VisibleForTesting
  public void setMetastoreSupplier( Supplier<IMetaStore> metastoreSupplier ) {
    this.metastoreSupplier = metastoreSupplier;
    this.customSupplier = true;
  }

  /**
   * Creates and return an instance of SharedObjectsIO using the default shared objects file location
   * @return SharedObjectsIO
   */
  @Override
  public SharedObjectsIO getSharedObjectsIO()  {
    if ( sharedObjectsIO == null ) {
      sharedObjectsIO = new VfsSharedObjectsIO();
    }
    return sharedObjectsIO;
  }

  public void setSharedObjectsIO( SharedObjectsIO sharedObjectsIO ) {
    this.sharedObjectsIO = sharedObjectsIO;
  }

}
