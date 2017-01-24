package org.pentaho.di.engine.kettleclassic;

import org.pentaho.di.engine.api.reporting.Metrics;

public class ClassicKettleMetrics extends Metrics {

  private final long read, written, updated, rejected, errors, copy;

  private ClassicKettleMetrics( Builder builder ) {
    super( builder.in, builder.out, builder.dropped, builder.inflight );
    this.read = builder.read;
    this.written = builder.written;
    this.updated = builder.updated;
    this.rejected = builder.rejected;
    this.errors = builder.errors;
    this.copy = builder.copy;
  }

  public long getRead() {
    return read;
  }

  public long getWritten() {
    return written;
  }

  public long getUpdated() {
    return updated;
  }

  public long getRejected() {
    return rejected;
  }

  public long getErrors() {
    return errors;
  }

  public long getCopy() {
    return copy;
  }

  public static class Builder {
    private long in, out, dropped, inflight, read, written, updated, rejected, errors, copy;

    public Builder withIn( long in ) {
      this.in = in;
      return this;
    }

    public Builder withOut( long out ) {
      this.out = out;
      return this;
    }

    public Builder withDropped( long dropped ) {
      this.dropped = dropped;
      return this;
    }

    public Builder withInflight( long inflight ) {
      this.inflight = inflight;
      return this;
    }

    public Builder withRead( long read ) {
      this.read = read;
      return this;
    }

    public Builder withWritten( long written ) {
      this.written = written;
      return this;
    }

    public Builder withUpdated( long updated ) {
      this.updated = updated;
      return this;
    }

    public Builder withRejected( long rejected ) {
      this.rejected = rejected;
      return this;
    }

    public Builder withErrors( long errors ) {
      this.errors = errors;
      return this;
    }

    public Builder withCopy( long copy ) {
      this.copy = copy;
      return this;
    }

    public ClassicKettleMetrics build() {
      return new ClassicKettleMetrics( this );
    }
  }
}
