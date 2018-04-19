package org.pentaho.di.ui.core.util.geo;

import com.vividsolutions.jts.geom.Geometry;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.pentaho.di.ui.core.util.geo.renderer.swt.LayerFactory;

//import org.geotools.feature.IllegalAttributeException;

/**
 * 
 * @author mouattara, jmathieu & tbadard
 * @since 22-03-2009
 */
public class GeometryConverter 
{
	public static SimpleFeatureType featureType;
	private static LayerFactory	layerFactory; 

	private static final String FEATURE_ATTRIBUTE_1_GEOM = "the_geom";
	private static final String FEATURE_TYPE_NAME = "MyFeature";

	static{
		layerFactory = new LayerFactory();
		SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
		builder.setCRS(GeometryConverter.layerFactory.getDefaultCRS());
		builder.add(GeometryConverter.FEATURE_ATTRIBUTE_1_GEOM, com.vividsolutions.jts.geom.Geometry.class);
		builder.setName(FEATURE_TYPE_NAME);
    	featureType = builder.buildFeatureType();
	}
	
	public static SimpleFeature JTSGeomToGeoToolsFeature(Geometry jtsGeom){
		String featureId= SimpleFeatureBuilder.createDefaultFeatureId();
		return SimpleFeatureBuilder.build(featureType,new Object[] {jtsGeom},featureId);
	}
	
	public static Geometry GeoToolsFeatureToJTSGeom(SimpleFeature feature){
		return (Geometry)feature.getDefaultGeometry();
	}
}
