package org.pentaho.di.core.geospatial;


import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;

import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.KmlFactory;
import de.micromata.opengis.kml.v_2_2_0.MultiGeometry;
import de.micromata.opengis.kml.v_2_2_0.Placemark;

/**
 * @author shudongping
 * @date 2018/04/17
 */
public class KMLWriter {

    private boolean error;
    private java.net.URL kmlURL;
    public File file;
    public Kml kml;
    private Document doc;
    private int namecolumn;
    private boolean testname;
    private int desccolumn;
    private boolean testdesc;
    private int geomcolumn;

    public KMLWriter(java.net.URL fileURL) {
        kmlURL = fileURL;
        file = new File(kmlURL.getPath());
        kml = KmlFactory.createKml();
        doc = kml.createAndSetDocument();
        error = false;
        testdesc = false;
        testname = false;
    }

    public void open() throws KettleException {
        try {
            // try closing first
            close();

            // TODO: detect file type and instanciate the right type of
            // DataStore
            // implementation (to support file formats other than Shapefile)

            // TODO: make charset configurable (in the step dialog box?)
            if (!kmlURL
                    .toString()
                    .substring(kmlURL.toString().length() - 3,
                            kmlURL.toString().length()).equalsIgnoreCase("KML"))
                throw new KettleException(
                        "The output specified is not in kml file format (.kml)");
        } catch (Exception e) {
            throw new KettleException("Error opening KML file at URL: "
                    + kmlURL, e);
        }
    }

    // en fonction de la geometrie JTS en entree cette metode cree un placemark
    // contenant la geometrie JAK correspondante.
    public void createNewPlacemark(Object[] r) throws FileNotFoundException {
        Geometry geomJTS = (Geometry) r[geomcolumn];

        // creation du placemark dans un document
        Placemark pl = doc.createAndAddPlacemark();

        // si il existe des champ name et desciption on attribut leur valeur au
        // placemark
        if (testname)
            pl.withName(r[namecolumn].toString());
        if (testdesc)
            pl.withDescription(r[desccolumn].toString());

        if (geomJTS instanceof Point || geomJTS instanceof MultiPoint){
            Point pJTS = geomJTS instanceof MultiPoint?(Point) ((MultiPoint) geomJTS).getGeometryN(0):(Point) geomJTS;
            pl.createAndSetPoint().withCoordinates(
                    changerCoordinates(pJTS.getCoordinate()));
        }else if (geomJTS instanceof LineString || geomJTS instanceof MultiLineString){
            LineString lJTS = geomJTS instanceof MultiLineString?(LineString) ((MultiLineString) geomJTS).getGeometryN(0):(LineString) geomJTS;
            pl.createAndSetLineString()
                    .withCoordinates(
                            changerCoordinates(lJTS
                                    .getCoordinates()));
        }else if (geomJTS instanceof LinearRing){
            pl.createAndSetLinearRing()
                    .withCoordinates(
                            changerCoordinates(((LinearRing) geomJTS)
                                    .getCoordinates()));
        }else if (geomJTS instanceof Polygon || geomJTS instanceof MultiPolygon) {
            Polygon polyJTS = geomJTS instanceof MultiPolygon?(Polygon) ((MultiPolygon) geomJTS).getGeometryN(0):(Polygon) geomJTS;
            de.micromata.opengis.kml.v_2_2_0.LinearRing extlrJAK = KmlFactory
                    .createLinearRing()
                    .withCoordinates(
                            changerCoordinates(changerLineStringenLineRing(
                                    polyJTS.getExteriorRing()).getCoordinates()));

            de.micromata.opengis.kml.v_2_2_0.Polygon polyJAK = pl
                    .createAndSetPolygon();
            polyJAK.createAndSetOuterBoundaryIs().withLinearRing(extlrJAK);
            int noir = polyJTS.getNumInteriorRing();
            for (int i = 0; i < noir; i++) {
                de.micromata.opengis.kml.v_2_2_0.LinearRing intlrJAK = KmlFactory
                        .createLinearRing().withCoordinates(
                                changerCoordinates(changerLineStringenLineRing(
                                        polyJTS.getInteriorRingN(i))
                                        .getCoordinates()));
                polyJAK.createAndAddInnerBoundaryIs().withLinearRing(intlrJAK);
            }
        }else if (geomJTS instanceof GeometryCollection) {
            GeometryCollection gc = (GeometryCollection) geomJTS;
            MultiGeometry mg = pl.createAndSetMultiGeometry();

            int nog = gc.getNumGeometries();
            for (int j = 0; j < nog; j++) {
                Geometry gJTS = gc.getGeometryN(j);
                if (gJTS instanceof Point)
                    mg.createAndAddPoint().withCoordinates(
                            changerCoordinates(((Point) gJTS).getCoordinate()));
                else if (gJTS instanceof LineString)
                    mg.createAndAddLineString().withCoordinates(
                            changerCoordinates(((LineString) gJTS)
                                    .getCoordinates()));
                else if (gJTS instanceof LinearRing)
                    mg.createAndAddLinearRing().withCoordinates(
                            changerCoordinates(((LinearRing) gJTS)
                                    .getCoordinates()));
                else if (gJTS instanceof Polygon) {
                    Polygon polyJTS = (Polygon) gJTS;
                    de.micromata.opengis.kml.v_2_2_0.LinearRing extlrJAK = KmlFactory
                            .createLinearRing()
                            .withCoordinates(
                                    changerCoordinates(changerLineStringenLineRing(
                                            polyJTS.getExteriorRing())
                                            .getCoordinates()));
                    de.micromata.opengis.kml.v_2_2_0.Polygon polyJAK = mg
                            .createAndAddPolygon();
                    polyJAK.createAndSetOuterBoundaryIs().withLinearRing(
                            extlrJAK);
                    int noir = polyJTS.getNumInteriorRing();
                    for (int l = 0; l < noir; l++) {
                        de.micromata.opengis.kml.v_2_2_0.LinearRing intlrJAK = KmlFactory
                                .createLinearRing()
                                .withCoordinates(
                                        changerCoordinates(changerLineStringenLineRing(
                                                polyJTS.getInteriorRingN(l))
                                                .getCoordinates()));
                        polyJAK.createAndAddInnerBoundaryIs().withLinearRing(
                                intlrJAK);
                    }
                }
            }
        }
    }

    // fonction speciale compte tenu des methode de JTS qui ne retourne pas les
    // anneaux des polygones mais plutot les lignes correspondantes qu'il nous
    // faut passer en anneau
    private LinearRing changerLineStringenLineRing(LineString ls) {
        GeometryFactory JTSFactory = new GeometryFactory();
        com.vividsolutions.jts.geom.Coordinate[] coordls = ls.getCoordinates();
        com.vividsolutions.jts.geom.Coordinate[] coordlr = new com.vividsolutions.jts.geom.Coordinate[coordls.length];
        for (int i = 0; i < coordls.length; i++) {
            coordlr[i] = coordls[i];
        }
        return JTSFactory.createLinearRing(coordlr);
    }

    // methode de changement de coordonee de JTS a JAK
    private List<Coordinate> changerCoordinates(
            com.vividsolutions.jts.geom.Coordinate[] coord) {
        List<Coordinate> list = new ArrayList<Coordinate>();
        for (int i = 0; i < coord.length; i++) {
            Coordinate c = KmlFactory.createCoordinate(0, 0, 0);
            c.setLongitude(coord[i].x);
            c.setLatitude(coord[i].y);
            c.setAltitude(coord[i].z);
            list.add(c);
        }
        return list;
    }

    // idem
    private List<Coordinate> changerCoordinates(
            com.vividsolutions.jts.geom.Coordinate coord) {
        List<Coordinate> list = new ArrayList<Coordinate>();
        Coordinate c = KmlFactory.createCoordinate(0, 0, 0);
        c.setLongitude(coord.x);
        c.setLatitude(coord.y);
        c.setAltitude(coord.z);
        list.add(c);
        return list;
    }

    // verifie la presence d'un champ de type geometry
    public boolean checkKmlPossibility(RowMetaInterface fields) {
        boolean test = false;
        for (int i = 0; i < fields.size(); i++) {
            if (fields.getValueMeta(i).getType() == ValueMeta.TYPE_GEOMETRY)
                test = true;
        }
        return test;
    }

    // verifie qu'il y a bien un chmap de type geometrie
    // et repere la position des champs interessants
    public void visionTable(RowMetaInterface fields) throws KettleException {
        if (checkKmlPossibility(fields) == false)
            throw new KettleException(
                    "There is no geometric attribute, so there is no possibility to create a Kml file");

        for (int i = 0; i < fields.size(); i++) {
            if (fields.getValueMeta(i).getType() == ValueMeta.TYPE_GEOMETRY)
                geomcolumn = i;
        }

        for (int i = 0; i < fields.size(); i++) {
            ValueMetaInterface value = fields.getValueMeta(i);
            if (value.getType() == ValueMeta.TYPE_STRING
                    && value.getName() == "name"){
                namecolumn = i;
                testname = true;
            }
            if (value.getType() == ValueMeta.TYPE_STRING
                    && value.getName() == "description"){
                desccolumn = i;
                testdesc = true;
            }
        }
    }

    public void write() throws KettleException {
    }

    public boolean close() {
        return false;
    }

    public boolean hasError() {
        return error;
    }

    public String getVersionInfo() {
        return null;
    }


}
