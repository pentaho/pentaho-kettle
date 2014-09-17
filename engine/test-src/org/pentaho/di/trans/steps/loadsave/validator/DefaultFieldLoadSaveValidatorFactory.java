package org.pentaho.di.trans.steps.loadsave.validator;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pentaho.di.trans.steps.loadsave.getter.Getter;

public class DefaultFieldLoadSaveValidatorFactory implements FieldLoadSaveValidatorFactory {

  private final Map<Getter<?>, FieldLoadSaveValidator<?>> getterMap;
  private final Map<String, FieldLoadSaveValidator<?>> typeMap;

  public DefaultFieldLoadSaveValidatorFactory() {
    this.typeMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    this.getterMap = new HashMap<Getter<?>, FieldLoadSaveValidator<?>>();
    this.typeMap.put( String.class.getCanonicalName(), new StringLoadSaveValidator() );
    this.typeMap.put( boolean.class.getCanonicalName(), new BooleanLoadSaveValidator() );
    this.typeMap.put( Boolean.class.getCanonicalName(), new BooleanLoadSaveValidator() );
    this.typeMap.put( int.class.getCanonicalName(), new IntLoadSaveValidator() );
    registerValidator( getName( List.class, String.class ), new ListLoadSaveValidator<String>(
      new StringLoadSaveValidator() ) {
    } );
    registerValidator( String[].class.getCanonicalName(), new ArrayLoadSaveValidator<String>(
        new StringLoadSaveValidator() ) );
    registerValidator( boolean[].class.getCanonicalName(), new PrimitiveBooleanArrayLoadSaveValidator(
        new BooleanLoadSaveValidator() ) );
    registerValidator( Boolean[].class.getCanonicalName(), new ArrayLoadSaveValidator<Boolean>(
        new BooleanLoadSaveValidator() ) );
  }

  public void registerValidator( String typeString, FieldLoadSaveValidator<?> validator ) {
    this.typeMap.put( typeString, validator );
  }

  public DefaultFieldLoadSaveValidatorFactory( Map<Getter<?>, FieldLoadSaveValidator<?>> map,
      Map<String, FieldLoadSaveValidator<?>> fieldLoadSaveValidatorTypeMap ) {
    this();
    getterMap.putAll( map );
    typeMap.putAll( fieldLoadSaveValidatorTypeMap );
  }

  @SuppressWarnings( "unchecked" )
  @Override
  public <T> FieldLoadSaveValidator<T> createValidator( Getter<T> getter ) {
    try {
      FieldLoadSaveValidator<?> validatorClass = getterMap.get( getter );
      if ( validatorClass == null ) {
        Type type = getter.getGenericType();
        validatorClass = typeMap.get( getName( type ) );
      }
      if ( validatorClass == null ) {
        throw new RuntimeException( "Unable to find validator for " + getter.getGenericType() + " or " + getter );
      }
      return (FieldLoadSaveValidator<T>) validatorClass;
    } catch ( Exception e ) {
      if ( e instanceof RuntimeException ) {
        throw (RuntimeException) e;
      }
      throw new RuntimeException( e );
    }
  }

  @Override
  public String getName( Type type ) {
    if ( type instanceof Class<?> ) {
      return ( (Class<?>) type ).getCanonicalName();
    } else {
      ParameterizedType type2 = (ParameterizedType) type;
      return getName( type2.getRawType() ) + getName( type2.getActualTypeArguments() );
    }
  }

  private Object getName( Type[] actualTypeArguments ) {
    StringBuilder sb = new StringBuilder();
    if ( actualTypeArguments.length > 0 ) {
      sb.append( "<" );
      for ( Type parameter : actualTypeArguments ) {
        sb.append( getName( parameter ) );
        sb.append( "," );
      }
      sb.setLength( sb.length() - 1 );
      sb.append( ">" );
    }
    return sb.toString();
  }

  @Override
  public String getName( Class<?> type, Class<?>... parameters ) {
    StringBuilder sb = new StringBuilder( type.getCanonicalName() );
    if ( parameters.length > 0 ) {
      sb.append( "<" );
      for ( Class<?> parameter : parameters ) {
        sb.append( parameter.getCanonicalName() );
        sb.append( "," );
      }
      sb.setLength( sb.length() - 1 );
      sb.append( ">" );
    }
    return sb.toString();
  }
}
