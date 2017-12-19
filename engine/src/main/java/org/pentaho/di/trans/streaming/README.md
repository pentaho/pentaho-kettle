
# Trans Streaming package

The org.pentaho.di.trans.streaming contains an api and common
base classes for creating steps which consume unbounded input
streams that are buffered via a window strategy.

To implement a new stream consuming step:
 
 1) Implement the org.pentaho.di.trans.streaming.api.StreamSource interface for
 your input source type.
 2) Extend the BaseStreamStepMeta class to define whatever metadata is needed
 for your step.    
 3)  Extend the BaseStreamStep for you step class.  The main responsibility of
 the StreamStep implementation is to initialize the stream source and stream window
 (in .init()).  If a fixed time interval is applicable for your stream step, the
 FixedTimeStreamWindow can be used directly.
 
 See plugins/file-stream for an example.
 
 
 
 
