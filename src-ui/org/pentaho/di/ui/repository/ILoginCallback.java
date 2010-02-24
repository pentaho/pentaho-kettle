package org.pentaho.di.ui.repository;

import org.pentaho.di.repository.Repository;

/**
* This interface defines a Spoon Login callback.
* 
* @author rmansoor
*
*/
public interface ILoginCallback {

  /**
   * On a successful login to the repository, this method is invoked
   * @param repository
   */
  void onSuccess(Repository repository);
  /**
   * On a user cancelation from the repository login dialog, this
   * method is invoked
   */
  void onCancel();
  /**
   * On any error caught during the login process, this method is
   * invoked 
   * @param t
   */
  void onError(Throwable t);
}
