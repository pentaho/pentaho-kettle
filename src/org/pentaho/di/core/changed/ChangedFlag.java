/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
package org.pentaho.di.core.changed;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class ChangedFlag implements ChangedFlagInterface
{
	private List<PDIObserver> obs = Collections.synchronizedList(new ArrayList<PDIObserver>());

	private AtomicBoolean changed = new AtomicBoolean();

	public void addObserver(PDIObserver o)
	{
		if (o == null)
			throw new NullPointerException();
		if (!obs.contains(o))
		{
			obs.add(o);
		}
	}

	public synchronized void deleteObserver(PDIObserver o)
	{
		obs.remove(o);
	}

	public void notifyObservers(Object arg)
	{

		PDIObserver[] lobs;
		synchronized (this)
		{
			if (!changed.get())
				return;
			lobs = obs.toArray(new PDIObserver[obs.size()]);
			clearChanged();
		}
		for (int i = lobs.length - 1; i >= 0; i--)
			lobs[i].update(this, arg);
	}

	public void setChanged()
	{
		changed.set(true);
	}
	
	public void setChanged(boolean b)
	{
		changed.set(b);
	}

	public void clearChanged()
	{
		changed.set(false);
	}

	public synchronized boolean hasChanged()
	{
		return changed.get();
	}

}
