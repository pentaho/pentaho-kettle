/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2023 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.step;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.pentaho.di.base.BaseMeta;
import org.pentaho.di.cluster.ClusterSchema;
import org.pentaho.di.core.AttributesInterface;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.CheckResultSourceInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.attributes.AttributesUtil;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettlePluginLoaderException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.gui.GUIPositionInterface;
import org.pentaho.di.core.gui.Point;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.metastore.MetaStoreConst;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.resource.ResourceDefinition;
import org.pentaho.di.resource.ResourceExportInterface;
import org.pentaho.di.resource.ResourceHolderInterface;
import org.pentaho.di.resource.ResourceNamingInterface;
import org.pentaho.di.resource.ResourceReference;
import org.pentaho.di.shared.SharedObjectBase;
import org.pentaho.di.shared.SharedObjectInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.errorhandling.StreamInterface;
import org.pentaho.di.trans.steps.missing.MissingTrans;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * This class contains everything that is needed to define a step.
 *
 * @since 27-mei-2003
 * @author Matt
 *
 */
public class StepMeta extends SharedObjectBase implements Cloneable, Comparable<StepMeta>, GUIPositionInterface,
    SharedObjectInterface, CheckResultSourceInterface, ResourceExportInterface, ResourceHolderInterface,
    AttributesInterface, BaseMeta {
  private static Class<?> PKG = StepMeta.class; // for i18n purposes, needed by Translator2!!

  public static final String XML_TAG = "step";

  public static final String STRING_ID_MAPPING = "Mapping";

  public static final String STRING_ID_SINGLE_THREADER = "SingleThreader";

  public static final String STRING_ID_ETL_META_INJECT = "MetaInject";

  public static final String STRING_ID_JOB_EXECUTOR = "JobExecutor";

  public static final String STRING_ID_MAPPING_INPUT = "MappingInput";

  public static final String STRING_ID_MAPPING_OUTPUT = "MappingOutput";

  private String stepid; // --> StepPlugin.id

  private String name;

  private StepMetaInterface stepMetaInterface;

  private boolean selected;

  private boolean distributes;

  private boolean isDeprecated;

  private String suggestion = "";

  private RowDistributionInterface rowDistribution;

  private String copiesString;

  private Point location;

  private boolean drawstep;

  private String description;

  private boolean terminator;

  private StepPartitioningMeta stepPartitioningMeta;

  private StepPartitioningMeta targetStepPartitioningMeta;

  private ClusterSchema clusterSchema;

  private String clusterSchemaName; // temporary to resolve later.

  private StepErrorMeta stepErrorMeta;

  // OK, we need to explain to this running step that we expect input from remote steps.
  // This only happens when the previous step "repartitions". (previous step has different
  // partitioning method than this one)
  //
  // So here we go, let's create List members for the remote input and output step
  //

  /** These are the remote input steps to read from, one per host:port combination */
  private List<RemoteStep> remoteInputSteps;

  /** These are the remote output steps to write to, one per host:port combination */
  private List<RemoteStep> remoteOutputSteps;

  private ObjectId id;

  private TransMeta parentTransMeta;

  private Integer copiesCache = null;

  protected Map<String, Map<String, String>> attributesMap;

  /**
   * @param stepid
   *          The ID of the step: this is derived information, you can also use the constructor without stepid. This
   *          constructor will be deprecated soon.
   * @param stepname
   *          The name of the new step
   * @param stepMetaInterface
   *          The step metadata interface to use (TextFileInputMeta, etc)
   */
  public StepMeta( String stepid, String stepname, StepMetaInterface stepMetaInterface ) {
    this( stepname, stepMetaInterface );
    if ( this.stepid == null ) {
      this.stepid = stepid;
    }
  }

  /**
   * @param stepname
   *          The name of the new step
   * @param stepMetaInterface
   *          The step metadata interface to use (TextFileInputMeta, etc)
   */
  public StepMeta( String stepname, StepMetaInterface stepMetaInterface ) {
    if ( stepMetaInterface != null ) {
      PluginRegistry registry = PluginRegistry.getInstance();
      this.stepid = registry.getPluginId( StepPluginType.class, stepMetaInterface );
      setDeprecationAndSuggestedStep();
    }
    this.name = stepname;
    setStepMetaInterface( stepMetaInterface );

    selected = false;
    distributes = true;
    copiesString = "1";
    location = new Point( 0, 0 );
    drawstep = false;
    description = null;
    stepPartitioningMeta = new StepPartitioningMeta();
    // targetStepPartitioningMeta = new StepPartitioningMeta();

    clusterSchema = null; // non selected by default.

    remoteInputSteps = new ArrayList<RemoteStep>();
    remoteOutputSteps = new ArrayList<RemoteStep>();

    attributesMap = new HashMap<String, Map<String, String>>();
  }

  public StepMeta() {
    this( (String) null, (String) null, (StepMetaInterface) null );
  }

  @Override
  public String getXML() throws KettleException {
    return getXML( true );
  }

  public String getXML( boolean includeInterface ) throws KettleException {
    StringBuilder retval = new StringBuilder( 200 );

    retval.append( "  " ).append( XMLHandler.openTag( XML_TAG ) ).append( Const.CR );
    retval.append( "    " ).append( XMLHandler.addTagValue( "name", getName() ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "type", getStepID() ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "description", description ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "distribute", distributes ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "custom_distribution", rowDistribution == null ? null
        : rowDistribution.getCode() ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "copies", copiesString ) );

    retval.append( stepPartitioningMeta.getXML() );
    if ( targetStepPartitioningMeta != null ) {
      retval.append( XMLHandler.openTag( "target_step_partitioning" ) ).append( targetStepPartitioningMeta.getXML() )
          .append( XMLHandler.closeTag( "target_step_partitioning" ) );
    }

    if ( includeInterface ) {
      retval.append( stepMetaInterface.getXML() );
    }

    retval.append( AttributesUtil.getAttributesXml( attributesMap ) );

    retval.append( "    " ).append( XMLHandler.addTagValue( "cluster_schema", clusterSchema == null ? ""
        : clusterSchema.getName() ) );

    retval.append( "    " ).append( XMLHandler.openTag( "remotesteps" ) ).append( Const.CR );
    // Output the remote input steps
    List<RemoteStep> inputSteps = new ArrayList<RemoteStep>( remoteInputSteps );
    Collections.sort( inputSteps ); // sort alphabetically, making it easier to compare XML files
    retval.append( "      " ).append( XMLHandler.openTag( "input" ) ).append( Const.CR );
    for ( RemoteStep remoteStep : inputSteps ) {
      retval.append( "      " ).append( remoteStep.getXML() ).append( Const.CR );
    }
    retval.append( "      " ).append( XMLHandler.closeTag( "input" ) ).append( Const.CR );

    // Output the remote output steps
    List<RemoteStep> outputSteps = new ArrayList<RemoteStep>( remoteOutputSteps );
    Collections.sort( outputSteps ); // sort alphabetically, making it easier to compare XML files
    retval.append( "      " ).append( XMLHandler.openTag( "output" ) ).append( Const.CR );
    for ( RemoteStep remoteStep : outputSteps ) {
      retval.append( "      " ).append( remoteStep.getXML() ).append( Const.CR );
    }
    retval.append( "      " ).append( XMLHandler.closeTag( "output" ) ).append( Const.CR );
    retval.append( "    " ).append( XMLHandler.closeTag( "remotesteps" ) ).append( Const.CR );

    retval.append( "    " ).append( XMLHandler.openTag( "GUI" ) ).append( Const.CR );
    retval.append( "      " ).append( XMLHandler.addTagValue( "xloc", location.x ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "yloc", location.y ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "draw", drawstep ? "Y" : "N" ) );
    retval.append( "    " ).append( XMLHandler.closeTag( "GUI" ) ).append( Const.CR );
    retval.append( "    " ).append( XMLHandler.closeTag( XML_TAG ) ).append( Const.CR ).append( Const.CR );

    return retval.toString();
  }

  /**
   * Read the step data from XML
   *
   * @param stepnode
   *          The XML step node.
   * @param databases
   *          A list of databases
   * @param counters
   *          A map with all defined counters.
   * @deprecated use {@link #StepMeta(Node, List, IMetaStore)}
   */
  @Deprecated
  public StepMeta( Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters )
    throws KettleXMLException, KettlePluginLoaderException {
    this( stepnode, databases, (IMetaStore) null );
  }

  /**
   * Read the step data from XML
   *
   * @param stepnode
   *          The XML step node.
   * @param databases
   *          A list of databases
   * @param metaStore
   *          The IMetaStore.
   *
   */
  public StepMeta( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException,
    KettlePluginLoaderException {
    this();
    PluginRegistry registry = PluginRegistry.getInstance();

    try {
      name = XMLHandler.getTagValue( stepnode, "name" );
      stepid = XMLHandler.getTagValue( stepnode, "type" );
      setDeprecationAndSuggestedStep();

      // Create a new StepMetaInterface object...
      PluginInterface sp = registry.findPluginWithId( StepPluginType.class, stepid, true );

      if ( sp == null ) {
        setStepMetaInterface( new MissingTrans( name, stepid ) );
      } else {
        setStepMetaInterface( (StepMetaInterface) registry.loadClass( sp ) );
      }
      if ( this.stepMetaInterface != null ) {
        if ( sp != null ) {
          stepid = sp.getIds()[0]; // revert to the default in case we loaded an alternate version
        }

        // Load the specifics from XML...
        if ( stepMetaInterface != null ) {
          loadXmlCompatibleStepMeta( stepMetaInterface, stepnode, databases );
          stepMetaInterface.loadXML( stepnode, databases, metaStore );
        }

        /* Handle info general to all step types... */
        description = XMLHandler.getTagValue( stepnode, "description" );
        copiesString = XMLHandler.getTagValue( stepnode, "copies" );
        String sdistri = XMLHandler.getTagValue( stepnode, "distribute" );
        distributes = "Y".equalsIgnoreCase( sdistri );
        if ( sdistri == null ) {
          distributes = true; // default=distribute
        }

        // Load the attribute groups map
        //
        attributesMap = AttributesUtil.loadAttributes( XMLHandler.getSubNode( stepnode, AttributesUtil.XML_TAG ) );

        // Determine the row distribution
        //
        String rowDistributionCode = XMLHandler.getTagValue( stepnode, "custom_distribution" );
        rowDistribution =
            PluginRegistry.getInstance().loadClass( RowDistributionPluginType.class, rowDistributionCode,
                RowDistributionInterface.class );

        // Handle GUI information: location & drawstep?
        String xloc, yloc;
        int x, y;
        xloc = XMLHandler.getTagValue( stepnode, "GUI", "xloc" );
        yloc = XMLHandler.getTagValue( stepnode, "GUI", "yloc" );
        try {
          x = Integer.parseInt( xloc );
        } catch ( Exception e ) {
          x = 0;
        }
        try {
          y = Integer.parseInt( yloc );
        } catch ( Exception e ) {
          y = 0;
        }
        location = new Point( x, y );

        drawstep = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "GUI", "draw" ) );

        // The partitioning information?
        //
        Node partNode = XMLHandler.getSubNode( stepnode, "partitioning" );
        stepPartitioningMeta = new StepPartitioningMeta( partNode );

        // Target partitioning information?
        //
        Node targetPartNode = XMLHandler.getSubNode( stepnode, "target_step_partitioning" );
        partNode = XMLHandler.getSubNode( targetPartNode, "partitioning" );
        if ( partNode != null ) {
          targetStepPartitioningMeta = new StepPartitioningMeta( partNode );
        }

        clusterSchemaName = XMLHandler.getTagValue( stepnode, "cluster_schema" );

        // The remote input and output steps...
        Node remotestepsNode = XMLHandler.getSubNode( stepnode, "remotesteps" );
        Node inputNode = XMLHandler.getSubNode( remotestepsNode, "input" );
        int nrInput = XMLHandler.countNodes( inputNode, RemoteStep.XML_TAG );
        for ( int i = 0; i < nrInput; i++ ) {
          remoteInputSteps.add( new RemoteStep( XMLHandler.getSubNodeByNr( inputNode, RemoteStep.XML_TAG, i ) ) );

        }
        Node outputNode = XMLHandler.getSubNode( remotestepsNode, "output" );
        int nrOutput = XMLHandler.countNodes( outputNode, RemoteStep.XML_TAG );
        for ( int i = 0; i < nrOutput; i++ ) {
          remoteOutputSteps.add( new RemoteStep( XMLHandler.getSubNodeByNr( outputNode, RemoteStep.XML_TAG, i ) ) );
        }
      }
    } catch ( KettlePluginLoaderException e ) {
      throw e;
    } catch ( Exception e ) {
      throw new KettleXMLException( BaseMessages.getString( PKG, "StepMeta.Exception.UnableToLoadStepInfo" ) + e
          .toString(), e );
    }
  }

  /**
   * Just in case we missed a v4 plugin using deprecated methods.
   *
   * @param stepMetaInterface2
   * @param stepnode
   * @param databases
   * @throws KettleXMLException
   */
  @SuppressWarnings( "deprecation" )
  private void loadXmlCompatibleStepMeta( StepMetaInterface stepMetaInterface2, Node stepnode,
      List<DatabaseMeta> databases ) throws KettleXMLException {
    stepMetaInterface.loadXML( stepnode, databases, new HashMap<String, Counter>() );
  }

  /**
   * Resolves the name of the cluster loaded from XML/Repository to the correct clusterSchema object
   *
   * @param clusterSchemas
   *          The list of clusterSchemas to reference.
   */
  public void setClusterSchemaAfterLoading( List<ClusterSchema> clusterSchemas ) {
    if ( clusterSchemaName == null ) {
      return;
    }
    for ( ClusterSchema look : clusterSchemas ) {
      if ( look.getName().equals( clusterSchemaName ) ) {
        clusterSchema = look;
      }
    }
  }


  public static StepMeta fromXml( String metaXml ) {
    Document doc;
    try {
      doc = XMLHandler.loadXMLString( metaXml );
      Node stepNode = XMLHandler.getSubNode( doc, "step" );
      return new StepMeta( stepNode, Collections.emptyList(), (IMetaStore) null );
    } catch ( KettleXMLException | KettlePluginLoaderException e ) {
      throw new RuntimeException( e );
    }
  }

  @Override
  public ObjectId getObjectId() {
    return id;
  }

  public void setObjectId( ObjectId id ) {
    this.id = id;
  }

  /**
   * See wether or not the step is drawn on the canvas.
   *
   * @return True if the step is drawn on the canvas.
   */
  public boolean isDrawn() {
    return drawstep;
  }

  /**
   * See wether or not the step is drawn on the canvas. Same as isDrawn(), but needed for findMethod(StepMeta, drawstep)
   * called by StringSearcher.findMetaData(). Otherwise findMethod() returns
   * org.pentaho.di.trans.step.StepMeta.drawStep() instead of isDrawn().
   *
   * @return True if the step is drawn on the canvas.
   */
  public boolean isDrawStep() {
    return drawstep;
  }

  /**
   * Sets the draw attribute of the step so that it will be drawn on the canvas.
   *
   * @param draw
   *          True if you want the step to show itself on the canvas, False if you don't.
   */
  public void setDraw( boolean draw ) {
    drawstep = draw;
    setChanged();
  }

  /**
   * Sets the number of parallel copies that this step will be launched with.
   *
   * @param c
   *          The number of copies.
   */
  public void setCopies( int c ) {
    setChanged();
    copiesString = Integer.toString( c );
    copiesCache = c;
  }

  /**
   * Get the number of copies to start of a step. This takes into account the partitioning logic.
   *
   * @return the number of step copies to start.
   */
  public int getCopies() {
    // If the step is partitioned, that's going to determine the number of copies, nothing else...
    //
    if ( isPartitioned() && getStepPartitioningMeta().getPartitionSchema() != null ) {
      List<String> partitionIDs = getStepPartitioningMeta().getPartitionSchema().getPartitionIDs();
      if ( partitionIDs != null && partitionIDs.size() > 0 ) { // these are the partitions the step can "reach"
        return partitionIDs.size();
      }
    }

    if ( copiesCache != null ) {
      return copiesCache.intValue();
    }

    if ( parentTransMeta != null ) {
      // Return -1 to indicate that the variable or string value couldn't be converted to number
      //
      copiesCache = Const.toInt( parentTransMeta.environmentSubstitute( copiesString ), -1 );
    } else {
      copiesCache = Const.toInt( copiesString, 1 );
    }

    return copiesCache;
  }

  public void drawStep() {
    setDraw( true );
    setChanged();
  }

  public void hideStep() {
    setDraw( false );
    setChanged();
  }

  /**
   * Two steps are equal if their names are equal.
   *
   * @return true if the two steps are equal.
   */
  @Override
  public boolean equals( Object obj ) {
    if ( obj == null ) {
      return false;
    }
    StepMeta stepMeta = (StepMeta) obj;
    // getName() is returning stepname, matching the hashCode() algorithm
    return getName().equalsIgnoreCase( stepMeta.getName() );
  }

  @Override
  public int hashCode() {
    return name.toLowerCase().hashCode();
  }

  @Override
  public int compareTo( StepMeta o ) {
    return toString().compareTo( o.toString() );
  }

  public boolean hasChanged() {
    StepMetaInterface bsi = this.getStepMetaInterface();
    return bsi != null ? bsi.hasChanged() : false;
  }

  public void setChanged( boolean ch ) {
    BaseStepMeta bsi = (BaseStepMeta) this.getStepMetaInterface();
    if ( bsi != null ) {
      bsi.setChanged( ch );
    }
  }

  public void setChanged() {
    StepMetaInterface bsi = this.getStepMetaInterface();
    if ( bsi != null ) {
      bsi.setChanged();
    }
  }

  public boolean chosesTargetSteps() {
    if ( getStepMetaInterface() != null ) {
      List<StreamInterface> targetStreams = getStepMetaInterface().getStepIOMeta().getTargetStreams();
      return targetStreams.isEmpty();
    }
    return false;
  }

  @Override
  public Object clone() {
    StepMeta stepMeta = new StepMeta();
    stepMeta.replaceMeta( this );
    stepMeta.setObjectId( null );
    return stepMeta;
  }

  public void replaceMeta( StepMeta stepMeta ) {
    this.stepid = stepMeta.stepid; // --> StepPlugin.id
    this.name = stepMeta.name;
    if ( stepMeta.stepMetaInterface != null ) {
      setStepMetaInterface( (StepMetaInterface) stepMeta.stepMetaInterface.clone() );
    } else {
      this.stepMetaInterface = null;
    }
    this.selected = stepMeta.selected;
    this.distributes = stepMeta.distributes;
    this.setRowDistribution( stepMeta.getRowDistribution() );
    this.copiesString = stepMeta.copiesString;
    this.copiesCache = null; // force re-calculation
    if ( stepMeta.location != null ) {
      this.location = new Point( stepMeta.location.x, stepMeta.location.y );
    } else {
      this.location = null;
    }
    this.drawstep = stepMeta.drawstep;
    this.description = stepMeta.description;
    this.terminator = stepMeta.terminator;

    if ( stepMeta.stepPartitioningMeta != null ) {
      this.stepPartitioningMeta = stepMeta.stepPartitioningMeta.clone();
    } else {
      this.stepPartitioningMeta = null;
    }
    if ( stepMeta.clusterSchema != null ) {
      this.clusterSchema = stepMeta.clusterSchema.clone();
    } else {
      this.clusterSchema = null;
    }
    this.clusterSchemaName = stepMeta.clusterSchemaName; // temporary to resolve later.

    // Also replace the remote steps with cloned versions...
    //
    this.remoteInputSteps = new ArrayList<RemoteStep>();
    for ( RemoteStep remoteStep : stepMeta.remoteInputSteps ) {
      this.remoteInputSteps.add( (RemoteStep) remoteStep.clone() );
    }
    this.remoteOutputSteps = new ArrayList<RemoteStep>();
    for ( RemoteStep remoteStep : stepMeta.remoteOutputSteps ) {
      this.remoteOutputSteps.add( (RemoteStep) remoteStep.clone() );
    }

    // The error handling needs to be done too...
    //
    if ( stepMeta.stepErrorMeta != null ) {
      this.stepErrorMeta = stepMeta.stepErrorMeta.clone();
    }

    this.attributesMap = copyStringMap( stepMeta.attributesMap );

    // this.setShared(stepMeta.isShared());
    this.id = stepMeta.getObjectId();
    this.setChanged( true );
  }

  private static Map<String, Map<String, String>> copyStringMap( Map<String, Map<String, String>> map ) {
    if ( map == null ) {
      return new HashMap<String, Map<String, String>>();
    }

    Map<String, Map<String, String>> result = new HashMap<String, Map<String, String>>( map.size() );
    for ( Map.Entry<String, Map<String, String>> entry : map.entrySet() ) {
      Map<String, String> value = entry.getValue();
      HashMap<String, String> copy = ( value == null ) ? null : new HashMap<String, String>( value );
      result.put( entry.getKey(), copy );
    }
    return result;
  }

  public StepMetaInterface getStepMetaInterface() {
    return stepMetaInterface;
  }

  public void setStepMetaInterface( StepMetaInterface stepMetaInterface ) {
    this.stepMetaInterface = stepMetaInterface;
    if ( stepMetaInterface != null ) {
      this.stepMetaInterface.setParentStepMeta( this );
    }
  }

  public String getStepID() {
    return stepid;
  }

  @Override
  public String getName() {
    return name;
  }

  public void setName( String sname ) {
    name = sname;
  }

  @Override
  public String getDescription() {
    return description;
  }

  public void setDescription( String description ) {
    this.description = description;
  }

  @Override
  public void setSelected( boolean sel ) {
    selected = sel;
  }

  public void flipSelected() {
    selected = !selected;
  }

  @Override
  public boolean isSelected() {
    return selected;
  }

  public void setTerminator() {
    setTerminator( true );
  }

  public void setTerminator( boolean t ) {
    terminator = t;
  }

  public boolean hasTerminator() {
    return terminator;
  }

  public StepMeta( ObjectId id_step ) {
    this( (String) null, (String) null, (StepMetaInterface) null );
    setObjectId( id_step );
  }

  @Override
  public void setLocation( int x, int y ) {
    int nx = ( x >= 0 ? x : 0 );
    int ny = ( y >= 0 ? y : 0 );

    Point loc = new Point( nx, ny );
    if ( !loc.equals( location ) ) {
      setChanged();
    }
    location = loc;
  }

  @Override
  public void setLocation( Point loc ) {
    if ( loc != null && !loc.equals( location ) ) {
      setChanged();
    }
    location = loc;
  }

  @Override
  public Point getLocation() {
    return location;
  }

  /**
   * @deprecated use {@link #check(List, TransMeta, RowMetaInterface, String[], String[], RowMetaInterface, VariableSpace, Repository, IMetaStore)}
   * @param remarks
   * @param transMeta
   * @param prev
   * @param input
   * @param output
   * @param info
   */
  @Deprecated
  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, RowMetaInterface prev, String[] input,
      String[] output, RowMetaInterface info ) {
    check( remarks, transMeta, prev, input, output, info, new Variables(), null, null );
  }

  @SuppressWarnings( "deprecation" )
  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, RowMetaInterface prev, String[] input,
      String[] output, RowMetaInterface info, VariableSpace space, Repository repository, IMetaStore metaStore ) {
    stepMetaInterface.check( remarks, transMeta, this, prev, input, output, info );
    stepMetaInterface.check( remarks, transMeta, this, prev, input, output, info, space, repository, metaStore );
  }

  @Override
  public String toString() {
    if ( getName() == null ) {
      return getClass().getName();
    }
    return getName();
  }

  /**
   * @return true is the step is partitioned
   */
  public boolean isPartitioned() {
    return stepPartitioningMeta.isPartitioned();
  }

  /**
   * @return true is the step is partitioned
   */
  public boolean isTargetPartitioned() {
    return targetStepPartitioningMeta.isPartitioned();
  }

  /**
   * @return the stepPartitioningMeta
   */
  public StepPartitioningMeta getStepPartitioningMeta() {
    return stepPartitioningMeta;
  }

  /**
   * @param stepPartitioningMeta
   *          the stepPartitioningMeta to set
   */
  public void setStepPartitioningMeta( StepPartitioningMeta stepPartitioningMeta ) {
    this.stepPartitioningMeta = stepPartitioningMeta;
  }

  /**
   * @return the clusterSchema
   */
  public ClusterSchema getClusterSchema() {
    return clusterSchema;
  }

  /**
   * @param clusterSchema
   *          the clusterSchema to set
   */
  public void setClusterSchema( ClusterSchema clusterSchema ) {
    this.clusterSchema = clusterSchema;
  }

  /**
   * @return the distributes
   */
  public boolean isDistributes() {
    return distributes;
  }

  /**
   * @param distributes
   *          the distributes to set
   */
  public void setDistributes( boolean distributes ) {
    if ( this.distributes != distributes ) {
      this.distributes = distributes;
      setChanged();
    }

  }

  /**
   * @return the StepErrorMeta error handling metadata for this step
   */
  public StepErrorMeta getStepErrorMeta() {
    return stepErrorMeta;
  }

  /**
   * @param stepErrorMeta
   *          the error handling metadata for this step
   */
  public void setStepErrorMeta( StepErrorMeta stepErrorMeta ) {
    this.stepErrorMeta = stepErrorMeta;
  }

  /**
   * Find a step with the ID in a given ArrayList of steps
   *
   * @param steps
   *          The List of steps to search
   * @param id
   *          The ID of the step
   * @return The step if it was found, null if nothing was found
   */
  public static final StepMeta findStep( List<StepMeta> steps, ObjectId id ) {
    if ( steps == null ) {
      return null;
    }

    for ( StepMeta stepMeta : steps ) {
      if ( stepMeta.getObjectId() != null && stepMeta.getObjectId().equals( id ) ) {
        return stepMeta;
      }
    }
    return null;
  }

  /**
   * Find a step with its name in a given ArrayList of steps
   *
   * @param steps
   *          The List of steps to search
   * @param stepname
   *          The name of the step
   * @return The step if it was found, null if nothing was found
   */
  public static final StepMeta findStep( List<StepMeta> steps, String stepname ) {
    if ( steps == null ) {
      return null;
    }

    for ( StepMeta stepMeta : steps ) {
      if ( stepMeta.getName().equalsIgnoreCase( stepname ) ) {
        return stepMeta;
      }
    }
    return null;
  }

  public boolean supportsErrorHandling() {
    return stepMetaInterface.supportsErrorHandling();
  }

  /**
   * @return if error handling is supported for this step, if error handling is defined and a target step is set
   */
  public boolean isDoingErrorHandling() {
    return stepMetaInterface.supportsErrorHandling() && stepErrorMeta != null && stepErrorMeta.getTargetStep() != null
        && stepErrorMeta.isEnabled();
  }

  public boolean isSendingErrorRowsToStep( StepMeta targetStep ) {
    return ( isDoingErrorHandling() && stepErrorMeta.getTargetStep().equals( targetStep ) );
  }

  /**
   * Support for CheckResultSourceInterface
   */
  @Override
  public String getTypeId() {
    return this.getStepID();
  }

  public boolean isMapping() {
    return STRING_ID_MAPPING.equals( stepid );
  }

  public boolean isSingleThreader() {
    return STRING_ID_SINGLE_THREADER.equals( stepid );
  }

  public boolean isEtlMetaInject() {
    return STRING_ID_ETL_META_INJECT.equals( stepid );
  }

  public boolean isJobExecutor() {
    return STRING_ID_JOB_EXECUTOR.equals( stepid );
  }

  public boolean isMappingInput() {
    return STRING_ID_MAPPING_INPUT.equals( stepid );
  }

  public boolean isMappingOutput() {
    return STRING_ID_MAPPING_OUTPUT.equals( stepid );
  }

  /**
   * Get a list of all the resource dependencies that the step is depending on.
   *
   * @return a list of all the resource dependencies that the step is depending on
   */
  public List<ResourceReference> getResourceDependencies( TransMeta transMeta ) {
    return stepMetaInterface.getResourceDependencies( transMeta, this );
  }

  /**
   * @deprecated use {@link #exportResources(VariableSpace, Map, ResourceNamingInterface, Repository, IMetaStore)}
   * @param space
   * @param definitions
   * @param resourceNamingInterface
   * @param repository
   * @return
   * @throws KettleException
   */
  @Deprecated
  public String exportResources( VariableSpace space, Map<String, ResourceDefinition> definitions,
      ResourceNamingInterface resourceNamingInterface, Repository repository ) throws KettleException {
    return exportResources( space, definitions, resourceNamingInterface, repository,
      MetaStoreConst.getDefaultMetastore() );
  }

  @Override
  @SuppressWarnings( "deprecation" )
  public String exportResources( VariableSpace space, Map<String, ResourceDefinition> definitions,
      ResourceNamingInterface resourceNamingInterface, Repository repository, IMetaStore metaStore )
        throws KettleException {

    // Compatibility with previous release...
    //
    String resources = stepMetaInterface.exportResources( space, definitions, resourceNamingInterface, repository );
    if ( resources != null ) {
      return resources;
    }

    // The step calls out to the StepMetaInterface...
    // These can in turn add anything to the map in terms of resources, etc.
    // Even reference files, etc. For now it's just XML probably...
    //
    return stepMetaInterface.exportResources( space, definitions, resourceNamingInterface, repository, metaStore );
  }

  /**
   * @return the remoteInputSteps
   */
  public List<RemoteStep> getRemoteInputSteps() {
    return remoteInputSteps;
  }

  /**
   * @param remoteInputSteps
   *          the remoteInputSteps to set
   */
  public void setRemoteInputSteps( List<RemoteStep> remoteInputSteps ) {
    this.remoteInputSteps = remoteInputSteps;
  }

  /**
   * @return the remoteOutputSteps
   */
  public List<RemoteStep> getRemoteOutputSteps() {
    return remoteOutputSteps;
  }

  /**
   * @param remoteOutputSteps
   *          the remoteOutputSteps to set
   */
  public void setRemoteOutputSteps( List<RemoteStep> remoteOutputSteps ) {
    this.remoteOutputSteps = remoteOutputSteps;
  }

  /**
   * @return the targetStepPartitioningMeta
   */
  public StepPartitioningMeta getTargetStepPartitioningMeta() {
    return targetStepPartitioningMeta;
  }

  /**
   * @param targetStepPartitioningMeta
   *          the targetStepPartitioningMeta to set
   */
  public void setTargetStepPartitioningMeta( StepPartitioningMeta targetStepPartitioningMeta ) {
    this.targetStepPartitioningMeta = targetStepPartitioningMeta;
  }

  public boolean isRepartitioning() {
    if ( !isPartitioned() && isTargetPartitioned() ) {
      return true;
    }
    if ( isPartitioned() && isTargetPartitioned() && !stepPartitioningMeta.equals( targetStepPartitioningMeta ) ) {
      return true;
    }
    return false;
  }

  @Override
  public String getHolderType() {
    return "STEP";
  }

  public boolean isClustered() {
    return clusterSchema != null;
  }

  /**
   * Set the plugin step id (code)
   *
   * @param stepid
   */
  public void setStepID( String stepid ) {
    this.stepid = stepid;
  }

  public void setClusterSchemaName( String clusterSchemaName ) {
    this.clusterSchemaName = clusterSchemaName;
  }

  public void setParentTransMeta( TransMeta parentTransMeta ) {
    this.parentTransMeta = parentTransMeta;
  }

  public TransMeta getParentTransMeta() {
    return parentTransMeta;
  }

  public RowDistributionInterface getRowDistribution() {
    return rowDistribution;
  }

  public void setRowDistribution( RowDistributionInterface rowDistribution ) {
    this.rowDistribution = rowDistribution;
    if ( rowDistribution != null ) {
      setDistributes( true );
    }
    setChanged( true );
  }

  /**
   * @return the copiesString
   */
  public String getCopiesString() {
    return copiesString;
  }

  /**
   * @param copiesString
   *          the copiesString to set
   */
  public void setCopiesString( String copiesString ) {
    this.copiesString = copiesString;
    copiesCache = null;
  }

  @Override
  public void setAttributesMap( Map<String, Map<String, String>> attributesMap ) {
    this.attributesMap = attributesMap;
  }

  @Override
  public Map<String, Map<String, String>> getAttributesMap() {
    return attributesMap;
  }

  @Override
  public void setAttribute( String groupName, String key, String value ) {
    Map<String, String> attributes = getAttributes( groupName );
    if ( attributes == null ) {
      attributes = new HashMap<String, String>();
      attributesMap.put( groupName, attributes );
    }
    attributes.put( key, value );
  }

  @Override
  public void setAttributes( String groupName, Map<String, String> attributes ) {
    attributesMap.put( groupName, attributes );
  }

  @Override
  public Map<String, String> getAttributes( String groupName ) {
    return attributesMap.get( groupName );
  }

  @Override
  public String getAttribute( String groupName, String key ) {
    Map<String, String> attributes = attributesMap.get( groupName );
    if ( attributes == null ) {
      return null;
    }
    return attributes.get( key );
  }

  private void setDeprecationAndSuggestedStep() {
    PluginRegistry registry = PluginRegistry.getInstance();
    final List<PluginInterface> deprecatedSteps = registry.getPluginsByCategory( StepPluginType.class,
      BaseMessages.getString( PKG, "BaseStep.Category.Deprecated" ) );
    for ( PluginInterface p : deprecatedSteps ) {
      String[] ids = p.getIds();
      if ( !ArrayUtils.isEmpty( ids ) && ids[0].equals( this.stepid ) ) {
        this.isDeprecated = true;
        this.suggestion = registry.findPluginWithId( StepPluginType.class, this.stepid ) != null
          ? registry.findPluginWithId( StepPluginType.class, this.stepid ).getSuggestion() : "";
        break;
      }
    }
  }

  public boolean isMissing() {
    return this.stepMetaInterface instanceof MissingTrans;
  }

  public boolean isDeprecated() {
    return isDeprecated;
  }

  public String getSuggestion() {
    return suggestion;
  }
}
