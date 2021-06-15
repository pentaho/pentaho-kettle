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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.eclipse.core.commands.common.EventManager;

/*
 * Exists in RAP only.  Serializable version of the EventManager from core.commands.
 */
public abstract class SerializableEventManager extends EventManager implements Serializable {

	private void writeObject(ObjectOutputStream stream) throws IOException {
		stream.writeObject(getListeners());
	}

	private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
		Object[] listeners = (Object[]) stream.readObject();
		for (int i = 0; i < listeners.length; i++) {
			addListenerObject(listeners[i]);
		}
	}

}
