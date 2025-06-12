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

package org.pentaho.di.core.bowl;

import org.pentaho.di.connections.ConnectionManager;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.metastore.MetaStoreConst;
import org.pentaho.di.shared.SharedObjectsIO;
import org.pentaho.di.shared.VfsSharedObjectsIO;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.api.IMetaStore;

import com.google.common.annotations.VisibleForTesting;
import java.util.function.Supplier;

/**
 * This Bowl will access the default local configuration for Kettle. It does not include configuration from any other
 * contexts.
 * <p/>
 * This Bowl is roughly backwards-compatible with pre-Bowl behavior. It will access metastore configuration from a
 * connected Repository if there is one, but otherwise uses all local config.
 * <p/> If available, most code that needs a Bowl should get Bowls for Management from Spoon, and for execution from
 * TransMeta or JobMeta.
 *
 */
public class DefaultBowl extends BaseBowl {
  private static final Class<?> PKG = Const.class; // for i18n purposes, needed by Translator2!!

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

  @Override
  public String getLevelDisplayName() {
    return BaseMessages.getString( PKG, "Default.Level.Name" );
  }

}
