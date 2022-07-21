// Copyright (c) 2021 MintJams Inc. Licensed under MIT License.

package api.security;

import org.mintjams.script.ScriptingContext;

class Group extends Authorizable {
	Group(context) {
		super(context);
	}

	static def create(ScriptingContext context) {
		return new Group(context);
	}

	def with(org.mintjams.jcr.security.GroupPrincipal authorizable) {
		super.with(authorizable);
		return this;
	}
}