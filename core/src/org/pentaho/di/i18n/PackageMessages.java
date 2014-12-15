package org.pentaho.di.i18n;

public class PackageMessages {

  private final Class<?> packageClass;
  private final String prefix;

  public PackageMessages( final Class<?> packageClass ) {
    this.packageClass = packageClass;
    prefix = packageClass.getSimpleName() + ".";
  }

  public String getString( final String key, final String... parameters ) {
    return BaseMessages.getString( packageClass, prefix + key, parameters );
  }
}
