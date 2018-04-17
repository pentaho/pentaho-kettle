package org.pentaho.di.core.geospatial;



import java.util.Calendar;
import java.util.TimeZone;

import org.gdal.ogr.DataSource;
import org.gdal.ogr.Driver;
import org.gdal.ogr.Feature;
import org.gdal.ogr.FeatureDefn;
import org.gdal.ogr.FieldDefn;
import org.gdal.ogr.Layer;
import org.gdal.ogr.ogr;
import org.gdal.ogr.ogrConstants;
import org.geotools.referencing.ReferencingFactoryFinder;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CRSFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;


/**
 * @author shudongping
 * @date 2018/04/17
 */
public class OGRReader {

    private static LogChannelInterface log = new LogChannel( "OGRReader" );
    private String ogrDataSourcePath;
    private boolean error;
    private boolean skipFailure;

    private String[] unsupportedDrivers = {"FileGDB","PostgresSQL/PostGIS","IDB","INGRES","OCI","MSSQLSpatial"};

    private String layerName;
    private String spatialFilter;
    private String attributeFilter;

    private DataSource ogrDataSource;
    private Layer ogrLayer;
    private FeatureDefn ogrLayerDefinition;

    public OGRReader(String dataSourcePath, String layerName, String spatialFilter, String attributeFilter, boolean skipFailure)
    {
        this.ogrDataSourcePath = dataSourcePath;
        this.layerName = layerName;
        this.spatialFilter = spatialFilter;
        this.attributeFilter = attributeFilter;
        this.skipFailure = skipFailure;
        error         = false;
        ogrDataSource = null;
        ogrLayer = null;
        ogrLayerDefinition = null;
    }

    public void open() throws KettleException
    {
        try {

            // try closing first
            close();

            // All OGR drivers are registered
            ogr.RegisterAll();
            // Try to open the data source in read only mode
            ogrDataSource = ogr.Open(ogrDataSourcePath, true);
            // If it fails, the data source is opened in read/write mode
            if (ogrDataSource == null)
            {
                ogrDataSource = ogr.Open(ogrDataSourcePath, false);
            }
            // Try to find the suitable driver for this data source
            Driver ogrDriver = ogrDataSource.GetDriver();

            for (int i=0;i<unsupportedDrivers.length;i++) {
                if (ogrDriver.getName().equals(unsupportedDrivers[i]))
                    throw new Exception("This driver is not supported.");
            }

            // Try to retrieve layer by name
            ogrLayer =  ogrDataSource.GetLayerByName(layerName);

            if (ogrLayer == null) {

                // Here we assume that each data source has at least one layer and we process the first one
                // that is not empty
                int nbLayers = ogrDataSource.GetLayerCount();
                for (int i=0;i<nbLayers;i++) {
                    ogrLayer =  ogrDataSource.GetLayer(i);
                    if (ogrLayer.GetFeatureCount()>0)
                        break;
                }
            }

            if ((spatialFilter != null) && !(spatialFilter.trim().equals(""))) {
                String[] spatialFilterArray = spatialFilter.split(",");
                ogrLayer.SetSpatialFilterRect(Double.parseDouble(spatialFilterArray[0]),Double.parseDouble(spatialFilterArray[1]),Double.parseDouble(spatialFilterArray[2]),Double.parseDouble(spatialFilterArray[3]));
            }

            if ((attributeFilter != null) && !(attributeFilter.trim().equals(""))) {
                ogrLayer.SetAttributeFilter(attributeFilter);
            }

            ogrLayerDefinition = ogrLayer.GetLayerDefn();

        }
        catch(Exception e) {
            throw new KettleException("Error opening the OGR data source: "+ogrDataSourcePath, e);
        }
    }

    public RowMetaInterface getFields() throws KettleException{
        String debug="get attributes from an OGR data source";
        RowMetaInterface row = new RowMeta();

        try{
            // Fetch all field information
            //
            debug="allocate data types";

            int nbrFieldCount = ogrLayerDefinition.GetFieldCount();
            FieldDefn ogrFieldDefinition = null;
            ValueMetaInterface value;
            int ogrFieldType;
            String ogrFieldName;

            for (int j = 0; j < nbrFieldCount; j++) {
                ogrFieldDefinition = ogrLayerDefinition.GetFieldDefn(j);

                if (log.isDebug()) debug="get attribute #"+j;

                value = null;
                ogrFieldType = ogrFieldDefinition.GetFieldType();
                ogrFieldName = ogrFieldDefinition.GetNameRef();

                switch (ogrFieldType) {
                    case ogrConstants.OFTInteger:
                        debug = "integer attribute";
                        value = new ValueMeta(ogrFieldName, ValueMetaInterface.TYPE_INTEGER);
                        value.setLength(ogrFieldDefinition.GetWidth());
                        break;
                    case ogrConstants.OFTReal:
                        debug = "double attribute";
                        value = new ValueMeta(ogrFieldName, ValueMetaInterface.TYPE_NUMBER);
                        value.setLength(ogrFieldDefinition.GetWidth());
                        value.setPrecision(ogrFieldDefinition.GetPrecision());
                        break;
                    case ogrConstants.OFTString:
                        debug = "string attribute";
                        value = new ValueMeta(ogrFieldName, ValueMetaInterface.TYPE_STRING);
                        value.setLength(ogrFieldDefinition.GetWidth());
                        break;
                    case ogrConstants.OFTWideString:
                        debug = "string attribute";
                        value = new ValueMeta(ogrFieldName, ValueMetaInterface.TYPE_STRING);
                        value.setLength(ogrFieldDefinition.GetWidth());
                        break;
                    case ogrConstants.OFTDate:
                        debug = "date attribute";
                        value = new ValueMeta(ogrFieldName, ValueMetaInterface.TYPE_DATE);
                        break;
                    case ogrConstants.OFTTime:
                        debug = "time attribute";
                        value = new ValueMeta(ogrFieldName, ValueMetaInterface.TYPE_DATE);
                        break;
                    case ogrConstants.OFTDateTime:
                        debug = "datetime attribute";
                        value = new ValueMeta(ogrFieldName, ValueMetaInterface.TYPE_DATE);
                        break;
                    //TODO Check if OGR OFTBinary data type actually matches TYPE_BOOLEAN?
                    case ogrConstants.OFTBinary:
                        debug = "binary attribute";
                        value = new ValueMeta(ogrFieldName, ValueMetaInterface.TYPE_BOOLEAN);
                        value.setLength(ogrFieldDefinition.GetWidth());
                        break;
                    // TODO Add other possible OGR data types (string|real|integer lists, etc.)
                    default:
                        value = new ValueMeta(ogrFieldName, ValueMetaInterface.TYPE_STRING);
                        value.setLength(ogrFieldDefinition.GetWidth());
                        //value.setPrecision(ogrFieldDefinition.GetPrecision());
                        break;
                }

                // TODO Check if the geometry column is processed by the default switch case when a geometry column exists
                if (value!=null)
                {
                    row.addValueMeta(value);
                }
            }
            // Add the geometry column
            value = null;
            if (ogrLayer.GetGeometryColumn().length()>0)
                value = new ValueMeta(ogrLayer.GetGeometryColumn(), ValueMetaInterface.TYPE_GEOMETRY);
            else if (ogrLayer.GetGeomType() != ogrConstants.wkbNone)
                value = new ValueMeta("the_geom", ValueMetaInterface.TYPE_GEOMETRY);
            if (value!=null) {
                value.setGeometrySRS(getSRS());
                row.addValueMeta(value);
            }
        }catch(Exception e){
            throw new KettleException("Error reading OGR data source metadata (in part "+debug+")", e);
        }

        return row;
    }

    public Object[] getRow(RowMetaInterface fields) throws KettleException{
        return getRow( RowDataUtil.allocateRowData(fields.size()) );
    }

    public Object[] getRow(Object[] r) throws KettleException{

        String debug = "";
        int ogrFieldsCount = 0;
        int k = 0;

        try{
            // Read the next record

            debug = "set the values in the row";

            // Set the values in the row ...

            Feature ogrFeature = null;
            Calendar cal = null;
            TimeZone tz = null;
            String tzString = null;
            int tzOffset = 0;

            ogrFeature = ogrLayer.GetNextFeature();

            if (ogrFeature == null)
                return null;

            ogrFieldsCount = ogrFeature.GetFieldCount();
            org.gdal.ogr.Geometry ogrGeometry = ogrFeature.GetGeometryRef();

            for (k = 0; k < ogrFieldsCount; k++) {
                debug = "getting value #"+k;
                int ogrFieldType = ogrFeature.GetFieldType(k);

                if (ogrFeature.IsFieldSet(k)) {

                    int[] pnYear = new int[1];
                    int[] pnMonth = new int[1];
                    int[] pnDay = new int[1];
                    int[] pnHour = new int[1];
                    int[] pnMinute = new int[1];
                    int[] pnSecond = new int[1];
                    int[] pnTZFlag = new int[1];

                    switch (ogrFieldType) {
                        case ogrConstants.OFTInteger:
                            debug = "integer attribute";
                            r[k] = new Long(((Integer)ogrFeature.GetFieldAsInteger(k)).longValue());
                            break;
                        case ogrConstants.OFTReal:
                            debug = "double attribute";
                            r[k] = ogrFeature.GetFieldAsDouble(k);
                            break;
                        case ogrConstants.OFTString:
                            debug = "string attribute";
                            r[k] = ogrFeature.GetFieldAsString(k);
                            break;
                        case ogrConstants.OFTWideString:
                            debug = "widestring attribute";
                            r[k] = ogrFeature.GetFieldAsString(k);
                            break;
                        case ogrConstants.OFTDate:
                        case ogrConstants.OFTTime:
                        case ogrConstants.OFTDateTime:
                            debug = "date attribute";
                            ogrFeature.GetFieldAsDateTime(k, pnYear, pnMonth, pnDay, pnHour, pnMinute, pnSecond, pnTZFlag);
                            if (pnTZFlag[0]>1) {
                                tzOffset = (pnTZFlag[0]-100)/4;
                                tzString = "GMT";
                                if (tzOffset<0)
                                    tzString+=tzOffset+":00";
                                else tzString+="+"+tzOffset+":00";
                                tz=TimeZone.getTimeZone(tzString);
                                cal = Calendar.getInstance(tz);
                            } else {
                                cal = Calendar.getInstance();
                            }
                            cal.set(pnYear[0], pnMonth[0]-1, pnDay[0], pnHour[0], pnMinute[0], pnSecond[0]);
                            r[k]=cal.getTime();
                            break;
                        // TODO Add the cases of other data types (binary, integer|real|string lists, etc.)
                        default:
                            debug = "default datatype attribute";
                            r[k] = ogrFeature.GetFieldAsString(k);
                            break;
                    }
                }else
                    r[k] = null;
            }

            if (ogrGeometry != null) {
                debug = "geometry attribute";
                Geometry jts_geom = new WKTReader().read(ogrGeometry.ExportToWkt());
                r[k] = jts_geom;
            } else if (k <= r.length-1)
                r[k] = null;

        }catch(Exception e){
            log.logError(toString(), "Unexpected error in part ["+debug+"] : "+e.toString());
            if (skipFailure) {
                log.logError(toString(), "But \"Skip failures\" option activated ... Continue processing with next row!");
                for (int i=k;i<ogrFieldsCount+1;i++)
                    r[i]=null;
                return r;
            } else {
                error = true;
                throw new KettleException("Unable to read row from the OGR data source", e);
            }
        }

        return r;
    }

    private SRS getSRS() throws KettleException {
        if (ogrLayer != null) {
            if(ogrLayer.GetSpatialRef() != null) {
                try {
                    CRSFactory crsFactory = ReferencingFactoryFinder.getCRSFactory(null);
                    CoordinateReferenceSystem crs = crsFactory.createFromWKT(ogrLayer.GetSpatialRef().ExportToWkt());

                    return new SRS(crs);
                }
                catch (FactoryException fe) {
                    // TODO Do we have to do anything else here, i.e. when a FactoryException occurs while parsing the CRS WKT of the data source?
                    return null;
                }
            }
            else {
                // TODO Do we have to do anything else here, i.e. when OGR SRS is set to unknown?
                return null;
            }
        }
        else {
            throw new KettleException("OGR data source is not open");
        }
    }

    public boolean close()
    {
        return true;
    }

    public boolean hasError()
    {
        return error;
    }

    public String toString()
    {
        if (ogrDataSourcePath != null)
            return ogrDataSourcePath;
        else return getClass().getName();
    }

    public String getVersionInfo()
    {
        return null;
    }

    public String getOgrDataSourcePath() {
        return ogrDataSourcePath;
    }

    public void setOgrDataSourcePath(String ogrDataSourcePath) {
        this.ogrDataSourcePath = ogrDataSourcePath;
    }

}
