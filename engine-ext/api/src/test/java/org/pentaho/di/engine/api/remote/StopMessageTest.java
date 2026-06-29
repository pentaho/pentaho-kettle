package org.pentaho.di.engine.api.remote;

import org.junit.Test;

import static org.junit.Assert.*;

public class StopMessageTest {

  private static final String REQUEST_UUID = "request-uuid-123";
  private static final String REASON_PHRASE = "stop requested";

  @Test
  public void reasonPhraseConstructorSetsSuccessStatusAndDefaults() {
    StopMessage message = new StopMessage( REASON_PHRASE );

    assertNull( message.getRequestUUID() );
    assertEquals( REASON_PHRASE, message.getReasonPhrase() );
    assertEquals( StopMessage.Status.SUCCESS, message.getStatus() );
    assertFalse( message.isSafeStop() );
  }

  @Test
  public void requestUUIDAndReasonPhraseConstructor() {
    StopMessage message = new StopMessage( REQUEST_UUID, REASON_PHRASE );

    assertEquals( REQUEST_UUID, message.getRequestUUID() );
    assertEquals( REASON_PHRASE, message.getReasonPhrase() );
    assertEquals( StopMessage.Status.SUCCESS, message.getStatus() );
    assertFalse( message.isSafeStop() );
  }

  @Test
  public void reasonPhraseAndStatusConstructor() {
    StopMessage message = new StopMessage( REASON_PHRASE, StopMessage.Status.FAILED );

    assertNull( message.getRequestUUID() );
    assertEquals( REASON_PHRASE, message.getReasonPhrase() );
    assertEquals( StopMessage.Status.FAILED, message.getStatus() );
    assertFalse( message.isSafeStop() );
  }

  @Test
  public void requestUUIDReasonPhraseAndStatusConstructor() {
    StopMessage message = new StopMessage( REQUEST_UUID, REASON_PHRASE, StopMessage.Status.SESSION_KILLED );

    assertEquals( REQUEST_UUID, message.getRequestUUID() );
    assertEquals( REASON_PHRASE, message.getReasonPhrase() );
    assertEquals( StopMessage.Status.SESSION_KILLED, message.getStatus() );
    assertFalse( message.isSafeStop() );
  }

  @Test
  public void getRequestUUID() {
    assertEquals( REQUEST_UUID, new StopMessage( REQUEST_UUID, REASON_PHRASE ).getRequestUUID() );
    assertNull( new StopMessage( REASON_PHRASE ).getRequestUUID() );
  }

  @Test
  public void getReasonPhrase() {
    assertEquals( REASON_PHRASE, new StopMessage( REASON_PHRASE ).getReasonPhrase() );
    assertNull( new StopMessage( null ).getReasonPhrase() );
  }

  @Test
  public void getStatus() {
    assertEquals( StopMessage.Status.SUCCESS, new StopMessage( REASON_PHRASE ).getStatus() );
    assertEquals( StopMessage.Status.FAILED,
      new StopMessage( REASON_PHRASE, StopMessage.Status.FAILED ).getStatus() );
    assertEquals( StopMessage.Status.SESSION_KILLED,
      new StopMessage( REQUEST_UUID, REASON_PHRASE, StopMessage.Status.SESSION_KILLED ).getStatus() );
  }

  @Test
  public void isSafeStop() {
    assertFalse( new StopMessage( REASON_PHRASE ).isSafeStop() );
    assertTrue( StopMessage.builder().safeStop( true ).build().isSafeStop() );
    assertFalse( StopMessage.builder().safeStop( false ).build().isSafeStop() );
  }

  @Test
  public void builder() {
    assertNotNull( StopMessage.builder() );

    StopMessage message = StopMessage.builder()
      .requestUUID( REQUEST_UUID )
      .reasonPhrase( REASON_PHRASE )
      .status( StopMessage.Status.FAILED )
      .safeStop( true )
      .build();

    assertEquals( REQUEST_UUID, message.getRequestUUID() );
    assertEquals( REASON_PHRASE, message.getReasonPhrase() );
    assertEquals( StopMessage.Status.FAILED, message.getStatus() );
    assertTrue( message.isSafeStop() );
  }

  @Test
  public void builderIsSafeStopFalseStatusSessionKilled() {
    assertNotNull( StopMessage.builder() );

    StopMessage message = StopMessage.builder()
      .requestUUID( REQUEST_UUID )
      .reasonPhrase( REASON_PHRASE )
      .status( StopMessage.Status.SESSION_KILLED)
      .safeStop( false )
      .build();

    assertEquals( REQUEST_UUID, message.getRequestUUID() );
    assertEquals( REASON_PHRASE, message.getReasonPhrase() );
    assertEquals( StopMessage.Status.SESSION_KILLED, message.getStatus() );
    assertFalse( message.isSafeStop() );
  }

  @Test
  public void builderUsesDefaultsWhenNothingIsSet() {
    StopMessage message = StopMessage.builder().build();

    assertNull( message.getRequestUUID() );
    assertNull( message.getReasonPhrase() );
    assertNull( message.getStatus() );
    assertFalse( message.isSafeStop() );
  }

  @Test
  @SuppressWarnings( "removal" )
  public void operationSuccessful() {
    assertTrue( new StopMessage( REASON_PHRASE, StopMessage.Status.SUCCESS ).operationSuccessful() );
    assertFalse( new StopMessage( REASON_PHRASE, StopMessage.Status.FAILED ).operationSuccessful() );
    assertFalse( new StopMessage( REASON_PHRASE, StopMessage.Status.SESSION_KILLED ).operationSuccessful() );
  }

  @Test
  @SuppressWarnings( "removal" )
  public void operationFailed() {
    assertTrue( new StopMessage( REASON_PHRASE, StopMessage.Status.FAILED ).operationFailed() );
    assertFalse( new StopMessage( REASON_PHRASE, StopMessage.Status.SUCCESS ).operationFailed() );
    assertFalse( new StopMessage( REASON_PHRASE, StopMessage.Status.SESSION_KILLED ).operationFailed() );
  }

  @Test
  @SuppressWarnings( "removal" )
  public void sessionWasKilled() {
    assertTrue( new StopMessage( REASON_PHRASE, StopMessage.Status.SESSION_KILLED ).sessionWasKilled() );
    assertFalse( new StopMessage( REASON_PHRASE, StopMessage.Status.SUCCESS ).sessionWasKilled() );
    assertFalse( new StopMessage( REASON_PHRASE, StopMessage.Status.FAILED ).sessionWasKilled() );
  }

  private static StopMessage fullMessage( boolean safeStop ) {
    return StopMessage.builder()
      .requestUUID( REQUEST_UUID )
      .reasonPhrase( REASON_PHRASE )
      .status( StopMessage.Status.SUCCESS )
      .safeStop( safeStop )
      .build();
  }

  @Test
  @SuppressWarnings( { "SimplifiableAssertion", "EqualsWithItself" } )
  public void equalsIsReflexive() {
    StopMessage message = fullMessage( true );

    assertTrue( message.equals( message ) );
  }

  @Test
  public void equalsIsTrueForEqualFields() {
    StopMessage one = fullMessage( true );
    StopMessage two = fullMessage( true );

    assertEquals( one, two );
    assertEquals( two, one );
  }

  @Test
  @SuppressWarnings( { "SimplifiableAssertion", "ConstantValue" } )
  public void equalsIsFalseForNull() {
    assertFalse( fullMessage( true ).equals( null ) );
  }

  @Test
  @SuppressWarnings( { "SimplifiableAssertion" } )
  public void equalsIsFalseForDifferentClass() {
    assertFalse( fullMessage( true ).equals( new Object() ) );
  }

  @Test
  public void equalsIsFalseForDifferentRequestUUID() {
    StopMessage one = new StopMessage( REQUEST_UUID, REASON_PHRASE );
    StopMessage two = new StopMessage( "other-uuid", REASON_PHRASE );

    assertNotEquals( one, two );
  }

  @Test
  public void equalsIsFalseForDifferentReasonPhrase() {
    StopMessage one = new StopMessage( REQUEST_UUID, REASON_PHRASE );
    StopMessage two = new StopMessage( REQUEST_UUID, "other reason" );

    assertNotEquals( one, two );
  }

  @Test
  public void equalsIsFalseForDifferentStatus() {
    StopMessage one = new StopMessage( REASON_PHRASE, StopMessage.Status.SUCCESS );
    StopMessage two = new StopMessage( REASON_PHRASE, StopMessage.Status.FAILED );

    assertNotEquals( one, two );
  }

  @Test
  public void equalsIsFalseForDifferentSafeStop() {
    assertNotEquals( fullMessage( true ), fullMessage( false ) );
  }

  @Test
  public void hashCodeIsEqualForEqualObjects() {
    assertEquals( fullMessage( true ).hashCode(), fullMessage( true ).hashCode() );
  }

  @Test
  public void hashCodeDiffersForDifferentObjects() {
    StopMessage one = new StopMessage( REASON_PHRASE, StopMessage.Status.SUCCESS );
    StopMessage two = new StopMessage( REASON_PHRASE, StopMessage.Status.FAILED );

    assertNotEquals( one.hashCode(), two.hashCode() );
  }
}
