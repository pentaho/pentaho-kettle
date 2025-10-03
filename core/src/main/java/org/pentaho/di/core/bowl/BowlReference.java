package org.pentaho.di.core.bowl;

import java.lang.ref.WeakReference;

/**
 * Wraps a WeakReference to a Bowl and provides equality based on the "==" equality of the referenced Bowl.
 * Used to prevent leaking Bowls in the vfs layer. 
 */
public class BowlReference {
  private final WeakReference<Bowl> bowlRef;

  public BowlReference( Bowl bowl ) {
    this.bowlRef = new WeakReference<>( bowl );
  }

  public Bowl getBowl() {
    return bowlRef.get();
  }

  @Override
  public boolean equals( Object obj ) {
    if ( this == obj ) {
      return true;
    }
    if ( obj == null || getClass() != obj.getClass() ) {
      return false;
    }
    BowlReference that = (BowlReference) obj;
    Bowl thisBowl = this.getBowl();
    Bowl thatBowl = that.getBowl();
    if ( thisBowl == null || thatBowl == null ) {
      return false;
    }
    // Compare by identity, so that newly opened Bowls get new FileSystems.
    return thisBowl == thatBowl;
  }

  // this is used by FileSystemOptions.compareTo, so it needs to match the Bowl identity, not the equals method. 
  @Override
  public int hashCode() {
    Bowl bowl = bowlRef.get();
    return bowl != null ? System.identityHashCode( bowl ) : 0;
  }
}
