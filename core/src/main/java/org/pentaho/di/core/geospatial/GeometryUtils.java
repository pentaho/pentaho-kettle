package org.pentaho.di.core.geospatial;


import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;

/**
 * @author shudongping
 * @date 2018/04/17
 */
public class GeometryUtils {

    /**
     * Constructs an empty Geometry (i.e. "GEOMETRYCOLLECTION EMPTY")
     * with an unknown (-1) SRID
     *
     * @return
     */
    public static Geometry makeEmptyGeometry() {
        return makeEmptyGeometry(-1);
    }

    /**
     * Constructs an empty Geometry (i.e. "GEOMETRYCOLLECTION EMPTY")
     * using the SRID contained in the specified SRS
     *
     * @param srs
     * @return
     */
    public static Geometry makeEmptyGeometry(SRS srs) {
        return makeEmptyGeometry(srs.getSRID());
    }

    /**
     * Constructs an empty Geometry (i.e. "GEOMETRYCOLLECTION EMPTY")
     * with the specified SRID
     *
     * @param srid
     * @return
     */
    public static Geometry makeEmptyGeometry(int srid) {
        GeometryFactory gf = new GeometryFactory(new PrecisionModel(), srid);
        Geometry geom = gf.createGeometryCollection(new Geometry [] {});
        return geom;
    }

}
