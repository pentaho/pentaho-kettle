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
package org.pentaho.di.trans.steps.named.cluster;

import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.osgi.api.NamedClusterOsgi;
import org.pentaho.di.core.osgi.api.NamedClusterServiceOsgi;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.persist.MetaStoreFactory;

import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class provides centralized logic to embed NamedClusters used by transformations/jobs in the embeddedMetaStore.
 * There are two or three steps in implementing embeddedNamedClusters in support of a step/entry.
 * <p/>
 * 1) Make the getXml() method of the step call the the registerUrl method here with any url used
 * by step/entry which may contain a reference to a named cluster.  This class will parse the url and determine if a
 * named cluster is present.  If a named cluster is found it will be added to embeddedMetastore which will ultimately
 * be written to the ktr file.
 * <p/>
 * 2) All accesses to KettleVFS.getFileObject should use a signature that provides a VariableSpace.  This is because
 * VFS will be passed a key to the embeddedMetastore through this nameSpace.  Some steps already do this and some don't.
 * <p/>
 * 3) At run time call the passEmbeddedMetastoreKey method to set variable in the namespace used in step 2.
 *
 * Created by tkafalas on 7/14/2017.
 */
public class NamedClusterEmbedManager {

  public static final String NAMESPACE = "NamedClusters";
  private static final String URL_PATTERN = "(^[^:/?#]+):?//([^/?#]*)?([^?#]*)";
  private static final String VARIABLE_START = "${";

  private static final int PARSE_URL_SCHEME = 1;
  private static final int PARSE_URL_AUTHORITY = 2;  //will include port if any
  private static final int PARSE_URL_PATH = 3; //Will include query if any

  protected static MetaStoreFactory testMetaStoreFactory; //For unit tests
  private MetaStoreFactory<NamedClusterOsgi> embeddedMetaStoreFactory;
  private AbstractMeta meta;
  private LogChannelInterface log;
  private boolean addedAllClusters;
  private boolean addedAnyClusters;

  // Pool of Named Clusters that have been used in the embeddedMetastore, we keep them here so we can rebuild the
  // embedded metastore as needed when the nc: url is changed, even if the user doesn't have the named clusters
  // available.
  private HashMap<String, NamedClusterOsgi> namedClusterPool = new HashMap<String, NamedClusterOsgi>();


  /**
   * Class creates an embedded metastores for NamedClusters
   *
   * @param meta The TransMeta or JobMeta
   */
  public NamedClusterEmbedManager( AbstractMeta meta, LogChannelInterface log ) {
    this.meta = meta;
    this.log = log;
    NamedClusterServiceOsgi ncso = meta.getNamedClusterServiceOsgi();
    if ( ncso == null ) {
      //throw new IllegalArgumentException( "Meta does not contain a NamedClusterService" );
      embeddedMetaStoreFactory = null; // Should only happen from test classes
      return;
    }
    if ( testMetaStoreFactory == null ) {
      embeddedMetaStoreFactory =
        new MetaStoreFactory( ncso.getClusterTemplate().getClass(), meta.getEmbeddedMetaStore(), NAMESPACE );
    } else {
      embeddedMetaStoreFactory = testMetaStoreFactory;
    }
  }

  /**
   * If hc:// protocol is explicitly defined, check for a literal named Cluster.  If present, add that cluster to the
   * embedded meta.  If the cluster name starts with a variable, add all named clusters.
   * <p/>
   * if the url starts with a variable, embed all named clusters.
   *
   * @param urlString The Url of the file being accessed as stored by the transformation/job
   * @return True if all clusters added, false otherwise
   */
  public void registerUrl( String urlString ) {
    if ( urlString == null || addedAllClusters == true ) {
      return; //We got no url or already added all clusters so nothing to do.
    }
    if ( urlString.startsWith( VARIABLE_START ) ) {
      addAllClusters();
    }

    Pattern r = Pattern.compile( URL_PATTERN );
    Matcher m = r.matcher( urlString );
    if ( m.find() ) {
      String protocol = m.group( PARSE_URL_SCHEME );
      String clusterName = m.group( PARSE_URL_AUTHORITY );
      if ( "hc".equals( protocol ) ) {
        if ( clusterName.startsWith( VARIABLE_START ) ) {
          addAllClusters();
        }
        addClusterToMeta( clusterName );
      }
    }
  }

  /**
   * Clear the embedded metastore of any named clusters
   */
  public void clear() {
    NamedClusterServiceOsgi ncso = meta.getNamedClusterServiceOsgi();
    if ( ncso != null ) {  //Don't kill the embedded if we don't have the service to rebuild
      addedAllClusters = false;
      addedAnyClusters = false;
      // The embeddedMetaStoreFactory may be null if creating a brand new job and attempting to run before it ever
      // saved.
      if ( embeddedMetaStoreFactory != null ) {
        try {
          List<NamedClusterOsgi> list = embeddedMetaStoreFactory.getElements();
          for ( NamedClusterOsgi nc : list ) {
            namedClusterPool.put( nc.getName(), nc );
            embeddedMetaStoreFactory.deleteElement( nc.getName() );
          }
        } catch ( MetaStoreException e ) {
          logMetaStoreException( e );
        }
      }
    }
  }

  public boolean isAddedAnyClusters() {
    return addedAnyClusters;
  }

  public void addClusterToMeta( String clusterName ) {
    NamedClusterServiceOsgi ncso = meta.getNamedClusterServiceOsgi();
    if ( ncso != null ) {
      NamedClusterOsgi nc = ncso.getNamedClusterByName( clusterName, meta.getMetaStore() );
      if ( nc == null ) {
        nc = namedClusterPool.get( clusterName ); // The local metastore doesn't have it.  Recover from pool
      }
      if ( nc != null ) {
        addClusterToMeta( nc );
      }
    }
  }

  private void addClusterToMeta( NamedClusterOsgi nc ) {
    try {
      if ( embeddedMetaStoreFactory.loadElement( nc.getName() ) == null ) {
        embeddedMetaStoreFactory.saveElement( nc );
        addedAnyClusters = true;
      }
    } catch ( MetaStoreException e ) {
      logMetaStoreException( e );
    }
  }

  private void addAllClusters() {
    NamedClusterServiceOsgi ncso = meta.getNamedClusterServiceOsgi();
    if ( ncso != null && meta.getMetaStore() != null ) {
      try {
        List<String> list = ncso.listNames( meta.getMetaStore() );
        for ( String name : list ) {
          addClusterToMeta( name );
        }
        for ( NamedClusterOsgi nc : namedClusterPool.values() ) {
          if ( !list.contains( nc.getName() ) ) {
            addClusterToMeta( nc );
          }
        }
        addedAllClusters = true;
      } catch ( MetaStoreException e ) {
        logMetaStoreException( e );
      }
    }
  }

  private void logMetaStoreException( MetaStoreException e ) {
    if ( log.isError() ) {
      log.logError( "Could not embed NamedCluster Information in ktr/kjb", e );
    }
  }

  /**
   * Set the variable that will be used by Vfs to set the FileSystemOptions for the file system.
   * @param nameSpace  The namespace used by the getFileObject method to access VFS.
   * @param embeddedMetastoreProviderKey  The key to the embeddedMetastore from the AbstraceMeta
   */
  public void passEmbeddedMetastoreKey( VariableSpace nameSpace, String embeddedMetastoreProviderKey ) {
    if ( nameSpace != null ) {
      if ( embeddedMetastoreProviderKey != null ) {
        nameSpace.setVariable( "vfs.hc.embeddedMetastoreKey", embeddedMetastoreProviderKey );
      }
    }
  }
}

