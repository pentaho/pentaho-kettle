package org.pentaho.di.plugins.repofvs.pur.converter;

import org.pentaho.platform.api.repository2.unified.Converter;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;

import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@ref Converter} supplier for a converter in a class loader from a different hierarchy.
 **/
public class ReflectedNodeConverter implements Supplier<Converter> {

  private static final Logger log = LoggerFactory.getLogger( ReflectedNodeConverter.class );

  private final String className;
  private final IUnifiedRepository pur;
  private final Supplier<ClassLoader> purPluginClassLoader;
  private final LazyLoader<Converter> loader = new LazyLoader<>( this::getByReflection );

  /**
   * Creates a supplier for a target {@link Converter}
   * @param className Full class name of the converter to instantiate. It is assumed to have a constructor taking {@link IUnifiedRepository}
   * @param pur The repository to pass to the constructor
   * @param purPluginClassLoader The classloader containing the target converter class
   */
  public ReflectedNodeConverter( String className, IUnifiedRepository pur, Supplier<ClassLoader> purPluginClassLoader ) {
    this.className = className;
    this.pur = pur;
    this.purPluginClassLoader = purPluginClassLoader;
  }

  private Converter getByReflection() {
    try {
      ClassLoader classLoader = purPluginClassLoader.get();
      Class<?> clazz = classLoader.loadClass( className );

      try ( var sw = new WithClassLoader( classLoader ) ) {
        Converter converter = (Converter) clazz.getDeclaredConstructor( IUnifiedRepository.class ).newInstance( pur );
        return new CLDelegatingConverter( classLoader, converter );
      }

    } catch ( Exception e ) {
      log.error( "Unable to load converter {}", className, e );
      return null;
    }
  }

  @Override
  public Converter get() {
    return loader.get();
  }


}
