package org.pentaho.di.compatibility;

public interface ValueUsedListener {
	public void valueIsUsed(int index, Value value);
}
