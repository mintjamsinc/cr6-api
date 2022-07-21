// Copyright (c) 2021 MintJams Inc. Licensed under MIT License.

package api.security;

import api.util.JSON;
import org.mintjams.script.ScriptingContext;

class Authorizable {
	def context;
	def identifier;
	def authorizable;

	Authorizable(context) {
		this.context = context;
	}

	static def create(ScriptingContext context) {
		return new Authorizable(context);
	}

	def with(java.security.Principal authorizable) {
		this.authorizable = authorizable;
		if (authorizable) {
			this.identifier = authorizable.name;
		}
		return this;
	}

	def getName() {
		if (!authorizable) {
			return null;
		}
		return authorizable.name;
	}

	def isGroup() {
		return (authorizable instanceof org.mintjams.jcr.security.GroupPrincipal);
	}

	Object toObject() {
		def o = [
			"id" : getName(),
			"isGroup" : isGroup()
		];
		return o;
	}

	String toJson() {
		return JSON.stringify(toObject());
	}
}
