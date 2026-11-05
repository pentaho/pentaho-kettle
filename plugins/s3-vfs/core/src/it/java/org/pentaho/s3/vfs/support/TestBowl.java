package org.pentaho.s3.vfs.support;

import org.pentaho.di.core.bowl.BaseBowl;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.shared.MemorySharedObjectsIO;
import org.pentaho.di.shared.SharedObjectsIO;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.stores.memory.MemoryMetaStore;

/** Gives a test class its own ConnectionManager and metastore, isolated from the global ones. */
public class TestBowl extends BaseBowl {

  private final IMetaStore metastore = new MemoryMetaStore();
  private final SharedObjectsIO sharedObjectsIO = new MemorySharedObjectsIO();

  @Override
  public IMetaStore getMetastore() {
    return metastore;
  }

  @Override
  public SharedObjectsIO getSharedObjectsIO() {
    return sharedObjectsIO;
  }

  @Override
  public VariableSpace getADefaultVariableSpace() {
    return Variables.getADefaultVariableSpace();
  }

  @Override
  public String getLevelDisplayName() {
    return "test";
  }
}
