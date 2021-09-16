// Copyright (c) 2021 MintJams Inc. Licensed under MIT License.

package api.bpm;

import api.util.JSON;
import jp.co.mintjams.osgi.service.jcr.script.ScriptingContext;

class ActivityInstance {
	def context;
	def activityInstance;
	def processDefinition;
	def processInstance;

	ActivityInstance(context) {
		this.context = context;
	}

	static def create(ScriptingContext context) {
		return new ActivityInstance(context);
	}

	def with(org.camunda.bpm.engine.runtime.ActivityInstance activityInstance) {
		this.activityInstance = activityInstance;
		return this;
	}

	def exists() {
		return (activityInstance != null);
	}

	def getIdentifier() {
		return activityInstance.activityId;
	}

	def getName() {
		return activityInstance.activityName;
	}

	def getType() {
		return activityInstance.activityType;
	}

	def getChildActivityInstances() {
		return activityInstance.childActivityInstances.collect {
			ActivityInstance.create(context).with(it);
		}
	}

	def getExecutionIds() {
		return activityInstance.executionIds;
	}

	def getProcessDefinition() {
		if (!processDefinition) {
			processDefinition = ProcessDefinition.create(context).findByIdentifier(activityInstance.processDefinitionId);
		}
		return processDefinition;
	}

	def getProcessInstance() {
		if (!processInstance) {
			processInstance = ProcessInstance.create(context).findByIdentifier(activityInstance.processInstanceId);
		}
		return processInstance;
	}

	def toObject() {
		if (!exists()) {
			return [
				"exists": false
			];
		}

		def ProcessAPI = context.getAttribute("ProcessAPI");
		def o = [
			"id": getIdentifier(),
			"name": getName(),
			"type": getType(),
			"childActivityInstances": [],
			"executionIds": getExecutionIds(),
			"exists": true
		];

		getChildActivityInstances().each { a ->
			o.childActivityInstances.add(a.toObject());
		}

		return o;
	}

	def toJson() {
		return JSON.stringify(toObject());
	}
}
