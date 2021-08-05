// Copyright (c) 2021 MintJams Inc. Licensed under MIT License.

package api.cms;

//import api.util.ISO8601;
import api.util.JSON;
import jp.co.mintjams.osgi.service.jcr.script.ScriptingContext;

class VersionHistory {
	def context;
	def versionHistory;

	VersionHistory(context) {
		this.context = context;
	}

	static def create(ScriptingContext context) {
		return new VersionHistory(context);
	}

	def with(jp.co.mintjams.osgi.service.jcr.VersionHistory versionHistory) {
		this.versionHistory = versionHistory;
		return this;
	}

// 	Iterator<Resource> getAllFrozenNodes() throws JcrException;

// 	Iterator<Resource> getAllLinearFrozenNodes() throws JcrException;

	def getAllLinearVersions() {
	    return versionHistory.allLinearVersions.collect {
            Version.create(context).with(it);
	    }
	}

	def getAllVersions() {
	    return versionHistory.allVersions.collect {
            Version.create(context).with(it);
	    }
	}

	def getRootVersion() {
        return Version.create(context).with(versionHistory.rootVersion);
	}

	def getVersion(versionName) {
        return Version.create(context).with(versionHistory.getVersion(versionName));
	}

	def getVersionByLabel(label) {
        return Version.create(context).with(versionHistory.getVersionByLabel(label));
	}

	def getVersionLabels() {
        return versionHistory.versionLabels;
	}

	def getVersionableIdentifier() {
        return versionHistory.versionableIdentifier;
	}

	def hasVersionLabel(label) {
        return versionHistory.hasVersionLabel(label);
	}

	def removeVersionLabel(label) {
        versionHistory.removeVersionLabel(label);
        return this;
	}

	def toObject() {
		def o = [
		    "versionableIdentifier": versionableIdentifier,
		    "versionLabels": versionLabels,
		    "versions": []
		];
		allVersions.each { version ->
		    o.versions.add(version.toObject());
		}

		return o;
	}

	def toJson() {
		return JSON.stringify(toObject());
	}
}
