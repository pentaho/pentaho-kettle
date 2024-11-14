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


package org.pentaho.di.core.util.serialization;

import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

import java.util.List;

import static org.pentaho.di.core.util.serialization.MetaXmlSerializer.deserialize;
import static org.pentaho.di.core.util.serialization.MetaXmlSerializer.serialize;
import static org.pentaho.di.core.util.serialization.StepMetaProps.from;

/**
 * Handles serialization of meta by implementing getXML/loadXML, readRep/saveRep.
 * <p>
 * Uses {@link MetaXmlSerializer} amd {@link RepoSerializer} for generically
 * handling child classes meta.
 */
public abstract class BaseSerializingMeta extends BaseStepMeta implements StepMetaInterface {

  @Override public String getXML() {
    return serialize( from( this ) );
  }

  @Override public void loadXML(
    Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    deserialize( stepnode ).to( this );
  }

  @Override public void readRep( Repository rep, IMetaStore metaStore, ObjectId
    id_step, List<DatabaseMeta> databases )
    throws KettleException {
    RepoSerializer.builder()
      .stepMeta( this )
      .repo( rep )
      .stepId( id_step )
      .deserialize();
  }

  @Override public void saveRep( Repository rep, IMetaStore metaStore, ObjectId transId, ObjectId stepId )
    throws KettleException {
    RepoSerializer.builder()
      .stepMeta( this )
      .repo( rep )
      .stepId( stepId )
      .transId( transId )
      .serialize();
  }

  /**
   * Creates a copy of this stepMeta with variables globally substituted.
   */
  public StepMetaInterface withVariables( VariableSpace variables ) {
    try {
      return StepMetaProps
        .from( this )
        .withVariables( variables )
        .to( getNewMeta() );
    } catch ( KettleException e ) {
      throw new RuntimeException( e );
    }
  }

  private StepMetaInterface getNewMeta() throws KettleException {
    PluginRegistry pluginRegistry = PluginRegistry.getInstance();
    String id = pluginRegistry.getPluginId( StepPluginType.class, this );
    PluginInterface plugin = pluginRegistry.getPlugin( StepPluginType.class, id );
    return (StepMetaInterface) pluginRegistry.loadClass( plugin );
  }
}
