// Copyright (c) 2021 MintJams Inc. Licensed under MIT License.

package api.security;

import api.util.JSON;
import jp.co.mintjams.osgi.service.jcr.script.ScriptingContext;

class Privilege {
	def context;
	def privilege;

	Privilege(context) {
		this.context = context;
	}

	static def create(ScriptingContext context) {
		return new Privilege(context);
	}

	def with(jp.co.mintjams.osgi.service.jcr.security.AccessControlManager.Privilege privilege) {
		this.privilege = privilege;
		return this;
	}

	def getAggregatePrivileges() {
		return privilege.aggregatePrivileges.collect {
			Privilege.create(context).with(it);
		}
	}

	def getDeclaredAggregatePrivileges() {
		return privilege.declaredAggregatePrivileges.collect {
			Privilege.create(context).with(it);
		}
	}

	def getName() {
		return privilege.name;
	}

	def isAbstract() {
		return privilege.isAbstract();
	}

	def isAggregate() {
		return privilege.isAggregate();
	}

	def toObject() {
		def o = [
			"id": name,
			"name": name,
			"isAbstract": isAbstract(),
			"isAggregate": isAggregate()
		];

		return o;
	}

	def toJson() {
		return JSON.stringify(toObject());
	}
}
