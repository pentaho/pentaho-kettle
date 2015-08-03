package org.pentaho.di.trans.steps.baseinput;

import java.io.Closeable;

import org.pentaho.di.core.exception.KettleException;

/**
 * Content-based reader for file.
 */
public interface IBaseInputReader extends Closeable {
    boolean readRow() throws KettleException;
}
