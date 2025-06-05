package com.pentaho.di.repovfs.cfg;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Read-only configuration access for a backend with string -> string
 */
public class ConfigReader {

  private static final Logger log = LoggerFactory.getLogger( ConfigReader.class );

  private final Function<String, String> propertyGetter;

  public ConfigReader( Function<String, String> propertyGetter ) {
    this.propertyGetter = propertyGetter;
  }

  /** @return property with given key if exists, empty otherwise */
  public Optional<String> getProperty( String key ) {
    return Optional.ofNullable( propertyGetter.apply( key ) );
  }

  /**
   * Returns property parsed by given function.
   *
   * @return parsed property or `Optional.empty()` if property not there or
   *         there is a parse error
   */
  public <T> Optional<T> parseProperty( String key, Function<String, T> parser ) {
    try {
      return getProperty( key ).map( parser::apply );
    } catch ( Exception e ) {
      log.error( "Error parsing property {}", key, e );
      return Optional.empty();
    }
  }

  /** Get property or throw */
  public String getMandatoryProperty( String key ) {
    String prop = propertyGetter.apply( key );
    if ( prop == null ) {
      throw new NoSuchElementException( key + " not present in properties" );
    }
    return prop;
  }
}
