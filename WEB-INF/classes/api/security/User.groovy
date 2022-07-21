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

	def with(jp.co.mintjams.osgi.service.jcr.security.UserManager.Authorizable authorizable) {
		if (!authorizable) {
			return this;
		}
		if (authorizable.isGroup()) {
			throw new IllegalArgumentException("authorizable is group.");
		}
		super.with(authorizable);
		return this;
	}

	def isAdmin() {
		return authorizable.isAdmin();
	}

	def isAnonymous() {
		return authorizable.isAnonymous();
	}

	def isDisabled() {
		return authorizable.isDisabled();
	}

	def getDisabledReason() {
		return authorizable.getDisabledReason();
	}

	def canEdit() {
		return authorizable.canEdit();
	}

	def changePassword(password) {
		return with(authorizable.changePassword(password));
	}

	def changePassword(password, oldPassword) {
		return with(authorizable.changePassword(password, oldPassword));
	}

	def disable(reason) {
		return with(authorizable.disable(reason));
	}
}