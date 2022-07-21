// Copyright (c) 2021 MintJams Inc. Licensed under MIT License.

package api.security;

import api.util.ISO8601;
import api.util.JSON;
import org.mintjams.script.ScriptingContext;

class Authorizable {
	def context;
	def identifier;
	def authorizable;

	Authorizable(context) {
		this.context = context;
	}

	static def create(ScriptingContext context) {
		return new Authorizable(context);
	}

	def with(jp.co.mintjams.osgi.service.jcr.security.UserManager.Authorizable authorizable) {
		this.authorizable = authorizable;
		if (authorizable) {
			this.identifier = authorizable.name;
		}
		return this;
	}

	def findByName(name) {
		this.identifier = name;
		with(context.repositorySession.userManager.getAuthorizable(name));
		if (!authorizable) {
			return this;
		}
		def a = isGroup() ? Group.create(context) : User.create(context);
		return a.with(authorizable);
	}

	def exists() {
		if (!authorizable) {
			return false;
		}
		return true;
	}

	def getName() {
		if (!authorizable) {
			return null;
		}
		return authorizable.name;
	}

	def isGroup() {
		return authorizable.isGroup();
	}

	def getDeclaredMemberOf() {
		return authorizable.declaredMemberOf().collect {
			Authorizable.create(context).with(it);
		}
	}

	def getMemberOf() {
		return authorizable.memberOf().collect {
			Authorizable.create(context).with(it);
		}
	}

	def contains(key) {
		return authorizable.hasProperty(key);
	}

	def getBoolean(key, defaultValue = false) {
		if (!contains(key)) {
			return defaultValue;
		}
		return authorizable.getProperty(key).boolean;
	}

	def getBooleanArray(key, defaultValue = new boolean[0]) {
		if (!contains(key)) {
			return defaultValue;
		}
		return authorizable.getProperty(key).booleanArray;
	}

	def getDate(key, defaultValue = new Date()) {
		if (!contains(key)) {
			return defaultValue;
		}
		return authorizable.getProperty(key).date.time;
	}

	def getDateArray(key, defaultValue = new Date[0]) {
		if (!contains(key)) {
			return defaultValue;
		}
		return authorizable.getProperty(key).dateArray;
	}

	def getDecimal(key, defaultValue = BigDecimal.ZERO) {
		if (!contains(key)) {
			return defaultValue;
		}
		return authorizable.getProperty(key).decimal;
	}

	def getDecimalArray(key, defaultValue = new BigDecimal[0]) {
		if (!contains(key)) {
			return defaultValue;
		}
		return authorizable.getProperty(key).decimalArray;
	}

	def getDouble(key, defaultValue = 0) {
		if (!contains(key)) {
			return defaultValue;
		}
		return authorizable.getProperty(key).double;
	}

	def getDoubleArray(key, defaultValue = new double[0]) {
		if (!contains(key)) {
			return defaultValue;
		}
		return authorizable.getProperty(key).doubleArray;
	}

	def getLong(key, defaultValue = 0) {
		if (!contains(key)) {
			return defaultValue;
		}
		return authorizable.getProperty(key).long;
	}

	def getLongArray(key, defaultValue = new long[0]) {
		if (!contains(key)) {
			return defaultValue;
		}
		return authorizable.getProperty(key).longArray;
	}

	def getInt(key, defaultValue = 0) {
		if (!contains(key)) {
			return defaultValue;
		}
		return authorizable.getProperty(key).int;
	}

	def getIntArray(key, defaultValue = new int[0]) {
		if (!contains(key)) {
			return defaultValue;
		}
		return authorizable.getProperty(key).intArray;
	}

	def getString(key, defaultValue = "") {
		if (!contains(key)) {
			return defaultValue;
		}
		return authorizable.getProperty(key).string;
	}

	def getStringArray(key, defaultValue = new String[0]) {
		if (!contains(key)) {
			return defaultValue;
		}
		return authorizable.getProperty(key).stringArray;
	}

	def getByteArray(key, defaultValue = new byte[0]) {
		if (!contains(key)) {
			return defaultValue;
		}
		def value = authorizable.getProperty(key).string;
		if (!value.startsWith("{binary:")) {
			return defaultValue;
		}
		return value.substring(value.indexOf("}") + 1).decodeBase64();
	}

	def getStream(key, defaultValue = new ByteArrayInputStream(new byte[0])) {
		if (!contains(key)) {
			return defaultValue;
		}
		def value = authorizable.getProperty(key).string;
		if (!value.startsWith("{binary:")) {
			return defaultValue;
		}
		return new ByteArrayInputStream(value.substring(value.indexOf("}") + 1).decodeBase64());
	}

	def getBinaryType(key) {
		if (!contains(key)) {
			throw new IllegalArgumentException(key);
		}
		def value = authorizable.getProperty(key).string;
		if (!value.startsWith("{binary:")) {
			throw new IllegalArgumentException(key);
		}
		def h = value.substring(0, value.indexOf("}") + 1);
		return h.substring("{binary:".length(), h.lastIndexOf(":"));
	}

	def getBinaryLength(key) {
		if (!contains(key)) {
			throw new IllegalArgumentException(key);
		}
		def value = authorizable.getProperty(key).string;
		if (!value.startsWith("{binary:")) {
			throw new IllegalArgumentException(key);
		}
		def h = value.substring(0, value.indexOf("}") + 1);
		return h.substring(h.lastIndexOf(":") + 1, h.length() - 1) as long;
	}

	def setAttribute(key, value) {
		if (value == null) {
			removeAttribute(key);
			return this;
		}

		authorizable.setProperty(key, value);
		return this;
	}

	def setAttribute(String key, String value, boolean mask) {
		if (value == null) {
			removeAttribute(key);
			return this;
		}

		authorizable.setProperty(key, value, mask);
		return this;
	}

	def setAttribute(String key, String[] value, boolean mask) {
		if (value == null) {
			removeAttribute(key);
			return this;
		}

		authorizable.setProperty(key, value, mask);
		return this;
	}

	def setAttribute(String key, byte[] value, String mimeType = "application/octet-stream") {
		if (value == null) {
			removeAttribute(key);
			return this;
		}

		authorizable.setProperty(key, "{binary:" + mimeType + ":" + value.length + "}" + value.encodeBase64().toString());
		return this;
	}

	def removeAttribute(key) {
		if (!contains(key)) {
			return this;
		}
		authorizable.getProperty(key).remove();
		return this;
	}

	def remove() {
		authorizable.remove();
		return this;
	}

	Object toObject() {
		if (!exists()) {
			return [
				"id": identifier,
				"exists": false
			];
		}

		def o = [
			"id" : authorizable.name,
			"exists": true,
			"isGroup" : authorizable.isGroup(),
			"canEdit" : authorizable.canEdit()
		];
		if (authorizable.hasProperty("rep:fullName")) {
			o.fullName = authorizable.getProperty("rep:fullName").getString();
		} else {
			o.fullName = "";
		}
		if (!authorizable.isGroup()) {
			o.isAdmin = authorizable.isAdmin();
			o.isAnonymous = authorizable.isAnonymous();
			o.isDisabled = authorizable.isDisabled();
			if (authorizable.isDisabled()) {
				o.disabledReason = authorizable.disabledReason;
			}
		} else {
			o.members = [];
			authorizable.declaredMembers.each { p ->
				o.members.add(p.name);
			}
		}
		o.groups = [];
		authorizable.declaredMemberOf().each { p ->
			o.groups.add(p.name);
		}
		o.properties = [:];
		for (p in authorizable.properties) {
			if (p.name.startsWith("jcr:")) {
				continue;
			}

			def prop = [
				"key" : p.name,
				"type" : p.typeName,
				"isMultiple" : p.isMultiple()
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
				// not set
			} else if (p.typeName == "Reference" || p.typeName == "WeakReference") {
				if (!p.isMultiple()) {
					prop.value = p.getValue(String.class);
				} else {
					prop.value = p.getValue(String[].class);
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
					if (!p.value.startsWith("{binary:")) {
						prop.value = p.value;
						if (p.isMasked()) {
							prop.value = Session.create(context).mask(p.value);
							prop.isMasked = true;
						}
					}
				} else {
					prop.value = p.values.collect { v ->
						if (v.startsWith("{binary:")) {
							return "";
						}
						if (p.isMasked()) {
							v = Session.create(context).mask(v);
							prop.isMasked = true;
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
		return o;
	}

	String toJson() {
		return JSON.stringify(toObject());
	}
}
