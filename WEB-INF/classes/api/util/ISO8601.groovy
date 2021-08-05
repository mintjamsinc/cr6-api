// Copyright (c) 2021 MintJams Inc. Licensed under MIT License.

package api.util;

class ISO8601 {
	static java.util.Date parseDate(String value) {
		return java.util.Date.from(java.time.OffsetDateTime.parse(value).toInstant());
	}

	static long parseDuration(String value) {
		return java.time.Duration.parse(value).toMillis();
	}

	static String formatDate(Object value) {
		if (value == null) {
			throw new IllegalArgumentException("Could not stringify date: null");
		}

		if (value instanceof java.util.Date) {
			return toISOString(value);
		}

		if (value instanceof java.util.Calendar) {
			return toISOString(value);
		}

		if (value instanceof Number) {
			return toISOString(new java.util.Date(value.longValue()));
		}

		throw new IllegalArgumentException("Could not stringify date: " + value.getClass().getName());
	}

	static String toISOString(java.util.Date value) {
		return value.format("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", TimeZone.getTimeZone("UTC"));
	}

	static String toISOString(java.util.Calendar value) {
		return toISOString(value.time);
	}
}
