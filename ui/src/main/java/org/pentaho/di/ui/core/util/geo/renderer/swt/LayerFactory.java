package org.pentaho.di.ui.core.util.geo.renderer.swt;

import org.eclipse.swt.graphics.RGB;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.Hints;
import org.geotools.filter.FilterFactoryImpl;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.referencing.ReferencingFactoryFinder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.styling.*;
import org.geotools.styling.Stroke;
import org.opengis.filter.FilterFactory;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.awt.*;
import java.util.Arrays;
import java.util.Random;

/**
 * Factory to create the layers style and the map context. It deeply depends on geotools library.
 * 
 * @author mouattara, jmathieu & tbadard
 * @since 22-03-2009
 */
public class LayerFactory 
{
    private final String DEFAULT_MAP_TITLE = "Geographic Preview OMM";
    private final String DEFAULT_ABSTRACT = "This is a geographic preview";
    private final String DEFAULT_POINT_LAYER_STYLE_NAME = "Point Layer Style";
    private final String DEFAULT_POLYGON_LAYER_STYLE_NAME = "Polygon Layer Style";
    private final String DEFAULT_LINE_LAYER_STYLE_NAME = "Line Layer Style";
    private final String DEFAULT_ALL_STYLE_LAYER_NAME = "All Style";
    
    private final double DEFAULT_POINT_ROTATION = 0;
    
    public static final String DEFAULT_OPACITY="1.0";    
    public static final String DEFAULT_COLOR = "#000000"; 
    public static final String DEFAULT_STROKE_WIDTH = "1";
    public static final String DEFAULT_RADIUS = "6"; 

    private CoordinateReferenceSystem DEFAULT_CRS;
    private StyleFactory sf;
	private FilterFactory filterFactory;

    public LayerFactory(){
		DEFAULT_CRS = DefaultGeographicCRS.WGS84;
		sf = CommonFactoryFinder.getStyleFactory(new Hints(Hints.KEY_RENDERING, Hints.VALUE_RENDER_SPEED));
		filterFactory = new FilterFactoryImpl();
    }

    public CoordinateReferenceSystem getDefaultCRS(){
		return DEFAULT_CRS;
	}

    public CoordinateReferenceSystem createCRS(String crsWkt){
		CoordinateReferenceSystem crs;
		try {
			crs = ReferencingFactoryFinder.getCRSFactory(null).createFromWKT(crsWkt);
			return crs;
		}catch (FactoryException exception) {
			exception.printStackTrace();
			return null;
		}		
	}

    public static RGB getRandomColor(){
    	Color color = Color.getHSBColor( (new Random()).nextFloat(), 1.0F, 1.0F );
    	return new RGB(color.getRed(), color.getGreen(), color.getBlue());
    }
    
    public static RGB getDefaultColor(){
    	Color color = Color.getHSBColor( 0.0F, 0.0F, 0.0F );
    	return new RGB(color.getRed(), color.getGreen(), color.getBlue());
    }
    
    public MapContext createMapContext(){
		DefaultMapContext context = new DefaultMapContext(this.DEFAULT_CRS);		
		context.setTitle(this.DEFAULT_MAP_TITLE);
		context.setAbstract(this.DEFAULT_ABSTRACT);		

		return context;
	}

	private FeatureTypeStyle createDefaultPolygonFeatureTypeStyle(String strokeColor, String strokeWidth, String fillColor, String opacity){
		Stroke polygonStroke = sf.getDefaultStroke();
        polygonStroke.setWidth(filterFactory.literal(new Integer(strokeWidth)));
        polygonStroke.setColor(filterFactory.literal(strokeColor));
        polygonStroke.setOpacity(filterFactory.literal(opacity));
        
        Fill polygonFill = sf.getDefaultFill();
        polygonFill.setColor(filterFactory.literal(fillColor));        
        polygonFill.setOpacity(filterFactory.literal(opacity));      
        
        PolygonSymbolizer polySym = sf.createPolygonSymbolizer();
        polySym.setFill(polygonFill);
        polySym.setStroke(polygonStroke);

        Rule polygonRule = sf.createRule();
        polygonRule.symbolizers().add(polySym);
        FeatureTypeStyle polygonFeatureTypeStyle = sf.createFeatureTypeStyle(new Rule[]{polygonRule});
        
        return polygonFeatureTypeStyle;
	}

	private FeatureTypeStyle createDefaultPointFeatureTypeStyle(String radius, String color, String opacity){
		Fill pointFill = sf.getDefaultFill();
		pointFill.setColor(filterFactory.literal(color));        
		pointFill.setOpacity(filterFactory.literal(opacity));
        
		Stroke pointStroke = sf.getDefaultStroke();
		pointStroke.setWidth(filterFactory.literal(new Integer(DEFAULT_STROKE_WIDTH)));
		pointStroke.setColor(filterFactory.literal(DEFAULT_COLOR));
		pointStroke.setOpacity(filterFactory.literal(opacity));

		StyleBuilder sb = new StyleBuilder();
		Mark circle = sb.createMark(StyleBuilder.MARK_CIRCLE, pointFill,pointStroke);
		Graphic graph = sb.createGraphic(null, circle, null, Double.parseDouble(opacity), Double.parseDouble(radius) , DEFAULT_POINT_ROTATION);
        PointSymbolizer pointSymbolizer = sb.createPointSymbolizer(graph);
        
        Rule pointRule = sf.createRule();
        pointRule.symbolizers().add(pointSymbolizer);
        FeatureTypeStyle pointFeatureTypeStyle = sf.createFeatureTypeStyle(new Rule[]{pointRule});
		
        return pointFeatureTypeStyle;
	}

	private FeatureTypeStyle createDefaultLineFeatureTypeStyle(String strokeWidth, String strokeColor, String opacity){
		StyleFactory sf = CommonFactoryFinder.getStyleFactory(new Hints(Hints.KEY_RENDERING, Hints.VALUE_RENDER_SPEED));
		FilterFactory filterFactory = new FilterFactoryImpl();

		Stroke lineStroke = sf.getDefaultStroke();
        lineStroke.setWidth(filterFactory.literal(new Integer(strokeWidth)));
        lineStroke.setColor(filterFactory.literal(strokeColor));
        lineStroke.setOpacity(filterFactory.literal(opacity));
        
        LineSymbolizer lineSymbolizer = sf.createLineSymbolizer();
        lineSymbolizer.setStroke(lineStroke);
        
        Rule lineRule = sf.createRule();
        lineRule.symbolizers().add(lineSymbolizer);
        FeatureTypeStyle lineFeatureTypeStyle = sf.createFeatureTypeStyle(new Rule[]{lineRule});
        
        return lineFeatureTypeStyle;
	}

	public Style createDefaultPolygonLayerStyle(String strokeColor, String strokeWidth, String fillColor, String opacity){
		FeatureTypeStyle polygonFeatureTypeStyle = createDefaultPolygonFeatureTypeStyle(strokeColor, strokeWidth, fillColor, opacity);
		Style polygonStyle = sf.createStyle();
		polygonStyle.featureTypeStyles().add(polygonFeatureTypeStyle);
		polygonStyle.setName(DEFAULT_POLYGON_LAYER_STYLE_NAME);
		
		return polygonStyle;
	}

	public Style createDefaultLineLayerStyle(String strokeWidth, String strokeColor, String opacity){
		FeatureTypeStyle lineFeatureTypeStyle = createDefaultLineFeatureTypeStyle(strokeWidth, strokeColor, opacity);
		Style lineStyle = sf.createStyle();
		lineStyle.featureTypeStyles().add(lineFeatureTypeStyle);
		lineStyle.setName(DEFAULT_LINE_LAYER_STYLE_NAME);
		
		return lineStyle;	
	}

	public Style createDefaultPointLayerStyle(String radius, String color, String opacity){
		FeatureTypeStyle pointFeatureTypeStyle = createDefaultPointFeatureTypeStyle(radius, color, opacity);
		Style pointStyle = sf.createStyle();
		pointStyle.featureTypeStyles().add(pointFeatureTypeStyle);
		pointStyle.setName(DEFAULT_POINT_LAYER_STYLE_NAME);
		
		return pointStyle;	
	}

	public Style createDefaultLayerStyles(){
		FeatureTypeStyle polygonFeatureTypeStyle = createDefaultPolygonFeatureTypeStyle(DEFAULT_COLOR, DEFAULT_STROKE_WIDTH, DEFAULT_COLOR, DEFAULT_OPACITY);
		FeatureTypeStyle pointFeatureTypeStyle = createDefaultPointFeatureTypeStyle(DEFAULT_RADIUS, DEFAULT_COLOR, DEFAULT_OPACITY);
		FeatureTypeStyle lineFeatureTypeStyle = createDefaultLineFeatureTypeStyle(DEFAULT_STROKE_WIDTH, DEFAULT_COLOR, DEFAULT_OPACITY);

		Style allStyle = sf.createStyle();
		allStyle.featureTypeStyles().addAll(Arrays.asList(new FeatureTypeStyle[]{polygonFeatureTypeStyle, pointFeatureTypeStyle, lineFeatureTypeStyle}));
        allStyle.setName(DEFAULT_ALL_STYLE_LAYER_NAME);
		
		return allStyle;		
	}
}