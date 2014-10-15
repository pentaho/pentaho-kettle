package org.pentaho.di.ui.spoon.partition;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.plugins.PartitionerPluginType;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.partition.PartitionSchema;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepPartitioningMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author Evgeniy_Lyakhov@epam.com
 */
public class PartitionSettingsTest {
  public static final String PARTITION_METHOD = "ModPartitioner";
  public static final String PARTITION_METHOD_DESCRIPTION = "Remainder of division";
  private static final String PARTITION_METHOD_DESCRIPTION_WRONG = "Remainder of division unrecognized";
  public static final String PARTITION_SCHEMA_NAME = "State";
  private TransMeta transMeta;
  private StepMeta stepMeta;
  private PartitionSettings partitionSetings;
  private PluginRegistry plugReg;
  private List<PluginInterface> plugins;
  private int exactSize;


  @Before
  public void setUp() throws KettleException {
    PartitionSchema ps1 = createPartitionSchema( PARTITION_SCHEMA_NAME, Arrays.asList( "P1", "P2" ) );
    PartitionSchema ps2 = createPartitionSchema( "Test", Arrays.asList( "S1", "S2", "S3" ) );

    stepMeta = createStepMeta( "MemoryGroupBy" );
    stepMeta.setStepPartitioningMeta( createStepParititonMeta() );
    stepMeta.getStepPartitioningMeta()
      .setPartitionSchema( ps1 );

    transMeta = createTransMeta();
    transMeta.setPartitionSchemas( Arrays.asList( ps2, ps1 ) );

    KettleEnvironment.init();
    plugReg = PluginRegistry.getInstance();
    plugins = plugReg.getPlugins( PartitionerPluginType.class );

    exactSize = getExactSize( plugins );


  }

  @Test
  public void PartitionSettingsConstructor() {
    int actualMethodCodesLength = StepPartitioningMeta.methodCodes.length;
    int actualMethodDescriptionLength = StepPartitioningMeta.methodDescriptions.length;

    partitionSetings = new PartitionSettings( exactSize, transMeta, stepMeta );
    String[] codes = partitionSetings.getCodes();
    String[] options = partitionSetings.getOptions();

    assertThat( codes.length, equalTo( actualMethodCodesLength + plugins.size() ) );
    assertThat( options.length, equalTo( actualMethodDescriptionLength + plugins.size() ) );

    for ( int i = 0; i < actualMethodCodesLength; i++ ) {
      assertThat( codes[ i ], equalTo( StepPartitioningMeta.methodCodes[ i ] ) );
    }
    for ( int i = 0; i < actualMethodDescriptionLength; i++ ) {
      assertThat( options[ i ], equalTo( StepPartitioningMeta.methodDescriptions[ i ] ) );
    }
  }

  @Test
  public void fillOptionsAndCodesByPlugins() {
    partitionSetings = new PartitionSettings( exactSize, transMeta, stepMeta );
    partitionSetings.fillOptionsAndCodesByPlugins( plugins );

    List<String> codes = Arrays.asList( partitionSetings.getCodes() );
    List<String> options = Arrays.asList( partitionSetings.getOptions() );

    for ( PluginInterface plugin : plugins ) {
      for ( String id : plugin.getIds() ) {
        assertThat( codes.contains( id ), equalTo( true ) );
      }
    }

    for ( PluginInterface plugin : plugins ) {
      assertThat( options.contains( plugin.getDescription() ), equalTo( true ) );
    }

  }

  @Test
  public void getDefaultSelectedMethodIndex() {
    partitionSetings = new PartitionSettings( exactSize, transMeta, stepMeta );
    partitionSetings.fillOptionsAndCodesByPlugins( plugins );
    int defaultSelectedMethod = partitionSetings.getDefaultSelectedMethodIndex();
    int actualIndex =
      Arrays.asList( partitionSetings.getCodes() ).indexOf( stepMeta.getStepPartitioningMeta().getMethod() );
    assertThat( actualIndex, equalTo( defaultSelectedMethod ) );

  }

  @Test
  public void getDefaultSelectedSchemaIndex() {
    partitionSetings = new PartitionSettings( exactSize, transMeta, stepMeta );
    partitionSetings.fillOptionsAndCodesByPlugins( plugins );
    int defaultSelectedSchema = partitionSetings.getDefaultSelectedSchemaIndex();
    assertThat( 1, equalTo( defaultSelectedSchema ) );
  }

  @Test
  public void getDefaultSelectedSchemaIndexNoAppropriateSchema() {
    stepMeta.getStepPartitioningMeta()
      .setPartitionSchema( createPartitionSchema( "State1", Arrays.asList( "P1", "P2" ) ) );

    partitionSetings = new PartitionSettings( exactSize, transMeta, stepMeta );
    partitionSetings.fillOptionsAndCodesByPlugins( plugins );
    int defaultSelectedSchema = partitionSetings.getDefaultSelectedSchemaIndex();
    assertThat( 0, equalTo( defaultSelectedSchema ) );
  }

  @Test
  public void getMethodByMethodDescription() {
    partitionSetings = new PartitionSettings( exactSize, transMeta, stepMeta );
    partitionSetings.fillOptionsAndCodesByPlugins( plugins );
    String method = partitionSetings.getMethodByMethodDescription( PARTITION_METHOD_DESCRIPTION );
    assertThat( PARTITION_METHOD, equalTo( method ) );
  }

  @Test
  public void getMethodByMethodDescriptionUnrecognizedDescription() {
    partitionSetings = new PartitionSettings( exactSize, transMeta, stepMeta );
    partitionSetings.fillOptionsAndCodesByPlugins( plugins );
    String method = partitionSetings.getMethodByMethodDescription( PARTITION_METHOD_DESCRIPTION_WRONG );
    assertThat( StepPartitioningMeta.methodCodes[ StepPartitioningMeta.PARTITIONING_METHOD_NONE ], equalTo( method ) );
  }

  @Test
  public void updateSchema() {
    PartitionSchema sc = createPartitionSchema( "AnotherState", Arrays.asList( "ID_1", "ID_2" ) );
    partitionSetings = new PartitionSettings( exactSize, transMeta, stepMeta );
    partitionSetings.updateSchema( sc );
    assertThat( stepMeta.getStepPartitioningMeta().getPartitionSchema(), equalTo( sc ) );
  }

  @Test
  public void updateSchemaNullSchemaName() {
    PartitionSchema sc = createPartitionSchema( null, new ArrayList<String>(  ) );
    partitionSetings = new PartitionSettings( exactSize, transMeta, stepMeta );
    partitionSetings.updateSchema( sc );
    assertThat( stepMeta.getStepPartitioningMeta().getPartitionSchema().getName(), equalTo( PARTITION_SCHEMA_NAME ) );
  }

  @Test
  public void updateSchemaEmptySchemaName() {
    PartitionSchema sc = createPartitionSchema( "", Arrays.asList( "ID_1", "ID_2" ) );
    partitionSetings = new PartitionSettings( exactSize, transMeta, stepMeta );
    partitionSetings.updateSchema( sc );
    assertThat( stepMeta.getStepPartitioningMeta().getPartitionSchema(), equalTo( sc ) );
  }

  private TransMeta createTransMeta() {
    TransMeta transMeta = new TransMeta();
    return transMeta;
  }

  private StepMeta createStepMeta( String stepId ) throws KettlePluginException {
    StepMeta stepMeta = new StepMeta();
    stepMeta.setStepID( stepId );
    return stepMeta;
  }

  private StepPartitioningMeta createStepParititonMeta() throws KettlePluginException {
    StepPartitioningMeta stepPartitionMeta = new StepPartitioningMeta();
    stepPartitionMeta.setMethodType( 2 );
    stepPartitionMeta.setMethod( PARTITION_METHOD );


    return stepPartitionMeta;
  }

  private PartitionSchema createPartitionSchema( String name, List<String> partitionIDs ) {
    return new PartitionSchema( name, partitionIDs );
  }

  private int getExactSize( List<PluginInterface> plugins ) {
    return StepPartitioningMeta.methodDescriptions.length + plugins.size();
  }
}
