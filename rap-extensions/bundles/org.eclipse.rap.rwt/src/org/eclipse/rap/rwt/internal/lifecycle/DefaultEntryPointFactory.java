/*******************************************************************************
 * Copyright (c) 2011, 2015 Frank Appel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Frank Appel - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.lifecycle;

import java.lang.reflect.Modifier;

import org.eclipse.rap.rwt.application.EntryPoint;
import org.eclipse.rap.rwt.application.EntryPointFactory;
import org.eclipse.rap.rwt.internal.util.ClassUtil;
import org.eclipse.rap.rwt.internal.util.ParamCheck;


/**
 * A default entrypoint factory that creates entrypoint instances from a given
 * class. Note that this does not work with member classes, which cannot be
 * instantiated.
 *
 * @since 2.0
 */
public class DefaultEntryPointFactory implements EntryPointFactory {

  private final Class<? extends EntryPoint> type;

  /**
   * Creates a new entrypoint factory for the given class.
   *
   * @param type the entrypoint class, must not be an abstract class or a non-static inner class
   */
  public DefaultEntryPointFactory( Class<? extends EntryPoint> type ) {
    ParamCheck.notNull( type, "type" );

    checkType( type );
    this.type = type;
  }

  @Override
  public EntryPoint create() {
    EntryPoint instance;
    try {
      instance = ClassUtil.newInstance( type );
    } catch( Exception exception ) {
      String message = "Could not create entrypoint instance: " + type.getName();
      throw new RuntimeException( message, exception );
    }
    return instance;
  }

  private static void checkType( Class<? extends EntryPoint> type ) {
    if( type.isInterface() || Modifier.isAbstract( type.getModifiers() ) ) {
      throw new IllegalArgumentException( "Abstract class or interface given as entrypoint: "
                                          + type.getName() );
    }
    if( type.isMemberClass() && !Modifier.isStatic( type.getModifiers() ) ) {
      throw new IllegalArgumentException( "Non-static inner class given as entrypoint: "
                                          + type.getName() );
    }
  }

}
