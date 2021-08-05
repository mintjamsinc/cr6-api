// Copyright (c) 2021 MintJams Inc. Licensed under MIT License.

package api.cms;

import api.util.ISO8601;
import api.util.JSON;
import api.util.ResourceLoader;
import java.util.Locale;

class Facet {
	def item;
	def itemObject;
	def facetDefinitions = [:];
	def values = [:];

	Facet(Item item) {
    	this.item = item;
	}

	def getDisplayText(key, locale = Locale.getDefault()) {
        if (!locale) {
	        locale = Locale.getDefault();
        }

	    def valueKey = key + "," + locale.toString();
	    if (values.containsKey(valueKey)) {
	        return values[valueKey];
	    }

	    if (!facetDefinitions.containsKey(key)) {
			def facetPath = "/WEB-INF/facets/" + key + ".yml";
			def facetResource = item.resource.resourceResolver.getResource(facetPath);
			if (facetResource.exists()) {
    			def d = ResourceLoader.create(item.resource).loadAsYaml(facetPath);
    			if (d) {
        			facetDefinitions[key] = d;
    			}
			}
	    }

		def ScriptAPI = item.context.getAttribute("ScriptAPI");
		if (!itemObject) {
		    itemObject = item.toObject();
		}
		def arguments = [
			"item": itemObject,
			"facetDefinitions": facetDefinitions,
			"key": key
		];
		if (locale) {
			arguments.locale = locale.toString();
		}

        def scriptExtension;
		if (item.context.scriptEngineManager.getEngineByExtension("njs")) {
		    scriptExtension = "njs";
		} else {
		    scriptExtension = "es";
		}
		def scriptResource = item.resource.resourceResolver.getResource("/WEB-INF/classes/api/cms/Facet_getDisplayText." + scriptExtension);
		def resultJson = ScriptAPI.createScript(scriptResource)
			.setAsync(false)
			.setAttribute("parametersJson", JSON.stringify(arguments))
			.eval();
		def value = JSON.parse(resultJson).value;
		values[valueKey] = value;
		return value;
	}
}
