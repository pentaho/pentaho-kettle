package org.pentaho.di.core.geospatial;


import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.deegree.datatypes.QualifiedName;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureCollection;
import org.deegree.model.feature.FeatureProperty;
import org.deegree.model.feature.GMLFeatureAdapter;
import org.deegree.model.feature.schema.FeatureType;
import org.deegree.model.feature.schema.GeometryPropertyType;
import org.deegree.model.feature.schema.PropertyType;
import org.deegree.model.feature.schema.SimplePropertyType;
import org.deegree.model.spatialschema.GeometryException;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;

import com.vividsolutions.jts.geom.Geometry;

/**
 * @author shudongping
 * @date 2018/04/17
 */
public class GMLWriter {

    private static LogChannelInterface log = new LogChannel( "GMLWriter" );
    private boolean     error;
    private java.net.URL gmlURL;
    private FeatureType ft;
    private ArrayList<Feature> features = new ArrayList<Feature>();
    private FeatureCollection fc;
    private RowMetaInterface rowMeta;
    private int count = 0;

    public GMLWriter(java.net.URL fileURL){
        gmlURL = fileURL;
        error  = false;
        rowMeta = null;
    }

    public void open() throws KettleException{
        try {
            if((!gmlURL.toString().substring(gmlURL.toString().length()-3,gmlURL.toString().length()).equalsIgnoreCase("gml")) && (!gmlURL.toString().substring(gmlURL.toString().length()-3,gmlURL.toString().length()).equalsIgnoreCase("xml")))
                throw new KettleException("The output specified is not in gml format (.gml, .xml)");
        }catch(Exception e) {
            throw new KettleException("Error opening GML file at URL: " + gmlURL, e);
        }
    }

    public void createFeatureType(RowMetaInterface fields, Object[] firstRow) throws KettleException{
        String debug="get attributes from table";

        rowMeta = fields;

        PropertyType[] props = new PropertyType[rowMeta.size()];

        try {
            // Fetch all field information
            debug = "allocate data types";

            for (int i = 0; i < fields.size(); i++) {
                if (log.isDebug())
                    debug = "get attribute #" + i;

                ValueMetaInterface value = fields.getValueMeta(i);
                QualifiedName qn = new QualifiedName(value.getName());
                if (value.getType() == ValueMeta.TYPE_STRING) {
                    props[i] = new SimplePropertyType(qn,
                            org.deegree.datatypes.Types.VARCHAR, 0, -1);
                } else if (value.getType() == ValueMeta.TYPE_INTEGER) {
                    props[i] = new SimplePropertyType(qn,
                            org.deegree.datatypes.Types.INTEGER, 0, -1);
                } else if (value.getType() == ValueMeta.TYPE_NUMBER) {
                    props[i] =  new SimplePropertyType(qn,
                            org.deegree.datatypes.Types.DOUBLE, 0, -1);
                } else if (value.getType() == ValueMeta.TYPE_DATE) {
                    props[i] = new SimplePropertyType(qn,
                            org.deegree.datatypes.Types.DATE, 0, -1);
                } else if (value.getType() == ValueMeta.TYPE_GEOMETRY) {
                    // determine the geometry type from the first row's geometry
                    // object
                    Object o = firstRow[i];
                    if (o instanceof Geometry) {
                        props[i] = new GeometryPropertyType(qn,
                                new QualifiedName(
                                        "GeometryPropertyType"), org.deegree.datatypes.Types.GEOMETRY,
                                0, -1);
                    } else
                        throw new KettleException(
                                "Wrong object type for Geometry field");
                } else {
                    props[i] = new SimplePropertyType(qn,
                            java.sql.Types.VARCHAR, 0, -1);
                }
            }
        }catch(Exception e){
            throw new KettleException("Error reading GML file metadata (in part "+debug+")", e);
        }
        ft = org.deegree.model.feature.FeatureFactory.createFeatureType(new QualifiedName("type"), false, props);
    }


    public void putRow(Object[] r) throws KettleException{
        Object[] rowCopy = rowMeta.cloneRow(r);
        PropertyType[] props = ft.getProperties();
        FeatureProperty[] fprop = new FeatureProperty[props.length];
        for ( int i = 0; i < props.length ; i++){
            FeatureProperty prop = null;
            QualifiedName ftprop_name = props[i].getName();
            if(rowCopy[i] instanceof Geometry) {
                try {
                    org.deegree.model.spatialschema.Geometry g = org.deegree.model.spatialschema.JTSAdapter.wrap((Geometry) rowCopy[i]);
                    prop = org.deegree.model.feature.FeatureFactory.createFeatureProperty(ftprop_name,g);
                } catch (GeometryException e) {
                    e.printStackTrace();
                }
            }else
                prop = org.deegree.model.feature.FeatureFactory.createFeatureProperty(ftprop_name,rowCopy[i]);

            fprop[i]=prop;
        }
        Feature feat = org.deegree.model.feature.FeatureFactory.createFeature("feature"+count, ft, fprop);
        count++;
        features.add(feat);
    }

    public void write() throws KettleException{
        FileOutputStream fos = null;
        try{
            Feature[] feats = new Feature[features.size()];
            for (int i = 0; i < features.size(); i++){
                feats[i] = features.get(i);
            }
            fc = org.deegree.model.feature.FeatureFactory.createFeatureCollection("kettlecoll", feats);
            fos = new FileOutputStream( gmlURL.toString().substring(5));
            new GMLFeatureAdapter().export( fc, fos );
        }catch(Exception e){
            throw new KettleException("Could not write features.", e);
        }finally{
            if(fos!=null){
                try {
                    fos.close();
                } catch (IOException e) {
                    throw new KettleException("Could not close output stream.", e);
                }
            }
        }
    }

    public boolean hasError(){
        return error;
    }

    public String getVersionInfo(){
        return null;
    }

    public void close() throws KettleException{
        write();
    }


}
