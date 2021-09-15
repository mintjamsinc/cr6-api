// Copyright (c) 2021 MintJams Inc. Licensed under MIT License.

package api.bpm;

import api.util.JSON;
import jp.co.mintjams.osgi.service.jcr.script.ScriptingContext;

class Execution {
	def context;
	def execution;

	Execution(context) {
		this.context = context;
	}

	static def create(ScriptingContext context) {
		return new Execution(context);
	}

	def with(org.camunda.bpm.engine.runtime.Execution execution) {
		this.execution = execution;
		return this;
	}

	def findByIdentifier(id) {
	    def ProcessAPI = context.getAttribute("ProcessAPI");
        def execution = ProcessAPI.engine.runtimeService
            .createExecutionQuery()
			.executionId(id)
			.listPage(0, 1).get(0);
		return with(execution);
	}

	def findByBusinessKey(businessKey) {
	    def ProcessAPI = context.getAttribute("ProcessAPI");
        def execution = ProcessAPI.engine.runtimeService
            .createExecutionQuery()
			.processInstanceBusinessKey(businessKey)
			.listPage(0, 1).get(0);
		return with(execution);
	}

	def findByProcessInstanceId(processInstanceId) {
	    def ProcessAPI = context.getAttribute("ProcessAPI");
        def execution = ProcessAPI.engine.runtimeService
            .createExecutionQuery()
			.processInstanceId(processInstanceId)
			.listPage(0, 1).get(0);
		return with(execution);
	}

    def getVariable(name) {
	    def ProcessAPI = context.getAttribute("ProcessAPI");
		return ProcessAPI.engine.runtimeService.getVariable(execution.id, name);
    }
    def setVariable(name, value) {
	    def ProcessAPI = context.getAttribute("ProcessAPI");
		ProcessAPI.engine.runtimeService.setVariable(execution.id, name, value);
		return this;
    }

    def getVariables() {
	    def ProcessAPI = context.getAttribute("ProcessAPI");
		return ProcessAPI.engine.runtimeService.getVariables(execution.id);
    }

    def removeVariable(name) {
	    def ProcessAPI = context.getAttribute("ProcessAPI");
		return ProcessAPI.engine.runtimeService.removeVariable(execution.id, name);
    }
    def removeVariables(names) {
	    def ProcessAPI = context.getAttribute("ProcessAPI");
		return ProcessAPI.engine.runtimeService.removeVariables(execution.id, names);
    }

	def toObject() {
		def o = [
			"id": execution.id,
			"processInstanceId": execution.processInstanceId,
			"isEnded": execution.isEnded(),
			"tenantId": execution.tenantId,
			"isSuspended": execution.isSuspended()
		];

		o.variables = getVariables();

		return o;
	}

	def toJson() {
		return JSON.stringify(toObject());
	}
}
