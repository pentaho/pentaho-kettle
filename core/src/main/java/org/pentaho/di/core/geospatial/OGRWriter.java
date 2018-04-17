package org.pentaho.di.core.geospatial;


import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import org.gdal.gdal.gdal;
import org.gdal.ogr.DataSource;
import org.gdal.ogr.Driver;
import org.gdal.ogr.Feature;
import org.gdal.ogr.FieldDefn;
import org.gdal.ogr.Layer;
import org.gdal.ogr.ogr;
import org.gdal.ogr.ogrConstants;
import org.gdal.osr.SpatialReference;
import org.pentaho.di.core.Const;
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
public class OGRWriter {

    private static final int OGR_CREATION_MODE=0;
    private static final int OGR_OVERRIDE_MODE=1;
    private static final int OGR_UPDATE_MODE=2;
    private static final int OGR_APPEND_MODE=3;
    private static final int OGR_DELETE_MODE=4;

    private static LogChannelInterface log = new LogChannel( "OGRWriter" );
    private boolean     error;
    private String ogrDataDestinationPath;
    private DataSource ogrDataDestination;
    private boolean isFileDataSource;
    private Layer ogrLayer;
    private String ogrLayerName;
    private String ogrDataFormat;
    private String ogrOptions;
    private int ogrGeomType;
    private int ogrWriteMode;
    private String ogrFIDField;
    //private String encoding;
    private boolean preserveFIDField;
    private Driver ogrDriver;
    private Vector<String> ogrDataDestinationOptions;
    private org.gdal.ogr.Geometry ogrGeometry;
    private SpatialReference ogrSpatialReference;

    //public OGRWriter(String dataDestinationPath, boolean isFileDataSource, String format, String options, int geomType, String layerName, int writeMode, String fidField, boolean isFIDFieldPreserved, String encoding){
    public OGRWriter(String dataDestinationPath, boolean isFileDataSource, String format, String options, int geomType, String layerName, int writeMode, String fidField, boolean isFIDFieldPreserved){
        ogrDataDestinationPath = dataDestinationPath;
        this.isFileDataSource = isFileDataSource;
        error = false;
        ogrLayer = null;
        ogrLayerName = layerName;
        ogrDataFormat = format;
        ogrOptions = options;
        ogrGeomType = geomType;
        ogrWriteMode = writeMode;
        ogrFIDField = fidField;
        preserveFIDField = isFIDFieldPreserved;
        ogrDriver = null;
        //this.encoding = encoding;
        ogrGeometry = null;
        ogrSpatialReference = new SpatialReference();
        ogrDataDestinationOptions = new Vector<String>();

        if (ogrOptions!= null) {
            String[] ogr_options = ogrOptions.trim().split(" ");
            for(int i=0;i<ogr_options.length;i++)
                ogrDataDestinationOptions.addElement(ogr_options[i]);
        }

    }

    public void open() throws KettleException
    {
        try {

            // try closing first
            close();

            ogr.RegisterAll();

            for(int i = 0; i < ogr.GetDriverCount() && ogrDriver == null; i++)
            {
                if( ogr.GetDriver(i).GetName().equalsIgnoreCase(ogrDataFormat) )
                {
                    ogrDriver = ogr.GetDriver(i);
                }
            }

            if (isFileDataSource) {
                if (Const.isWindows()) {
                    ogrDataDestinationPath = ogrDataDestinationPath.replace('/', '\\');
                    //} else {
                    //ogrDataDestinationPath = ogrDataDestinationPath.substring(2);
                }

                //				if ((new File(ogrDataDestinationPath)).exists())
                //					if (ogrDriver.TestCapability( ogr.ODrCDeleteDataSource ))
                //						ogrDriver.DeleteDataSource(ogrDataDestinationPath);
            }

            // We try to open the datasource ...
            ogrDataDestination = ogr.Open(ogrDataDestinationPath, true);

            if (ogrWriteMode == OGR_CREATION_MODE && ogrDataDestination == null) {

                if( ogrDriver.TestCapability( ogr.ODrCCreateDataSource ) == false ) {
                    //TODO i18n required there!
                    throw new Exception("Selected driver does not support data source creation.");
                }

                // We create the datasource if it does not exist and we are in creation mode
                ogrDataDestination = ogrDriver.CreateDataSource(ogrDataDestinationPath, ogrDataDestinationOptions);

            }

            if (ogrDataDestination == null) {
                //TODO i18n required there!
                throw new Exception("Unable to open or to create the output datasource: "+ogrDataDestinationPath);
            }

        }
        catch (Exception e) {
            //TODO i18n required there!
            throw new KettleException("Error opening OGR data destination: "+ogrDataDestinationPath, e);
        }
    }

    public void createLayer(RowMetaInterface fields) throws KettleException
    {
        String debug="get attributes from table";

        try
        {
            debug = "create layer";

            SpatialReference sr = new SpatialReference();
            for(int i = 0; i < fields.size(); i++)
            {
                ValueMetaInterface value = fields.getValueMeta(i);
                if (value.getType()==ValueMeta.TYPE_GEOMETRY) {
                    SRS srs = value.getGeometrySRS();
                    if (srs!=null) {
                        if (srs.getCRS()!=null) {
                            sr.ImportFromWkt(srs.getCRS().toWKT());
                        }
                    }
                    break;
                }
            }

            if ((ogrWriteMode == OGR_APPEND_MODE) || (ogrWriteMode == OGR_OVERRIDE_MODE) || (ogrWriteMode == OGR_UPDATE_MODE) || (ogrWriteMode == OGR_DELETE_MODE)) {

                gdal.PushErrorHandler("CPLQuietErrorHandler");
                ogrLayer = ogrDataDestination.GetLayerByName(ogrLayerName);
                gdal.PopErrorHandler();
                gdal.ErrorReset();

                int iLayer = -1;
                if( ogrLayer != null )
                {
                    int nLayerCount = ogrDataDestination.GetLayerCount();
                    for( iLayer = 0; iLayer < nLayerCount; iLayer++ )
                    {
                        Layer        poLayer = ogrDataDestination.GetLayer(iLayer);

                        if( poLayer != null
                                && poLayer.GetName().equals(ogrLayer.GetName()) )
                        {
                            break;
                        }
                    }

                    if (iLayer == nLayerCount) {
                        ogrLayer = null;
                    }

                } else {

                    // Detect cases where a layer can not be retrieved by name. Usually, it happens
                    // when a data format manages only one layer (with no name).
                    int nLayerCount = ogrDataDestination.GetLayerCount();
                    for( iLayer = 0; iLayer < nLayerCount; iLayer++ )
                    {
                        Layer        poLayer = ogrDataDestination.GetLayer(iLayer);

                        if( poLayer != null )
                        {
                            ogrLayer = poLayer;
                            break;
                        }
                    }

                }

                if( ogrLayer != null && ogrWriteMode == OGR_OVERRIDE_MODE )
                {
                    if( ogrDataDestination.DeleteLayer(iLayer) != 0 )
                    {
                        throw new Exception("DeleteLayer failed when overwrite requested" ); //TODO i18n required there!

                    }
                    ogrLayer = null;
                }


            }

            if ((ogrWriteMode == OGR_CREATION_MODE) || (ogrWriteMode == OGR_OVERRIDE_MODE)) {

                gdal.ErrorReset();

                if (ogrLayerName!=null)
                    ogrLayer = ogrDataDestination.CreateLayer(ogrLayerName,sr,ogrGeomType,ogrDataDestinationOptions);

                if (ogrLayer==null) {

                    ogrLayerName = ogrDataDestination.GetName();

                    // Works if data destination is a file
                    if (Const.isWindows()) {
                        if (ogrLayerName.lastIndexOf('\\')!=-1)
                            ogrLayerName = ogrLayerName.substring(ogrLayerName.lastIndexOf('\\')+1);
                    } else {
                        if (ogrLayerName.lastIndexOf('/')!=-1)
                            ogrLayerName = ogrLayerName.substring(ogrLayerName.lastIndexOf('/')+1);
                    }
                    if (ogrLayerName.lastIndexOf('.')!=-1)
                        ogrLayerName = ogrLayerName.substring(0, ogrLayerName.lastIndexOf('.'));

                    ogrLayer = ogrDataDestination.CreateLayer(ogrLayerName,sr,ogrGeomType,ogrDataDestinationOptions);

                }

                if (ogrLayer==null) {
                    ogrLayerName="spatialytics";
                    ogrLayer = ogrDataDestination.CreateLayer(ogrLayerName,sr,ogrGeomType,ogrDataDestinationOptions);
                }

                // Fetch all field information
                //
                debug="allocate data types";
                FieldDefn ogrFieldDefinition = null;

                for(int i = 0; i < fields.size(); i++)
                {
                    if (log.isDebug()) debug="get attribute #"+i;

                    ValueMetaInterface value = fields.getValueMeta(i);
                    switch(value.getType()) {
                        case ValueMeta.TYPE_NUMBER:
                            ogrFieldDefinition = new FieldDefn(value.getName(), ogrConstants.OFTReal);
                            // NOTE: I have switch the writing of width and precision attributes because MapInfo driver fails
                            // to write real values correctly if they are set. Seems to be a bug in GDAL/OGR.
                            //ogrFieldDefinition.SetWidth(value.getLength());
                            //ogrFieldDefinition.SetPrecision(value.getPrecision());
                            ogrLayer.CreateField(ogrFieldDefinition);
                            break;
                        case ValueMeta.TYPE_STRING:
                            ogrFieldDefinition = new FieldDefn(value.getName(), ogrConstants.OFTString);
                            ogrFieldDefinition.SetWidth(value.getLength());
                            ogrLayer.CreateField(ogrFieldDefinition);
                            break;
                        case ValueMeta.TYPE_DATE:
                            ogrFieldDefinition = new FieldDefn(value.getName(), ogrConstants.OFTDateTime);
                            ogrLayer.CreateField(ogrFieldDefinition);
                            break;
                        //TODO Check if OFTBinary actually matches the TYPE_BOOLEAN data type
                        case ValueMeta.TYPE_BOOLEAN:
                            ogrFieldDefinition = new FieldDefn(value.getName(), ogrConstants.OFTBinary);
                            ogrFieldDefinition.SetWidth(value.getLength());
                            ogrLayer.CreateField(ogrFieldDefinition);
                            break;
                        case ValueMeta.TYPE_INTEGER:
                            ogrFieldDefinition = new FieldDefn(value.getName(), ogrConstants.OFTInteger);
                            ogrFieldDefinition.SetWidth(value.getLength());
                            if (value.getName().equals(ogrFIDField)) {
                                if (preserveFIDField) {
                                    ogrLayer.CreateField(ogrFieldDefinition);
                                }
                            } else {
                                ogrLayer.CreateField(ogrFieldDefinition);
                            }
                            break;
                        case ValueMeta.TYPE_BIGNUMBER:
                            ogrFieldDefinition = new FieldDefn(value.getName(), ogrConstants.OFTReal);
                            // NOTE: I have switch the writing of width and precision attributes because MapInfo driver fails
                            // to write real values correctly if they are set. Seems to be a bug in GDAL/OGR.
                            //ogrFieldDefinition.SetWidth(value.getLength());
                            //ogrFieldDefinition.SetPrecision(value.getPrecision());
                            ogrLayer.CreateField(ogrFieldDefinition);
                            break;
                        case ValueMeta.TYPE_GEOMETRY:
                            break;
                        case ValueMeta.TYPE_NONE:
                        case ValueMeta.TYPE_SERIALIZABLE:
                        case ValueMeta.TYPE_BINARY:
                        default:
                            throw new KettleException("Wrong object type for OGR data destination field: "
                                    + ValueMetaInterface.typeCodes[value.getType()]);
                    }

                }
            }
        }
        catch (Exception e) {
            throw new KettleException("Error reading metadata (in part "+debug+")", e);
        }
    }

	/*private String encode(String s){
		try {
			return new String(s.getBytes(encoding));
		} catch (UnsupportedEncodingException e) {
			return s;
		}
	}*/

    public void putRow(Object[] r, RowMetaInterface fields) throws KettleException
    {
        String debug = "access to layer definition";

        try {

            ValueMetaInterface value = null;
            Feature ogrFeature = new Feature(ogrLayer.GetLayerDefn());
            //int j=0;
            for(int i = 0; i < fields.size(); i++)
            {
                value = fields.getValueMeta(i);
                switch(value.getType()) {
                    case ValueMeta.TYPE_NUMBER:
                        debug = "double attribute "+i;
                        //ogrFeature.SetField(j, ((Double)r[i]).doubleValue());
                        ogrFeature.SetField(value.getName(), ((Double)r[i]).doubleValue());
                        //j++;
                        break;
                    case ValueMeta.TYPE_STRING:
                        debug = "string attribute "+i;
                        //ogrFeature.SetField(j, encode((String)r[i]));
                        //ogrFeature.SetField(j, (String)r[i]);
                        ogrFeature.SetField(value.getName(), (String)r[i]);
                        //j++;
                        break;
                    case ValueMeta.TYPE_DATE:
                        debug = "date attribute "+i;
                        Calendar cal = Calendar.getInstance();
                        cal.setTime((Date)r[i]);
                        int tzflag = 100+(cal.get(Calendar.ZONE_OFFSET)/1000/3600)*4;
                        //ogrFeature.SetField(j, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH)+1, cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND), tzflag);
                        ogrFeature.SetField(value.getName(), cal.get(Calendar.YEAR), cal.get(Calendar.MONTH)+1, cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND), tzflag);
                        //j++;
                        break;
                    case ValueMeta.TYPE_BOOLEAN:
                        debug = "boolean attribute "+i;
                        //ogrFeature.SetField(j, (Integer)r[i]);
                        ogrFeature.SetField(value.getName(), (Integer)r[i]);
                        //j++;
                        break;
                    case ValueMeta.TYPE_INTEGER:
                        debug = "integer attribute "+i;
                        if (value.getName().equals(ogrFIDField)) {
                            ogrFeature.SetFID(((Long)r[i]).intValue());
                            if (preserveFIDField) {
                                //ogrFeature.SetField(j, (Long)r[i]);
                                ogrFeature.SetField(value.getName(), (Long)r[i]);
                            }
                        } else {
                            //ogrFeature.SetField(j, (Long)r[i]);
                            ogrFeature.SetField(value.getName(), (Long)r[i]);
                        }
                        //j++;
                        break;
                    case ValueMeta.TYPE_BIGNUMBER:
                        debug = "big number attribute "+i;
                        //ogrFeature.SetField(j, ((BigDecimal)r[i]).doubleValue());
                        ogrFeature.SetField(value.getName(), ((BigDecimal)r[i]).doubleValue());
                        //j++;
                        break;
                    // TODO We have to handle here the case where there are more than one geometry fields!
                    case ValueMeta.TYPE_GEOMETRY:
                        debug = "geometry attribute "+i;
                        ogrGeometry = org.gdal.ogr.Geometry.CreateFromWkt(((Geometry)r[i]).toText());
                        SRS srs = value.getGeometrySRS();
                        if (srs!=null) {
                            if (srs.getCRS()!=null) {
                                ogrSpatialReference.ImportFromWkt(srs.getCRS().toWKT());
                                ogrGeometry.AssignSpatialReference(ogrSpatialReference);
                            }
                        }
                        ogrFeature.SetGeometry(ogrGeometry);
                        break;
                    case ValueMeta.TYPE_NONE:
                    case ValueMeta.TYPE_SERIALIZABLE:
                    case ValueMeta.TYPE_BINARY:
                    default:
                        debug = "default data type attribute "+i;
                        //ogrFeature.SetField(j, encode((String)r[i]));
                        //ogrFeature.SetField(j, (String)r[i]);
                        ogrFeature.SetField(value.getName(), (String)r[i]);
                        //j++;
                        break;
                }
            }

            //System.out.println(">>> FID= "+ogrFeature.GetFID());

            if (ogrWriteMode == OGR_UPDATE_MODE)
                ogrLayer.SetFeature(ogrFeature);
            else if (ogrWriteMode == OGR_DELETE_MODE)
                ogrLayer.DeleteFeature(ogrFeature.GetFID());
            else ogrLayer.CreateFeature(ogrFeature);

            //System.out.println(">>> FID= "+ogrFeature.GetFID());

        }
        catch (Exception e) {
            throw new KettleException("An error has occured while writing features ("+debug+"):", e);
        }
    }

    public boolean close()
    {
        boolean retval = false;
        try
        {
            if (ogrDataDestination != null) ogrDataDestination.delete();
            retval=true;
        }
        catch (Exception e)
        {
            log.logError(toString(), "Couldn't close the OGR data destination ["+ogrDataDestinationPath+"] : "+e.toString());
            error = true;
        }

        return retval;
    }

    public boolean hasError()
    {
        return error;
    }

    public String getVersionInfo()
    {
        return null;
    }

}
