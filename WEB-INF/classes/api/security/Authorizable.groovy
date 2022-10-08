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
		return Item.create(context).findByPath(getHomeFolder().path + "/attributes");
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
		def prefObj = null;
		if (pref.exists()) {
			prefObj = pref.toObject();
			for (e in prefObj.properties) {
				o.properties[e.key] = e.value;
			}
		}
		def attr = getAttributes();
		def attrObj = null;
		if (attr.exists()) {
			attrObj = attr.toObject();
			for (e in attrObj.properties) {
				o.properties[e.key] = e.value;
			}
			o.creationTime = attrObj.creationTime;
			o.createdBy = attrObj.createdBy;
			o.lastModificationTime = attrObj.lastModificationTime;
			o.lastModifiedBy = attrObj.lastModifiedBy;
		}
		if (pref.exists()) {
			if (!attr.exists()) {
				o.creationTime = prefObj.creationTime;
				o.createdBy = prefObj.createdBy;
				o.lastModificationTime = prefObj.lastModificationTime;
				o.lastModifiedBy = prefObj.lastModifiedBy;
			} else {
				if (pref.created.time < attr.created.time) {
					o.creationTime = prefObj.creationTime;
					o.createdBy = prefObj.createdBy;
				}
				if (pref.lastModified.time > attr.lastModified.time) {
					o.lastModificationTime = prefObj.lastModificationTime;
					o.lastModifiedBy = prefObj.lastModifiedBy;
				}
			}
		}

		if (getIdentifier() == context.session.userID) {
			try {
				o.authenticatedFactors = context.getAttribute("session").getAttribute("org.mintjams.cms.security.auth.AuthenticatedFactors");
			} catch (Throwable ignore) {
				o.authenticatedFactors = "";
			}
		}

		return o;
	}

	String toJson() {
		return JSON.stringify(toObject());
	}
}
