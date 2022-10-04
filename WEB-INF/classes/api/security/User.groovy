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

	def with(org.mintjams.jcr.security.UserPrincipal principal) {
		super.with(principal);
		return this;
	}

	def isAdmin() {
		return (principal instanceof org.mintjams.jcr.security.AdminPrincipal);
	}

	def isAnonymous() {
		return (principal instanceof org.mintjams.jcr.security.GuestPrincipal);
	}
}