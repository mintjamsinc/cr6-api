// Copyright (c) 2021 MintJams Inc. Licensed under MIT License.

package api.util;

import org.mintjams.script.resource.Resource;
import org.mintjams.script.resource.ResourceResolver;
import org.mintjams.script.ScriptingContext;

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
