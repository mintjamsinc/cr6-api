// Copyright (c) 2021 MintJams Inc. Licensed under MIT License.

package api.util;

import org.yaml.snakeyaml.Yaml;

class YAML {
	static Object parse(Object value) {
		if (value == null) {
			throw new IllegalArgumentException("Could not parse YAML text: null");
		}

		if (value instanceof String) {
			return new Yaml().load(value);
		}

		if (value instanceof java.io.InputStream ||
				value instanceof java.io.Reader) {
			return value.withCloseable { vin ->
				return new Yaml().load(vin);
			}
		}

		if (value instanceof jp.co.mintjams.osgi.service.jcr.Resource) {
			return value.getContentAsReader().withCloseable { reader ->
				return new Yaml().load(reader);
			}
		}

		if (value instanceof File) {
			return new Yaml().load(value.text);
		}

		throw new IllegalArgumentException("Could not parse YAML text: Class: " + value.getClass().getName());
	}

	static String stringify(Object value) {
		return new Yaml().dump(value);
	}
}
