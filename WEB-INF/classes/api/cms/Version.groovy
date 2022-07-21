// Copyright (c) 2021 MintJams Inc. Licensed under MIT License.

package api.cms;

import api.util.ISO8601;
import api.util.JSON;
import org.mintjams.script.ScriptingContext;

class Version {
	def context;
	def version;

	Version(context) {
		this.context = context;
	}

	static def create(ScriptingContext context) {
		return new Version(context);
	}

	def with(org.mintjams.script.resource.version.Version version) {
		this.version = version;
		return this;
	}

	def getName() {
		return version.name;
	}

	def getContainingHistory() {
		return VersionHistory.create(context).with(version.containingHistory);
	}

	def getCreated() {
		return version.created;
	}

	def getCreatedBy() {
		return version.frozen.createdBy;
	}

	def getFrozen() {
		return Item.create(context).with(version.frozen);
	}

	def getLinearPredecessor() {
		return Version.create(context).with(version.linearPredecessor);
	}

	def getLinearSuccessor() {
		return Version.create(context).with(version.linearSuccessor);
	}

	def getPredecessors() {
		def l = [];
		version.predecessors.each { e ->
			l.add(Version.create(context).with(e));
		}
		return l;
	}

	def getSuccessors() {
		def l = [];
		version.successors.each { e ->
			l.add(Version.create(context).with(e));
		}
		return l;
	}

	def getLabels() {
		return version.labels;
	}

	def hasLabel(label) {
		return version.hasLabel(label);
	}

	def addLabel(label, moveLabel) {
		version.addLabel(label, moveLabel);
		return this;
	}

	def remove() {
		version.remove();
		return this;
	}

	def restore() {
		version.restore();
		return this;
	}

	def toObject() {
		def o = [
			"name": name,
			"creationTime": ISO8601.formatDate(created),
			"createdBy": createdBy,
			"labels": labels
		];

		return o;
	}

	def toJson() {
		return JSON.stringify(toObject());
	}
}
