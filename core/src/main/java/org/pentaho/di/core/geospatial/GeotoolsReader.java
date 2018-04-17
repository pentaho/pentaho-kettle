package org.pentaho.di.core.geospatial;

import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;

import org.geotools.data.DataStore;
import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;

import com.vividsolutions.jts.geom.Geometry;
/**
 * @author shudongping
 * @date 2018/04/17
 */
public class GeotoolsReader {

    private static LogChannelInterface log = new LogChannel( "GeotoolsReader" );
    private java.net.URL gisURL;
    private String charset;
    private boolean     error;

    private DataStore gtDataStore;
    private FeatureSource<SimpleFeatureType, SimpleFeature> featSrc;
    private FeatureCollection<SimpleFeatureType, SimpleFeature> featColl;
    private FeatureIterator<SimpleFeature> featIter;

    public GeotoolsReader(URL fileURL, String charset){
        gisURL = fileURL;
        this.charset = charset;
        error = false;
        gtDataStore = null;
        featSrc = null;
        featColl = null;
        featIter = null;
    }

    public void open() throws KettleException{
        try {
            // try closing first
            close();

            // TODO: Don't use a memory-mapped file reader (3rd arg) because this
            // causes out of memory errors with large files (~500mb).

            gtDataStore = new ShapefileDataStore(gisURL, null, false, Charset.forName(Const.isEmpty(this.charset)?"ISO-8859-1":this.charset));
            featSrc = gtDataStore.getFeatureSource(gtDataStore.getTypeNames()[0]);
            featColl = featSrc.getFeatures();
            featIter = featColl.features();
        }catch(Exception e) {
            throw new KettleException("Error opening GIS file at URL: "+gisURL, e);
        }
    }

    public RowMetaInterface getFields() throws KettleException{
        String debug="get attributes from Geotools datastore";
        RowMetaInterface row = new RowMeta();

        try{
            // Fetch all field information
            debug="allocate data types";

            List<AttributeDescriptor> attrDescriptors = featSrc.getSchema().getAttributeDescriptors();

            int i = 0;
            for(AttributeDescriptor ad : attrDescriptors){
                if (log.isDebug())
                    debug="get attribute #"+i;

                ValueMetaInterface value = null;

                Class<?> c = ad.getType().getBinding();

                if(c == java.lang.String.class) {
                    // String
                    debug = "string attribute";
                    value = new ValueMeta(ad.getName().getLocalPart(), ValueMetaInterface.TYPE_STRING);
                    // value.setLength(); // TODO: check if there is a way to get max string length from AttributeType
                }else if(c == java.lang.Integer.class || c == java.lang.Long.class) {
                    // Integer
                    debug = "integer attribute";
                    value = new ValueMeta(ad.getName().getLocalPart(), ValueMetaInterface.TYPE_INTEGER);
                }else if(c == java.lang.Double.class) {
                    // Double
                    debug = "double attribute";
                    value = new ValueMeta(ad.getName().getLocalPart(), ValueMetaInterface.TYPE_NUMBER);
                }else if(c == java.util.Date.class) {
                    // Date
                    debug = "date attribute";
                    value = new ValueMeta(ad.getName().getLocalPart(), ValueMetaInterface.TYPE_DATE);
                }else if( com.vividsolutions.jts.geom.Geometry.class.isAssignableFrom(c)){
                    // Geometry
                    debug = "geometry attribute";
                    value = new ValueMeta(ad.getName().getLocalPart(), ValueMetaInterface.TYPE_GEOMETRY);

                    // set the SRS
                    value.setGeometrySRS(getSRS());
                }else
                    value = new ValueMeta(ad.getName().getLocalPart(), ValueMetaInterface.TYPE_STRING);

                if (value!=null)
                    row.addValueMeta(value);

                i++;
            }
        }catch(Exception e){
            throw new KettleException("Error reading GIS file metadata (in part "+debug+")", e);
        }
        return row;
    }

    public Object[] getRow(RowMetaInterface fields) throws KettleException{
        return getRow( RowDataUtil.allocateRowData(fields.size()) );
    }

    public Object[] getRow(Object[] r) throws KettleException{
        String debug = "";
        try{
            // Are we at the end yet?
            if (!featIter.hasNext()) return null;

            debug = "set the values in the row";
            // Set the values in the row...

            SimpleFeature f = featIter.next();
            List<Object> attributeValues = f.getAttributes();

            int i = 0;
            for(Object val : attributeValues){
                debug = "getting value #"+i;

                if(val == null) {
                    debug = "null attribute";
                    r[i] = null;
                }else {
                    Class<?> c = val.getClass();
                    if(c == java.lang.String.class) {
                        debug = "string attribute";
                        r[i] = (String) val;
                    }else if(c == java.lang.Integer.class) {
                        debug = "integer attribute";
                        r[i] = new Long( ((Integer)val).longValue() );
                    }else if(c == java.lang.Long.class) {
                        debug = "long integer attribute";
                        // TODO: check if this is supported:
                        r[i] = (Long) val;
                    }else if(c == java.lang.Double.class) {
                        debug = "double attribute";
                        r[i] = (Double) val;
                    }else if(c == java.util.Date.class) {
                        debug = "date attribute";
                        r[i] = (java.util.Date) val;
                    }else if( com.vividsolutions.jts.geom.Geometry.class.isAssignableFrom(c)){
                        // Geometry
                        debug = "geometry attribute";
                        // TODO: add logic to convert simple MultiPolygons to Polygons
                        // (needed for JCS)
                        // could we put this in another step instead??
                        r[i] = (Geometry) val;
                    }else
                        r[i] = null;
                }
                i++;
            }
        }catch(Exception e){
            log.logError(toString(), "Unexpected error in part ["+debug+"] : "+e.toString());
            error = true;
            throw new KettleException("Unable to read row from Geotools datastore", e);
        }
        return r;
    }

    private SRS getSRS() throws KettleException {
        if(featColl == null)
            throw new KettleException("FeatureSource is not open");
        return new SRS(featSrc.getSchema().getGeometryDescriptor().getCoordinateReferenceSystem());
    }

    public boolean close(){
        boolean retval = false;
        try{
            if(featIter != null)
                featIter.close();
            retval=true;
        }catch(Exception e){
            log.logError(toString(), "Couldn't close iterator for datastore ["+gisURL+"] : "+e.toString());
            error = true;
        }
        return retval;
    }

    public boolean hasError(){
        return error;
    }

    public String toString(){
        return gisURL!=null?gisURL.toString():getClass().getName();
    }

    public String getVersionInfo(){
        return null;
    }

    /**
     * @return the gisURL
     */
    public java.net.URL getGisURL(){
        return gisURL;
    }

    /**
     * @param gisURL the gisURL to set
     */
    public void setGisURL(java.net.URL gisURL){
        this.gisURL = gisURL;
    }

}
