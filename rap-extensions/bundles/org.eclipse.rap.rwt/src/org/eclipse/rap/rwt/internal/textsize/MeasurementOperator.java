/*******************************************************************************
 * Copyright (c) 2011, 2014 Frank Appel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Frank Appel - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.textsize;

import static org.eclipse.rap.rwt.internal.service.ContextProvider.getApplicationContext;
import static org.eclipse.rap.rwt.internal.service.ContextProvider.getProtocolWriter;
import static org.eclipse.rap.rwt.internal.textsize.MeasurementUtil.createItemParamObject;
import static org.eclipse.rap.rwt.internal.textsize.MeasurementUtil.createProbeParamObject;
import static org.eclipse.rap.rwt.internal.textsize.MeasurementUtil.getId;
import static org.eclipse.rap.rwt.remote.JsonMapping.readPoint;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.rap.json.JsonArray;
import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.json.JsonValue;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.internal.lifecycle.LifeCycleUtil;
import org.eclipse.rap.rwt.internal.lifecycle.ProcessActionRunner;
import org.eclipse.rap.rwt.internal.remote.ConnectionImpl;
import org.eclipse.rap.rwt.remote.AbstractOperationHandler;
import org.eclipse.rap.rwt.remote.RemoteObject;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.internal.SerializableCompatibility;


class MeasurementOperator implements SerializableCompatibility {

  static final String TYPE = "rwt.client.TextSizeMeasurement";
  static final String METHOD_MEASURE_ITEMS = "measureItems";
  static final String PARAM_ITEMS = "items";
  static final String METHOD_STORE_MEASUREMENTS = "storeMeasurements";
  static final String PARAM_RESULTS = "results";

  private final RemoteObject remoteObject;
  private final Set<Probe> probes;
  private final Set<MeasurementItem> items;

  MeasurementOperator() {
    ConnectionImpl connection = ( ConnectionImpl )RWT.getUISession().getConnection();
    remoteObject = connection.createServiceObject( TYPE );
    remoteObject.setHandler( new MeasurementOperatorHandler() );
    probes = new HashSet<>();
    items = new HashSet<>();
    addStartupProbesToBuffer();
  }

  private void addStartupProbesToBuffer() {
    Probe[] probeList = getApplicationContext().getProbeStore().getProbes();
    probes.addAll( Arrays.asList( probeList ) );
  }

  int getProbeCount() {
    return probes.size();
  }

  Probe[] getProbes() {
    return probes.toArray( new Probe[ probes.size() ] );
  }

  void addProbeToMeasure( FontData fontData ) {
    Probe probe = getApplicationContext().getProbeStore().getProbe( fontData );
    if( probe == null ) {
      probe = getApplicationContext().getProbeStore().createProbe( fontData );
    }
    probes.add( probe );
  }

  int getItemCount() {
    return items.size();
  }

  MeasurementItem[] getItems() {
    return items.toArray( new MeasurementItem[ items.size() ] );
  }

  void addItemToMeasure( MeasurementItem newItem ) {
    items.add( newItem );
  }

  void renderMeasurementItems() {
    Probe[] probes = getProbes();
    MeasurementItem[] items = getItems();
    if( probes.length > 0 || items.length > 0 ) {
      JsonArray itemsArray = new JsonArray();
      for( Probe probe : probes ) {
        itemsArray.add( createProbeParamObject( probe ) );
      }
      for( MeasurementItem item : items ) {
        itemsArray.add( createItemParamObject( item ) );
      }
      // [if] This call operation must be at the end of protocol message.
      // Render call operation directly to protocol writer (instead through remote object) as
      // this method is called after RemoteObjectLifeCycleAdapter.render() in DisplayLCA.render().
      JsonObject parameters = new JsonObject().add( PARAM_ITEMS, itemsArray );
      getProtocolWriter().appendCall( TYPE, METHOD_MEASURE_ITEMS, parameters );
    }
  }

  private final class MeasurementOperatorHandler extends AbstractOperationHandler {

    @Override
    public void handleCall( String method, JsonObject parameters ) {
      if( METHOD_STORE_MEASUREMENTS.equals( method ) ) {
        final JsonObject results = parameters.get( PARAM_RESULTS ).asObject();
        if( LifeCycleUtil.isStartup() ) {
          handleMeasuredFontProbeSizes( results );
        } else {
          ProcessActionRunner.add( new Runnable() {
            @Override
            public void run() {
              handleMeasuredFontProbeSizes( results );
              if( handleMeasuredTextSizes( results ) ) {
                TextSizeRecalculation.execute();
              }
            }
          } );
        }
      }
    }

    private void handleMeasuredFontProbeSizes( JsonObject results ) {
      Iterator<Probe> probeList = probes.iterator();
      while( probeList.hasNext() ) {
        Probe probe = probeList.next();
        Point size = readMeasuredSize( results, getId( probe ) );
        if( size != null ) {
          createProbeResult( probe, size );
          probeList.remove();
        }
      }
    }

    private boolean handleMeasuredTextSizes( JsonObject results ) {
      int originalItemsSize = items.size();
      Iterator<MeasurementItem> itemList = items.iterator();
      while( itemList.hasNext() ) {
        MeasurementItem item = itemList.next();
        Point size = readMeasuredSize( results, getId( item ) );
        if( size != null ) {
          storeTextMeasurement( item, size );
          itemList.remove();
        }
      }
      return originalItemsSize != items.size();
    }

    private Point readMeasuredSize( JsonObject results, String id ) {
      JsonValue value = results.get( id );
      if( value != null ) {
        return readPoint( value );
      }
      return null;
    }

    private void createProbeResult( Probe probe, Point size ) {
      ProbeResultStore.getInstance().createProbeResult( probe, size );
    }

    private void storeTextMeasurement( MeasurementItem item, Point size ) {
      FontData fontData = item.getFontData();
      String textToMeasure = item.getTextToMeasure();
      int wrapWidth = item.getWrapWidth();
      int mode = item.getMode();
      TextSizeStorageUtil.store( fontData, textToMeasure, wrapWidth, mode, size );
    }

  }

}
