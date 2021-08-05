// Copyright (c) 2021 MintJams Inc. Licensed under MIT License.

package api.bpm;

import api.util.ISO8601;
import api.util.JSON;
import jp.co.mintjams.osgi.service.jcr.script.ScriptingContext;

class ProcessInstance {
	def context;
	def processInstance;
	def processDefinition;

	ProcessInstance(context) {
		this.context = context;
	}

	static def create(ScriptingContext context) {
		return new ProcessInstance(context);
	}

	def with(org.camunda.bpm.engine.runtime.ProcessInstance processInstance) {
		this.processInstance = processInstance;
		return this;
	}

	def findByIdentifier(id) {
	    def ProcessAPI = context.getAttribute("ProcessAPI");
	    try {
    		return with(ProcessAPI.engine.runtimeService.createProcessInstanceQuery()
    			.processInstanceId(id)
    			.singleResult());
	    } catch (Throwable ignore) {}
	    return this;
	}

	def findByBusinessKey(businessKey) {
	    def ProcessAPI = context.getAttribute("ProcessAPI");
	    try {
    		return with(ProcessAPI.engine.runtimeService.createProcessInstanceQuery()
    			.processInstanceBusinessKey(businessKey)
    			.listPage(0, 1).get(0));
	    } catch (Throwable ignore) {}
	}

    def exists() {
        return (processInstance != null);
    }

    def getIdentifier() {
        return processInstance.id;
    }

    def isEnded() {
        return processInstance.isEnded();
    }

    def getBusinessKey() {
        return processInstance.businessKey;
    }

    def getCaseInstanceId() {
        return processInstance.caseInstanceId;
    }

    def getProcessDefinitionId() {
        return processInstance.processDefinitionId;
    }

    def getRootProcessInstanceId() {
        return processInstance.rootProcessInstanceId;
    }

    def isSuspended() {
        return processInstance.isSuspended();
    }

    def getExecution() {
		return Execution.create(context).findByProcessInstanceId(processInstance.id);
    }
    def listExecutions() {
	    def ProcessAPI = context.getAttribute("ProcessAPI");
	    def l = [];
        ProcessAPI.engine.runtimeService
            .createExecutionQuery()
			.processInstanceId(processInstanceId)
			.list().each { execution ->
			    l.add(Execution.create(context).with(execution));
			};
		return l;
    }

    def getVariable(name) {
		return getExecution().getVariable(name);
    }
    def setVariable(name, value) {
		getExecution().setVariable(name, value);
		return this;
    }

    def getVariables() {
		return getExecution().getVariables();
    }

    def removeVariable(name) {
		return getExecution().removeVariable(name);
    }
    def removeVariables(names) {
		return getExecution().removeVariables(names);
    }

	def activate() {
	    def ProcessAPI = context.getAttribute("ProcessAPI");
		ProcessAPI.engine.runtimeService.activateProcessInstanceById(processInstance.id);
		return with(ProcessAPI.engine.runtimeService.createProcessInstanceQuery()
				.processInstanceId(processInstance.id)
				.singleResult());
	}

	def suspend() {
	    def ProcessAPI = context.getAttribute("ProcessAPI");
		ProcessAPI.engine.runtimeService.suspendProcessInstanceById(processInstance.id);
		return with(ProcessAPI.engine.runtimeService.createProcessInstanceQuery()
				.processInstanceId(processInstance.id)
				.singleResult());
	}

	def remove() {
	    def ProcessAPI = context.getAttribute("ProcessAPI");
		ProcessAPI.engine.runtimeService.deleteProcessInstance(processInstance.id, null);
		return this;
	}

	def getProcessDefinition() {
	    if (!processDefinition) {
    	    processDefinition = ProcessDefinition.create(context).findByIdentifier(processInstance.processDefinitionId);
	    }
		return processDefinition;
	}

	def getActivityInstance() {
	    def ProcessAPI = context.getAttribute("ProcessAPI");
		return ActivityInstance.create(context).with(ProcessAPI.engine.runtimeService.getActivityInstance(processInstance.id));
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
			"isEnded": isEnded(),
			"businessKey": getBusinessKey(),
			"caseInstanceId": getCaseInstanceId(),
			"rootProcessInstanceId": getRootProcessInstanceId(),
			"tenantId": processInstance.tenantId,
			"isSuspended": isSuspended(),
			"processDefinition": getProcessDefinition().toObject(),
			"exists": true
		];

		o.variables = ProcessInstanceHelper.create(context).with(this).exportVariables();

		return o;
	}

	def toJson() {
		return JSON.stringify(toObject());
	}
}
