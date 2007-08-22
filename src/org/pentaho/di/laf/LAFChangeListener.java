package org.pentaho.di.laf;

public interface LAFChangeListener<E> {
	public void notify(E changedObject);
}
