package org.pentaho.di.ui.repository.dialog;

import org.pentaho.di.repository.RepositoryMeta;

public interface RepositoryDialogInterface {
  public static enum MODE {ADD, EDIT};
  /**
   * Open the dialog
   * @param mode (Add or Edit)
   * @return the description of the repository
   * @throws RepositoryAlreadyExistException
   */
	public RepositoryMeta open(final MODE mode);
}
