// Copyright (c) 2021 MintJams Inc. Licensed under MIT License.

package api.cms;

import api.security.Session;
import api.util.ISO8601;
import api.util.JSON;
import java.io.ByteArrayInputStream; 
import java.io.IOException;
import java.util.Locale;
import org.mintjams.script.ScriptingContext;

class Item {
	def context;
	def resource;
	def locale = Locale.getDefault();
	def facet;
	def currentItem;

	Item(context) {
		this.context = context;
	}

	static def create(ScriptingContext context) {
		return new Item(context);
	}

	def with(org.mintjams.script.resource.Resource resource) {
		this.resource = resource;
		currentItem = null;
		facet = null;
		return this;
	}

	def with(Item item) {
		return with(item.resource);
	}

	def with(Locale locale) {
		this.locale = locale;
		return this;
	}

	def findByIdentifier(id) {
		return with(context.resourceResolver.getResourceByIdentifier(id));
	}

	def findByPath(path) {
		return with(context.resourceResolver.getResource(path));
	}

	def getIdentifier() {
		if (!resource) {
			return null;
		}
		return resource.identifier;
	}

	def getName() {
		if (!resource) {
			return null;
		}
		return resource.name;
	}

	def getPath() {
		if (!resource) {
			return null;
		}
		return resource.path;
	}

	def exists() {
		if (!resource) {
			return false;
		}
		return resource.exists();
	}

	def getParent() {
		return Item.create(context).with(resource.parent);
	}

	def getItem(relPath) {
		def parentPath = path;
		if (isCollection()) {
			if (!parentPath.endsWith("/")) {
				parentPath += "/";
			}
		} else {
			parentPath = parentPath.substring(0, parentPath.lastIndexOf("/") + 1);
		}
		return Item.create(context).findByPath(parentPath + relPath);
	}

	def mkdirs() {
		if (resource.exists()) {
			return;
		}
	
		if (!resource.parent.exists()) {
			parent.mkdirs();
		}
	
		resource.createFolder();
		return this;
	}

	def createNewFile() {
		parent.mkdirs();
		return with(resource.createFile().allowAnyProperties());
	}

	def allowAnyProperties() {
		resource.allowAnyProperties();
		return this;
	}

	def list() {
		return ItemIterator.create(context).with(resource.list());
	}

	def isCollection() {
		return resource.isCollection();
	}

	def getContent() {
		return current.resource.content;
	}

	def getContentAsReader() {
		return current.resource.contentAsReader;
	}

	def getContentAsStream() {
		return current.resource.contentAsStream;
	}

	def getContentAsByteArray() {
		return current.resource.contentAsByteArray;
	}

	def setContent(value) {
		resource.write(value);
		return this;
	}

	def getContentType() {
		return current.resource.contentType;
	}

	def setContentType(type) {
		resource.setProperty("jcr:mimeType", type);
		return this;
	}

	def getContentEncoding() {
		return current.resource.contentEncoding;
	}

	def getContentLength() {
		return current.resource.contentLength;
	}

	def getCreated() {
		return resource.created;
	}

	def getLastModified() {
		return current.resource.lastModified;
	}

	def getCreatedBy() {
		return resource.createdBy;
	}

	def getLastModifiedBy() {
		return current.resource.lastModifiedBy;
	}

	def isVersionControlled() {
		return resource.isVersionControlled();
	}

	def isCheckedOut() {
		return resource.isCheckedOut();
	}

	def addVersionControl() {
		resource.addVersionControl();
		return this;
	}

	def checkout() {
		resource.checkout();
		return this;
	}

	def checkin() {
		return Version.create(context).with(resource.checkin());
	}

	def checkpoint() {
		return Version.create(context).with(resource.checkpoint());
	}

	def getBaseVersion() {
		return Version.create(context).with(resource.baseVersion);
	}

	def getCurrent() {
		if (resource.isCollection()) {
			return this;
		}
		if (resource instanceof org.mintjams.script.resource.version.FrozenResource) {
			return this;
		}
		if (currentItem) {
			return currentItem;
		}

		try {
			if (isVersionControlled() && isCheckedOut() && isLocked()) {
				if (isLocked() && (lockedBy == context.repositorySession.userID)) {
					currentItem = this;
				} else {
					currentItem = baseVersion.frozen;
				}
			}
		} catch (java.lang.UnsupportedOperationException ignore) {
			currentItem = this;
		}
		if (!currentItem) {
			currentItem = this;
		}
		return currentItem;
	}

	def getVersionHistory() {
		return VersionHistory.create(context).with(resource.versionHistory);
	}

	def isReferenceable() {
		return resource.isReferenceable();
	}

	def addReferenceable() {
		resource.addReferenceable();
		return this;
	}

	def removeReferenceable() {
		resource.removeReferenceable();
		return this;
	}

	def isLocked() {
		return resource.isLocked();
	}

	def holdsLock() {
		return resource.holdsLock();
	}

	def getLockedBy() {
		return resource.lockedBy;
	}

	def lock() {
		resource.lock();
		return this;
	}

	def lock(isDeep) {
		resource.lock(isDeep);
		return this;
	}

	def lock(isDeep, isSessionScoped) {
		resource.lock(isDeep, isSessionScoped);
		return this;
	}

	def unlock() {
		resource.unlock();
		return this;
	}

 	def contains(key) {
		return current.resource.hasProperty(key);
 	}

	def getBoolean(key, defaultValue = false) {
		if (!contains(key)) {
			return defaultValue;
		}
		return current.resource.getProperty(key).boolean;
	}

 	def getBooleanArray(key, defaultValue = new boolean[0]) {
		if (!contains(key)) {
			return defaultValue;
		}
		return current.resource.getProperty(key).booleanArray;
 	}

 	def getDate(key, defaultValue = new Date()) {
		if (!contains(key)) {
			return defaultValue;
		}
		return current.resource.getProperty(key).date.time;
 	}

 	def getDateArray(key, defaultValue = new Date[0]) {
		if (!contains(key)) {
			return defaultValue;
		}
		return current.resource.getProperty(key).dateArray;
 	}

 	def getDecimal(key, defaultValue = BigDecimal.ZERO) {
		if (!contains(key)) {
			return defaultValue;
		}
		return current.resource.getProperty(key).decimal;
 	}

 	def getDecimalArray(key, defaultValue = new BigDecimal[0]) {
		if (!contains(key)) {
			return defaultValue;
		}
		return current.resource.getProperty(key).decimalArray;
 	}

 	def getDouble(key, defaultValue = 0) {
		if (!contains(key)) {
			return defaultValue;
		}
		return current.resource.getProperty(key).double;
 	}

 	def getDoubleArray(key, defaultValue = new double[0]) {
		if (!contains(key)) {
			return defaultValue;
		}
		return current.resource.getProperty(key).doubleArray;
 	}

 	def getLong(key, defaultValue = 0) {
		if (!contains(key)) {
			return defaultValue;
		}
		return current.resource.getProperty(key).long;
 	}

 	def getLongArray(key, defaultValue = new long[0]) {
		if (!contains(key)) {
			return defaultValue;
		}
		return current.resource.getProperty(key).longArray;
 	}

 	def getInt(key, defaultValue = 0) {
		if (!contains(key)) {
			return defaultValue;
		}
		return current.resource.getProperty(key).int;
 	}

 	def getIntArray(key, defaultValue = new int[0]) {
		if (!contains(key)) {
			return defaultValue;
		}
		return current.resource.getProperty(key).intArray;
 	}

 	def getString(key, defaultValue = "") {
		if (!contains(key)) {
			return defaultValue;
		}
		return current.resource.getProperty(key).string;
 	}

 	def getStringArray(key, defaultValue = new String[0]) {
		if (!contains(key)) {
			return defaultValue;
		}
		return current.resource.getProperty(key).stringArray;
 	}

 	def getByteArray(key, defaultValue = new byte[0]) {
		if (!contains(key)) {
			return defaultValue;
		}
		return current.resource.getProperty(key).byteArray;
 	}

 	def getStream(key, defaultValue = new ByteArrayInputStream(new byte[0])) {
		if (!contains(key)) {
			return defaultValue;
		}
		return current.resource.getProperty(key).stream;
 	}

	def getReferencedItem(key, defaultValue = null) {
		if (!contains(key)) {
			return defaultValue;
		}
		return Item.create(context).with(current.resource.getProperty(key).resource);
	}

	def getTemplate(prefix, suffix) {
		def tmpl = context.templateResolver.getTemplate(resource, prefix, suffix);
		if (!tmpl) {
			return null;
		}
		return [
			"item": Item.create(context).with(tmpl.resource),
			"scriptExtension": tmpl.scriptExtension
		];
	}

	def getTemplate(suffix) {
		def tmpl = context.templateResolver.getTemplate(resource, suffix);
		if (!tmpl) {
			return null;
		}
		return [
			"item": Item.create(context).with(tmpl.resource),
			"scriptExtension": tmpl.scriptExtension
		];
	}

 	def setAttribute(key, value) {
		if (value == null) {
			removeAttribute(key);
			return this;
		}

		resource.setProperty(key, value);
		return this;
 	}

 	def setAttribute(String key, String value, boolean mask) {
		if (value == null) {
			removeAttribute(key);
			return this;
		}

		resource.setProperty(key, value, mask);
		return this;
 	}

 	def setAttribute(String key, String[] value, boolean mask) {
		if (value == null) {
			removeAttribute(key);
			return this;
		}

		resource.setProperty(key, value, mask);
		return this;
 	}

	def setAttribute(String key, byte[] value, String mimeType = "application/octet-stream") {
		if (value == null) {
			removeAttribute(key);
			return this;
		}

		resource.setProperty(key, "{binary:" + mimeType + ":" + value.length + "}" + value.encodeBase64().toString());
		return this;
	}

 	def removeAttribute(key) {
		if (!contains(key)) {
			return this;
		}
		resource.getProperty(key).remove();
		return this;
 	}

	def getDisplayText(key) {
		if (!facet) {
			facet = new Facet(this);
		}
		return facet.getDisplayText(key, locale);
	}

	def asScript() {
		def ScriptAPI = context.getAttribute("ScriptAPI");
		return ScriptAPI.createScript(resource).setAsync(false);
	}

	def canRead() {
		if (!resource) {
			return false;
		}
		return resource.canRead();
	}

	def canWrite() {
		if (!resource) {
			return false;
		}
		return resource.canWrite();
	}

	def canReadACL() {
		if (!resource) {
			return false;
		}
		return resource.canReadACL();
	}

	def canWriteACL() {
		if (!resource) {
			return false;
		}
		return resource.canWriteACL();
	}

	def canRemove() {
		if (!resource) {
			return false;
		}
		return resource.canRemove();
	}

	def moveTo(destAbsPath) {
		with(resource.moveTo(destAbsPath));
		return this;
	}

	def copyTo(destAbsPath) {
		return Item.create(context).with(resource.copyTo(destAbsPath));
	}

	def calculate() {
		if (isCollection()) {
			return this;
		}
		Calculator.create(context).with(this).calculate();
		return this;
	}

	def remove() {
		resource.remove();
		return this;
	}

	def toObject(exportsBinary = false) {
		if (!exists()) {
			return [
				"name": resource.name,
				"path": resource.path,
				"exists": false
			];
		}

		def o = [
			"id": getIdentifier(),
			"name": getName(),
			"path": getPath(),
			"exists": true,
			"isCollection": isCollection(),
			"creationTime": ISO8601.formatDate(getCreated()),
			"lastModificationTime": ISO8601.formatDate(getLastModified()),
			"createdBy": getCreatedBy(),
			"lastModifiedBy": getLastModifiedBy(),
			"isLocked": isLocked(),
			"holdsLock": holdsLock(),
			"isReferenceable": isReferenceable(),
			"canWrite": canWrite(),
			"canReadACL": canReadACL(),
			"canWriteACL": canWriteACL(),
			"canRemove": canRemove(),
			"isFrozen": false
		];
		if (isLocked()) {
			o.lockedBy = getLockedBy();
		}
		if (!isCollection()) {
			if (resource.hasProperty("jcr:uuid")) {
				o["uuid"] = resource.getProperty("jcr:uuid").getString();
			}
			o["contentLength"] = getContentLength();
			o["mimeType"] = getContentType();
			if (getContentEncoding()) {
				o["encoding"] = getContentEncoding();
			}
			o["isVersionControlled"] = isVersionControlled();
			if (isVersionControlled()) {
				o["isCheckedOut"] = isCheckedOut();
				o["version"] = baseVersion.getName();
			}
			if (resource instanceof org.mintjams.script.resource.version.FrozenResource) {
				o["isFrozen"] = true;
				o["frozenPath"] = resource.frozenPath;
				o["version"] = resource.version.name;
			}

			def r = current.resource;
			if (r.hasProperty("mi:imageWidth")) {
				o["mi:imageWidth"] = r.getProperty("mi:imageWidth").getInt();
			}
			if (r.hasProperty("mi:imageHeight")) {
				o["mi:imageHeight"] = r.getProperty("mi:imageHeight").getInt();
			}
			if (r.hasProperty("mi:orientation")) {
				o["mi:orientation"] = r.getProperty("mi:orientation").getInt();
			}
			o["hasThumbnail"] = r.hasProperty("mi:thumbnail");

			o.properties = [:];
			for (p in r.properties) {
				if (p.name.startsWith("jcr:")) {
					continue;
				}

				def prop = [
					"key": p.name,
					"type": p.typeName,
					"isMultiple": p.isMultiple()
				];

				if (!p.isMultiple()) {
					if (p.value == null) {
						o.properties[p.name] = prop;
						continue;
					}
				} else {
					if (p.values == null) {
						o.properties[p.name] = prop;
						continue;
					}
				}

				if (p.typeName == "Binary") {
					if (exportsBinary) {
						prop.value = p.getByteArray().encodeBase64().toString();
					}
				} else if (p.typeName == "Reference" || p.typeName == "WeakReference" || p.typeName == "Path") {
					try {
						prop.value = Item.create(context).with(p.resource).toObject();
					} catch (Throwable ex) {
						context.getAttribute("log").warn("Could not obtain a node which this property refers: Path: " + resource.path + "@" + p.name + ", Exception: " + ex.message);
					}
				} else if (p.typeName == "Date") {
					if (!p.isMultiple()) {
						prop.value = ISO8601.formatDate(p.value);
					} else {
						prop.value = [];
						for (def d : p.getValues()) {
							prop.value.add(ISO8601.formatDate(d));
						}
					}
				} else if (p.typeName == "String") {
					if (!p.isMultiple()) {
						prop.value = p.value;
						if (p.isMasked()) {
							prop.value = Session.create(context).mask(p.value);
						}
					} else {
						prop.value = p.values.collect { v ->
							if (p.isMasked()) {
								v = Session.create(context).mask(v);
							}
							return v;
						}
					}
				} else {
					if (!p.isMultiple()) {
						prop.value = p.value;
					} else {
						prop.value = p.values;
					}
				}
				o.properties[p.name] = prop;
			}
		}

		return o;
	}

	def toJson(exportsBinary = false) {
		return JSON.stringify(toObject(exportsBinary));
	}
}
