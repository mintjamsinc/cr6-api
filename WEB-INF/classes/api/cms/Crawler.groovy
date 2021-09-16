// Copyright (c) 2021 MintJams Inc. Licensed under MIT License.

package api.cms;

import api.util.ISO8601;
import api.util.JSON;
import jp.co.mintjams.osgi.service.jcr.script.ScriptingContext;

class Crawler {
	def context;
	def statusFile;
	def status;

	Crawler(context) {
		this.context = context;
	}

	static def create(ScriptingContext context) {
		return new Crawler(context);
	}

	def start(String statement, String language = "XPath") {
		statusFile = File.createTempFile("crawl-", ".status");
		statusFile.deleteOnExit();

		def identifier = {
			def id = statusFile.name;
			id = id.substring(0, id.lastIndexOf("."));
		}();
		def created = new Date();
		status = [
			"identifier": identifier,
			"status": "in-progress",
			"created": ISO8601.formatDate(created)
		];
		statusFile.text = JSON.stringify(status);

		def batchContext = context.newBuilder().build();
		Thread.startDaemon(status.identifier, {
			batchContext.withCloseable { _context ->
				def log = _context.getAttribute("log");
				def repositorySession = _context.repositorySession;
				def limit = 100;
				try {
					for (def offset = 0;; offset += limit) {
						def result = Search.create(_context).execute([
							"statement": statement,
							"language": language,
							"offset": offset,
							"limit": limit,
						]);

						result.items.each { item ->
							item.calculate();
							item.setAttribute("jcr:lastModified", new Date());
							repositorySession.commit();
						}

						if (!result.hasMore()) {
							break;
						}
					}
				} catch (Throwable ex) {
					log.error(ex.message, ex);
					status.status = "error";
					status.statusText = ex.message;
					statusFile.text = JSON.stringify(status);
				} finally {
					try {
						repositorySession.rollback();
					} catch (Throwable ignore) {}
				}
			}

			if (status.status == "in-progress") {
				status.status = "completed";
				statusFile.text = JSON.stringify(status);
			}
		});

		return this;
	}

	def resolve(identifier) {
		statusFile = new File(System.getProperty("java.io.tmpdir"), identifier + ".status");
		if (statusFile.exists()) {
			status = JSON.parse(statusFile.text);
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
		return o;
	}

	def toJson() {
		return JSON.stringify(toObject());
	}
}
