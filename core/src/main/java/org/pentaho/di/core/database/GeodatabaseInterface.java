package org.pentaho.di.core.database;

import com.vividsolutions.jts.geom.Geometry;
import org.pentaho.di.core.geospatial.SRS;
import org.pentaho.di.core.row.ValueMetaInterface;

import java.sql.Connection;

/**
 * @author shudongping
 * @date 2018/04/18
 */
public interface GeodatabaseInterface {

    /**
     * Converts the RDBMS proprietary geometry object (e.g. PostGIS's PGeometry or Oracle's JGeometry)
     * to JTS's Geometry object.
     *
     * @param o  the proprietary geometry format (e.g. PGeometry for PostGIS, JGeometry for Oracle etc.).
     * @param db the Kettle Database object for the current connection
     * @return the Geometry object from the JTS library.
     */
    Geometry convertToJTSGeometry(ValueMetaInterface vmi, Object o, Database db);

    /**
     * Converts the JTS Geometry object to the RDBMS proprietary geometry object (e.g. PostGIS's
     * PGeometry or Oracle's JGeometry).
     *
     * @param geom the JTS Geometry object to convert.
     * @param db   the Kettle Database object for the current connection
     * @return the RDBMS proprietary geometry object.
     */
    Object convertToObject(ValueMetaInterface vmi, Geometry geom, Database db);

    /**
     * Returns the DBMS SRID according to an EPSG-SRID.
     *
     * @param epsg_srid The EPSG-SRID to lookup.
     * @return The DBMS-SRID from the cache.
     */
    int convertToDBMS_SRID(SRS epsg_srid, Connection conn);

    /**
     * Returns the SRS metadata according to a RDBMS proprietary SRID.
     *
     * @param dbms_srid The DBMS-SRID to lookup.
     * @return The EPSG-SRID from the cache.
     */
    SRS convertToEPSG_SRID(int dbms_srid, Connection conn);


//-- End GeoKettle modification --

}
