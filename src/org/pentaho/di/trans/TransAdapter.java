package org.pentaho.di.trans;

import org.pentaho.di.core.exception.KettleException;

/**
 * Utility class to allow only certain methods of TransListener to be
 * overridden.
 * 
 * @author matt
 * 
 */
public class TransAdapter implements TransListener {

  public void transActive(Trans trans) {
  }

  public void transIdle(Trans trans) {
  }

  public void transFinished(Trans trans) throws KettleException {
  }

}
