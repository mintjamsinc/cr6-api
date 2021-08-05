// Copyright (c) 2021 MintJams Inc. Licensed under MIT License.

package api.cms;

import api.util.ISO8601;
import api.util.JSON;
import jp.co.mintjams.osgi.service.jcr.script.ScriptingContext;
 
class MultipartUpload {
	def context;
	def dataFile;

	MultipartUpload(context) {
		this.context = context;
	}

	static def create(ScriptingContext context) {
		return new MultipartUpload(context);
	}

	def initiate() {
		dataFile = File.createTempFile("upload-", ".data");
		dataFile.deleteOnExit();
        return this;
	}

	def resolve(id) {
		dataFile = new File(System.getProperty("java.io.tmpdir"), id + ".data");
        return this;
	}

	def append(data) {
		dataFile.append(data.decodeBase64());
        return this;
	}

	def getFile() {
	    if (!dataFile || !dataFile.exists()) {
	        throw new IOException("The data file cannot be found.");
	    }
		return dataFile;
	}

	def newInputStream() {
	    if (!dataFile || !dataFile.exists()) {
	        throw new IOException("The data file cannot be found.");
	    }
		return dataFile.newInputStream();
	}

	def getIdentifier() {
	    if (!dataFile || !dataFile.exists()) {
	        return null;
	    }
        return dataFile.name.substring(0, dataFile.name.lastIndexOf("."));
	}

	def exists() {
	    return (dataFile && dataFile.exists());
	}

	def remove() {
	    if (dataFile && dataFile.exists()) {
	        dataFile.delete();
	    }
        return this;
	}

	def toObject() {
	    def o = [
			"id": identifier
        ];
        if (dataFile && dataFile.exists()) {
            o.file = [
                "lastModified": ISO8601.formatDate(new Date(dataFile.lastModified())),
			    "length": dataFile.length()
			];
        }
        return o;
	}

	def toJson() {
		return JSON.stringify(toObject());
	}
}
