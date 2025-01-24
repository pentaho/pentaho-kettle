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


package org.pentaho.di.trans.steps.rest.analyzer;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.steps.rest.Rest;
import org.pentaho.di.trans.steps.rest.RestMeta;
import org.pentaho.metaverse.api.IAnalysisContext;
import org.pentaho.metaverse.api.analyzer.kettle.step.BaseStepExternalResourceConsumer;
import org.pentaho.metaverse.api.model.ExternalResourceInfoFactory;
import org.pentaho.metaverse.api.model.IExternalResourceInfo;
import org.pentaho.metaverse.api.model.WebServiceResourceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by rfellows on 5/11/15.
 */
public class RestClientExternalResourceConsumer extends BaseStepExternalResourceConsumer<Rest, RestMeta> {

  private static RestClientExternalResourceConsumer instance;

  @VisibleForTesting
  protected RestClientExternalResourceConsumer() {
  }

  public static RestClientExternalResourceConsumer getInstance() {
    if ( null == instance ) {
      instance = new RestClientExternalResourceConsumer();
    }
    return instance;
  }

  private Logger log = LoggerFactory.getLogger( RestClientExternalResourceConsumer.class );

  @Override
  public Collection<IExternalResourceInfo> getResourcesFromMeta( RestMeta meta, IAnalysisContext context ) {

    List<IExternalResourceInfo> resources = new ArrayList<>();

    if ( !meta.isUrlInField() ) {
      String url = meta.getUrl();

      WebServiceResourceInfo resourceInfo = createResourceInfo( url, meta );
      resources.add( resourceInfo );
    }

    return resources;
  }

  private WebServiceResourceInfo createResourceInfo( String url, RestMeta meta ) {

    WebServiceResourceInfo resourceInfo =
      (WebServiceResourceInfo) ExternalResourceInfoFactory.createURLResource( url, true );

    if ( !meta.isDynamicMethod() ) {
      resourceInfo.setMethod( meta.getMethod() );
    }
    if ( StringUtils.isNotEmpty( meta.getAttBodyField() ) ) {
      resourceInfo.setBody( meta.getAttBodyField() );
    }
    resourceInfo.setApplicationType( meta.getAttApplicationType() );
    return resourceInfo;
  }

  @Override
  public Collection<IExternalResourceInfo> getResourcesFromRow( Rest step, RowMetaInterface rowMeta, Object[] row ) {
    Set<IExternalResourceInfo> resources = new HashSet<>();

    RestMeta meta = (RestMeta) step.getStepMetaInterface();
    if ( meta == null ) {
      meta = (RestMeta) step.getStepMeta().getStepMetaInterface();
    }

    if ( meta != null ) {
      String url;
      String method;
      String body;

      try {
        if ( meta.isUrlInField() ) {
          url = rowMeta.getString( row, meta.getAttUrlField(), null );
        } else {
          url = meta.getUrl();
        }
        if ( StringUtils.isNotEmpty( url ) ) {
          WebServiceResourceInfo resourceInfo = createResourceInfo( url, meta );
          if ( ArrayUtils.isNotEmpty( meta.getHeaderField() ) ) {
            for ( int i = 0; i < meta.getHeaderField().length; i++ ) {
              String field = meta.getHeaderField()[ i ];
              String label = meta.getHeaderName()[ i ];
              resourceInfo.addHeader( label, rowMeta.getString( row, field, null ) );
            }
          }
          if ( ArrayUtils.isNotEmpty( meta.getParameterField() ) ) {
            for ( int i = 0; i < meta.getParameterField().length; i++ ) {
              String field = meta.getParameterField()[ i ];
              String label = meta.getParameterName()[ i ];
              resourceInfo.addParameter( label, rowMeta.getString( row, field, null ) );
            }
          }
          if ( meta.isDynamicMethod() ) {
            method = rowMeta.getString( row, meta.getAttMethodFieldName(), null );
            resourceInfo.setMethod( method );
          }

          if ( StringUtils.isNotEmpty( meta.getAttBodyField() ) ) {
            body = rowMeta.getString( row, meta.getAttBodyField(), null );
            resourceInfo.setBody( body );
          }

          resources.add( resourceInfo );
        }
      } catch ( KettleValueException e ) {
        // could not find a url on this row
        log.debug( e.getMessage(), e );
      }
    }
    return resources;
  }

  @Override
  public boolean isDataDriven( RestMeta meta ) {
    // this step is data driven no matter what.
    // either the url, method, body, headers, and/or parameters come from the previous step
    return true;
  }

  @Override
  public Class<RestMeta> getMetaClass() {
    return RestMeta.class;
  }
}
