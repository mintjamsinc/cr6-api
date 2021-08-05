// Copyright (c) 2021 MintJams Inc. Licensed under MIT License.

package api.security;

import groovy.json.JsonOutput;
import jp.co.mintjams.osgi.service.jcr.script.ScriptingContext;

class Group extends Authorizable {
    Group(context) {
        super(context);
    }

	static def create(ScriptingContext context) {
		return new Group(context);
	}

	def with(jp.co.mintjams.osgi.service.jcr.security.UserManager.Authorizable authorizable) {
	    if (!authorizable) {
	        return this;
	    }
	    if (!authorizable.isGroup()) {
	        throw new IllegalArgumentException("authorizable is user.");
	    }
		super.with(authorizable);
		return this;
	}

	def canEdit() {
	    return authorizable.canEdit();
	}

	def getDeclaredMembers() {
	    return authorizable.getDeclaredMembers().collect {
	        def a;
	        if (it.isGroup()) {
	            a = Group.create(context);
	        } else {
	            a = User.create(context);
	        }
	        a.with(it);
	    };
	}

	def getMembers() {
	    return authorizable.getMembers().collect {
	        def a;
	        if (it.isGroup()) {
	            a = Group.create(context);
	        } else {
	            a = User.create(context);
	        }
	        a.with(it);
	    };
	}

	def addMember(another) {
	    return authorizable.addMember(another);
	}

	def addMembers(String... names) {
	    return authorizable.addMembers(names);
	}

	def removeMember(another) {
	    return authorizable.removeMember(another);
	}

	def removeMembers(String... names) {
	    return authorizable.removeMembers(names);
	}

	def isDeclaredMember(another) {
	    return authorizable.isDeclaredMember(another);
	}

	def isMember(another) {
	    return authorizable.isMember(another);
	}
}