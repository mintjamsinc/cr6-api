// Copyright (c) 2021 MintJams Inc. Licensed under MIT License.

package api.http;

import api.util.Text;
import groovy.json.JsonOutput;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.yaml.snakeyaml.Yaml;

class WebResponse {
	HttpServletResponse response;
	long contentLength = -1;
	String eTag;

	WebResponse(HttpServletResponse response) {
		this.response = response;
	}

	static WebResponse create(HttpServletResponse response) {
		return new WebResponse(response);
	}

	WebResponse setStatus(int status) {
		response.setStatus(status);
		return this;
	}

    def getHeaderNames() {
        return response.getHeaderNames().toList();
    }

    def containsHeader(name) {
        return response.containsHeader(name);
    }

    def getHeaders(name) {
        return response.getHeaders(name).toList();
    }

    def getHeader(name) {
        return response.getHeader(name);
    }

    def getIntHeader(name) {
        return response.getIntHeader(name);
    }

    def getDateHeader(name) {
        return response.getDateHeader(name);
    }

    def setHeader(name, value) {
        if (value instanceof Number ||
            value instanceof int ||
            value instanceof long ||
            value instanceof float ||
            value instanceof double) {
            response.setIntHeader(name, value as int);
        } else if (value instanceof Date) {
            response.setDateHeader(name, value);
        } else if (value instanceof String) {
            response.setHeader(name, value as String);
        } else if (value instanceof List) {
            for (def i = 0; i < value.size(); i++) {
                def e = value.get(i);
                if (i == 0) {
                    setHeader(name, e);
                } else {
                    addHeader(name, e);
                }
            }
        } else {
            throw new IllegalArgumentException("Invalid value: " + value.class.name);
        }
        return this;
    }

    def addHeader(name, value) {
        if (value instanceof Number ||
            value instanceof int ||
            value instanceof long ||
            value instanceof float ||
            value instanceof double) {
            response.addIntHeader(name, value as int);
        } else if (value instanceof Date) {
            response.addDateHeader(name, value);
        } else if (value instanceof String) {
            response.addHeader(name, value as String);
        } else if (value instanceof List) {
            for (def i = 0; i < value.size(); i++) {
                def e = value.get(i);
                addHeader(name, e);
            }
        } else {
            throw new IllegalArgumentException("Invalid value: " + value.class.name);
        }
        return this;
    }

	WebResponse setContentType(String type) {
		response.setContentType(type);
		return this;
	}

	String getCharacterEncoding() {
		return response.getCharacterEncoding();
	}

	WebResponse setCharacterEncoding(String encoding) {
		response.setCharacterEncoding(encoding);
		return this;
	}

	WebResponse setContentLength(long length) {
		response.setContentLengthLong(length);
		contentLength = length;
		return this;
	}

	WebResponse setETag(String tag) {
		response.setHeader("ETag", tag);
		eTag = tag;
		return this;
	}

	WebResponse setInline() {
		response.setHeader("Content-Disposition", "inline");
		return this;
	}

	WebResponse setAttachment() {
		response.setHeader("Content-Disposition", "attachment");
		return this;
	}

	WebResponse setAttachment(String filename) {
		response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + Text.encodeURIComponent(filename));
		return this;
	}

	WebResponse write(java.io.InputStream value) {
		value.withCloseable { stream ->
			IOUtils.copyLarge(stream, response.getOutputStream());
		}
		return this;
	}

	WebResponse writePartial(java.io.InputStream value, Object range) {
		if (contentLength < 0) {
			throw new IllegalArgumentException("Could not write content: ContentLength: " + contentLength);
		}
		if (!eTag) {
			throw new IllegalArgumentException("Could not write content: eTag: " + eTag);
		}

		if (range != null) {
			if (range.ifRange && range.ifRange != eTag) {
				range = null;
			}
		}

		if (range == null) {
			setStatus(200);
			setContentLength(contentLength);
			response.setHeader("Accept-Ranges", "bytes");
			value.withCloseable { stream ->
				IOUtils.copyLarge(stream, response.getOutputStream());
			}
			return this;
		}

		value.withCloseable { stream ->
			def length;
			if (range.end >= 0) {
				length = (range.end - range.start + 1);
			} else {
				range.end = contentLength - 1;
				length = (contentLength - range.start);
			}
			setStatus(206);
			setContentLength(length);
			response.setHeader("Content-Range", "bytes " + range.start + "-" + range.end + "/" + contentLength);
			IOUtils.copyLarge(stream, response.getOutputStream(), range.start, length);
		}
		return this;
	}

	WebResponse write(java.io.Reader value) {
		value.withCloseable { reader ->
			IOUtils.copyLarge(reader, response.getWriter());
		}
		return this;
	}

	WebResponse write(CharSequence value) {
		response.getWriter().append(value);
		return this;
	}

	WebResponse writeAsJson(Object value) {
		setContentType("application/json");
		write(JsonOutput.toJson(value));
		return this;
	}

	WebResponse writeAsYaml(Object value) {
		setContentType("application/yaml");
		write(new Yaml().dump(value));
		return this;
	}

	WebResponse enableContentCache() {
		response.setHeader("Cache-Control", "public, max-age=31536000");
		response.setHeader("Expires", "");
		response.setHeader("Pragma", "");
		return this;
	}

	WebResponse disableContentCache() {
		response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
		response.setHeader("Expires", "0");
		response.setHeader("Pragma", "no-cache");
		return this;
	}

	def addCookie(Cookie cookie) {
	    response.addCookie(cookie);
	    return this;
	}

	def sendError(throwable) {
		def name = throwable.class.simpleName;
		def message = (throwable.message?.trim() ?: "");

		def names = [];
		if (name == "JcrException") {
			names.add(name);
			for (def cause = throwable.cause; cause; cause = cause.cause) {
				name = cause.class.simpleName;
				names.add(name);
			}
		}

		def resp = [
			"status": 500,
			"statusText": (message?.trim() ?: "")
		];
		if (name == "IllegalArgumentException") {
			resp.status = 400;
			resp.statusText = "Bad request: " + resp.statusText;
		} else if (name == "CredentialExpiredException") {
			resp.status = 409;
			resp.statusText = "Credential expired: " + resp.statusText;
		} else if (name == "LoginException") {
			resp.status = 401;
			resp.statusText = "Login: " + resp.statusText;
		} else if (name == "PasswordHistoryException") {
			resp.status = 409;
			resp.statusText = "Password history: " + resp.statusText;
		} else if (name == "AccessDeniedException") {
			resp.status = 403;
			resp.statusText = "Access denied: " + resp.statusText;
		} else if (name == "AccessControlException") {
			resp.status = 403;
			resp.statusText = "Access denied: " + resp.statusText;
		} else if (name == "NotFoundException") {
			resp.status = 404;
			resp.statusText = "Not found: " + resp.statusText;
		} else if (name == "AlreadyExistsException") {
			resp.status = 409;
			resp.statusText = "Already exists: " + resp.statusText;
		} else if (name == "LockException") {
			resp.status = 423;
			resp.statusText = "Locked: " + resp.statusText;
		} else if (name == "InvalidItemStateException") {
			resp.status = 412;
			resp.statusText = "Invalid item state: " + resp.statusText;
		} else if (name == "CommitFailedException") {
			if (names.contains("ReferentialIntegrityException")) {
				resp.status = 412;
				resp.statusText = "Referential integrity: " + resp.statusText;
			}
		} else if (name == "SuspendedEntityInteractionException") {
			resp.status = 412;
			resp.statusText = "Suspended: " + resp.statusText;
		} else if (name == "NullValueException") {
			resp.status = 404;
			resp.statusText = "Not found: " + resp.statusText;
		}

		response.setStatus(resp.status);
		response.setContentType("application/json");
		response.getWriter().print(JsonOutput.toJson(resp));
	}
}
