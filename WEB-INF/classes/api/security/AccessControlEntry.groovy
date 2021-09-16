// Copyright (c) 2021 MintJams Inc. Licensed under MIT License.

package api.security;

import api.util.JSON;
import jp.co.mintjams.osgi.service.jcr.script.ScriptingContext;

class AccessControlEntry {
	def context;
	def accessControlEntry;

	AccessControlEntry(context) {
		this.context = context;
	}

	static def create(ScriptingContext context) {
		return new AccessControlEntry(context);
	}

	def with(jp.co.mintjams.osgi.service.jcr.security.AccessControlManager.AccessControlEntry accessControlEntry) {
		this.accessControlEntry = accessControlEntry;
		return this;
	}

	def getGrantee() {
		return Authorizable.create(context).with(accessControlEntry.principal);
	}

	def isAllow() {
		return accessControlEntry.isAllow();
	}

	def getPrivileges() {
		return accessControlEntry.privileges.collect {
			Privilege.create(context).with(it);
		}
	}

	def toObject() {
		def o = [
			"id": grantee.name,
			"grantee": grantee.name,
			"isAllow": isAllow(),
			"privileges": privileges.collect {
				it.name;
			}
		];

		return o;
	}

	def toJson() {
		return JSON.stringify(toObject());
	}
}
