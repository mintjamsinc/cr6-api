// Copyright (c) 2021 MintJams Inc. Licensed under MIT License.

package api.security;

import org.mintjams.script.ScriptingContext;

class User extends Authorizable {
	User(context) {
		super(context);
	}

	static def create(ScriptingContext context) {
		return new User(context);
	}

	def with(org.mintjams.jcr.security.UserPrincipal authorizable) {
		super.with(authorizable);
		return this;
	}

	def isAdmin() {
		return (authorizable instanceof org.mintjams.jcr.security.AdminPrincipal);
	}

	def isAnonymous() {
		return (authorizable instanceof org.mintjams.jcr.security.GuestPrincipal);
	}
}