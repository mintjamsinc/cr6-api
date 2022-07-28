// Copyright (c) 2021 MintJams Inc. Licensed under MIT License.

package api.http;

import api.util.JSON;
import api.util.YAML;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import org.mintjams.script.ScriptingContext;

class WebRequest {
	def context;
	HttpServletRequest request;

	WebRequest(context) {
		this.context = context;
	}

	static def create(ScriptingContext context) {
		return new WebRequest(context);
	}

	def with(HttpServletRequest request) {
		this.request = request;
		return this;
	}

	Object parseRequest() {
		def type = request.contentType?:"";
		def p = type.indexOf(";");
		if (p != -1) {
			type = type.substring(0, p);
		}

		if (type == "application/json") {
			return request.getReader().withCloseable { entity ->
				return JSON.parse(entity);
			}
		}

		if (type == "application/yaml") {
			return request.getReader().withCloseable { entity ->
				return YAML.parse(entity);
			}
		}

		throw new IllegalArgumentException("Could not parse HTTP request: ContentType: " + request.contentType);
	}

	String getScheme() {
		return request.getScheme();
	}

	String getMethod() {
		return request.getMethod();
	}

	String getContentType() {
		return request.getContentType();
	}

	String getCharacterEncoding() {
		return request.getCharacterEncoding();
	}

	WebRequest setCharacterEncoding(String encoding) {
		request.setCharacterEncoding(encoding);
		return this;
	}

	Cookie getCookie(name) {
		def cookies = request.cookies;
		if (cookies != null) {
			for (cookie in cookies) {
				if (cookie.name == name) {
					return cookie;
				}
			}
		}
		return null;
	}

	def getHeaderNames() {
		return request.getHeaderNames().toList();
	}

	def getHeaders(name) {
		return request.getHeaders(name).toList();
	}

	def getHeader(name) {
		return request.getHeader(name);
	}

	def getIntHeader(name) {
		return request.getIntHeader(name);
	}

	def getDateHeader(name) {
		return request.getDateHeader(name);
	}

	boolean hasRange() {
		return !!request.getHeader("Range");
	}

	Range getRange() {
		if (!hasRange()) {
			return null;
		}

		def header = request.getHeader("Range");
		def keyValue = header.split("=");
		def ranges = keyValue[1].split("-");
		return new Range(
			"ifRange": request.getHeader("If-Range"),
			"unit": keyValue[0],
			"start": (ranges[0]) ? (ranges[0] as Long) : 0,
			"end": (ranges.length > 1) ? (ranges[1] as Long) : -1
		);
	}

	class Range {
		String ifRange;
		String unit;
		long start;
		long end;

		boolean isValid() {
			if (unit != "bytes") {
				return false;
			}

			if (start < 0) {
				return false;
			}

			if (end >= 0) {
				if (start > end) {
					return false;
				}
			}

			return true;
		}
	}
}
