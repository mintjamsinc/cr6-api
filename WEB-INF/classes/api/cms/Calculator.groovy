// Copyright (c) 2021 MintJams Inc. Licensed under MIT License.

package api.cms;

import api.util.JSON;
import api.util.ResourceLoader;
import org.mintjams.script.ScriptingContext;

class Calculator {
	def context;
	def item;

	Calculator(context) {
		this.context = context;
	}

	static def create(ScriptingContext context) {
		return new Calculator(context);
	}

	def with(Item item) {
		this.item = item;
		return this;
	}

	def calculate() {
		if (!item) {
			throw new java.lang.IllegalStateException("item");
		}

		def arguments = [
			"item" : item.toObject(),
			"facetDefinitions" : getFacetDefinitions()
		];

		def ScriptAPI = context.getAttribute("ScriptAPI");
		def resourceResolver = context.resourceResolver;
		def scriptExtension;
		if (context.scriptEngineManager.getEngineByExtension("njs")) {
			scriptExtension = "njs";
		} else {
			scriptExtension = "es";
		}
		def scriptResource = context.resourceResolver.getResource("/content/WEB-INF/classes/api/cms/Calculator_calculate." + scriptExtension);
		def resultJson = ScriptAPI.createScript(scriptResource)
			.setAsync(false)
			.setAttribute("parametersJson", JSON.stringify(arguments))
			.eval();
		def properties = JSON.parse(resultJson);
		if (!properties) {
			return;
		}

		ItemHelper.create(context).with(item).importAttributes(properties);
	}

	def getFacetDefinitions() {
		def resourceResolver = context.resourceResolver;
		def facetDefinitions = [:];
		for (p in item.resource.properties) {
			def facetPath = "/content/WEB-INF/facets/" + p.name + ".yml";
			def facetResource = resourceResolver.getResource(facetPath);
			if (!facetResource.exists()) {
				continue;
			}

			def d = ResourceLoader.create(item.resource).loadAsYaml(facetPath);
			if (!d) {
				continue;
			}
			facetDefinitions[p.name] = d;
		}
		return facetDefinitions;
	}
}
