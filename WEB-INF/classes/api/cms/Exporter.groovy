// Copyright (c) 2021 MintJams Inc. Licensed under MIT License.

package api.cms;

import api.util.ISO8601;
import api.util.JSON;
import jp.co.mintjams.osgi.service.jcr.script.ScriptingContext;
import org.apache.commons.compress.archivers.zip.UnicodePathExtraField;
import org.apache.commons.compress.archivers.zip.Zip64Mode;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipEncodingHelper;
import org.apache.commons.io.IOUtils;
 
class Exporter {
	def context;
	def dataFile;
	def statusFile;
	def status;

	Exporter(context) {
		this.context = context;
	}

	static def create(ScriptingContext context) {
		return new Exporter(context);
	}

	def execute(String[] absPaths, boolean noMetadata) {
		dataFile = File.createTempFile("export-", ".data");
		dataFile.deleteOnExit();

		def identifier = {
			def id = dataFile.name;
			id = id.substring(0, id.lastIndexOf("."));
		}();
		def filename = {
			def path = absPaths[0];
			if (!path.startsWith("/")) {
				path = "/" + path;
			}
			def name = path.substring(path.lastIndexOf("/") + 1);
			def i = name.lastIndexOf(".");
			if (i == -1) {
				name = name + ".zip";
			} else {
				name = name.substring(0, i) + ".zip";
			}
			return name;
		}();
		def created = new Date();
		status = [
			"identifier": identifier,
			"filename": filename,
			"status": "in-progress",
			"eTag": "" + created.time,
			"created": ISO8601.formatDate(created)
		];
		statusFile = new File(System.getProperty("java.io.tmpdir"), identifier + ".status");
		statusFile.deleteOnExit();
		statusFile.text = JSON.stringify(status);

		def batchSession = context.repositorySession.newSession();
		Thread.startDaemon(status.identifier, {
			batchSession.withCloseable { repositorySession ->
				new FileOutputStream(dataFile).withCloseable { out ->
					new ZipArchiveOutputStream(out).withCloseable { zip ->
						try {
							zip.setCreateUnicodeExtraFields(ZipArchiveOutputStream.UnicodeExtraFieldPolicy.ALWAYS);
							zip.setUseLanguageEncodingFlag(true);
							zip.setFallbackToUTF8(true);
							zip.setEncoding("UTF-8");
							zip.setUseZip64(Zip64Mode.Always);

							for (path in absPaths) {
								if (!path.startsWith("/")) {
									path = "/" + path;
								}
								def item = Item.create(context).with(repositorySession.resourceResolver.getResource(path));
								_mkzip(item, zip, null, noMetadata);
							}

							zip.finish();
						} catch (Throwable ex) {
							status.status = "error";
							status.statusText = ex.message;
							statusFile.text = JSON.stringify(status);
						}
					}
				}
			}

			if (status.status == "in-progress") {
				status.status = "completed";
				statusFile.text = JSON.stringify(status);
			}
		});

		return this;
	}

	def _mkzip(item, zip, rootPath, noMetadata) {
		if (!item.exists()) {
			throw new java.lang.IllegalArgumentException("The item does not exist: " + item.path);
		}
		if (item.name.startsWith("rep:")) {
			return;
		}

		if (!rootPath) {
			rootPath = item.parent.path;
		}

		def zipEncoder = ZipEncodingHelper.getZipEncoding("UTF-8");

		// content
		{ ->
			def path = item.path.substring(rootPath.length());
			if (item.isCollection()) {
				path += "/";
			}
			if (!path.startsWith("/")) {
				path = "/" + path;
			}

			ZipArchiveEntry entry = new ZipArchiveEntry("/" + path);
			entry.setTime(item.lastModified.time);

			zip.putArchiveEntry(entry);
			if (!item.isCollection()) {
				item.contentAsStream.withCloseable { stream ->
					IOUtils.copyLarge(stream, zip);
				}
			}
			zip.closeArchiveEntry();

			if (item.isCollection()) {
				for (child in item.list()) {
					_mkzip(child, zip, rootPath, noMetadata);
				}
			}
		}();

		// metadata
		if (!noMetadata && !item.isCollection()) {
			def path = item.parent.path + "/." + item.name + ".metadata.json";
			path = path.substring(rootPath.length());
			if (!path.startsWith("/")) {
				path = "/" + path;
			}

			ZipArchiveEntry entry = new ZipArchiveEntry("/" + path);
			entry.setTime(item.lastModified.time);

			zip.putArchiveEntry(entry);
			zip.write(item.toJson(true).getBytes("UTF-8"));
			zip.closeArchiveEntry();
		}
	}

	def resolve(identifier) {
		dataFile = new File(System.getProperty("java.io.tmpdir"), identifier + ".data");
		statusFile = new File(System.getProperty("java.io.tmpdir"), identifier + ".status");
		if (statusFile.exists()) {
			status = JSON.parse(statusFile.text);
		}
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
		if (!status) {
			return null;
		}
		return status.identifier;
	}

	def exists() {
		return (dataFile && dataFile.exists());
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

	def toObject() {
		def o = [
			"identifier": identifier
		];
		if (status) {
			o.status = status.status;
			if (status.statusText) {
				o.statusText = status.statusText;
			}
		}
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
