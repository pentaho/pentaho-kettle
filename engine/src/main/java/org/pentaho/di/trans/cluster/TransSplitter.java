/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.cluster;

import org.pentaho.di.cluster.ClusterSchema;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.NotePadMeta;
import org.pentaho.di.core.encryption.CertificateGenEncryptUtil;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.TransLogTable;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.partition.PartitionSchema;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.SlaveStepCopyPartitionDistribution;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransMetaFactory;
import org.pentaho.di.trans.TransMetaFactoryImpl;
import org.pentaho.di.trans.step.RemoteStep;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepPartitioningMeta;
import org.pentaho.di.trans.steps.dummytrans.DummyTransMeta;
import org.pentaho.di.trans.steps.socketreader.SocketReaderMeta;
import org.pentaho.di.trans.steps.socketwriter.SocketWriterMeta;

import javax.crypto.IllegalBlockSizeException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

/**
 * This class takes care of the separation of the original transformation into pieces that run on the different slave
 * servers in the clusters used.
 *
 * @author Matt
 *
 */
public class TransSplitter {
  private static Class<?> PKG = TransMeta.class;
  private static final int FANOUT = 30;
  private static final int SPLIT = 120;

  private TransMeta originalTransformation;
  private Map<SlaveServer, TransMeta> slaveTransMap;
  private TransMeta masterTransMeta;
  private StepMeta[] referenceSteps;
  private Map<SlaveServer, Map<PartitionSchema, List<String>>> slaveServerPartitionsMap;
  private Map<TransMeta, Map<StepMeta, String>> slaveStepPartitionFlag;

  /**
   * Decide in advance which step / copy in which slave server is going to server which partition. It's not a guessing
   * game.
   */
  private SlaveStepCopyPartitionDistribution slaveStepCopyPartitionDistribution =
    new SlaveStepCopyPartitionDistribution();
  private int socketsBufferSize;
  private boolean compressingSocketStreams;

  private Map<String, Integer> portCache;

  private Map<TransMeta, String> carteObjectMap;

  private String clusteredRunId;

  public TransSplitter() {
    clear();
  }

  private void clear() {
    slaveTransMap = new Hashtable<SlaveServer, TransMeta>();
    slaveStepPartitionFlag = new Hashtable<TransMeta, Map<StepMeta, String>>();
    portCache = new Hashtable<String, Integer>();
    carteObjectMap = new Hashtable<TransMeta, String>();

    clusteredRunId = UUID.randomUUID().toString();
  }

  /**
   * @param transMeta The original transformation
   */
  public TransSplitter( TransMeta transMeta ) throws KettleException {
    this( transMeta, new TransMetaFactoryImpl() );
  }

  protected TransSplitter( TransMeta transMeta, TransMetaFactory transMetaFactory ) throws KettleException {
    this();
    // We want to make sure there is no trace of the old transformation left when we
    // Modify during split.
    // As such, we deflate/inflate over XML
    //
    String transXML = transMeta.getXML();
    this.originalTransformation =
        transMetaFactory
            .create( XMLHandler.getSubNode( XMLHandler.loadXMLString( transXML ), TransMeta.XML_TAG ), null );
    this.originalTransformation.shareVariablesWith( transMeta );
    this.originalTransformation.copyParametersFrom( transMeta );

    // Retain repository information
    this.originalTransformation.setRepository( transMeta.getRepository() );
    this.originalTransformation.setRepositoryDirectory( transMeta.getRepositoryDirectory() );

    Repository rep = transMeta.getRepository();
    if ( rep != null ) {
      rep.readTransSharedObjects( this.originalTransformation );
    }

    checkClusterConfiguration();

    // FIRST : convert the dynamic master into a fixed one.
    // This is why we copied the original transformation
    //
    ClusterSchema clusterSchema = this.originalTransformation.findFirstUsedClusterSchema();
    if ( clusterSchema == null ) {
      throw new KettleException( "No clustering is used in this transformation." );
    }
    if ( clusterSchema.isDynamic() ) {
      List<SlaveServer> slaveServers = clusterSchema.getSlaveServersFromMasterOrLocal();
      SlaveServer masterSlaveServer = clusterSchema.findMaster();
      if ( masterSlaveServer == null ) {
        throw new KettleException( "You always need at least one master in a cluster schema." );
      }
      slaveServers.add( 0, masterSlaveServer );
      clusterSchema.setDynamic( false );
      clusterSchema.setSlaveServers( slaveServers );
    }
  }

  /**
   * @return the originalTransformation
   */
  public TransMeta getOriginalTransformation() {
    return originalTransformation;
  }

  /**
   * @param originalTransformation
   *          the originalTransformation to set
   */
  public void setOriginalTransformation( TransMeta originalTransformation ) {
    this.originalTransformation = originalTransformation;
  }

  private void checkClusterConfiguration() throws KettleException {
    Map<String, ClusterSchema> map = new Hashtable<String, ClusterSchema>();
    List<StepMeta> steps = originalTransformation.getSteps();
    for ( int i = 0; i < steps.size(); i++ ) {
      StepMeta step = steps.get( i );
      ClusterSchema clusterSchema = step.getClusterSchema();
      if ( clusterSchema != null ) {
        map.put( clusterSchema.getName(), clusterSchema );

        // Make sure we have at least one master to work with.
        //
        if ( clusterSchema.findMaster() == null ) {
          throw new KettleException( "No master server was specified in cluster schema [" + clusterSchema + "]" );
        }

        // Remember cluster details while we have the cluster handy
        //
        socketsBufferSize =
          Const.toInt(
            originalTransformation.environmentSubstitute( clusterSchema.getSocketsBufferSize() ), 50000 );
        compressingSocketStreams = clusterSchema.isSocketsCompressed();

        // Validate the number of slaves. We need at least one to have a valid cluster
        //
        List<SlaveServer> slaves = clusterSchema.getSlaveServersFromMasterOrLocal();
        int count = 0;
        for ( int s = 0; s < slaves.size(); s++ ) {
          if ( !slaves.get( s ).isMaster() ) {
            count++;
          }
        }
        if ( count <= 0 ) {
          throw new KettleException( "At least one slave server is required to be present in cluster schema ["
            + clusterSchema + "]" );
        }
      }
    }
    if ( map.size() == 0 ) {
      throw new KettleException(
        "No cluster schemas are being used.  As such it is not possible to split and cluster this transformation." );
    }
    if ( map.size() > 1 ) {
      throw new KettleException(
        "At this time we don't support the use of multiple cluster schemas in one and the same transformation." );
    }
  }

  private String getWriterName( ClusterSchema clusterSchema, SlaveServer sourceSlaveServer, String sourceStepname,
    int sourceStepCopy, SlaveServer targetSlaveServer, String targetStepName, int targetStepCopy ) throws Exception {
    return "Writer : "
      + getPort(
        clusterSchema, sourceSlaveServer, sourceStepname, sourceStepCopy, targetSlaveServer, targetStepName,
        targetStepCopy );
  }

  private String getReaderName( ClusterSchema clusterSchema, SlaveServer sourceSlaveServer, String sourceStepname,
    int sourceStepCopy, SlaveServer targetSlaveServer, String targetStepName, int targetStepCopy ) throws Exception {
    return "Reader : "
      + getPort(
        clusterSchema, sourceSlaveServer, sourceStepname, sourceStepCopy, targetSlaveServer, targetStepName,
        targetStepCopy );
  }

  private String getSlaveTransName( String transName, ClusterSchema clusterSchema, SlaveServer slaveServer ) {
    return transName + " (" + clusterSchema + ":" + slaveServer.getName() + ")";
  }

  /**
   * Get the port for the given cluster schema, slave server and step.
   *
   * If a port was allocated, that is returned, otherwise a new one is allocated. We need to verify that the port wasn't
   * already used on the same host with perhaps several Carte instances on it. In order
   *
   * @param clusterSchema
   *          The cluster schema to use
   *
   * @return the port to use for that step/slaveserver/cluster combination
   */
  private int getPort( ClusterSchema clusterSchema, SlaveServer sourceSlave, String sourceStepName,
    int sourceStepCopy, SlaveServer targetSlave, String targetStepName, int targetStepCopy ) throws Exception {
    SlaveServer masterSlave = clusterSchema.findMaster();

    String portCacheKey =
      createPortCacheKey(
        sourceSlave, sourceStepName, sourceStepCopy, targetSlave, targetStepName, targetStepCopy );
    Integer portNumber = portCache.get( portCacheKey );
    if ( portNumber != null ) {
      return portNumber.intValue();
    }

    String realHostname = sourceSlave.environmentSubstitute( sourceSlave.getHostname() );

    int port =
      masterSlave.allocateServerSocket(
        clusteredRunId, Const.toInt( clusterSchema.getBasePort(), 40000 ), realHostname,
        originalTransformation.getName(), sourceSlave.getName(), sourceStepName, Integer
          .toString( sourceStepCopy ), // Source
        targetSlave.getName(), targetStepName, Integer.toString( targetStepCopy ) ); // Target

    portCache.put( portCacheKey, port );

    return port;
  }

  public String createPortCacheKey( SlaveServer sourceSlave, String sourceStepName, int sourceStepCopy,
    SlaveServer targetSlave, String targetStepName, int targetStepCopy ) {
    return clusteredRunId
      + "/" + sourceSlave.getHostname() + sourceSlave.getName() + "/" + sourceStepName + "." + sourceStepCopy
      + " - " + targetSlave.getName() + "/" + targetStepName + "." + targetStepCopy;
  }

  /**
   * Create or get a slave transformation for the specified cluster & slave server
   *
   * @param clusterSchema
   *          the cluster schema to reference
   * @param slaveServer
   *          the slave server to reference
   * @return
   */
  private TransMeta getSlaveTransformation( ClusterSchema clusterSchema,
      SlaveServer slaveServer ) throws KettleException {
    TransMeta slave = slaveTransMap.get( slaveServer );
    if ( slave == null ) {
      slave = getOriginalCopy( true, clusterSchema, slaveServer );
      slaveTransMap.put( slaveServer, slave );
    }
    return slave;
  }

  private TransMeta getOriginalCopy( boolean isSlaveTrans, ClusterSchema clusterSchema,
      SlaveServer slaveServer ) throws KettleException {
    TransMeta transMeta = new TransMeta();
    transMeta.setSlaveTransformation( true );

    if ( isSlaveTrans ) {
      transMeta.setName( getSlaveTransName( originalTransformation.getName(), clusterSchema, slaveServer ) );

      NotePadMeta slaveNote =
        new NotePadMeta( "This is a generated slave transformation.\nIt will be run on slave server: "
          + slaveServer, 0, 0, -1, -1 );
      transMeta.addNote( slaveNote );

      // add the slave partitioning schema's here.
      for ( int i = 0; i < referenceSteps.length; i++ ) {
        StepMeta stepMeta = referenceSteps[i];
        verifySlavePartitioningConfiguration( transMeta, stepMeta, clusterSchema, slaveServer );
      }
    } else {
      transMeta.setName( originalTransformation.getName() + " (master)" );

      NotePadMeta masterNote =
        new NotePadMeta( "This is a generated master transformation.\nIt will be run on server: "
          + getMasterServer(), 0, 0, -1, -1 );
      transMeta.addNote( masterNote );
    }

    // Copy the cluster schemas
    //
    for ( ClusterSchema schema : originalTransformation.getClusterSchemas() ) {
      transMeta.getClusterSchemas().add( schema.clone() );
    }

    transMeta.setDatabases( originalTransformation.getDatabases() );

    // Feedback
    transMeta.setFeedbackShown( originalTransformation.isFeedbackShown() );
    transMeta.setFeedbackSize( originalTransformation.getFeedbackSize() );

    // Priority management
    transMeta.setUsingThreadPriorityManagment( originalTransformation.isUsingThreadPriorityManagment() );

    // Unique connections
    transMeta.setUsingUniqueConnections( originalTransformation.isUsingUniqueConnections() );

    // Repository
    transMeta.setRepository( originalTransformation.getRepository() );
    transMeta.setRepositoryDirectory( originalTransformation.getRepositoryDirectory() );

    // Also set the logging details...
    transMeta.setTransLogTable( (TransLogTable) originalTransformation.getTransLogTable().clone() );

    // Rowset size
    transMeta.setSizeRowset( originalTransformation.getSizeRowset() );

    return transMeta;
  }

  private void verifySlavePartitioningConfiguration( TransMeta slave, StepMeta stepMeta,
    ClusterSchema clusterSchema, SlaveServer slaveServer ) {
    Map<StepMeta, String> stepPartitionFlag = slaveStepPartitionFlag.get( slave );
    if ( stepPartitionFlag == null ) {
      stepPartitionFlag = new Hashtable<StepMeta, String>();
      slaveStepPartitionFlag.put( slave, stepPartitionFlag );
    }
    if ( stepPartitionFlag.get( stepMeta ) != null ) {
      return; // already done;
    }

    StepPartitioningMeta partitioningMeta = stepMeta.getStepPartitioningMeta();
    if ( partitioningMeta != null
      && partitioningMeta.getMethodType() != StepPartitioningMeta.PARTITIONING_METHOD_NONE
      && partitioningMeta.getPartitionSchema() != null ) {
      // Find the schemaPartitions map to use
      Map<PartitionSchema, List<String>> schemaPartitionsMap = slaveServerPartitionsMap.get( slaveServer );
      if ( schemaPartitionsMap != null ) {
        PartitionSchema partitionSchema = partitioningMeta.getPartitionSchema();
        List<String> partitionsList = schemaPartitionsMap.get( partitionSchema );
        if ( partitionsList != null ) {
          // We found a list of partitions, now let's create a new partition schema with this data.
          String targetSchemaName = createSlavePartitionSchemaName( partitionSchema.getName() );
          PartitionSchema targetSchema = slave.findPartitionSchema( targetSchemaName );
          if ( targetSchema == null ) {
            targetSchema = new PartitionSchema( targetSchemaName, partitionsList );
            slave.getPartitionSchemas().add( targetSchema ); // add it to the slave if it doesn't exist.
          }
        }
      }
    }

    stepPartitionFlag.put( stepMeta, "Y" ); // is done.
  }

  public static String createSlavePartitionSchemaName( String name ) {
    return name;
  }

  private static final String STRING_TARGET_PARTITION_NAME_SUFFIX = " (target)";

  public static String createTargetPartitionSchemaName( String name ) {
    return name + STRING_TARGET_PARTITION_NAME_SUFFIX;
  }

  public static String createPartitionSchemaNameFromTarget( String targetName ) {
    if ( targetName.endsWith( STRING_TARGET_PARTITION_NAME_SUFFIX ) ) {
      return targetName.substring( 0, targetName.length() - STRING_TARGET_PARTITION_NAME_SUFFIX.length() );
    } else {
      return targetName;
    }
  }

  /**
   * @return the master
   */
  public TransMeta getMaster() {
    return masterTransMeta;
  }

  /**
   * @return the slaveTransMap : the mapping between a slaveServer and the transformation
   *
   */
  public Map<SlaveServer, TransMeta> getSlaveTransMap() {
    return slaveTransMap;
  }

  public TransMeta[] getSlaves() {
    Collection<TransMeta> collection = slaveTransMap.values();
    return collection.toArray( new TransMeta[collection.size()] );
  }

  public SlaveServer[] getSlaveTargets() {
    Set<SlaveServer> set = slaveTransMap.keySet();
    return set.toArray( new SlaveServer[set.size()] );
  }

  public SlaveServer getMasterServer() throws KettleException {
    StepMeta[] steps = originalTransformation.getStepsArray();
    for ( int i = 0; i < steps.length; i++ ) {
      ClusterSchema clusterSchema = steps[i].getClusterSchema();
      if ( clusterSchema != null ) {
        return clusterSchema.findMaster();
      }
    }
    throw new KettleException( "No master server could be found in the original transformation" );
  }

  public void splitOriginalTransformation() throws KettleException {
    clear();
    // Mixing clusters is not supported at the moment
    // Perform some basic checks on the cluster configuration.
    //
    findUsedOriginalSteps();
    checkClusterConfiguration();
    generateSlavePartitionSchemas();

    try {
      SlaveServer masterSlaveServer = getMasterServer();
      masterTransMeta = getOriginalCopy( false, null, null );
      ClusterSchema clusterSchema = originalTransformation.findFirstUsedClusterSchema();
      List<SlaveServer> slaveServers = clusterSchema.getSlaveServers();
      int nrSlavesNodes = clusterSchema.findNrSlaves();

      boolean encrypt = false;
      byte[] transformationKey = null;
      PublicKey pubK = null;
      if ( encrypt ) {
        KeyPair pair = CertificateGenEncryptUtil.generateKeyPair();
        pubK = pair.getPublic();
        PrivateKey privK = pair.getPrivate();

        Key key1 = CertificateGenEncryptUtil.generateSingleKey();
        try {
          transformationKey = CertificateGenEncryptUtil.encodeKeyForTransmission( privK, key1 );
        } catch ( InvalidKeyException ex ) {
          masterTransMeta.getLogChannel().logError( "Invalid key was used for encoding", ex );
        } catch ( IllegalBlockSizeException ex ) {
          masterTransMeta.getLogChannel().logError( "Error happenned during key encoding", ex );
        } catch ( Exception ex ) {
          masterTransMeta.getLogChannel().logError( "Error happenned during encryption initialization", ex );
        }
      }

      for ( int r = 0; r < referenceSteps.length; r++ ) {
        StepMeta referenceStep = referenceSteps[r];
        List<StepMeta> prevSteps = originalTransformation.findPreviousSteps( referenceStep );
        int nrPreviousSteps = prevSteps.size();
        for ( int p = 0; p < nrPreviousSteps; p++ ) {
          StepMeta previousStep = prevSteps.get( p );

          if ( !referenceStep.isClustered() ) {
            if ( !previousStep.isClustered() ) {
              // No clustering involved here: just add the reference step to the master
              //
              StepMeta target = masterTransMeta.findStep( referenceStep.getName() );
              if ( target == null ) {
                target = (StepMeta) referenceStep.clone();
                masterTransMeta.addStep( target );
              }

              StepMeta source = masterTransMeta.findStep( previousStep.getName() );
              if ( source == null ) {
                source = (StepMeta) previousStep.clone();
                masterTransMeta.addStep( source );
              }

              // Add a hop too...
              //
              TransHopMeta masterHop = new TransHopMeta( source, target );
              masterTransMeta.addTransHop( masterHop );
            } else {
              // reference step is NOT clustered
              // Previous step is clustered
              // --> We read from the slave server using socket readers.
              // We need a reader for each slave server in the cluster
              //

              // Also add the reference step to the master. (cloned)
              //
              StepMeta masterStep = masterTransMeta.findStep( referenceStep.getName() );
              if ( masterStep == null ) {
                masterStep = (StepMeta) referenceStep.clone();
                masterStep.setLocation( masterStep.getLocation().x, masterStep.getLocation().y );
                masterTransMeta.addStep( masterStep );
              }

              Queue<Integer> masterStepCopyNumbers = new LinkedList<Integer>();
              for ( int i = 0; i < masterStep.getCopies(); i++ ) {
                masterStepCopyNumbers.add( i );
              }

              // Then add the remote input/output steps to master and slave
              //
              for ( int slaveNr = 0; slaveNr < slaveServers.size(); slaveNr++ ) {
                SlaveServer sourceSlaveServer = slaveServers.get( slaveNr );

                if ( !sourceSlaveServer.isMaster() ) {
                  // MASTER: add remote input steps to the master step. That way it can receive data over sockets.
                  //

                  // SLAVE : add remote output steps to the previous step
                  //
                  TransMeta slave = getSlaveTransformation( clusterSchema, sourceSlaveServer );

                  // See if we can add a link to the previous using the Remote Steps concept.
                  //
                  StepMeta slaveStep = slave.findStep( previousStep.getName() );
                  if ( slaveStep == null ) {
                    slaveStep = addSlaveCopy( slave, previousStep, sourceSlaveServer );
                  }

                  // Make sure the data finds its way back to the master.
                  //
                  // Verify the partitioning for this slave step.
                  // It's running in 1 or more copies depending on the number of partitions
                  // Get the number of target partitions...
                  //
                  StepPartitioningMeta previousStepPartitioningMeta = previousStep.getStepPartitioningMeta();
                  PartitionSchema previousPartitionSchema = previousStepPartitioningMeta.getPartitionSchema();

                  int nrOfSourceCopies = determineNrOfStepCopies( sourceSlaveServer, previousStep );

                  // Verify that the number of copies is equal to the number of slaves or 1
                  // FIXME check this code, it no longer is relevant after the change to use determineNrOfStepCopies().
                  // It probably wasn't working before either.
                  //
                  if ( masterStep.getCopies() != 1 && masterStep.getCopies() != nrOfSourceCopies ) {
                    // this case might be handled correctly later
                    String message = BaseMessages.getString( PKG, "TransSplitter.Clustering.CopyNumberStep", nrSlavesNodes,
                        previousStep.getName(), masterStep.getName() );
                    throw new KettleException( message );
                  }

                  // Add the required remote input and output steps to make the partitioning a reality.
                  //
                  for ( int sourceCopyNr = 0; sourceCopyNr < nrOfSourceCopies; sourceCopyNr++ ) {
                    // The masterStepCopy number is increasing for each remote copy on each slave.
                    // This makes the master distribute to each copy of the slave properly.
                    // There is a check above to make sure that the master has either 1 copy or the same as slave*copies
                    Integer masterStepCopyNr = masterStepCopyNumbers.poll();
                    if ( masterStepCopyNr == null ) {
                      masterStepCopyNr = 0;
                    }

                    // We open a port on the various slave servers...
                    // So the source is the slave server, the target the master.
                    //
                    int port =
                      getPort(
                        clusterSchema, sourceSlaveServer, slaveStep.getName(), sourceCopyNr,
                        masterSlaveServer, masterStep.getName(), masterStepCopyNr );

                    RemoteStep remoteMasterStep =
                      new RemoteStep(
                        sourceSlaveServer.getHostname(), masterSlaveServer.getHostname(), Integer
                          .toString( port ), slaveStep.getName(), sourceCopyNr, masterStep.getName(),
                        masterStepCopyNr, sourceSlaveServer.getName(), masterSlaveServer.getName(),
                        socketsBufferSize, compressingSocketStreams, originalTransformation
                          .getStepFields( previousStep ) );
                    remoteMasterStep.setEncryptingStreams( encrypt );
                    remoteMasterStep.setKey( transformationKey );
                    masterStep.getRemoteInputSteps().add( remoteMasterStep );

                    RemoteStep remoteSlaveStep =
                      new RemoteStep(
                        sourceSlaveServer.getHostname(), masterSlaveServer.getHostname(), Integer
                          .toString( port ), slaveStep.getName(), sourceCopyNr, masterStep.getName(),
                        masterStepCopyNr, sourceSlaveServer.getName(), masterSlaveServer.getName(),
                        socketsBufferSize, compressingSocketStreams, originalTransformation
                          .getStepFields( previousStep ) );
                    remoteSlaveStep.setEncryptingStreams( encrypt );
                    remoteSlaveStep.setKey( transformationKey );
                    slaveStep.getRemoteOutputSteps().add( remoteSlaveStep );

                    // OK, create a partition number for the target step in the partition distribution...
                    //
                    if ( slaveStep.isPartitioned() ) {
                      slaveStepCopyPartitionDistribution.addPartition(
                        sourceSlaveServer.getName(), previousPartitionSchema.getName(), sourceCopyNr );
                    }
                  }

                  // Also set the target partitioning on the slave step.
                  // A copy of the original previous step partitioning schema
                  //
                  if ( referenceStep.isPartitioned() ) {

                    // Set the target partitioning schema for the source step (master)
                    //
                    StepPartitioningMeta stepPartitioningMeta = previousStepPartitioningMeta.clone();
                    PartitionSchema partitionSchema = stepPartitioningMeta.getPartitionSchema();
                    partitionSchema.setName( createTargetPartitionSchemaName( partitionSchema.getName() ) );

                    if ( partitionSchema.isDynamicallyDefined() ) {
                      // Expand the cluster definition to: nrOfSlaves*nrOfPartitionsPerSlave...
                      //
                      partitionSchema.expandPartitionsDynamically(
                        clusterSchema.findNrSlaves(), originalTransformation );
                    }
                    masterStep.setTargetStepPartitioningMeta( stepPartitioningMeta );
                    masterTransMeta.addOrReplacePartitionSchema( partitionSchema );

                    // Now set the partitioning schema for the slave step...
                    // For the slave step, we only should those partition IDs that are interesting for the current
                    // slave...
                    //
                    stepPartitioningMeta = previousStepPartitioningMeta.clone();
                    partitionSchema = stepPartitioningMeta.getPartitionSchema();
                    partitionSchema.setName( createSlavePartitionSchemaName( partitionSchema.getName() ) );

                    if ( partitionSchema.isDynamicallyDefined() ) {
                      // Expand the cluster definition to: nrOfSlaves*nrOfPartitionsPerSlave...
                      //
                      partitionSchema.expandPartitionsDynamically(
                        clusterSchema.findNrSlaves(), originalTransformation );
                    }

                    partitionSchema.retainPartitionsForSlaveServer(
                      clusterSchema.findNrSlaves(), getSlaveServerNumber( clusterSchema, sourceSlaveServer ) );
                    slave.addOrReplacePartitionSchema( partitionSchema );
                  }
                }
              }
            }
          } else {
            if ( !previousStep.isClustered() ) {
              // reference step is clustered
              // previous step is not clustered
              // --> Add a socket writer for each slave server
              //

              // MASTER : add remote output step to the previous step
              //
              StepMeta sourceStep = masterTransMeta.findStep( previousStep.getName() );
              if ( sourceStep == null ) {
                sourceStep = (StepMeta) previousStep.clone();
                sourceStep.setLocation( previousStep.getLocation().x, previousStep.getLocation().y );

                masterTransMeta.addStep( sourceStep );
              }

              Queue<Integer> masterStepCopyNumbers = new LinkedList<Integer>();
              for ( int i = 0; i < sourceStep.getCopies(); i++ ) {
                masterStepCopyNumbers.add( i );
              }

              for ( int s = 0; s < slaveServers.size(); s++ ) {
                SlaveServer targetSlaveServer = slaveServers.get( s );
                if ( !targetSlaveServer.isMaster() ) {
                  // SLAVE : add remote input step to the reference slave step...
                  //
                  TransMeta slaveTransMeta = getSlaveTransformation( clusterSchema, targetSlaveServer );

                  // also add the step itself.
                  StepMeta targetStep = slaveTransMeta.findStep( referenceStep.getName() );
                  if ( targetStep == null ) {
                    targetStep = addSlaveCopy( slaveTransMeta, referenceStep, targetSlaveServer );
                  }

                  // Verify the partitioning for this slave step.
                  // It's running in 1 or more copies depending on the number of partitions
                  // Get the number of target partitions...
                  //
                  StepPartitioningMeta targetStepPartitioningMeta = referenceStep.getStepPartitioningMeta();
                  PartitionSchema targetPartitionSchema = targetStepPartitioningMeta.getPartitionSchema();

                  int nrOfTargetCopies = determineNrOfStepCopies( targetSlaveServer, referenceStep );

                  // Verify that the number of copies is equal to the number of slaves or 1
                  // FIXME check this code, it no longer is relevant after the change to use determineNrOfStepCopies().
                  // It probably wasn't working before either.
                  //
                  // if (sourceStep.getCopies()!=1 && sourceStep.getCopies()!=nrOfTargetCopies) {
                  // throw new
                  // KettleException("The number of step copies on the master has to be 1 or equal
                  // to the number of slaves ("+nrSlavesNodes+") to work. Note that you can insert a dummy step to make
                  // the transformation work as desired.");
                  // }

                  // Add the required remote input and output steps to make the partitioning a reality.
                  //
                  for ( int targetCopyNr = 0; targetCopyNr < nrOfTargetCopies; targetCopyNr++ ) {
                    // The masterStepCopy number is increasing for each remote copy on each slave.
                    // This makes the master distribute to each copy of the slave properly.
                    // There is a check above to make sure that the master has either 1 copy or the same as slave*copies
                    Integer masterStepCopyNr = masterStepCopyNumbers.poll();
                    if ( masterStepCopyNr == null ) {
                      masterStepCopyNr = 0;
                    }

                    // The master step opens server socket ports
                    // So the IP address should be the same, in this case, the master...
                    //
                    int port =
                      getPort(
                        clusterSchema, masterSlaveServer, sourceStep.getName(), masterStepCopyNr,
                        targetSlaveServer, referenceStep.getName(), targetCopyNr );

                    RemoteStep remoteMasterStep =
                      new RemoteStep(
                        masterSlaveServer.getHostname(), targetSlaveServer.getHostname(), Integer
                          .toString( port ), sourceStep.getName(), masterStepCopyNr,
                        referenceStep.getName(), targetCopyNr, masterSlaveServer.getName(), targetSlaveServer
                          .getName(), socketsBufferSize, compressingSocketStreams, originalTransformation
                          .getStepFields( previousStep ) );
                    remoteMasterStep.setEncryptingStreams( encrypt );
                    remoteMasterStep.setKey( transformationKey );
                    sourceStep.getRemoteOutputSteps().add( remoteMasterStep );

                    RemoteStep remoteSlaveStep =
                      new RemoteStep(
                        masterSlaveServer.getHostname(), targetSlaveServer.getHostname(), Integer
                          .toString( port ), sourceStep.getName(), masterStepCopyNr,
                        referenceStep.getName(), targetCopyNr, masterSlaveServer.getName(), targetSlaveServer
                          .getName(), socketsBufferSize, compressingSocketStreams, originalTransformation
                          .getStepFields( previousStep ) );
                    remoteSlaveStep.setEncryptingStreams( encrypt );
                    remoteSlaveStep.setKey( transformationKey );
                    targetStep.getRemoteInputSteps().add( remoteSlaveStep );

                    // OK, create a partition number for the target step in the partition distribution...
                    //
                    if ( targetStep.isPartitioned() ) {
                      slaveStepCopyPartitionDistribution.addPartition(
                        targetSlaveServer.getName(), targetPartitionSchema.getName(), targetCopyNr );
                    }
                  }

                  // Also set the target partitioning on the master step.
                  // A copy of the original reference step partitioning schema
                  //
                  if ( targetStepPartitioningMeta.isPartitioned() ) {

                    // Set the target partitioning schema for the source step (master)
                    //
                    StepPartitioningMeta stepPartitioningMeta = targetStepPartitioningMeta.clone();
                    PartitionSchema partitionSchema = stepPartitioningMeta.getPartitionSchema();
                    partitionSchema.setName( createTargetPartitionSchemaName( partitionSchema.getName() ) );

                    if ( partitionSchema.isDynamicallyDefined() ) {
                      // Expand the cluster definition to: nrOfSlaves*nrOfPartitionsPerSlave...
                      //
                      partitionSchema.expandPartitionsDynamically(
                        clusterSchema.findNrSlaves(), originalTransformation );
                    }
                    sourceStep.setTargetStepPartitioningMeta( stepPartitioningMeta );
                    masterTransMeta.addOrReplacePartitionSchema( partitionSchema );

                    // Now set the partitioning schema for the slave step...
                    // For the slave step, we only should those partition IDs that are interesting for the current
                    // slave...
                    //
                    stepPartitioningMeta = targetStepPartitioningMeta.clone();
                    partitionSchema = stepPartitioningMeta.getPartitionSchema();
                    partitionSchema.setName( createSlavePartitionSchemaName( partitionSchema.getName() ) );

                    if ( partitionSchema.isDynamicallyDefined() ) {
                      // Expand the cluster definition to: nrOfSlaves*nrOfPartitionsPerSlave...
                      //
                      partitionSchema.expandPartitionsDynamically(
                        clusterSchema.findNrSlaves(), originalTransformation );
                    }

                    partitionSchema.retainPartitionsForSlaveServer(
                      clusterSchema.findNrSlaves(), getSlaveServerNumber( clusterSchema, targetSlaveServer ) );
                    slaveTransMeta.addOrReplacePartitionSchema( partitionSchema );
                  }
                }
              }
            } else {
              // reference step is clustered
              // previous step is clustered
              // --> Add reference step to the slave transformation(s)
              //
              for ( int slaveNr = 0; slaveNr < slaveServers.size(); slaveNr++ ) {
                SlaveServer targetSlaveServer = slaveServers.get( slaveNr );
                if ( !targetSlaveServer.isMaster() ) {
                  // SLAVE
                  TransMeta slaveTransMeta = getSlaveTransformation( clusterSchema, targetSlaveServer );

                  // This is the target step
                  //
                  StepMeta targetStep = slaveTransMeta.findStep( referenceStep.getName() );
                  if ( targetStep == null ) {
                    targetStep = addSlaveCopy( slaveTransMeta, referenceStep, targetSlaveServer );
                  }

                  // This is the source step
                  //
                  StepMeta sourceStep = slaveTransMeta.findStep( previousStep.getName() );
                  if ( sourceStep == null ) {
                    sourceStep = addSlaveCopy( slaveTransMeta, previousStep, targetSlaveServer );
                  }

                  // Add a hop between source and target
                  //
                  TransHopMeta slaveHop = new TransHopMeta( sourceStep, targetStep );
                  slaveTransMeta.addTransHop( slaveHop );

                  // Verify the partitioning
                  // That means is this case that it is possible that
                  //
                  // 1) the number of partitions is larger than the number of slaves
                  // 2) the partitioning method might change requiring the source step to do re-partitioning.
                  //
                  // We need to provide the source step with the information to re-partition correctly.
                  //
                  // Case 1: both source and target are partitioned on the same partition schema.
                  //
                  StepPartitioningMeta sourceStepPartitioningMeta = previousStep.getStepPartitioningMeta();
                  StepPartitioningMeta targetStepPartitioningMeta = referenceStep.getStepPartitioningMeta();

                  if ( previousStep.isPartitioned()
                    && referenceStep.isPartitioned()
                    && sourceStepPartitioningMeta.equals( targetStepPartitioningMeta ) ) {

                    // Just divide the partitions over the available slaves...
                    // set the appropriate partition schema for both step...
                    //
                    StepPartitioningMeta stepPartitioningMeta = sourceStepPartitioningMeta.clone();
                    PartitionSchema partitionSchema = stepPartitioningMeta.getPartitionSchema();
                    partitionSchema.setName( createSlavePartitionSchemaName( partitionSchema.getName() ) );

                    if ( partitionSchema.isDynamicallyDefined() ) {
                      partitionSchema.expandPartitionsDynamically(
                        clusterSchema.findNrSlaves(), originalTransformation );
                    }
                    partitionSchema.retainPartitionsForSlaveServer(
                      clusterSchema.findNrSlaves(), getSlaveServerNumber( clusterSchema, targetSlaveServer ) );

                    sourceStep.setStepPartitioningMeta( stepPartitioningMeta );
                    targetStep.setStepPartitioningMeta( stepPartitioningMeta );
                    slaveTransMeta.addOrReplacePartitionSchema( partitionSchema );
                  } else if ( ( !previousStep.isPartitioned() && referenceStep.isPartitioned() )
                    || ( previousStep.isPartitioned() && referenceStep.isPartitioned() && !sourceStepPartitioningMeta
                      .equals( targetStep.getStepPartitioningMeta() ) ) ) {

                    // Case 2: both source and target are partitioned on a different partition schema.
                    // Case 3: source is not partitioned, target is partitioned.
                    //
                    // --> This means that we're re-partitioning!!
                    //

                    PartitionSchema targetPartitionSchema = targetStepPartitioningMeta.getPartitionSchema();
                    PartitionSchema sourcePartitionSchema = sourceStepPartitioningMeta.getPartitionSchema();

                    // Since the source step is running clustered, it's running in a number of slaves, one copy each.
                    // All these source steps need to be able to talk to all the other target steps...
                    // If there are N slaves, there are N source steps and N target steps.
                    // We need to add N-1 remote output and input steps to the source and target step.
                    // This leads to Nx(N-1) extra data paths.
                    //
                    // Let's see if we can find them...
                    //
                    for ( int partSlaveNr = 0; partSlaveNr < slaveServers.size(); partSlaveNr++ ) {
                      SlaveServer sourceSlaveServer = slaveServers.get( partSlaveNr );
                      if ( !sourceSlaveServer.isMaster() ) {

                        // It's running in 1 or more copies depending on the number of partitions
                        // Get the number of target partitions...
                        //
                        Map<PartitionSchema, List<String>> partitionsMap =
                          slaveServerPartitionsMap.get( sourceSlaveServer );
                        int nrOfTargetPartitions = 1;
                        if ( targetStep.isPartitioned() && targetPartitionSchema != null ) {
                          List<String> targetPartitionsList = partitionsMap.get( targetPartitionSchema );
                          nrOfTargetPartitions = targetPartitionsList.size();
                        } else if ( targetStep.getCopies() > 1 ) {
                          nrOfTargetPartitions = targetStep.getCopies();
                        }

                        // Get the number of source partitions...
                        //
                        int nrOfSourcePartitions = 1;
                        if ( sourceStep.isPartitioned() && sourcePartitionSchema != null ) {
                          List<String> sourcePartitionsList = partitionsMap.get( sourcePartitionSchema );
                          nrOfSourcePartitions = sourcePartitionsList.size();
                        } else if ( sourceStep.getCopies() > 1 ) {
                          nrOfSourcePartitions = sourceStep.getCopies();
                        }

                        // We can't just specify 0 as the target/source copy number. At runtime we could have multiple
                        // copies of a step running for multiple partitions.
                        // Those ports will have to be allocated too.
                        //
                        // The port: steps A-->B
                        // A is clustered and so is B
                        //
                        // So the data has to be re-partitioned.
                        //
                        // A0->B1, A0->B2, A0->B3, A0->B4
                        // A1->B0, A1->B2, A1->B3, A1->B4
                        // A2->B0, A2->B1, A2->B3, A2->B4
                        // A3->B0, A3->B1, A3->B2, A3->B4
                        // A4->B0, A4->B1, A4->B2, A4->B3
                        //
                        // Where the 0 in A0 specifies the source slave server
                        // Where the 0 in B0 specified the target slave server
                        //
                        // So all in all, we need to allocate Nx(N-1) ports.
                        //
                        for ( int sourceCopyNr = 0; sourceCopyNr < nrOfSourcePartitions; sourceCopyNr++ ) {
                          for ( int targetCopyNr = 0; targetCopyNr < nrOfTargetPartitions; targetCopyNr++ ) {

                            if ( !targetSlaveServer.equals( sourceSlaveServer ) ) {
                              // We hit only get the remote steps, NOT the local ones.
                              // That's why it's OK to generate all combinations.
                              //
                              int outPort =
                                getPort(
                                  clusterSchema, targetSlaveServer, sourceStep.getName(), sourceCopyNr,
                                  sourceSlaveServer, targetStep.getName(), targetCopyNr );
                              RemoteStep remoteOutputStep =
                                new RemoteStep(
                                  targetSlaveServer.getHostname(), sourceSlaveServer.getHostname(), Integer
                                    .toString( outPort ), sourceStep.getName(), sourceCopyNr, targetStep
                                    .getName(), targetCopyNr, targetSlaveServer.getName(), sourceSlaveServer
                                    .getName(), socketsBufferSize, compressingSocketStreams,
                                  originalTransformation.getStepFields( previousStep ) );
                              remoteOutputStep.setEncryptingStreams( encrypt );
                              remoteOutputStep.setKey( transformationKey );
                              sourceStep.getRemoteOutputSteps().add( remoteOutputStep );

                              // OK, so the source step is sending rows out on the reserved ports
                              // What we need to do now is link all the OTHER slaves up to them.
                              //
                              int inPort =
                                getPort(
                                  clusterSchema, sourceSlaveServer, sourceStep.getName(), sourceCopyNr,
                                  targetSlaveServer, targetStep.getName(), targetCopyNr );
                              RemoteStep remoteInputStep =
                                new RemoteStep(
                                  sourceSlaveServer.getHostname(), targetSlaveServer.getHostname(), Integer
                                    .toString( inPort ), sourceStep.getName(), sourceCopyNr, targetStep
                                    .getName(), targetCopyNr, sourceSlaveServer.getName(), targetSlaveServer
                                    .getName(), socketsBufferSize, compressingSocketStreams,
                                  originalTransformation.getStepFields( previousStep ) );
                              remoteInputStep.setEncryptingStreams( encrypt );
                              remoteInputStep.setKey( transformationKey );
                              targetStep.getRemoteInputSteps().add( remoteInputStep );
                            }
                            // OK, save the partition number for the target step in the partition distribution...
                            //
                            slaveStepCopyPartitionDistribution.addPartition(
                              sourceSlaveServer.getName(), targetPartitionSchema.getName(), targetCopyNr );
                          }
                        }

                        // Set the target partitioning schema on the source step so that we can use that in the
                        // transformation...
                        // On the one hand we can't keep all partitions, otherwise the slave transformations start up N
                        // copies for N partitions.
                        // On the other hand we need the information to repartition.
                        //

                        if ( sourceStepPartitioningMeta.isPartitioned() ) {
                          // Set the correct partitioning schema for the source step.
                          //
                          // Set the target partitioning schema for the target step (slave)
                          //
                          StepPartitioningMeta stepPartitioningMeta = sourceStepPartitioningMeta.clone();
                          PartitionSchema partitionSchema = stepPartitioningMeta.getPartitionSchema();
                          partitionSchema.setName( createSlavePartitionSchemaName( partitionSchema.getName() ) );

                          if ( partitionSchema.isDynamicallyDefined() ) {
                            // Expand the cluster definition to: nrOfSlaves*nrOfPartitionsPerSlave...
                            //
                            partitionSchema.expandPartitionsDynamically(
                              clusterSchema.findNrSlaves(), originalTransformation );
                          }

                          partitionSchema.retainPartitionsForSlaveServer(
                            clusterSchema.findNrSlaves(),
                            getSlaveServerNumber( clusterSchema, targetSlaveServer ) );

                          sourceStep.setStepPartitioningMeta( stepPartitioningMeta );
                          slaveTransMeta.addOrReplacePartitionSchema( partitionSchema );

                        }

                        if ( targetStepPartitioningMeta.isPartitioned() ) {

                          // Set the target partitioning schema for the target step (slave)
                          //
                          StepPartitioningMeta stepPartitioningMeta = targetStepPartitioningMeta.clone();
                          PartitionSchema partitionSchema = stepPartitioningMeta.getPartitionSchema();
                          partitionSchema.setName( createSlavePartitionSchemaName( partitionSchema.getName() ) );

                          if ( partitionSchema.isDynamicallyDefined() ) {
                            partitionSchema.expandPartitionsDynamically(
                              clusterSchema.findNrSlaves(), originalTransformation );
                          }
                          partitionSchema.retainPartitionsForSlaveServer(
                            clusterSchema.findNrSlaves(),
                            getSlaveServerNumber( clusterSchema, targetSlaveServer ) );

                          targetStep.setStepPartitioningMeta( stepPartitioningMeta );
                          slaveTransMeta.addOrReplacePartitionSchema( partitionSchema );
                        }

                        // If we're re-partitioning, set the target step partitioning meta data on the source step
                        //
                        if ( !sourceStepPartitioningMeta.isPartitioned()
                          || !sourceStepPartitioningMeta.equals( targetStepPartitioningMeta ) ) {

                          // Not partitioned means the target is partitioned.
                          // Set the target partitioning on the source...

                          // Set the correct partitioning schema for the source step.
                          //
                          // Set the target partitioning schema for the target step (slave)
                          //
                          StepPartitioningMeta stepPartitioningMeta = targetStepPartitioningMeta.clone();
                          PartitionSchema partitionSchema = stepPartitioningMeta.getPartitionSchema();
                          partitionSchema.setName( createTargetPartitionSchemaName( partitionSchema.getName() ) );

                          if ( partitionSchema.isDynamicallyDefined() ) {
                            // Expand the cluster definition to: nrOfSlaves*nrOfPartitionsPerSlave...
                            //
                            partitionSchema.expandPartitionsDynamically(
                              clusterSchema.findNrSlaves(), originalTransformation );
                          }

                          sourceStep.setTargetStepPartitioningMeta( stepPartitioningMeta );
                          slaveTransMeta.addOrReplacePartitionSchema( partitionSchema );
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }

        if ( nrPreviousSteps == 0 ) {
          if ( !referenceStep.isClustered() ) {
            // Not clustered, simply add the step.
            if ( masterTransMeta.findStep( referenceStep.getName() ) == null ) {
              masterTransMeta.addStep( (StepMeta) referenceStep.clone() );
            }
          } else {
            for ( int s = 0; s < slaveServers.size(); s++ ) {
              SlaveServer slaveServer = slaveServers.get( s );

              if ( !slaveServer.isMaster() ) {
                // SLAVE
                TransMeta slave = getSlaveTransformation( clusterSchema, slaveServer );
                if ( slave.findStep( referenceStep.getName() ) == null ) {
                  addSlaveCopy( slave, referenceStep, slaveServer );
                }
              }
            }
          }
        }
      }

      // This block of code, this loop, checks the informational steps. (yellow hops).
      //
      for ( int i = 0; i < referenceSteps.length; i++ ) {
        StepMeta originalStep = referenceSteps[i];

        // Also take care of the info steps...
        // For example: StreamLookup, Table Input, etc.
        //
        StepMeta[] infoSteps = originalTransformation.getInfoStep( originalStep );
        for ( int p = 0; infoSteps != null && p < infoSteps.length; p++ ) {
          StepMeta infoStep = infoSteps[p];
          if ( infoStep != null ) {
            if ( !originalStep.isClustered() ) {
              if ( !infoStep.isClustered() ) {
                // No clustering involved here: just add a link between the reference step and the infostep
                //
                StepMeta target = masterTransMeta.findStep( originalStep.getName() );
                StepMeta source = masterTransMeta.findStep( infoStep.getName() );

                // Add a hop too...
                TransHopMeta masterHop = new TransHopMeta( source, target );
                masterTransMeta.addTransHop( masterHop );
              } else {
                // reference step is NOT clustered
                // Previous step is clustered
                // --> We read from the slave server using socket readers.
                // We need a reader for each slave server in the cluster
                // On top of that we need to merge the data from all these steps using a dummy. (to make sure)
                // That dummy needs to feed into Merge Join
                //
                int nrSlaves = clusterSchema.getSlaveServers().size();
                for ( int s = 0; s < nrSlaves; s++ ) {
                  SlaveServer sourceSlaveServer = clusterSchema.getSlaveServers().get( s );

                  if ( !sourceSlaveServer.isMaster() ) {
                    // //////////////////////////////////////////////////////////////////////////////////////////
                    // On the SLAVES: add a socket writer...
                    //
                    TransMeta slave = getSlaveTransformation( clusterSchema, sourceSlaveServer );

                    SocketWriterMeta socketWriterMeta = new SocketWriterMeta();
                    int port =
                      getPort(
                        clusterSchema, sourceSlaveServer, infoStep.getName(), 0, masterSlaveServer,
                        originalStep.getName(), 0 );
                    socketWriterMeta.setPort( "" + port );
                    socketWriterMeta.setBufferSize( clusterSchema.getSocketsBufferSize() );
                    socketWriterMeta.setFlushInterval( clusterSchema.getSocketsFlushInterval() );
                    socketWriterMeta.setCompressed( clusterSchema.isSocketsCompressed() );

                    StepMeta writerStep =
                      new StepMeta( getWriterName(
                        clusterSchema, sourceSlaveServer, infoStep.getName(), 0, masterSlaveServer,
                        originalStep.getName(), 0 ), socketWriterMeta );
                    writerStep.setLocation( infoStep.getLocation().x + 50, infoStep.getLocation().y + 50 );
                    writerStep.setDraw( true );

                    slave.addStep( writerStep );

                    // We also need to add a hop between infoStep and the new writer step
                    //
                    TransHopMeta slaveHop = new TransHopMeta( infoStep, writerStep );
                    if ( slave.findTransHop( slaveHop ) == null ) {
                      slave.addTransHop( slaveHop );
                    }

                    // //////////////////////////////////////////////////////////////////////////////////////////
                    // On the MASTER : add a socket reader and a dummy step to merge the data...
                    //
                    SocketReaderMeta socketReaderMeta = new SocketReaderMeta();
                    socketReaderMeta.setPort( "" + port );
                    socketReaderMeta.setBufferSize( clusterSchema.getSocketsBufferSize() );
                    socketReaderMeta.setCompressed( clusterSchema.isSocketsCompressed() );

                    StepMeta readerStep =
                      new StepMeta( getReaderName(
                        clusterSchema, sourceSlaveServer, infoStep.getName(), 0, masterSlaveServer,
                        originalStep.getName(), 0 ), socketReaderMeta );
                    readerStep.setLocation( infoStep.getLocation().x, infoStep.getLocation().y
                      + ( s * FANOUT * 2 ) - ( nrSlaves * FANOUT / 2 ) );
                    readerStep.setDraw( true );

                    masterTransMeta.addStep( readerStep );

                    // Also add a single dummy step in the master that will merge the data from the slave
                    // transformations.
                    //
                    String dummyName = infoStep.getName();
                    StepMeta dummyStep = masterTransMeta.findStep( dummyName );
                    if ( dummyStep == null ) {
                      DummyTransMeta dummy = new DummyTransMeta();
                      dummyStep = new StepMeta( dummyName, dummy );
                      dummyStep.setLocation( infoStep.getLocation().x + ( SPLIT / 2 ), infoStep.getLocation().y );
                      dummyStep.setDraw( true );
                      dummyStep.setDescription( "This step merges the data from the various data streams coming "
                        + "from the slave transformations.\nIt does that right before it hits the step that "
                        + "reads from a specific (info) step." );

                      masterTransMeta.addStep( dummyStep );

                      // Now we need a hop from the dummy merge step to the actual target step (original step)
                      //
                      StepMeta masterTargetStep = masterTransMeta.findStep( originalStep.getName() );
                      TransHopMeta targetHop = new TransHopMeta( dummyStep, masterTargetStep );
                      masterTransMeta.addTransHop( targetHop );

                      // Set the master target step as an info step... (use the cloned copy)
                      //
                      String[] infoStepNames =
                        masterTargetStep.getStepMetaInterface().getStepIOMeta().getInfoStepnames();
                      if ( infoStepNames != null ) {
                        StepMeta[] is = new StepMeta[infoStepNames.length];
                        for ( int n = 0; n < infoStepNames.length; n++ ) {
                          is[n] = slave.findStep( infoStepNames[n] ); // OK, info steps moved to the slave steps
                          if ( infoStepNames[n].equals( infoStep.getName() ) ) {
                            // We want to replace this one with the reader step: that's where we source from now
                            infoSteps[n] = readerStep;
                          }
                        }
                        masterTargetStep.getStepMetaInterface().getStepIOMeta().setInfoSteps( infoSteps );
                      }
                    }

                    // Add a hop between the reader step and the dummy
                    //
                    TransHopMeta mergeHop = new TransHopMeta( readerStep, dummyStep );
                    if ( masterTransMeta.findTransHop( mergeHop ) == null ) {
                      masterTransMeta.addTransHop( mergeHop );
                    }
                  }
                }
              }
            } else {
              if ( !infoStep.isClustered() ) {
                // reference step is clustered
                // info step is not clustered
                // --> Add a socket writer for each slave server
                //
                for ( int s = 0; s < slaveServers.size(); s++ ) {
                  SlaveServer targetSlaveServer = slaveServers.get( s );

                  if ( !targetSlaveServer.isMaster() ) {
                    // MASTER
                    SocketWriterMeta socketWriterMeta = new SocketWriterMeta();
                    socketWriterMeta.setPort( ""
                      + getPort(
                        clusterSchema, masterSlaveServer, infoStep.getName(), 0, targetSlaveServer,
                        originalStep.getName(), 0 ) );
                    socketWriterMeta.setBufferSize( clusterSchema.getSocketsBufferSize() );
                    socketWriterMeta.setFlushInterval( clusterSchema.getSocketsFlushInterval() );
                    socketWriterMeta.setCompressed( clusterSchema.isSocketsCompressed() );

                    StepMeta writerStep =
                      new StepMeta( getWriterName(
                        clusterSchema, masterSlaveServer, infoStep.getName(), 0, targetSlaveServer,
                        originalStep.getName(), 0 ), socketWriterMeta );
                    writerStep.setLocation( originalStep.getLocation().x, originalStep.getLocation().y
                      + ( s * FANOUT * 2 ) - ( nrSlavesNodes * FANOUT / 2 ) );
                    writerStep.setDraw( originalStep.isDrawn() );

                    masterTransMeta.addStep( writerStep );

                    // The previous step: add a hop to it.
                    // It still has the original name as it is not clustered.
                    //
                    StepMeta previous = masterTransMeta.findStep( infoStep.getName() );
                    if ( previous == null ) {
                      previous = (StepMeta) infoStep.clone();
                      masterTransMeta.addStep( previous );
                    }
                    TransHopMeta masterHop = new TransHopMeta( previous, writerStep );
                    masterTransMeta.addTransHop( masterHop );

                    // SLAVE
                    TransMeta slave = getSlaveTransformation( clusterSchema, targetSlaveServer );

                    SocketReaderMeta socketReaderMeta = new SocketReaderMeta();
                    socketReaderMeta.setHostname( masterSlaveServer.getHostname() );
                    socketReaderMeta.setPort( ""
                      + getPort(
                        clusterSchema, masterSlaveServer, infoStep.getName(), 0, targetSlaveServer,
                        originalStep.getName(), 0 ) );
                    socketReaderMeta.setBufferSize( clusterSchema.getSocketsBufferSize() );
                    socketReaderMeta.setCompressed( clusterSchema.isSocketsCompressed() );

                    StepMeta readerStep =
                      new StepMeta( getReaderName(
                        clusterSchema, masterSlaveServer, infoStep.getName(), 0, targetSlaveServer,
                        originalStep.getName(), 0 ), socketReaderMeta );
                    readerStep.setLocation( originalStep.getLocation().x - ( SPLIT / 2 ), originalStep
                      .getLocation().y );
                    readerStep.setDraw( originalStep.isDrawn() );
                    slave.addStep( readerStep );

                    // also add the step itself.
                    StepMeta slaveStep = slave.findStep( originalStep.getName() );
                    if ( slaveStep == null ) {
                      slaveStep = addSlaveCopy( slave, originalStep, targetSlaveServer );
                    }

                    // And a hop from the
                    TransHopMeta slaveHop = new TransHopMeta( readerStep, slaveStep );
                    slave.addTransHop( slaveHop );

                    //
                    // Now we have to explain to the slaveStep that it has to source from previous
                    //
                    String[] infoStepNames = slaveStep.getStepMetaInterface().getStepIOMeta().getInfoStepnames();
                    if ( infoStepNames != null ) {
                      StepMeta[] is = new StepMeta[infoStepNames.length];
                      for ( int n = 0; n < infoStepNames.length; n++ ) {
                        is[n] = slave.findStep( infoStepNames[n] ); // OK, info steps moved to the slave steps
                        if ( infoStepNames[n].equals( infoStep.getName() ) ) {
                          // We want to replace this one with the reader step: that's where we source from now
                          infoSteps[n] = readerStep;
                        }
                      }
                      slaveStep.getStepMetaInterface().getStepIOMeta().setInfoSteps( infoSteps );
                    }
                  }
                }
              } else {
                /*
                 * // reference step is clustered // previous step is clustered // --> Add reference step to the slave
                 * transformation(s) //
                 */
                //
                // Now we have to explain to the slaveStep that it has to source from previous
                //
                for ( int s = 0; s < slaveServers.size(); s++ ) {
                  SlaveServer slaveServer = slaveServers.get( s );
                  if ( !slaveServer.isMaster() ) {
                    TransMeta slave = getSlaveTransformation( clusterSchema, slaveServer );
                    StepMeta slaveStep = slave.findStep( originalStep.getName() );
                    String[] infoStepNames = slaveStep.getStepMetaInterface().getStepIOMeta().getInfoStepnames();
                    if ( infoStepNames != null ) {
                      StepMeta[] is = new StepMeta[infoStepNames.length];
                      for ( int n = 0; n < infoStepNames.length; n++ ) {
                        is[n] = slave.findStep( infoStepNames[n] ); // OK, info steps moved to the slave steps

                        // Hang on... is there a hop to the previous step?
                        if ( slave.findTransHop( is[n], slaveStep ) == null ) {
                          TransHopMeta infoHop = new TransHopMeta( is[n], slaveStep );
                          slave.addTransHop( infoHop );
                        }
                      }
                      slaveStep.getStepMetaInterface().getStepIOMeta().setInfoSteps( infoSteps );
                    }
                  }
                }
              }
            }
          }
        }
      }

      // Also add the original list of partition schemas to the slave step copy partition distribution...
      //
      slaveStepCopyPartitionDistribution
        .setOriginalPartitionSchemas( originalTransformation.getPartitionSchemas() );
      // for (SlaveStepCopyPartitionDistribution.SlaveStepCopy slaveStepCopy :
      // slaveStepCopyPartitionDistribution.getDistribution().keySet()) {
      // int partition = slaveStepCopyPartitionDistribution.getPartition(slaveStepCopy.getSlaveServerName(),
      // slaveStepCopy.getPartitionSchemaName(), slaveStepCopy.getStepCopyNr());
      // System.out.println("slave step copy: slaveServer="+slaveStepCopy.getSlaveServerName()+
      // ", partition schema="+slaveStepCopy.getPartitionSchemaName()+
      // ", copynr="+slaveStepCopy.getStepCopyNr()+" ---> partition="+partition);
      // }

      // Get a hold of all the slave transformations & the master...
      // Assign the SAME slave-step-copy-partition distribution to all of them.
      // That way the slave transformations can figure out where to send data.
      //
      // Also clear the changed flag.
      //
      for ( TransMeta transMeta : slaveTransMap.values() ) {
        transMeta.setSlaveStepCopyPartitionDistribution( slaveStepCopyPartitionDistribution );
        if ( encrypt ) {
          transMeta.setKey( pubK.getEncoded() );
          transMeta.setPrivateKey( false );
        }
        transMeta.clearChanged();
      }
      // do not erase partitioning schema for master transformation
      // if some of steps is expected to run on master partitioned, that is the case
      // when partition schema should exists as 'local' partition schema instead of slave's remote one
      // see PDI-12766
      masterTransMeta.setPartitionSchemas( originalTransformation.getPartitionSchemas() );

      masterTransMeta.setSlaveStepCopyPartitionDistribution( slaveStepCopyPartitionDistribution );
      if ( encrypt ) {
        masterTransMeta.setKey( pubK.getEncoded() );
        masterTransMeta.setPrivateKey( !false );
      }
      masterTransMeta.clearChanged();

      // We're absolutely done here...
    } catch ( Exception e ) {
      throw new KettleException( "Unexpected problem while generating master transformation", e );
    }
  }

  /**
   * Calculate the number of step copies in a step.<br>
   * If a step is not running clustered, it's simply returning getCopies().<br>
   * If a step is clustered and not doing any partitioning, it's simply returning getCopies().<br>
   * If a step is clustered and partitioned, we need to look in the partitioning map for the specified slave server.<br>
   * That is because the number of copies can vary over the slaves. (5 partitions over 3 slaves for example)
   *
   * @param slaveServer
   *          the slave server
   * @param step
   *          the reference step
   * @return the number of step copies that we run.
   */
  private int determineNrOfStepCopies( SlaveServer slaveServer, StepMeta step ) {
    if ( !step.isClustered() ) {
      return step.getCopies();
    }
    if ( !step.isPartitioned() ) {
      return step.getCopies();
    }
    if ( slaveServer.isMaster() ) {
      return step.getCopies();
    }

    // Partitioned and clustered...
    //
    StepPartitioningMeta stepPartitioningMeta = step.getStepPartitioningMeta();
    PartitionSchema partitionSchema = stepPartitioningMeta.getPartitionSchema();

    Map<PartitionSchema, List<String>> partitionMap = slaveServerPartitionsMap.get( slaveServer );
    List<String> partitionList = partitionMap.get( partitionSchema );

    return partitionList.size();
  }

  private int getSlaveServerNumber( ClusterSchema clusterSchema, SlaveServer slaveServer ) throws KettleException {
    int index = 0;
    for ( SlaveServer check : clusterSchema.getSlaveServers() ) {
      if ( !check.isMaster() ) {
        if ( check.equals( slaveServer ) ) {
          return index;
        }
        index++;
      }
    }
    return -1;
  }

  /**
   * Create a copy of a step from the original transformation for use in the a slave transformation. If the step is
   * partitioned, the partitioning will be changed to "schemaName (slave)"
   *
   * @param stepMeta
   *          The step to copy / clone.
   * @return a copy of the specified step for use in a slave transformation.
   */
  private StepMeta addSlaveCopy( TransMeta transMeta, StepMeta stepMeta, SlaveServer slaveServer ) {
    StepMeta copy = (StepMeta) stepMeta.clone();
    if ( copy.isPartitioned() ) {
      StepPartitioningMeta stepPartitioningMeta = copy.getStepPartitioningMeta();
      PartitionSchema partitionSchema = stepPartitioningMeta.getPartitionSchema();
      String slavePartitionSchemaName = createSlavePartitionSchemaName( partitionSchema.getName() );
      PartitionSchema slaveSchema = transMeta.findPartitionSchema( slavePartitionSchemaName );
      if ( slaveSchema != null ) {
        stepPartitioningMeta.setPartitionSchema( slaveSchema );
      }
      // Always just start a single copy on the slave server...
      // Otherwise the confusion w.r.t. to partitioning & re-partitioning would be complete.
      //
      copy.setCopies( 1 );
    }

    // Remove the clustering information on the slave transformation step
    // We don't need it anymore, it only confuses.
    //
    copy.setClusterSchema( null );

    transMeta.addStep( copy );
    return copy;
  }

  private void findUsedOriginalSteps() {
    List<StepMeta> transHopSteps = originalTransformation.getTransHopSteps( false );
    referenceSteps = transHopSteps.toArray( new StepMeta[transHopSteps.size()] );
  }

  /**
   * We want to divide the available partitions over the slaves. Let's create a hashtable that contains the partition
   * schema's Since we can only use a single cluster, we can divide them all over a single set of slave servers.
   *
   * @throws KettleException
   */
  private void generateSlavePartitionSchemas() throws KettleException {
    slaveServerPartitionsMap = new Hashtable<SlaveServer, Map<PartitionSchema, List<String>>>();

    for ( int i = 0; i < referenceSteps.length; i++ ) {
      StepMeta stepMeta = referenceSteps[i];
      StepPartitioningMeta stepPartitioningMeta = stepMeta.getStepPartitioningMeta();

      if ( stepPartitioningMeta == null ) {
        continue;
      }
      if ( stepPartitioningMeta.getMethodType() == StepPartitioningMeta.PARTITIONING_METHOD_NONE ) {
        continue;
      }

      ClusterSchema clusterSchema = stepMeta.getClusterSchema();
      if ( clusterSchema == null ) {
        continue;
      }

      // Make a copy of the partition schema because we might change the object.
      // Let's not alter the original transformation.
      // The match is done on name, and the name is preserved in this case, so it should be safe to do so.
      // Also, all cloned steps re-match with the cloned schema name afterwards...
      //
      PartitionSchema partitionSchema = (PartitionSchema) stepPartitioningMeta.getPartitionSchema().clone();

      int nrSlaves = clusterSchema.findNrSlaves();
      if ( nrSlaves == 0 ) {
        continue; // no slaves: ignore this situation too
      }

      // Change the partitioning layout dynamically if the user requested this...
      //
      if ( partitionSchema.isDynamicallyDefined() ) {
        partitionSchema.expandPartitionsDynamically( nrSlaves, originalTransformation );
      }

      int nrPartitions = partitionSchema.getPartitionIDs().size();

      if ( nrPartitions < nrSlaves ) {
        throw new KettleException(
          "It doesn't make sense to have a partitioned, clustered step with less partitions ("
            + nrPartitions + ") than that there are slave servers (" + nrSlaves + ")" );
      }

      int slaveServerNr = 0;
      List<SlaveServer> slaveServers = clusterSchema.getSlaveServers();

      for ( int p = 0; p < nrPartitions; p++ ) {
        String partitionId = partitionSchema.getPartitionIDs().get( p );

        SlaveServer slaveServer = slaveServers.get( slaveServerNr );

        // Skip the master...
        //
        if ( slaveServer.isMaster() ) {
          slaveServerNr++;
          if ( slaveServerNr >= slaveServers.size() ) {
            slaveServerNr = 0; // re-start
          }
          slaveServer = slaveServers.get( slaveServerNr );
        }

        Map<PartitionSchema, List<String>> schemaPartitionsMap = slaveServerPartitionsMap.get( slaveServer );
        if ( schemaPartitionsMap == null ) {
          // Add the schema-partitions map to the the slave server
          //
          schemaPartitionsMap = new HashMap<PartitionSchema, List<String>>();
          slaveServerPartitionsMap.put( slaveServer, schemaPartitionsMap );
        }

        // See if we find a list of partitions
        //
        List<String> partitions = schemaPartitionsMap.get( partitionSchema );
        if ( partitions == null ) {
          partitions = new ArrayList<String>();
          schemaPartitionsMap.put( partitionSchema, partitions );
        }

        // Add the partition ID to the appropriate list
        //
        if ( partitions.indexOf( partitionId ) < 0 ) {
          partitions.add( partitionId );
        }

        // Switch to next slave.
        slaveServerNr++;
        if ( slaveServerNr >= clusterSchema.getSlaveServers().size() ) {
          slaveServerNr = 0; // re-start
        }
      }
    }
    // System.out.println("We have "+(slaveServerPartitionsMap.size())+" entries in the slave server partitions map");
  }

  public Map<TransMeta, String> getCarteObjectMap() {
    return carteObjectMap;
  }

  public String getClusteredRunId() {
    return clusteredRunId;
  }
}
