// Copyright (c) 2021 MintJams Inc. Licensed under MIT License.

package api.util;

import groovy.json.JsonOutput;
import groovy.json.JsonSlurper;

class JSON {
	static Object parse(Object value) {
		if (value == null) {
			throw new IllegalArgumentException("Could not parse JSON text: null");
		}

		if (value instanceof String) {
			return new JsonSlurper().parseText(value);
		}

		if (value instanceof java.io.InputStream ||
				value instanceof java.io.Reader) {
			return value.withCloseable { vin ->
				return new JsonSlurper().parse(vin);
			}
		}

		if (value instanceof jp.co.mintjams.osgi.service.jcr.Resource) {
			return value.getContentAsReader().withCloseable { reader ->
				return new JsonSlurper().parse(reader);
			}
		}

		if (value instanceof File) {
			return new JsonSlurper().parseText(value.text);
		}

		throw new IllegalArgumentException("Could not parse JSON text: Class: " + value.getClass().getName());
	}

	static String stringify(Object value) {
		return JsonOutput.toJson(value);
	}
}
