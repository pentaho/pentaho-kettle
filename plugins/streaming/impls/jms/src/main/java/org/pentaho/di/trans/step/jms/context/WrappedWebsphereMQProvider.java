/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.di.trans.step.jms.context;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import org.pentaho.di.trans.step.jms.JmsDelegate;

import javax.jms.Destination;
import javax.jms.JMSContext;

import static org.pentaho.di.i18n.BaseMessages.getString;
import static org.pentaho.di.trans.step.jms.JmsConstants.PKG;
import static org.pentaho.di.trans.step.jms.context.JmsProvider.ConnectionType.WEBSPHERE;

/**
 * This class is used to prevent blueprint from failing with
 * ClassNotFound when loading the WebsphereMQProvider, since
 * that class directly references IBMMQ classes which may not
 * be available.
 * Also makes it possible to give a message for users to
 * check whether they've installed the IBMMQ jars, comparable
 * to the older version of the jms plugin.
 */
public class WrappedWebsphereMQProvider implements JmsProvider {

  @SuppressWarnings( { "squid:S4738", "Guava" } )  //using guava memoize, so no gain switching to java Supplier
  private Supplier<JmsProvider> prov = Suppliers.memoize( this::getProvider );

  private JmsProvider getProvider() {
    try {
      Class.forName( "com.ibm.mq.jms.MQQueue", false, this.getClass().getClassLoader() );
      return new WebsphereMQProvider();
    } catch ( Exception e ) {
      throw new IllegalStateException( getString( PKG, "WrappedWebsphereMQProvider.ErrorLoadingClass" ) );
    }
  }

  @Override public boolean supports( ConnectionType type ) {
    return type == WEBSPHERE;
  }

  @Override public String getConnectionDetails( JmsDelegate meta ) {
    return prov.get().getConnectionDetails( meta );
  }

  @Override public JMSContext getContext( JmsDelegate meta ) {
    return prov.get().getContext( meta );
  }

  @Override public Destination getDestination( JmsDelegate meta ) {
    return prov.get().getDestination( meta );
  }
}
