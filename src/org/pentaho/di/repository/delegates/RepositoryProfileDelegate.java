package org.pentaho.di.repository.delegates;

import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.KettleDatabaseRepository;
import org.pentaho.di.repository.PermissionMeta;
import org.pentaho.di.repository.ProfileMeta;

public class RepositoryProfileDelegate extends BaseRepositoryDelegate {

	private static Class<?>	PKG	= ProfileMeta.class;	// for i18n purposes, needed by Translator2!! $NON-NLS-1$

	public RepositoryProfileDelegate(KettleDatabaseRepository repository) {
		super(repository);
	}

	public RowMetaAndData getProfile(long id_profile) throws KettleException
	{
		return repository.connectionDelegate.getOneRow(quoteTable(KettleDatabaseRepository.TABLE_R_PROFILE), quote(KettleDatabaseRepository.FIELD_PROFILE_ID_PROFILE), id_profile);
	}

	public synchronized long getProfileID(String profilename) throws KettleException
	{
		return repository.connectionDelegate.getIDWithValue(quoteTable(KettleDatabaseRepository.TABLE_R_PROFILE), quote(KettleDatabaseRepository.FIELD_PROFILE_ID_PROFILE), quote(KettleDatabaseRepository.FIELD_PROFILE_NAME), profilename);
	}

	public ProfileMeta loadProfileMeta(ProfileMeta profileMeta, long id_profile) throws KettleException {
		try {
			RowMetaAndData r = getProfile(id_profile);
			if (r != null) {
				profileMeta.setID(id_profile);
				profileMeta.setName(r.getString("NAME", null));
				profileMeta.setDescription(r.getString("DESCRIPTION", null));

				long pid[] = repository.getPermissionIDs(id_profile);
				profileMeta.removeAllPermissions();

				for (int i = 0; i < pid.length; i++) {
					PermissionMeta pi = repository.loadPermissionMeta(pid[i]);
					if (pi.getID() > 0) {
						profileMeta.addPermission(pi);
					}
				}

				return profileMeta;
			} else {
				throw new KettleException(BaseMessages.getString(PKG, "ProfileMeta.Error.NotFound", Long.toString(id_profile)));
			}
		} catch (KettleDatabaseException dbe) {
			throw new KettleException(BaseMessages.getString(PKG, "ProfileMeta.Error.NotCreated", Long.toString(id_profile)), dbe);
		}
	}

	public boolean saveProfileMeta(ProfileMeta profileMeta) throws KettleException {
		try {
			if (profileMeta.getID() <= 0) {
				profileMeta.setID(getProfileID(profileMeta.getName()));
			}

			if (profileMeta.getID() <= 0) // Insert...
			{
				profileMeta.setID(repository.connectionDelegate.getNextProfileID());

				// First save Profile info
				//
				repository.connectionDelegate.insertTableRow("R_PROFILE", fillTableRow(profileMeta));

				// Save permission-profile relations
				//
				saveProfilePermissions(profileMeta);
			} else // Update
			{
				// First save permissions
				//
				repository.connectionDelegate.updateTableRow("R_PROFILE", "ID_PROFILE", fillTableRow(profileMeta));

				// Then save profile_permission relationships
				//
				repository.delProfilePermissions(profileMeta.getID());

				// Save permission-profile relations
				//
				saveProfilePermissions(profileMeta);
			}
		} catch (KettleDatabaseException dbe) {
			throw new KettleException(BaseMessages.getString(PKG, "ProfileMeta.Error.NotSaved", Long.toString(profileMeta.getID())), dbe);
		}
		return true;
	}

	private void saveProfilePermissions(ProfileMeta profileMeta) throws KettleException {
		try {
			// Then save profile_permission relationships
			for (int i = 0; i < profileMeta.nrPermissions(); i++) {
				PermissionMeta pi = profileMeta.getPermission(i);
				long id_permission = repository.permissionDelegate.getPermissionID(pi.getTypeDesc());

				RowMetaAndData pr = new RowMetaAndData();
				pr.addValue(new ValueMeta("ID_PROFILE", ValueMetaInterface.TYPE_INTEGER), new Long(profileMeta.getID()));
				pr.addValue(new ValueMeta("ID_PERMISSION", ValueMetaInterface.TYPE_INTEGER), new Long(id_permission));

				repository.connectionDelegate.insertTableRow("R_PROFILE_PERMISSION", pr);
			}
		} catch (KettleDatabaseException dbe) {
			throw new KettleException(BaseMessages.getString(PKG, "ProfileMeta.Error.PermissionNotSaved", Long.toString(profileMeta.getID())), dbe);
		}
	}

	private RowMetaAndData fillTableRow(ProfileMeta profileMeta)
	{
		RowMetaAndData r = new RowMetaAndData();
		r.addValue(new ValueMeta("ID_PROFILE", ValueMetaInterface.TYPE_INTEGER), new Long(profileMeta.getID()));
		r.addValue(new ValueMeta("NAME", ValueMetaInterface.TYPE_STRING), profileMeta.getName());
		r.addValue(new ValueMeta("DESCRIPTION", ValueMetaInterface.TYPE_STRING), profileMeta.getDescription());
		
		return r;		
	}

	public synchronized int getNrProfiles() throws KettleException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_PROFILE);
		RowMetaAndData r = repository.connectionDelegate.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getInteger(0, 0L);
		}

		return retval;
	}


	public synchronized void renameProfile(long id_profile, String newname) throws KettleException
	{
		String sql = "UPDATE "+quoteTable(KettleDatabaseRepository.TABLE_R_PROFILE)+" SET "+quote(KettleDatabaseRepository.FIELD_PROFILE_NAME)+" = ? WHERE "+quote(KettleDatabaseRepository.FIELD_PROFILE_ID_PROFILE)+" = ?";

		RowMetaAndData table = new RowMetaAndData();
		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_PROFILE_NAME, ValueMetaInterface.TYPE_STRING), newname);
		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_PROFILE_ID_PROFILE, ValueMetaInterface.TYPE_INTEGER), new Long(id_profile));

		repository.connectionDelegate.getDatabase().execStatement(sql, table.getRowMeta(), table.getData());
	}

	
}
