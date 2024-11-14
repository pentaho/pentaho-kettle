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


package org.pentaho.di.trans.step.jms.context;

import org.pentaho.di.trans.step.jms.JmsDelegate;

import javax.jms.Destination;
import javax.jms.JMSContext;

import static org.pentaho.di.i18n.BaseMessages.getString;
import static org.pentaho.di.trans.step.jms.JmsConstants.PKG;
import static org.pentaho.di.trans.step.jms.context.JmsProvider.DestinationType.QUEUE;


public interface JmsProvider {

  boolean supports( ConnectionType type );

  JMSContext getContext( JmsDelegate meta );

  Destination getDestination( JmsDelegate meta );

  default boolean isQueue( JmsDelegate meta ) {
    return DestinationType.valueOf( meta.destinationType ).equals( QUEUE );
  }

  String getConnectionDetails( JmsDelegate meta );

  enum ConnectionType {
    ACTIVEMQ {
      @Override public String toString() {
        return getString( PKG, "JmsProvider.ActiveMQ" );
      }

      @Override public String getUrlHint() {
        return getString( PKG, "JmsProvider.ActiveMQUrlHint" );

      }
    },
    WEBSPHERE {
      @Override public String toString() {
        return getString( PKG, "JmsProvider.IBMMQ" );
      }

      @Override public String getUrlHint() {
        return getString( PKG, "JmsProvider.WSUrlHint" );

      }
    };

    public abstract String getUrlHint();
  }

  enum DestinationType {
    QUEUE,
    TOPIC
  }

}

