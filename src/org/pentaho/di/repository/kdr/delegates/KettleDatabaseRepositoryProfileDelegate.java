package org.pentaho.di.repository.kdr.delegates;

import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.ProfileMeta;
import org.pentaho.di.repository.ProfileMeta.Permission;
import org.pentaho.di.repository.kdr.KettleDatabaseRepository;

public class KettleDatabaseRepositoryProfileDelegate extends KettleDatabaseRepositoryBaseDelegate {

    private static Class<?>	PKG	= ProfileMeta.class;  // for i18n purposes, needed by Translator2!! $NON-NLS-1$

	public KettleDatabaseRepositoryProfileDelegate(KettleDatabaseRepository repository) {
		super(repository);
	}

	public RowMetaAndData getProfile(ObjectId id_profile) throws KettleException
	{
		return repository.connectionDelegate.getOneRow(quoteTable(KettleDatabaseRepository.TABLE_R_PROFILE), quote(KettleDatabaseRepository.FIELD_PROFILE_ID_PROFILE), id_profile);
	}

	public synchronized ObjectId getProfileID(String profilename) throws KettleException
	{
		return repository.connectionDelegate.getIDWithValue(quoteTable(KettleDatabaseRepository.TABLE_R_PROFILE), quote(KettleDatabaseRepository.FIELD_PROFILE_ID_PROFILE), quote(KettleDatabaseRepository.FIELD_PROFILE_NAME), profilename);
	}

	public ProfileMeta loadProfileMeta(ProfileMeta profileMeta, ObjectId id_profile) throws KettleException {
		try {
			RowMetaAndData r = getProfile(id_profile);
			if (r != null) {
				profileMeta.setObjectId(id_profile);
				profileMeta.setName(r.getString("NAME", null));
				profileMeta.setDescription(r.getString("DESCRIPTION", null));

				ObjectId pid[] = repository.connectionDelegate.getPermissionIDs(id_profile);
				profileMeta.removeAllPermissions();

				for (int i = 0; i < pid.length; i++) {
					Permission pi = repository.permissionDelegate.loadPermissionMeta(pid[i]);
					if (pi != null) {
						profileMeta.addPermission(pi);
					}
				}

				return profileMeta;
			} else {
				throw new KettleException(BaseMessages.getString(PKG, "ProfileMeta.Error.NotFound", id_profile.toString()));
			}
		} catch (KettleDatabaseException dbe) {
			throw new KettleException(BaseMessages.getString(PKG, "ProfileMeta.Error.NotCreated", id_profile.toString()), dbe);
		}
	}

	public boolean saveProfileMeta(ProfileMeta profileMeta) throws KettleException {
		try {
			if (profileMeta.getObjectId() == null) {
				profileMeta.setObjectId(getProfileID(profileMeta.getName()));
			}

			if (profileMeta.getObjectId() == null) // Insert...
			{
				profileMeta.setObjectId(repository.connectionDelegate.getNextProfileID());

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
				repository.connectionDelegate.delProfilePermissions(profileMeta.getObjectId());

				// Save permission-profile relations
				//
				saveProfilePermissions(profileMeta);
			}
		} catch (KettleDatabaseException dbe) {
			throw new KettleException(BaseMessages.getString(PKG, "ProfileMeta.Error.NotSaved", profileMeta.getObjectId().toString()), dbe);
		}
		return true;
	}

	private void saveProfilePermissions(ProfileMeta profileMeta) throws KettleException {
		try {
			// Then save profile_permission relationships
			for (int i = 0; i < profileMeta.nrPermissions(); i++) {
				Permission pi = profileMeta.getPermission(i);
				ObjectId id_permission = repository.permissionDelegate.getPermissionID(pi.getCode());

				RowMetaAndData pr = new RowMetaAndData();
				pr.addValue(new ValueMeta("ID_PROFILE", ValueMetaInterface.TYPE_INTEGER), profileMeta.getObjectId());
				pr.addValue(new ValueMeta("ID_PERMISSION", ValueMetaInterface.TYPE_INTEGER), id_permission);

				repository.connectionDelegate.insertTableRow("R_PROFILE_PERMISSION", pr);
			}
		} catch (KettleDatabaseException dbe) {
			throw new KettleException(BaseMessages.getString(PKG, "ProfileMeta.Error.PermissionNotSaved", profileMeta.getObjectId().toString()), dbe);
		}
	}

	private RowMetaAndData fillTableRow(ProfileMeta profileMeta)
	{
		RowMetaAndData r = new RowMetaAndData();
		r.addValue(new ValueMeta("ID_PROFILE", ValueMetaInterface.TYPE_INTEGER), profileMeta.getObjectId());
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


	public synchronized void renameProfile(ObjectId id_profile, String newname) throws KettleException
	{
		String sql = "UPDATE "+quoteTable(KettleDatabaseRepository.TABLE_R_PROFILE)+" SET "+quote(KettleDatabaseRepository.FIELD_PROFILE_NAME)+" = ? WHERE "+quote(KettleDatabaseRepository.FIELD_PROFILE_ID_PROFILE)+" = ?";

		RowMetaAndData table = new RowMetaAndData();
		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_PROFILE_NAME, ValueMetaInterface.TYPE_STRING), newname);
		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_PROFILE_ID_PROFILE, ValueMetaInterface.TYPE_INTEGER), id_profile);

		repository.connectionDelegate.getDatabase().execStatement(sql, table.getRowMeta(), table.getData());
	}

	
}
