package org.pentaho.di.ui.spoon.partition;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.partition.PartitionSchema;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepPartitioningMeta;

import java.util.List;

/**
 * @author Evgeniy_Lyakhov@epam.com
 */
public class PartitionSettings {

  private StepMeta stepMeta;
  private TransMeta transMeta;
  private String[] options;
  private String[] codes;
  private String[] schemaNames;
  private StepMeta before;
  private StepMeta after;

  public PartitionSettings( int exactSize, TransMeta transMeta, StepMeta stepMeta ) {
    this.transMeta = transMeta;
    this.stepMeta = stepMeta;
    this.schemaNames = this.transMeta.getPartitionSchemasNames();
    this.options = new String[ exactSize ];
    this.codes = new String[ exactSize ];
    this.before = (StepMeta) stepMeta.clone();
    System.arraycopy(
      StepPartitioningMeta.methodDescriptions, 0, options, 0, StepPartitioningMeta.methodDescriptions.length );
    System.arraycopy( StepPartitioningMeta.methodCodes, 0, codes, 0, StepPartitioningMeta.methodCodes.length );
  }

  public void fillOptionsAndCodesByPlugins( List<PluginInterface> plugins ) {
    int pluginIndex = 0;
    for ( PluginInterface plugin : plugins ) {
      options[ StepPartitioningMeta.methodDescriptions.length + pluginIndex ] = plugin.getDescription();
      codes[ StepPartitioningMeta.methodCodes.length + pluginIndex ] = plugin.getIds()[ 0 ];
      pluginIndex++;
    }
  }

  public int getDefaultSelectedMethodIndex() {
    for ( int i = 0; i < codes.length; i++ ) {
      if ( codes[ i ].equals( stepMeta.getStepPartitioningMeta().getMethod() ) ) {
        return i;
      }
    }
    return 0;
  }

  public int getDefaultSelectedSchemaIndex() {
    int defaultSelectedSchemaIndex = 0;
    if ( stepMeta.getStepPartitioningMeta().getPartitionSchema() != null && schemaNames.length > 0 ) {
      defaultSelectedSchemaIndex =
        Const.indexOfString( stepMeta.getStepPartitioningMeta().getPartitionSchema().getName(), schemaNames );
    }
    return defaultSelectedSchemaIndex != -1 ? defaultSelectedSchemaIndex : 0;
  }

  public String getMethodByMethodDescription( String methodDescription ) {
    String method = StepPartitioningMeta.methodCodes[ StepPartitioningMeta.PARTITIONING_METHOD_NONE ];
    for ( int i = 0; i < options.length; i++ ) {
      if ( options[ i ].equals( methodDescription ) ) {
        method = codes[ i ];
      }
    }
    return method;
  }

  public String[] getOptions() {
    return options;
  }

  public void setOptions( String[] options ) {
    this.options = options;
  }

  public String[] getCodes() {
    return codes;
  }

  public void setCodes( String[] codes ) {
    this.codes = codes;
  }

  public String[] getSchemaNames() {
    return schemaNames;
  }

  public void setSchemaNames( String[] schemaNames ) {
    this.schemaNames = schemaNames;
  }

  public StepMeta getStepMeta() {
    return stepMeta;
  }

  public void updateMethodType( int methodType ) {
    stepMeta.getStepPartitioningMeta().setMethodType( methodType );
  }

  public void updateMethod( String method ) throws KettlePluginException {
    stepMeta.getStepPartitioningMeta().setMethod( method );
  }

  public void updateSchema( PartitionSchema schema ) {
    if ( schema != null && schema.getName() != null ) {
      stepMeta.getStepPartitioningMeta().setPartitionSchema( schema );
    }
  }

  public void rollback( StepMeta before ) throws KettlePluginException {
    updateMethod( before.getStepPartitioningMeta().getMethod() );
    updateMethodType( before.getStepPartitioningMeta().getMethodType() );
    updateSchema( before.getStepPartitioningMeta().getPartitionSchema() );
  }

  public StepMeta getBefore() {
    return before;
  }

  public StepMeta getAfter() {
    return (StepMeta) stepMeta.clone();
  }

  public TransMeta getTransMeta() {
    return transMeta;
  }
}
