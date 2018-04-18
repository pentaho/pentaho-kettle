package org.pentaho.di.core.row.value;

import com.vividsolutions.jts.geom.Geometry;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.ValueMetaInterface;

/**
 * @author shudongping
 * @date 2018/04/18
 */
public class ValueMetaGeometry extends ValueMetaBase implements ValueMetaInterface {

    public ValueMetaGeometry() {
        this( null );
    }

    public ValueMetaGeometry(String name) {
        super(name,ValueMetaInterface.TYPE_GEOMETRY);
    }

    public ValueMetaGeometry(String name, int type) {
        super(name, type);
    }

    public ValueMetaGeometry( String name, int length, int precision ) {
        super(name, ValueMetaInterface.TYPE_GEOMETRY, length, precision);
    }

    @Override
    public Object getNativeDataType(Object object) throws KettleValueException {
        return getGeometry(object);
    }

    @Override
    public Class<?> getNativeDataTypeClass() throws KettleValueException {
        return Geometry.class;
    }
}
