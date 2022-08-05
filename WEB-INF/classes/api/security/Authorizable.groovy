// Copyright (c) 2021 MintJams Inc. Licensed under MIT License.

package api.security;

import api.cms.Item;
import api.util.JSON;
import api.util.Text;
import org.mintjams.script.ScriptingContext;

class Authorizable {
	def context;
	def _identifier;

	Authorizable(context) {
		this.context = context;
	}

	static def create(ScriptingContext context) {
		return new Authorizable(context);
	}

	def with(String identifier) {
		_identifier = identifier;
		return this;
	}

	def with(java.security.Principal principal) {
		if (principal instanceof org.mintjams.jcr.security.GroupPrincipal) {
			return Group.create(context).with(principal);
		}
		return User.create(context).with(principal);
	}

	def getIdentifier() {
		return _identifier;
	}

	def getHomeFolder() {
		return Item.create(context).with(context.resourceResolver.session.userManager.getHomeFolder(getIdentifier()));
	}

	def exists() {
		return getHomeFolder().exists();
	}

	def isGroup() {
		if (exists()) {
			return getHomeFolder().getBoolean("isGroup");
		}
		return (this instanceof Group);
	}

	Object toObject() {
		def o = [
			"id" : getIdentifier(),
			"isGroup" : isGroup()
		];

		return o;
	}

	String toJson() {
		return JSON.stringify(toObject());
	}
}
