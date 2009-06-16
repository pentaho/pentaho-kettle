package org.pentaho.di.repository;

import org.pentaho.di.core.plugins.Plugin;

public class RepositoryPlugin extends Plugin<String> {

	public RepositoryPlugin(int type, String id, String description, String tooltip, String directory, String[] jarfiles, String icon_filename, String classname) {
		super(type, id, description, tooltip, directory, jarfiles, icon_filename, classname);
	}

}
