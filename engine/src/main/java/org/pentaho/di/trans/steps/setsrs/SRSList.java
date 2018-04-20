package org.pentaho.di.trans.steps.setsrs;

import org.geotools.referencing.ReferencingFactoryFinder;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.pentaho.di.core.geospatial.SRS;

import java.util.Set;
import java.util.TreeSet;

/**
 * This class is used to retrieve a list of all available EPSG-{@link CoordinateReferenceSystem}
 * of the GeoTools library. When the {@link Thread} has finished work, the result can be 
 * retrieved by calling <code>getAllSRS()</code>. This result is sorted.
 * 
 * @author phobus, sgoldinger
 * @since 12-nov-2008
 */
public class SRSList extends Thread {
	/** A sorted list that contains all EPSG-SRS **/
	private final TreeSet<SRS> allSRS;
	
	
	public SRSList() {
		allSRS = new TreeSet<SRS>();
		start();
	}
	
	/**
	 * Retrieves all SRS (ascending ordering). 
	 */
	public void run() {
		String[] srids = getAllSRSCodes();
		for (String srid : srids) {
			SRS newSRS = SRS.createFromEPSG(srid);
			if (!newSRS.equals(SRS.UNKNOWN))
				allSRS.add(newSRS);
		}
	}

	/**
	 * @return A sorted TreeSet containing all available SRS.
	 */
	public TreeSet<SRS> getAllSRS() {
		// Wait until the data for all SRS are created. This happens in a separate thread.
		try {
			// join(15000);
			join();
		} catch (InterruptedException e) {
			throw new RuntimeException("A multi-threading problem in SRSList occured.");
		}
		return allSRS; 
	}
	
	/**
	 * @return A plain array with all EPSG-codes.
	 */
	public static String[] getAllSRSCodes() {
		CRSAuthorityFactory factory = ReferencingFactoryFinder.getCRSAuthorityFactory(SRS.AUTH_EPSG, null);
		try {
			Set<String> srids = factory.getAuthorityCodes(CoordinateReferenceSystem.class);
			String[] epsg_codes = new String[srids.size()];
			return srids.toArray(epsg_codes);
		} catch (FactoryException e) {
			return null;
		}
	}
}
