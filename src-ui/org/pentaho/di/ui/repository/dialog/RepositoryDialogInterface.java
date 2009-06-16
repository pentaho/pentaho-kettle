package org.pentaho.di.ui.repository.dialog;

import org.pentaho.di.repository.RepositoryMeta;

public interface RepositoryDialogInterface {

	/**
	 * Open the dialog
	 * @return the description of the repository
	 */
	public RepositoryMeta open();
}
