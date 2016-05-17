package org.pentaho.di.trans.steps.ldifinput;

public class MockLDIFInputMeta extends LDIFInputMeta {


  /**
   * This is only here because the allocate method must be called or
   * loadSaveTester bombs.
   */
  public MockLDIFInputMeta() {
    super();
    this.allocate( 5, 5 );
  }

}
