/*******************************************************************************
 * Copyright (c) 2011 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.jface.internal.util;

import java.io.Serializable;

/*
 * Exists in RAP only.  Serializable version of the ListenerList from core.runtime.
 */
public class SerializableListenerList 
  extends org.eclipse.core.runtime.ListenerList 
  implements Serializable 
{

}
