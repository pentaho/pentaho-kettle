package org.pentaho.di.ui.core.dialog.geopreview;

import org.pentaho.di.i18n.BaseMessages;

/**
 * This class allows the personalisation of colors and opacity (backgound and lines) and of line styles (strokewidth)
 *
 * @author mouattara, jmathieu & tbadard
 * @since 22-03-2009
 */
public class Symbolisation {

    private static Class<?> PKG = Symbolisation.class;

    public static final int LineStrokeColor = 0;
    public static final int PolygonStrokeColor = 1;
    public static final int LineStrokeWidth = 2;
    public static final int PolygonStrokeWidth = 3;
    public static final int PolygonFillColor = 4;
    public static final int PointColor = 5;
    public static final int PointOpacity = 6;
    public static final int LineOpacity = 7;
    public static final int PolygonOpacity = 8;
    public static final int Radius = 9;

    public static final String STROKECOLOR = BaseMessages.getString(PKG,"layerControl.style.strokeColor");
    public static final String STROKEWIDTH = BaseMessages.getString(PKG,"layerControl.style.strokeWidth");
    public static final String FILLCOLOR = BaseMessages.getString(PKG,"layerControl.style.fillColor");
    public static final String OPACITY = BaseMessages.getString(PKG,"layerControl.style.opacity");
    public static final String RADIUS = BaseMessages.getString(PKG,"layerControl.style.radius");
    public static final String COLOR = BaseMessages.getString(PKG,"layerControl.style.color");
    public static final String COLLECTIONLINESTROKECOLOR = BaseMessages.getString(PKG,"layerControl.style.collectionLineStrokeColor");
    public static final String COLLECTIONPOLYGONSTROKECOLOR = BaseMessages.getString(PKG,"layerControl.style.collectionPolygonStrokeColor");
    public static final String COLLECTIONLINESTROKEWIDTH = BaseMessages.getString(PKG,"layerControl.style.collectionLineStrokeWidth");
    public static final String COLLECTIONPOLYGONSTROKEWIDTH = BaseMessages.getString(PKG,"layerControl.style.collectionPolygonStrokeWidth");
    public static final String COLLECTIONPOLYGONFILLCOLOR = BaseMessages.getString(PKG,"layerControl.style.collectionPolygonfillColor");
    public static final String COLLECTIONPOINTCOLOR = BaseMessages.getString(PKG,"layerControl.style.collectionPointColor");
    public static final String COLLECTIONPOINTOPACITY = BaseMessages.getString(PKG,"layerControl.style.collectionPointOpacity");
    public static final String COLLECTIONLINEOPACITY = BaseMessages.getString(PKG,"layerControl.style.collectionLineOpacity");
    public static final String COLLECTIONPOLYGONOPACITY = BaseMessages.getString(PKG,"layerControl.style.collectionPolygonOpacity");
    public static final String COLLECTIONPOINTRADIUS = BaseMessages.getString(PKG,"layerControl.style.collectionPointRadius");

    public static final String[] usage = new String[]{STROKECOLOR, STROKECOLOR, STROKEWIDTH, STROKEWIDTH, FILLCOLOR, COLOR, OPACITY, OPACITY, OPACITY, RADIUS, COLLECTIONLINESTROKECOLOR, COLLECTIONPOLYGONSTROKECOLOR, COLLECTIONLINESTROKEWIDTH, COLLECTIONPOLYGONSTROKEWIDTH,
            COLLECTIONPOLYGONFILLCOLOR, COLLECTIONPOINTCOLOR, COLLECTIONPOINTOPACITY, COLLECTIONLINEOPACITY, COLLECTIONPOLYGONOPACITY,
            COLLECTIONPOINTRADIUS};

    private int styleUsage;

    private Object featureStyle;//
    private Object lastFeatureStyle;//

    private Layer layerParent;

    public boolean isCustom;

    public void setStyleUsage(int usage) {
        this.styleUsage = usage;
    }

    public void setFeatureStyle(Object o) {
        this.featureStyle = o;
    }

    public void setLastFeatureStyle(Object o) {
        this.lastFeatureStyle = o;
    }

    public int getStyleUsage() {
        return styleUsage;
    }

    public Object getFeatureStyle() {
        return featureStyle;
    }

    public Object getLastFeatureStyle() {
        return lastFeatureStyle;
    }

    public void setLayerParent(Layer l) {
        this.layerParent = l;
    }

    public Symbolisation(int usage, Object fs) {
        this.styleUsage = usage;
        this.featureStyle = fs;
        setLastFeatureStyle(fs);
        isCustom = true;
    }

    public void setIsCustom(boolean isCustom) {
        this.isCustom = isCustom;
    }

    public boolean isCustom() {
        return isCustom;
    }

    public Layer getLayerParent() {
        return layerParent;
    }

    public void updateParent() {
        layerParent.update();
    }

    public String getUsage(int styleUsage) {
        return usage[styleUsage];
    }
}