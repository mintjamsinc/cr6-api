// Copyright (c) 2021 MintJams Inc. Licensed under MIT License.

package api.cms;

import api.util.ISO8601;
import api.util.JSON;
import org.mintjams.script.ScriptingContext;
import org.apache.commons.compress.archivers.zip.ZipFile;

class Importer {
	def context;
	def dataFile;
	def statusFile;
	def status;
	def preImportedEntries = [];
	def statusMonitor;

	Importer(context) {
		this.context = context;
	}

	static def create(ScriptingContext context) {
		return new Importer(context);
	}

	def prepare(String absPath, file) {
		dataFile = file;
		statusFile = File.createTempFile("import-", ".status");
		statusFile.deleteOnExit();

		def identifier = {
			def id = statusFile.name;
			id = id.substring(0, id.lastIndexOf("."));
		}();

		status = [
			"identifier": identifier,
			"path": absPath,
			"dataPath": dataFile.canonicalPath,
			"status": "prepared",
			"statusText": "",
		];
		statusFile.text = JSON.stringify(status);

		return this;
	}

	def execute() {
		status.status = "in-progress";
		status.statusText = "";

		try {
			def repositorySession = context.repositorySession;
			new ZipFile(dataFile).withCloseable { zip ->
				try {
					def item = Item.create(context).with(resourceResolver.getResource(status.path));
					if (!item.exists()) {
						throw new java.lang.IllegalArgumentException("The item does not exist: " + item.path);
					}
					if (!item.isCollection()) {
						throw new java.lang.IllegalArgumentException("The item is not collection: " + item.path);
					}

					zip.entries.each { entry ->
						_imp(item, zip, entry);
					}

					status.status = "done";
					status.statusText = "";
					_setStatus();
				} catch (Throwable ex) {
					status.status = "error";
					status.statusText = ex.message;
					_setStatus();
				} finally {
					try {
						repositorySession.rollback();
					} catch (Throwable ignore) {}
				}
			}
		} finally {
			remove();
		}

		return this;
	}

	def _imp(rootItem, zip, entry) {
		if (preImportedEntries.contains(entry.name)) {
			return;
		}

		def repositorySession = rootItem.resource.session;
		try {
			def impRelPath = {
				def name = entry.name;
				if (name.startsWith("//")) {
					name = name.substring(2);
				}
				if (name.startsWith("/")) {
					name = name.substring(1);
				}
				if (name.endsWith("/")) {
					name = name.substring(0, name.length() - 1);
				}
				return name;
			}();

			def item = rootItem.getItem(impRelPath);
			if (item.name.startsWith(".") && item.name.endsWith(".metadata.json")) {
				return;
			}

			if (entry.isDirectory()) {
				item.mkdirs();
				return;
			}

			status.status = "in-progress";
			status.statusText = 'Importing "' + impRelPath + '"';
			_setStatus();

			if (item.exists()) {
				if (item.isVersionControlled()) {
					if (!item.isCheckedOut()) {
						item.checkout();
					}
				}
			} else {
				item.createNewFile();
			}

			zip.getInputStream(entry).withCloseable { stream ->
				ItemHelper.create(context).with(item).importContent(stream, item.contentType);
			}

			def metadataName = {
				def name = entry.name;
				def i = name.lastIndexOf("/");
				if (i == -1) {
					name = "." + name + ".metadata.json";
				} else {
					def prefix = name.substring(0, i + 1);
					name = prefix + "." + name.substring(i + 1) + ".metadata.json";
				}
				return name;
			}();
			def metadataEntry = zip.getEntry(metadataName);
			if (metadataEntry) {
				def metadata = JSON.parse(zip.getInputStream(metadataEntry).getText("UTF-8"));
				item.allowAnyProperties();
				if (metadata.containsKey("mimeType")) {
					item.setAttribute("jcr:mimeType", metadata["mimeType"]);
				}
				if (metadata.containsKey("encoding")) {
					item.setAttribute("jcr:encoding", metadata["encoding"]);
				}
				if (metadata.containsKey("isReferenceable")) {
					if (!!metadata["isReferenceable"]) {
						item.addReferenceable();
					} else {
						//item.removeReferenceable();
					}
				}
				if (metadata.containsKey("lastModificationTime")) {
					item.setAttribute("jcr:lastModified", ISO8601.parseDate(metadata["lastModificationTime"]));
				}
				if (metadata.containsKey("properties")) {
					ItemHelper.create(context).with(item).importAttributes(metadata.properties);
				}
			}

			repositorySession.commit();
			return item;
		} catch (ReferencedItemNotFoundException ex) {
			if (!ex.path) {
				throw ex;
			}
			if (!ex.path.startsWith(rootItem.path)) {
				throw ex;
			}

			repositorySession.rollback();

			def refEntryPath = ex.path.substring(rootItem.path.length());
			def refEntry = zip.getEntry(refEntryPath);
			if (!refEntry) {
				if (!refEntryPath.startsWith("/")) {
					refEntryPath = "/" + refEntryPath;
				}
				if (!refEntryPath.startsWith("//")) {
					refEntryPath = "/" + refEntryPath;
				}
				refEntry = zip.getEntry(refEntryPath);
			}
			if (!refEntry) {
				throw ex;
			}
			def refItem = _imp(rootItem, zip, refEntry);
			preImportedEntries.add(refEntry.name);

			// retry
			return _imp(rootItem, zip, entry);
		}
	}

	def _setStatus() {
		if (statusMonitor == null) {
			return;
		}
		statusMonitor.setStatus([
			"status": status.status,
			"statusText": status.statusText,
		]);
	}

	def resolve(identifier) {
		statusFile = new File(System.getProperty("java.io.tmpdir"), identifier + ".status");
		if (statusFile.exists()) {
			status = JSON.parse(statusFile.text);
			dataFile = new File(status.dataPath);
		}
		return this;
	}

	def getIdentifier() {
		if (!status) {
			return null;
		}
		return status.identifier;
	}

	def exists() {
		return (statusFile && statusFile.exists());
	}

	def remove() {
		if (dataFile && dataFile.exists()) {
			dataFile.delete();
		}
		if (statusFile && statusFile.exists()) {
			statusFile.delete();
		}
		return this;
	}

	def setStatusMonitor(statusMonitor) {
		this.statusMonitor = statusMonitor;
		return this;
	}

	def toObject() {
		def o = [
			"identifier": status.identifier,
		];
		return o;
	}

	def toJson() {
		return JSON.stringify(toObject());
	}
}
