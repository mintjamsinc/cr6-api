// Copyright (c) 2021 MintJams Inc. Licensed under MIT License.

package api.util;

import java.text.MessageFormat;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

class Text {
	static String encodeURIComponent(String value) {
		return org.apache.jackrabbit.util.Text.escape(value);
	}

	static String encodeURI(String value) {
		return org.apache.jackrabbit.util.Text.escapePath(value);
	}

	static String decodeURIComponent(String value) {
		return org.apache.jackrabbit.util.Text.unescape(value);
	}

	static def escapeHtml(text) {
		return StringEscapeUtils.escapeHtml4(text);
	}

	static def escapeEcmaScript(text) {
		return StringEscapeUtils.escapeEcmaScript(text);
	}

	static def escapeJson(text) {
		return StringEscapeUtils.escapeJson(text);
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
}
