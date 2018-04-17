package org.pentaho.di.core.geospatial;


import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.deegree.framework.xml.XMLParsingException;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureCollection;
import org.deegree.model.feature.FeatureProperty;
import org.deegree.model.feature.GMLFeatureCollectionDocument;
import org.deegree.model.feature.schema.FeatureType;
import org.deegree.model.feature.schema.PropertyType;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.xml.sax.SAXException;

/**
 * @author shudongping
 * @date 2018/04/17
 */
public class GMLReader {

    private URL url;
    private FeatureCollection fc = null;
    private static LogChannelInterface log = new LogChannel( "GMLReader" );
    private boolean error;
    private int row_index;

    public GMLReader(URL url) {
        error = false;
        try {
            this.url = new File(url.toString().substring(5)).toURI().toURL();
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void open() throws IOException, SAXException, XMLParsingException {
        if ((url.toString().substring(url.toString().length() - 3,
                url.toString().length()).equalsIgnoreCase("gml"))
                || (url.toString().substring(url.toString().length() - 3,
                url.toString().length()).equalsIgnoreCase("xml"))) {

            GMLFeatureCollectionDocument doc = new GMLFeatureCollectionDocument();
            doc.load(url.openStream(), url.toString());
            try {
                fc = doc.parse();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("Reading " + url + " ... ");
        }
    }

    public RowMetaInterface getFields() throws KettleException {
        String debug = "get attributes from Deegree datastore";
        RowMetaInterface row = new RowMeta();
        row_index = fc.size();
        try {
            // Fetch all field information
            debug = "allocate data types";

            FeatureType ft = fc.getFeature(0).getFeatureType();

            PropertyType[] props = ft.getProperties();

            int i = 0;
            for (int j = 0; j < props.length; j++) {
                if (log.isDebug())
                    debug = "get attribute #" + i;

                ValueMetaInterface value = null;

                int pt = props[j].getType();

                if (pt == java.sql.Types.VARCHAR) {
                    // String
                    debug = "string attribute";
                    value = new ValueMeta(props[j].getName().getLocalName(),
                            ValueMetaInterface.TYPE_STRING);
                    // TODO: check if there is a way to get max string length
                    // from AttributeType
                } else if (pt == java.sql.Types.INTEGER
                        || pt == java.sql.Types.BIGINT) {
                    // Integer
                    debug = "integer attribute";
                    value = new ValueMeta(props[j].getName().getLocalName(),
                            ValueMetaInterface.TYPE_INTEGER);
                } else if (pt == java.sql.Types.DOUBLE) {
                    // Double
                    debug = "double attribute";
                    value = new ValueMeta(props[j].getName().getLocalName(),
                            ValueMetaInterface.TYPE_NUMBER);
                } else if (pt == java.sql.Types.DATE) {
                    // Date
                    debug = "date attribute";
                    value = new ValueMeta(props[j].getName().getLocalName(),
                            ValueMetaInterface.TYPE_DATE);
                } else if (pt >= 10012 && pt < 11019) {
                    // Geometry
                    debug = "geometry attribute";
                    value = new ValueMeta(props[j].getName().getLocalName(),
                            ValueMetaInterface.TYPE_GEOMETRY);
                }else
                    value = new ValueMeta(props[j].getName().getLocalName(),
                            ValueMetaInterface.TYPE_STRING);

                if (value != null)
                    row.addValueMeta(value);

                i++;
            }
        } catch (Exception e) {
            throw new KettleException(
                    "Error reading GML file metadata (in part " + debug + ")",
                    e);
        }
        return row;
    }

    public Object[] getRow(RowMetaInterface fields) throws KettleException {
        return getRow(RowDataUtil.allocateRowData(fields.size()));
    }

    public Object[] getRow(Object[] r) throws KettleException {
        PropertyType[] props = fc.getFeature(0).getFeatureType().getProperties();
        String debug ="Retrieving row.";
        try {
            if (fc.size() == 0 || row_index == 0)
                return null;

            // Copy the default row for speed...
            debug = "copy the default row for speed!";

            debug = "set the values in the row";
            // Set the values in the row...

            Feature feat = fc.getFeature(row_index - 1);
            FeatureProperty[] featProperties = feat.getProperties();

            for (int j = 0; j < props.length; j++) {
                debug = "getting value #" + j;
                int pt = props[j].getType();

                if (featProperties[j].getValue() == null)
                    r[j] = null;
                else if (pt == org.deegree.datatypes.Types.VARCHAR) {
                    debug = "string attribute";
                    r[j] = (String) featProperties[j].getValue();
                } else if (pt == org.deegree.datatypes.Types.INTEGER) {
                    debug = "integer attribute";
                    r[j] = new Long(
                            ((Integer) featProperties[j].getValue())
                                    .longValue());
                } else if (pt == org.deegree.datatypes.Types.BIGINT) {
                    debug = "long integer attribute";
                    // TODO: check if this is supported:
                    r[j] = (Long) featProperties[j].getValue();
                } else if (pt == org.deegree.datatypes.Types.DOUBLE) {
                    debug = "double attribute";
                    r[j] = (Double) featProperties[j].getValue();
                } else if (pt == org.deegree.datatypes.Types.DATE) {
                    debug = "date attribute";
                    r[j] = (java.util.Date) featProperties[j].getValue();
                } else if ((pt >= 10012 && pt <= 10016)
                        || (pt >= 11012 && pt <= 11019)) {
                    // Geometry
                    debug = "geometry attribute";
                    r[j] = org.deegree.model.spatialschema.JTSAdapter
                            .export((org.deegree.model.spatialschema.Geometry) featProperties[j]
                                    .getValue());
                }else
                    r[j] = null;
            }
        } catch (Exception e) {
            log.logError(toString(), "Unexpected error in part [" + debug
                    + "] : " + e.toString());
            error = true;
            throw new KettleException(
                    "Unable to read row from Deegree datastore", e);
        }
        row_index--;
        return r;
    }

    public boolean hasError() {
        return error;
    }

    public String toString() {
        return url != null ? url.toString() : getClass().getName();
    }

    public String getVersionInfo() {
        return null;
    }

    /**
     * @return the gmlURL
     */
    public java.net.URL getGMLURL() {
        return url;
    }

    /**
     * @param gmlURL
     *            the gmlURL to set
     */
    public void setGMLURL(java.net.URL url) {
        this.url = url;
    }


}
