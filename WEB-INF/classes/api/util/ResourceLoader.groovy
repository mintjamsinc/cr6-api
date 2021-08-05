// Copyright (c) 2021 MintJams Inc. Licensed under MIT License.

package api.util;

import jp.co.mintjams.osgi.service.jcr.Resource;
import jp.co.mintjams.osgi.service.jcr.ResourceResolver;
import jp.co.mintjams.osgi.service.jcr.script.ScriptingContext;

class ResourceLoader {
	ResourceResolver resolver;

	ResourceLoader(ResourceResolver resolver) {
		this.resolver = resolver;
	}

	static ResourceLoader create(ResourceResolver value) {
		return new ResourceLoader(value);
	}

	static ResourceLoader create(Resource value) {
		return new ResourceLoader(value.resourceResolver);
	}

	static ResourceLoader create(ScriptingContext value) {
		return new ResourceLoader(value.repositorySession.resourceResolver);
	}

	Object loadAsYaml(String path) {
		return YAML.parse(resolver.getResource(path));
	}

	Object loadAsJson(String path) {
		return JSON.parse(resolver.getResource(path));
	}
}
