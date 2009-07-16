package org.pentaho.di.repository.kdr.delegates;

import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.ProfileMeta;
import org.pentaho.di.repository.ProfileMeta.Permission;
import org.pentaho.di.repository.kdr.KettleDatabaseRepository;

public class KettleDatabaseRepositoryPermissionDelegate extends KettleDatabaseRepositoryBaseDelegate {

	private static Class<?> PKG = ProfileMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	public KettleDatabaseRepositoryPermissionDelegate(KettleDatabaseRepository repository) {
		super(repository);
	}
	
	public RowMetaAndData getPermission(ObjectId id_permission) throws KettleException
	{
		return repository.connectionDelegate.getOneRow(quoteTable(KettleDatabaseRepository.TABLE_R_PERMISSION), quote(KettleDatabaseRepository.FIELD_PERMISSION_ID_PERMISSION), id_permission);
	}
	
	public synchronized ObjectId getPermissionID(String code) throws KettleException
	{
		return repository.connectionDelegate.getIDWithValue(quoteTable(KettleDatabaseRepository.TABLE_R_PERMISSION), quote(KettleDatabaseRepository.FIELD_PERMISSION_ID_PERMISSION), quote(KettleDatabaseRepository.FIELD_PERMISSION_CODE), code);
	}

	/**
	 * Load a permission from the repository
	 * 
	 * @param id_permission The id of the permission to load
	 * @throws KettleException
	 */
	public Permission loadPermissionMeta(ObjectId id_permission) throws KettleException
	{
		try
		{
			RowMetaAndData r = getPermission(id_permission);
			String code = r.getString("CODE", null);
			return Permission.getPermissionWithCode(code);
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException(BaseMessages.getString(PKG, "PermissionMeta.Error.LoadPermisson", id_permission.toString()), dbe);
		}
	}


	public synchronized int getNrPermissions(long id_profile) throws KettleException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_PROFILE_PERMISSION)+" WHERE "+quote(KettleDatabaseRepository.FIELD_PROFILE_PERMISSION_ID_PROFILE)+" = " + id_profile;
		RowMetaAndData r = repository.connectionDelegate.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getInteger(0, 0L);
		}

		return retval;
	}

}
