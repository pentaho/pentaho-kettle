package org.pentaho.di.repository;

import java.util.Date;

/**
 * A version is simply a name, a commit comment and a date
 * 
 * @author matt
 *
 */
public interface ObjectVersion {

	public String getName();
	public Date getCreationDate();
	public String getComment();
	public String getLogin();
}
