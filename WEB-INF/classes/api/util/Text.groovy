// Copyright (c) 2021 MintJams Inc. Licensed under MIT License.

package api.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.MessageFormat;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.mintjams.rt.cms.internal.web.Webs;

class Text {
	static String encodeURIComponent(String value) {
		return Webs.encode(value);
	}

	static String encodeURI(String value) {
		return Webs.encodePath(value);
	}

	static String decodeURIComponent(String value) {
		return Webs.decode(value);
	}

	static def escapeHtml(String value) {
		return StringEscapeUtils.escapeHtml4(value);
	}

	static def escapeEcmaScript(String value) {
		return StringEscapeUtils.escapeEcmaScript(value);
	}

	static def escapeJson(String value) {
		return StringEscapeUtils.escapeJson(value);
	}

	static def format(pattern, ... args) {
		return new MessageFormat(pattern).format(args);
	}

	static def split(value, separatorChars) {
		return StringUtils.split(value, separatorChars);
	}

	static def replace(value, searchString, replacement) {
		return StringUtils.replace(value, searchString, replacement);
	}

	static def digestAsHex(String value, String algorithm) {
		return MessageDigest.getInstance(algorithm).digest(value.getBytes(StandardCharsets.UTF_8)).encodeHex().toString();
	}
}
