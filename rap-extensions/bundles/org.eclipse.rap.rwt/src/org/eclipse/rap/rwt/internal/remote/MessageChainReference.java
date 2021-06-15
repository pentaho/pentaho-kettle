/*******************************************************************************
 * Copyright (c) 2014 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.remote;


public class MessageChainReference {

  private MessageChainElement chain;

  public MessageChainReference( MessageChainElement chain ) {
    this.chain = chain;
  }

  public MessageFilterChain get() {
    synchronized( chain ) {
      return chain;
    }
  }

  public void add( MessageFilter filter ) {
    synchronized( chain ) {
      chain = new MessageChainElement( filter, chain );
    }
  }

  public void remove( MessageFilter filter ) {
    synchronized( chain ) {
      chain = chain.remove( filter );
    }
  }

}
