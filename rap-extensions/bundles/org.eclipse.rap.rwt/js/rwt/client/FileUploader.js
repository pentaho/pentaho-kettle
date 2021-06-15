/*******************************************************************************
 * Copyright (c) 2014, 2019 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/

namespace( "rwt.client" );

rwt.client.FileUploader = function() {
  this._sequenceId = 0;
  this._holder = {};
  this._pendingUploads = {};
};


rwt.client.FileUploader.getInstance = function() {
  return rwt.runtime.Singletons.get( rwt.client.FileUploader );
};

rwt.client.FileUploader.createFormData = function() {
  return new FormData();
};

rwt.client.FileUploader.prototype = {

  addFile : function( file ) {
    var result = "f" + this._sequenceId;
    this._holder[ result ] = file;
    this._sequenceId++;
    return result;
  },

  // For testing only:
  getFileById : function( id ) {
    return this._holder[ id ];
  },

  submit : function( callProperties ) {
    var url = callProperties.url;
    var fileIds = callProperties.fileIds;
    var uploadId = callProperties.uploadId;
    var formData = rwt.client.FileUploader.createFormData();
    for( var i = 0; i < fileIds.length; i++ ) {
      var file = this._holder[ fileIds[ i ] ];
      if( !file ) {
        throw new Error( "Unkown file id \"" + fileIds[ i ] + "\"." );
      }
      formData.append( "file", file );
    }
    var that = this;
    var xhr = rwt.remote.Request.createXHR();
    this._pendingUploads[ uploadId ] = xhr;
    xhr.open( "POST", url );
    xhr.onreadystatechange = function () {
      if( xhr.readyState === 4 ) {
        delete that._pendingUploads[ uploadId ];
      }
    };
    xhr.send( formData );
  },

  abort : function( abortProperties ) {
    var id = abortProperties.uploadId;
    var upload = this._pendingUploads[ id ];
    if ( upload ) {
      delete this._pendingUploads[ id ];
      upload.abort();
    }
  }

};
