package org.pentaho.di.ui.core.dialog.geopreview;

import com.vividsolutions.jts.geom.Geometry;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.util.geo.renderer.swt.LayerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

/**
 * Encapsulates data related to a layer. This class allows to define layers which appear when a geometry column is previewed.
 *
 * @author mouattara, jmathieu & tbadard
 * @since 22-03-2009
 */
public class Layer extends Observable {

    private static Class<?> PKG  = Layer.class;

    public static final int POINT_LAYER = 0;
    public static final int LINE_LAYER = 1;
    public static final int POLYGON_LAYER = 2;
    public static final int COLLECTION_LAYER = 3;

    private static final String POINTS = BaseMessages.getString(PKG,"layerControl.geometry.points");
    private static final String LINES = BaseMessages.getString(PKG,"layerControl.geometry.lines");
    private static final String POLYGONS = BaseMessages.getString(PKG,"layerControl.geometry.polygons");
    private static final String COLLECTIONS = BaseMessages.getString(PKG,"layerControl.geometry.collections");

    public final String[] labels = new String[]{POINTS, LINES, POLYGONS, COLLECTIONS};

    private final int DEFAULT_SIZE = 50;

    private final boolean DEFAULT_VISIBLE = true;

    private String name;

    private int type;

    private int displayCount;

    private boolean visible;

    private List<Symbolisation> style;

    private ArrayList<GeometryWrapper> geodata;

    private LayerCollection layerCollectionParent;

    public Layer(String name, int layerType, LayerCollection layerCollectionParent) {
        displayCount = 0;
        Symbolisation s;
        this.name = name + labels[layerType];
        this.type = layerType;
        this.layerCollectionParent = layerCollectionParent;
        visible = DEFAULT_VISIBLE;
        geodata = new ArrayList<GeometryWrapper>(DEFAULT_SIZE);
        style = new ArrayList<Symbolisation>();

        switch (layerType) {
            case POINT_LAYER:
                s = new Symbolisation(Symbolisation.PointColor, (Object) LayerFactory.getRandomColor());
                s.setLayerParent(this);
                style.add(s);
                s = new Symbolisation(Symbolisation.Radius, LayerFactory.DEFAULT_RADIUS);
                s.setLayerParent(this);
                style.add(s);
                s = new Symbolisation(Symbolisation.PointOpacity, LayerFactory.DEFAULT_OPACITY);
                s.setLayerParent(this);
                style.add(s);
                break;
            case LINE_LAYER:
                s = new Symbolisation(Symbolisation.LineStrokeWidth, LayerFactory.DEFAULT_STROKE_WIDTH);
                s.setLayerParent(this);
                style.add(s);
                s = new Symbolisation(Symbolisation.LineStrokeColor, (Object) LayerFactory.getRandomColor());
                s.setLayerParent(this);
                style.add(s);
                s = new Symbolisation(Symbolisation.LineOpacity, LayerFactory.DEFAULT_OPACITY);
                s.setLayerParent(this);
                style.add(s);
                break;
            case POLYGON_LAYER:
                s = new Symbolisation(Symbolisation.PolygonStrokeWidth, LayerFactory.DEFAULT_STROKE_WIDTH);
                s.setLayerParent(this);
                style.add(s);
                s = new Symbolisation(Symbolisation.PolygonStrokeColor, (Object) LayerFactory.getDefaultColor());
                s.setLayerParent(this);
                style.add(s);
                s = new Symbolisation(Symbolisation.PolygonFillColor, (Object) LayerFactory.getRandomColor());
                s.setLayerParent(this);
                style.add(s);
                s = new Symbolisation(Symbolisation.PolygonOpacity, LayerFactory.DEFAULT_OPACITY);
                s.setLayerParent(this);
                style.add(s);
                break;
            case COLLECTION_LAYER:
                Object color = (Object) LayerFactory.getRandomColor();
                s = new Symbolisation(Symbolisation.PointColor, color);
                s.setLayerParent(this);
                style.add(s);
                s = new Symbolisation(Symbolisation.Radius, LayerFactory.DEFAULT_RADIUS);
                s.setLayerParent(this);
                style.add(s);
                s = new Symbolisation(Symbolisation.PointOpacity, LayerFactory.DEFAULT_OPACITY);
                s.setLayerParent(this);
                style.add(s);
                s = new Symbolisation(Symbolisation.LineStrokeWidth, LayerFactory.DEFAULT_STROKE_WIDTH);
                s.setLayerParent(this);
                style.add(s);
                s = new Symbolisation(Symbolisation.LineStrokeColor, color);
                s.setLayerParent(this);
                style.add(s);
                s = new Symbolisation(Symbolisation.LineOpacity, LayerFactory.DEFAULT_OPACITY);
                s.setLayerParent(this);
                style.add(s);
                s = new Symbolisation(Symbolisation.PolygonStrokeWidth, LayerFactory.DEFAULT_STROKE_WIDTH);
                s.setLayerParent(this);
                style.add(s);
                s = new Symbolisation(Symbolisation.PolygonStrokeColor, (Object) LayerFactory.getDefaultColor());
                s.setLayerParent(this);
                style.add(s);
                s = new Symbolisation(Symbolisation.PolygonFillColor, color);
                s.setLayerParent(this);
                style.add(s);
                s = new Symbolisation(Symbolisation.PolygonOpacity, LayerFactory.DEFAULT_OPACITY);
                s.setLayerParent(this);
                style.add(s);
                break;
            default:
                break;
        }
    }

    public int getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Symbolisation> getStyle() {
        return style;
    }

    public void setStyle(List<Symbolisation> style) {
        this.style = style;
        update();
    }

    public void update() {
        setChanged();
        notifyObservers(this);
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
        update();
    }

    public void addGeometry(Geometry geom, boolean batchMode) {
        geodata.add(new GeometryWrapper(geom));
        if (!batchMode)
            update();
    }

    public int getGeometryCount() {
        return geodata.size();
    }

    public void setDisplayCount(int count) {
        displayCount = count;
    }

    public int getDisplayCount() {
        return this.type == COLLECTION_LAYER ? displayCount : getGeometryCount();
    }

    public GeometryWrapper getGeometry(int index) {
        return (GeometryWrapper) geodata.get(index);
    }

    public LayerCollection getLayerCollectionParent() {
        return layerCollectionParent;
    }
}
