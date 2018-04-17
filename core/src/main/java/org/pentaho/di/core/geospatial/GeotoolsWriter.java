package org.pentaho.di.core.geospatial;


import java.io.Serializable;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Date;
import java.util.HashMap;
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.FeatureWriter;
import org.geotools.data.FileDataStoreFactorySpi;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * @author shudongping
 * @date 2018/04/17
 */
public class GeotoolsWriter {

    private static LogChannelInterface log = new LogChannel( "GeotoolsWriter" );
    private boolean     error;
    private java.net.URL gisURL;
    private DataStore newDS;
    private FileDataStoreFactorySpi factory;
    private SimpleFeatureType featureType;
    private SimpleFeature sf;
    private String charset;
    private FeatureWriter<SimpleFeatureType, SimpleFeature> featWriter;
    private RowMetaInterface rowMeta;

    public GeotoolsWriter(URL fileURL, String charset){
        gisURL = fileURL;
        this.charset = charset;
        error = false;
        sf = null;
        featWriter = null;
        featureType = null;
        factory = null;
        rowMeta = null;
    }

    public void open() throws KettleException{
        try {
            // try closing first
            close();

            // TODO: detect file type and instanciate the right type of DataStore
            // implementation (to support file formats other than Shapefile)

            // TODO: make charset configurable (in the step dialog box?)
            if(!gisURL.toString().substring(gisURL.toString().length()-3,gisURL.toString().length()).equalsIgnoreCase("SHP"))
                throw new KettleException("The output specified is not in shapefile format (.shp)");
        }catch (Exception e){
            throw new KettleException("Error opening GIS file at URL: "+gisURL, e);
        }
    }

    public void createSimpleFeatureType(RowMetaInterface fields, Object[] firstRow) throws KettleException{
        String debug="get attributes from table";

        rowMeta = fields;

        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName( "Type" );
        try{
            // Fetch all field information
            debug="allocate data types";

            for(int i = 0; i < fields.size(); i++){
                if (log.isDebug()) debug="get attribute #"+i;

                ValueMetaInterface value = fields.getValueMeta(i);
                switch(value.getType()) {
                    case ValueMeta.TYPE_NUMBER:
                        builder.add(value.getName(),Double.class);
                        break;
                    case ValueMeta.TYPE_STRING:
                        if ( value.getLength() > 0 ) builder.length(value.getLength());
                        builder.add(value.getName(),String.class);
                        break;
                    case ValueMeta.TYPE_DATE:
                        builder.add(value.getName(),Date.class);
                        break;
                    case ValueMeta.TYPE_BOOLEAN:
                        builder.add(value.getName(),Boolean.class);
                        break;
                    case ValueMeta.TYPE_INTEGER:
                        builder.add(value.getName(),Long.class);
                        break;
                    case ValueMeta.TYPE_BIGNUMBER:
                        builder.add(value.getName(),BigDecimal.class);
                        break;
                    case ValueMeta.TYPE_GEOMETRY:
                        // determine the geometry type from the first row's geometry object
                        if(firstRow[i] instanceof Geometry) {

                            // set the output Coordinate Reference System for this geometry
                            builder.setCRS(value.getGeometrySRS().getCRS());

                            Geometry g = (Geometry) firstRow[i];
                            if (g instanceof MultiPolygon || g instanceof Polygon) {
                                builder.add(value.getName(), MultiPolygon.class);
                            }else if (g instanceof MultiPoint) {
                                builder.add(value.getName(), MultiPoint.class);
                            }else if (g instanceof Point) {
                                builder.add(value.getName(), Point.class);
                            }else if (g instanceof MultiLineString || g instanceof LineString) {
                                builder.add(value.getName(), MultiLineString.class);
                            }else
                                throw new KettleException("Unsupported geometry type in GeotoolsWriter: "+ g.getClass().toString());


                            // set this field as the default geometry
                            // caveat: if we have more than one geometry field the last
                            // one will be set as default (could be fixed by a "set default
                            // geometry" drop down list in dialog)
                            // edit: not necessary, SimpleFeatureTypeBuilder uses
                            // 1st geometry as default
                            // builder.setDefaultGeometry(value.getName());
                        }else
                            throw new KettleException("Unexpected class for Geometry field: "+ firstRow[i].getClass().toString());
                        break;

                    case ValueMeta.TYPE_NONE:
                    case ValueMeta.TYPE_SERIALIZABLE:
                    case ValueMeta.TYPE_BINARY:
                    default:
                        throw new KettleException("Wrong object type for Geometry field: "+ ValueMetaInterface.typeCodes[value.getType()]);
                }
            }
        }catch (Exception e) {
            throw new KettleException("Error reading GIS file metadata (in part "+debug+")", e);
        }

        try{
            featureType = builder.buildFeatureType();

            Map <String, Serializable> create = new HashMap<String, Serializable>();

            create.put(ShapefileDataStoreFactory.URLP.key, gisURL);
            create.put(ShapefileDataStoreFactory.CREATE_SPATIAL_INDEX.key, Boolean.TRUE);
            create.put(ShapefileDataStoreFactory.MEMORY_MAPPED.key, Boolean.TRUE);
            create.put(ShapefileDataStoreFactory.DBFCHARSET.key, charset);

            factory = new ShapefileDataStoreFactory();

            newDS = (ShapefileDataStore)factory.createNewDataStore(create);

            newDS.createSchema((SimpleFeatureType)featureType);

            featWriter = newDS.getFeatureWriterAppend(featureType.getName().getLocalPart(), Transaction.AUTO_COMMIT);
        }catch (Exception e){
            throw new KettleException("An error has occured...");
        }
    }

    public void putRow(Object[] r) throws KettleException{
        Object[] rowCopy = rowMeta.cloneRow(r);

        for ( int i = 0; i < featureType.getAttributeCount() ; i++){
            if (rowCopy[i] != null) {
                Class<?> kettleClass = rowCopy[i].getClass();
                Class<?> geotoolsClass = featureType.getAttributeDescriptors().get(i).getType().getBinding();

                if (!geotoolsClass.isAssignableFrom(kettleClass)) {
                    // put Polygon in a MultiPolygon (ShapeFiles contains only MultiPolygons)
                    if ( kettleClass.equals(Polygon.class)
                            && geotoolsClass.equals(MultiPolygon.class) ) {
                        Polygon poly = (Polygon) r[i];
                        Polygon[] polyArray = {poly};
                        GeometryFactory fac = new GeometryFactory(poly.getPrecisionModel(), poly.getSRID());
                        MultiPolygon mpoly = fac.createMultiPolygon(polyArray);
                        rowCopy[i] = mpoly;
                    }else if ( kettleClass.equals(LineString.class)
                            && geotoolsClass.equals(MultiLineString.class) ) {
                        LineString linestring = (LineString) r[i];
                        LineString[] linestringArray = {linestring};
                        GeometryFactory fac = new GeometryFactory(linestring.getPrecisionModel(), linestring.getSRID());
                        MultiLineString mlinestring = fac.createMultiLineString(linestringArray);
                        rowCopy[i] = mlinestring;
                    }else if ( kettleClass.equals(java.util.Date.class)
                            && geotoolsClass.equals(java.sql.Date.class) ) {
                        java.sql.Date sqlDate = new java.sql.Date(((java.util.Date)r[i]).getTime());
                        rowCopy[i] = sqlDate;
                    }else {
                        // TODO: internationalize error message
                        throw new KettleException(
                                "The type of attribute [ "+featureType.getAttributeDescriptors().get(i).getName()
                                        +" ] is not the one expected ..."
                                        + rowCopy[i].getClass().getName() + " :: "
                                        + featureType.getAttributeDescriptors().get(i).getType().getBinding());
                    }
                }
            }
        }

        try {
            sf = featWriter.next();
            sf.setAttributes(rowCopy);
            featWriter.write();
        }catch (Exception e) {
            throw new KettleException("An error has occured", e);
        }
    }

    public boolean close(){
        boolean retval = false;
        try{
            if(featWriter!=null)
                featWriter.close();
            if(newDS != null)
                newDS.dispose();
            retval=true;
        }catch (Exception e){
            log.logError(toString(), "Couldn't close iterator for datastore ["+gisURL+"] : "+e.toString());
            error = true;
        }
        return retval;
    }

    public boolean hasError(){
        return error;
    }

    public String getVersionInfo(){
        // return reader.getHeader().getSignatureDesc();
        return null;
    }

}
