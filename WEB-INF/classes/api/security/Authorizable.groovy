// Copyright (c) 2021 MintJams Inc. Licensed under MIT License.

package api.security;

import api.cms.Item;
import api.util.JSON;
import api.util.Text;
import org.mintjams.script.ScriptingContext;

class Authorizable {
	def context;
	def principal;

	Authorizable(context) {
		this.context = context;
	}

	static def create(ScriptingContext context) {
		return new Authorizable(context);
	}

	def with(java.security.Principal principal) {
		if (principal instanceof org.mintjams.jcr.security.GroupPrincipal) {
			return Group.create(context).with(principal);
		}
		return User.create(context).with(principal);
	}

	def getIdentifier() {
		return principal.name;
	}

	def getHomeFolder() {
		return Item.create(context).with(context.session.userManager.getHomeFolder(principal));
	}

	def getAttributes() {
		def item = getHomeFolder().getItem("attributes");
		if (!item.exists()) {
			item.createNewFile();
		}
		return item;
	}

	def exists() {
		return getHomeFolder().exists();
	}

	def isGroup() {
		if (exists()) {
			return getAttributes().getBoolean("isGroup");
		}
		return (this instanceof Group);
	}

	Object toObject() {
		def o = [
			"id" : getIdentifier(),
			"isGroup" : isGroup(),
			"properties" : [:]
		];

		def pref = Item.create(context).findByPath("/home/" + principal.name + "/preferences");
		if (pref.exists()) {
			for (e in pref.toObject().properties) {
				o.properties[e.key] = e.value;
			}
		}
		def attr = getAttributes().toObject();
		for (e in attr.properties) {
			o.properties[e.key] = e.value;
		}
		o.creationTime = attr.creationTime;
		o.lastModificationTime = attr.lastModificationTime;
		o.createdBy = attr.createdBy;
		o.lastModifiedBy = attr.lastModifiedBy;

		return o;
	}

	String toJson() {
		return JSON.stringify(toObject());
	}
}
