package org.pentaho.di.core.geospatial;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.w3c.dom.Node;

public class SRSFactory {

	/**
	 * Creates a new instance of {@link SRS} by reading an XML node.
	 * 
	 * @param node The XML node.
	 * @throws KettleXMLException
	 */
	public static SRS createSRS(final Node node) throws KettleException {
		
		// Read properties from XML
		String auth = Const.NVL(XMLHandler.getTagValue(node, SRS.XML_AUTH), "");
		String srid = Const.NVL(XMLHandler.getTagValue(node, SRS.XML_SRID), "");
		String desc = Const.NVL(XMLHandler.getTagValue(node, SRS.XML_DESC), "");
		boolean is_custom = Const.NVL(XMLHandler.getTagValue(node, SRS.XML_CUST), "N").equalsIgnoreCase("Y");
		String wkt = ""; 
		
		// If the properties are empty, try to create the SRS from a WKT representation
		if (is_custom) {
			wkt = XMLHandler.getTagValue(node, SRS.XML_WKT);
		}
		
		return new SRS(auth, srid, desc, is_custom, wkt);
	}
	
	public static SRS createSRS(Repository rep, ObjectId id_step, String prefix) throws KettleException {
		
		// Read properties from repository
		String auth = Const.NVL(rep.getStepAttributeString(id_step, prefix+"srs_authority"), ""); //$NON-NLS-1$ //$NON-NLS-2$
		String srid = Const.NVL(rep.getStepAttributeString(id_step, prefix+"srs_srid"), ""); //$NON-NLS-1$ //$NON-NLS-2$
		String desc = Const.NVL(rep.getStepAttributeString(id_step, prefix+"srs_description"), ""); //$NON-NLS-1$ //$NON-NLS-2$
		boolean is_custom = Const.NVL(rep.getStepAttributeString(id_step, prefix+"srs_description"), "N").equals("Y");
		String wkt = "";
		
		// If the properties are empty, try to create the SRS from a WKT representation
		if (is_custom) {
			wkt = Const.NVL(rep.getStepAttributeString(id_step, prefix+"srs_wkt"), ""); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		return new SRS(auth, srid, desc, is_custom, wkt);
	}
	
}
