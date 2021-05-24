Client library builder for RWT WebClient
========================================

The ClientBuilder is used for assembling the JavaScript library for the WebClient.
It reads JavaScript files from the project org.eclipse.rap.rwt, compresses and
concatenates them, and writes the resulting JavaScript file back into the project
as client.js.

Usage
-----
Use the included launch configuration JSCompressor.launch to create a new client.js.
This launch configuration calls JsCompressor as a plain Java application, passing the
necessary parameters (see JavaDoc of JSCompressor).
Remember to refresh (F5) the rwt project when the compressor is finished.
