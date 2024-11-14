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


package org.pentaho.di.core.auth.core.impl;

import org.junit.Test;
import org.pentaho.di.core.auth.DelegatingKerberosConsumer;
import org.pentaho.di.core.auth.core.AuthenticationFactoryException;

public class DefaultAuthenticationConsumerFactoryTest {
  @Test( expected = AuthenticationFactoryException.class )
  public void testDefaultAuthenticationConsumerFactoryFailsWithMultipleConstructors() throws AuthenticationFactoryException {
    new DefaultAuthenticationConsumerFactory( TwoConstructorConsumer.class );
  }

  @Test( expected = AuthenticationFactoryException.class )
  public void testDefaultAuthenticationConsumerFactoryFailsWithWrongConstructorArgCount() throws AuthenticationFactoryException {
    new DefaultAuthenticationConsumerFactory( TwoConstructorArgConsumer.class );
  }

  @Test( expected = AuthenticationFactoryException.class )
  public void testDefaultAuthenticationConsumerFactoryFailsNoConsumeMethod() throws AuthenticationFactoryException {
    new DefaultAuthenticationConsumerFactory( NoConsumeConsumer.class );
  }

  @Test
  public void testDefaultAuthenticationConsumerFactorySucceedsWithConsumer() throws AuthenticationFactoryException {
    new DefaultAuthenticationConsumerFactory( DelegatingKerberosConsumer.class );
  }
}
