package org.pentaho.di.core.geospatial;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Set;

import org.geotools.data.PrjFileReader;
import org.geotools.factory.FactoryRegistryException;
import org.geotools.factory.Hints;
import org.geotools.metadata.iso.citation.Citations;
import org.geotools.referencing.CRS;
import org.geotools.referencing.ReferencingFactoryFinder;
import org.geotools.referencing.wkt.Parser;
import org.geotools.referencing.wkt.Preprocessor;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.ReferenceIdentifier;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.core.xml.XMLInterface;


/**
 * @author shudongping
 * @date 2018/04/17
 */
public class SRS implements Comparable<SRS>, Cloneable, XMLInterface {

    private static LogChannelInterface log = new LogChannel( "SRS" );

    // Properties
    /** Mandatory: The authority **/
    public final String authority;
    /** Mandatory: The SRID as a String. Use this value for GUI methods **/
    public final String srid;
    /** Mandatory: The description of this {@link SRS} instance **/
    public final String description;
    /** Optional: The {@link CoordinateReferenceSystem} of this {@link SRS} instance **/
    private CoordinateReferenceSystem crs;
    /** Mandatory: Indicates, if this {@link SRS} is custom defined **/
    public boolean is_custom = false;

    public final static Hints HINTS = new Hints(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, Boolean.TRUE);
    // XML tags
    public final static String XML_AUTH = "authority";
    public final static String XML_SRID = "srid";
    public final static String XML_DESC = "description";
    public final static String XML_CUST = "is_custom";
    public final static String XML_WKT = "wkt";

    // Constants
    public static final SRS UNKNOWN = new SRS("", "",
            "");
    public static final int UNKNOWN_SRID = -1;

    /** The code for the EPSG authority **/
    public static final String AUTH_EPSG = Citations.getIdentifier(Citations.EPSG);

    /**
     * Helper class to allow multiple return-values by getProperties().
     *
     * @author phobus, sgoldinger
     * @since 12-dec-2008
     */
    private final class SRSInit {
        public String auth = null;
        public String srid = null;
        public String desc = null;
    }

    /**
     * Creates a new instance of {@link SRS} by defining all necessary attributes
     * manually.
     *
     * @param authority The authority e.g. EPSG.
     * @param srid The spatial reference system identifier.
     * @param description A description text of this spatial reference system.
     * @param crs The CoordinateReferenceSystem
     */
    public SRS(final String authority, final String srid, final String description,
               CoordinateReferenceSystem crs) {
        this.authority = authority;
        this.srid = srid;
        this.description = description;
        this.crs = crs;
    }

    /**
     * Creates a new instance of {@link SRS} by defining authority, srid and
     * and description.
     *
     * @param authority The authority e.g. EPSG.
     * @param srid The spatial reference system identifier.
     * @param description A description text of this spatial reference system.
     */
    public SRS(final String authority, final String srid, final String description) {
        this.authority = Const.NVL(authority, "");
        this.srid = Const.NVL(srid, "");
        this.description = Const.NVL(description, "");
        this.crs = null;
    }


    /**
     * Creates a new instance of {@link SRS} according to an existing
     * {@link CoordinateReferenceSystem} object.
     *
     * @param crs The {@link CoordinateReferenceSystem} object.
     * @throws KettleStepException
     */
    public SRS(CoordinateReferenceSystem crs) throws KettleStepException {
        this.crs = crs;
        if (this.crs != null) {
            SRSInit init = new SRSInit();
            getProperties(init, this.crs);
            this.authority = init.auth;
            this.srid = init.srid;
            this.description = init.desc;
        }else {
            this.authority = "";
            this.srid = "";
            this.description = "";
        }
    }

    /**
     * Creates a new instance of {@link SRS} according to a WKT definition
     * This is a convenience constructor.
     *
     * @param wkt The WKT string
     * @throws KettleStepException
     */
    public SRS(String wkt) throws KettleStepException {
        // parse the WKT-string and create a CRS instance
        this.crs = createCRSFromWKT(wkt);
        SRSInit init = new SRSInit();
        getProperties(init, this.crs);

        this.authority = init.auth;
        this.srid = init.srid;
        this.description = init.desc;
    }

    public SRS(File prjFile) {
        SRSInit init = new SRSInit();
        try {
            PrjFileReader prjFileReader = new PrjFileReader(new FileInputStream(prjFile).getChannel());
            this.crs = prjFileReader.getCoordinateReferenceSystem();
            getProperties(init, this.crs);
        } catch (FileNotFoundException e) {
            System.out.println("FileNotFound");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("IO Exception");
            e.printStackTrace();
        } catch (FactoryException e) {
            init.srid = Const.NVL(init.srid, Integer.toString(UNKNOWN_SRID));
            init.auth = Const.NVL(init.auth, "Invalid file");
            init.desc = Const.NVL(init.desc, "Invalid file");
        }
        this.authority = init.auth;
        this.srid = init.srid;
        this.description = init.desc;
    }


    /**
     * Helper constructor for SRSFactory
     *
     * @param authority
     * @param srid
     * @param description
     * @param is_custom
     * @param wkt
     * @throws KettleException
     */
    public SRS(final String authority, final String srid, final String description,
               boolean is_custom, final String wkt) throws KettleException {

        SRSInit init = new SRSInit();

        // Read properties from repository
        init.auth = authority;
        init.srid = srid;
        init.desc = description;
        this.is_custom = is_custom;
        this.crs = null;

        // If the properties are empty, try to create the SRS from a WKT representation
        if (is_custom) {
            this.crs = createCRSFromWKT(wkt);
            getProperties(init, this.crs);
        }

        this.authority = init.auth;
        this.srid = init.srid;
        this.description = init.desc;
    }

    /**
     * Fills a {@link SRSInit} object with the correct initialization values
     * needed by constructors (authority, description, srid).
     *
     * Note: The method tries to retrieve as much information from the
     * {@link CoordinateReferenceSystem} <code>crs</code> as possible.
     *
     * @param init Pass the {@link SRSInit} object to allow multiple return-values.
     * @param crs The {@link CoordinateReferenceSystem} where the information
     *            are retrieved from.
     */
    private void getProperties(SRSInit init, CoordinateReferenceSystem crs) {
        // Try to find srid, authority and description from the CRS.
        // Abort if the correct EPSG identifiers were found.
        Set<ReferenceIdentifier> identifiers = crs.getIdentifiers();
        if (!identifiers.isEmpty()) {
            for (ReferenceIdentifier id : identifiers) {
                init.auth = Citations.getIdentifier(id.getAuthority());
                init.srid = id.getCode();
                try {
                    CRSAuthorityFactory factory = ReferencingFactoryFinder.getCRSAuthorityFactory(init.auth, HINTS);
                    init.desc = factory.getDescriptionText(init.srid).toString();
                    this.is_custom = false;
                    break;
                } catch (Exception e) {
                    this.is_custom = true;
                }
            }
        } else
            this.is_custom = true;

        // If this is not an EPSG spatial reference system, use WKT to describe it but get
        // as much information as possible about the SRS from the WKT.
        if (this.is_custom) {
            init.srid = Const.NVL(init.srid, Integer.toString(UNKNOWN_SRID));
            init.auth = Const.NVL(init.auth, "Custom Authority");
            init.desc = Const.NVL(init.desc, "Custom SRS from WKT");
        }
    }

    /**
     * Creates a {@link CoordinateReferenceSystem} instance from a WKT string.
     * (helper method)
     *
     * @param wkt The WKT string.
     * @return The appropriate {@link CoordinateReferenceSystem} instance.
     * @throws KettleStepException
     */
    private static CoordinateReferenceSystem createCRSFromWKT(String wkt) throws KettleStepException {
        Parser wktParser = new Parser();
        Preprocessor preproc = new Preprocessor(wktParser);
        try {
            preproc.parseObject(wkt, CoordinateReferenceSystem.class);
            return CRS.parseWKT(wkt);
        } catch (Exception e) {
            throw new KettleStepException("Could not create a SRS metadata according to " +
                    "the delivered WKT representation of the spatial reference system");
        }
    }

    /**
     * Get the SRID for the corresponding {@link CoordinateReferenceSystem}.
     *
     * Note: For GUI operations that must be performed fast, use the public
     * property <code>srid</code> instead.
     *
     * @param crs CRS to map to an EPSG code.
     * @return the SRID if it exists, <code>SRS.UNKNOWN_SRID</code> otherwise.
     */
    public int getSRID() {
        try {
            return Integer.parseInt(this.srid);
        } catch (NumberFormatException e) {
            return UNKNOWN_SRID;
        }
    }

    /**
     * Creates a {@link CoordinateReferenceSystem} object from the existing properties
     * in this object. The CRS will only be created, if needed, otherwise the existing
     * instance will be returned.
     *
     * @return A new {@link CoordinateReferenceSystem}.
     * @throws KettleStepException
     */
    public CoordinateReferenceSystem getCRS() throws KettleStepException {
        if (crs != null)
            return crs;
        return crsFromAuthSrid(authority, srid);
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(SRS other) {
        return description.toLowerCase().compareTo(other.description.toLowerCase());
    }

    @Override
    public Object clone() {
        if (authority == null || srid == null || description == null) {
            return UNKNOWN;
        } else {
            SRS cloned = new SRS(new String(authority), new String(srid), new String(description), crs);
            cloned.is_custom = is_custom;
            return cloned;
        }
    }

    @Override
    public boolean equals(Object obj) {
        SRS other = (SRS) obj;
        boolean eqProps = srid.equals(other.srid) && authority.equals(other.authority) && description.equals(other.description);
        boolean eqCRS = (crs == null && other.crs == null) || (crs != null && other.crs != null && crs.equals(other.crs));
        return eqProps && eqCRS;
    }

    /*
     * (non-Javadoc)
     * @see org.pentaho.di.core.xml.XMLInterface#getXML()
     */
    public String getXML() {
        String xml = "";

        // XML: write the SRS containing the authority, srid and descriptioin
        xml += XMLHandler.addTagValue(XML_AUTH, authority);
        xml += XMLHandler.addTagValue(XML_SRID, srid);
        xml += XMLHandler.addTagValue(XML_DESC, description);
        xml += XMLHandler.addTagValue(XML_CUST, is_custom);
        if (crs != null) {
            xml += XMLHandler.addTagValue(XML_WKT, crs.toWKT());
        }

        return xml;
    }

    public static SRS createFromEPSG(String srid) {
        CRSAuthorityFactory factory = ReferencingFactoryFinder.getCRSAuthorityFactory(AUTH_EPSG, HINTS);
        try {
            return new SRS(AUTH_EPSG, srid, factory.getDescriptionText(srid).toString());
        } catch (NoSuchAuthorityCodeException e) {
            return SRS.UNKNOWN;
        } catch (FactoryException e) {
            return SRS.UNKNOWN;
        }
    }

    private static CoordinateReferenceSystem crsFromAuthSrid(String auth, String srid) {
        // try to get the CoordinateReferenceSystem object from the authority and srid

        CoordinateReferenceSystem crs = null;

        if (!auth.equals("") && (!srid.equals("") || !srid.equals("-1")) ) {
            try {
                CRSAuthorityFactory factory = ReferencingFactoryFinder.getCRSAuthorityFactory(auth, HINTS);
                crs = factory.createCoordinateReferenceSystem(srid);
            } catch (NoSuchAuthorityCodeException e) {
                log.logDetailed("GeoKettle SRS",
                        "Could not find a CRS from authority \"" + auth +
                                "\" and code \"" + srid + "\"", e);
            } catch (FactoryRegistryException e) {
                log.logDetailed("GeoKettle SRS",
                        "FactoryRegistryException occured; could not find instance for authority \"" +
                                auth + "\"", e);
            } catch (FactoryException e) {
                log.logError("GeoKettle SRS",
                        "FactoryException occured", e);
            } catch (NullPointerException e) {
                log.logError("GeoKettle SRS",
                        "NullPointerException occured", e);
            }
        }
        return crs;
    }
}
