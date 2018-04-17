package org.pentaho.di.core.geospatial;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;

import com.vividsolutions.jts.geom.GeometryFactory;

import de.micromata.opengis.kml.v_2_2_0.Boundary;
import de.micromata.opengis.kml.v_2_2_0.Container;
import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Feature;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Geometry;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.KmlFactory;
import de.micromata.opengis.kml.v_2_2_0.LineString;
import de.micromata.opengis.kml.v_2_2_0.LinearRing;
import de.micromata.opengis.kml.v_2_2_0.MultiGeometry;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Point;
import de.micromata.opengis.kml.v_2_2_0.Polygon;


/**
 * @author shudongping
 * @date 2018/04/17
 */
public class KMLReader {


    private Kml kml;
    private static LogChannelInterface log = new LogChannel( "KMLReader" );
    private java.net.URL kmlURL;
    private boolean error;

    private List<Placemark> place = new ArrayList<Placemark>();

    // Tableau contenant les geometries JTS obtenues
    private com.vividsolutions.jts.geom.Geometry[] geomlistJTS = new com.vividsolutions.jts.geom.Geometry[0];
    private List<String> geomname = new ArrayList<String>();// pour son champ name
    private List<String> geomdesc = new ArrayList<String>();// pour son champ description

    // Objet de la librairie JTS permettant la creation des geometries JTS
    private GeometryFactory JTSFactory = new GeometryFactory();

    private int k;// iterateur sur la rangee a ecrire

    public KMLReader(java.net.URL fileURL) {
        kmlURL = fileURL;
        kml = KmlFactory.createKml();
        k = 0;
    }

    // Methode permettant l'ouverture d'un fichier kml, le stockage de ses
    // informations et leur traitements pour obtenir une liste de geometrie de
    // type JTS

    public void open() throws KettleException {
        try {
            File file = new File(kmlURL.getPath());

            kml = Kml.unmarshal(file,true);// Etapes permettant la lecture d'un
            // fichier kml (XML) et son stockage
            // sous la forme d'un objet Kml
            // unmarshal(file):METHODE DE JAK

            // appel de la fonction recolterGeometryJTS qui va recolter toutes
            // les geometries JAK presentes dans kml et les transformer en
            // geometries JTS lisibles par GeooKettle et stockees dans un
            // tableau:geomlistJTS
            // NB: la difference entre geometrie JAK et JTS vient du format des
            // deux objets disctincts mais les geometries obeissent aux memes
            // regles
            // Ex: un polygone qqs son format est forme d'un anneau externe et
            // de possibles anneaux internes
            geomlistJTS = recolterGeometryJTS(kml);
        } catch (Exception e) {
            throw new KettleException("Error opening KML file at URL: "
                    + kmlURL, e);
        }
    }

    public com.vividsolutions.jts.geom.Geometry[] recolterGeometryJTS(Kml kml)
            throws KettleException {

        // getFeature():methode de JAK qui permet d'extraire de l'objet kml ses
        // features qui peuvent etre
        // soit un placemark (qui contient une et une seule geometrie)
        // soit un container:
        // -de type document (contient un ou plusieurs placemarks)
        // -de type folder (peut contenir differents placemarks ou document)
        Feature feature = kml.getFeature();

        if (feature instanceof de.micromata.opengis.kml.v_2_2_0.Container)
            parcourirContainer((Container) feature);// traitement des features de type
        else if (feature instanceof de.micromata.opengis.kml.v_2_2_0.Placemark)
            place.add((Placemark) feature);// stockage des placemark dans la liste place

        com.vividsolutions.jts.geom.Geometry[] geomlist = new com.vividsolutions.jts.geom.Geometry[place.size()];

        int j = 0;
        for (Placemark p : place) {
            geomname.add(p.getName());
            geomdesc.add(p.getDescription());
            geomlist[j] = gestionGeometryJAK(p.getGeometry());
            j++;
        }

        return geomlist;// retourne geomlist dans geomlistJTS
    }

    // Traitement des features de type container: parcourirContainer() doit
    // extraire tous les placemarks et les placer dans la liste place
    public void parcourirContainer(Container cont) {
        if (cont instanceof de.micromata.opengis.kml.v_2_2_0.Folder)
            parcourirFolder((Folder) cont);
        if (cont instanceof de.micromata.opengis.kml.v_2_2_0.Document)
            parcourirDocument((Document) cont);
    }

    // Traitement des container de type Document (rappel: ils possedent un ou
    // plusieurs palcemark)
    public void parcourirDocument(Document doc) {
        List<Feature> listdoc = doc.getFeature();// getFeature()(methode JAK)
        // associe a un objet document permet d'en extraire tous les feature retournes dans
        // une liste

        // Et en parcourant cette liste, si les features correspondent a un
        // placemark, on les place dans la liste de placemark a traiter
        for (Feature f : listdoc) {
            if (f != null) {
                if (f instanceof Placemark)
                    place.add((Placemark) f);
                if (f instanceof Folder)
                    parcourirFolder((Folder) f);
                if(f instanceof Document)
                    parcourirDocument((Document) f);
            }
        }
    }

    // Traitement des container de type Folder (rappel: ils possedent un ou
    // plusieurs placemark ou container)
    public void parcourirFolder(Folder fold) {
        List<Feature> listfold = fold.getFeature();// getFeature()(methode JAK)
        // associe a un objet folder permet d'en extraire tous les
        // feature retournes dans une liste

        // Et en parcourant celle ci
        for (Feature f : listfold) {
            if (f != null) {
                if (f instanceof Placemark)
                    place.add((Placemark) f);
                if (f instanceof Document)
                    parcourirDocument((Document) f);
                if (f instanceof Folder)
                    parcourirFolder((Folder) f);
            }
        }
    }

    // Traitement de chaque geometry JAK pour en ressortir la geometrie JTS
    // correpondante
    // Quelque soit la geometrie etudiee le principe est le meme, il faut
    // revenir aux coordonnees des points definissant la dite geometrie JAK,
    // attribuer les valeurs de chacun de ces coordonnees aux coordonnees de
    // type JTS, et grace a ces dernieres stockees dans un tableau et au
    // constructeur d'objet JTS: GeometryFactory JTSFactory, on construit
    // des geometries JAK.
    public com.vividsolutions.jts.geom.Geometry gestionGeometryJAK(Geometry g) {
        com.vividsolutions.jts.geom.Geometry geom = null;
        if (g instanceof de.micromata.opengis.kml.v_2_2_0.Point){
            geom = JTSFactory.createPoint(changementCoord(((Point) g)
                    .getCoordinates())[0]);
        }else if (g instanceof de.micromata.opengis.kml.v_2_2_0.LineString){
            geom = JTSFactory
                    .createLineString(changementCoord(((LineString) g)
                            .getCoordinates()));
        }else if (g instanceof de.micromata.opengis.kml.v_2_2_0.LinearRing){
            geom = changeLinearRingJAKenJTS((LinearRing) g);
        }else if (g instanceof de.micromata.opengis.kml.v_2_2_0.Polygon){
            Polygon pt = (Polygon) g;

            com.vividsolutions.jts.geom.LinearRing outer = changeLinearRingJAKenJTS(pt
                    .getOuterBoundaryIs().getLinearRing());

            List<Boundary> bound = pt.getInnerBoundaryIs();

            int j = 0;

            com.vividsolutions.jts.geom.LinearRing[] inner = new com.vividsolutions.jts.geom.LinearRing[bound.size()];

            for (Boundary b : bound) {
                inner[j] = changeLinearRingJAKenJTS(b.getLinearRing());
                j++;
            }
            geom = JTSFactory.createPolygon(outer, inner);
        } else if (g instanceof de.micromata.opengis.kml.v_2_2_0.MultiGeometry) {
            MultiGeometry mg = (MultiGeometry) g;
            List<de.micromata.opengis.kml.v_2_2_0.Geometry> listgeom2 = mg
                    .getGeometry();// getGeometry()(methode JAK) extrait toutes
            // les geometries JAK d'une multigeometrie JAK
            com.vividsolutions.jts.geom.Geometry[] listgeom3 = new com.vividsolutions.jts.geom.Geometry[listgeom2
                    .size()];

            // puis chacune de ces geometries JAK est traiter en reappelant la
            // fonction gestionGeometryJAK()et les geometries JTS obtenues
            // stockees dans un tableau
            int j = 0;
            for (Geometry ng : listgeom2) {
                listgeom3[j] = gestionGeometryJAK(ng);
                j++;
            }
            geom = JTSFactory.createGeometryCollection(listgeom3);
        }
        return geom;
    }

    // methode permettant de changer une liste d'objet coordonneeJAK en tableau
    // d'objet coordonnee JTS
    public com.vividsolutions.jts.geom.Coordinate[] changementCoord(
            List<de.micromata.opengis.kml.v_2_2_0.Coordinate> listJAK) {
        com.vividsolutions.jts.geom.Coordinate[] listJTS = new com.vividsolutions.jts.geom.Coordinate[listJAK
                .size()];
        int j = 0;

        // pour chaque coordonnee JAK, on cree un coordonnee JTS dont les
        // attributs x,y,z doivent correspondre au attributs longitude, latitude
        // et altitude de coordonnee JAK
        for (de.micromata.opengis.kml.v_2_2_0.Coordinate i : listJAK) {
            com.vividsolutions.jts.geom.Coordinate b = new com.vividsolutions.jts.geom.Coordinate();
            b.x = i.getLongitude();
            b.y = i.getLatitude();
            b.z = i.getAltitude();
            listJTS[j] = b;
            j = j + 1;
        }
        return listJTS;
    }

    // Methode specifique au traitement des anneaux
    public com.vividsolutions.jts.geom.LinearRing changeLinearRingJAKenJTS(
            de.micromata.opengis.kml.v_2_2_0.LinearRing l) {
        return JTSFactory
                .createLinearRing(this.changementCoord(l
                        .getCoordinates()));
    }

    // METHODE DE BASE: retourne un RowMetaInterface contenant les differents
    // champs (ValueMetaInterface) a extraire du fichier kml pour former une BDD
    // et leurs caracteristiques
    public RowMetaInterface getFields() throws KettleException {
        String debug = "get attributes from Geotools datastore";
        RowMetaInterface row = new RowMeta();
        try {
            debug = "allocate data types";
            debug = "geometry attribute";

            ValueMetaInterface value1 = new ValueMeta("name", ValueMetaInterface.TYPE_STRING);// le champ name issu des placemark
            ValueMetaInterface value2 = new ValueMeta("description",
                    ValueMetaInterface.TYPE_STRING);// le champ description issu des placemark
            ValueMetaInterface value3 = new ValueMeta("the_geom", ValueMetaInterface.TYPE_GEOMETRY);// le champ geometry

            if (value1 != null)
                row.addValueMeta(value1);
            if (value2 != null)
                row.addValueMeta(value2);
            if (value3 != null)
                row.addValueMeta(value3);
        } catch (Exception e) {
            throw new KettleException(
                    "Error reading KML file metadata (in part " + debug + ")",
                    e);
        }
        return row;
    }

    // METHODE DE BASE: appelee pour chaque ligne (entree de la BDD), cette
    // methode permet de remplir les champs (name, description et the_geom) pour
    // chacune des geometrie JTS obtenues
    public Object[] getRow(RowMetaInterface fields) throws KettleException {
        return getRow(RowDataUtil.allocateRowData(fields.size()));
    }

    public Object[] getRow(Object[] r) throws KettleException {
        String debug = "";
        try {
            if (k < geomlistJTS.length) {// on verifie donc que la methode n'a
                // pas deja parcouru toutes les geometries de la liste
                // geomlistJTS
                r[0] = geomname.get(k);// pour la geometry consideree, on
                // extrait le name de son placemark
                r[1] = geomdesc.get(k);// la description de son placemark
                r[2] = geomlistJTS[k];// et la geometrie en elle meme
                // qui sont stockes dans un tableau d'objet r, ou leur
                // emplacement designe leur champ

                k++;// on increment le nombre de fois ou la methode a ete
                // appelee ce qui correspond au numero de la prochaine ligne a
                // traiter
            } else
                return null;// acheve le step car la liste geomlistJTS a ete parcourue dans son ensemble
        } catch (Exception e) {
            log.logError(toString(), "Unexpected error in part [" + debug
                    + "] : " + e.toString());
            error = true;
            throw new KettleException(
                    "Unable to read row from KMLtools datastore", e);
        }
        return r;
    }

    public boolean close() {
        return false;
    }

    public boolean hasError() {
        return error;
    }

    public String toString() {
        return kmlURL != null?kmlURL.toString():getClass().getName();
    }

    public String getVersionInfo() {
        return null;
    }

    public java.net.URL getKmlURL() {
        return kmlURL;
    }

    public void setKmlURL(java.net.URL kmlURL) {
        this.kmlURL = kmlURL;
    }


}
