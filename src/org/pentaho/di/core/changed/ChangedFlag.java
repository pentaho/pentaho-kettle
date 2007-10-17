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
