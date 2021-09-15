// Copyright (c) 2021 MintJams Inc. Licensed under MIT License.

package api.bpm;

import api.util.ISO8601;
import api.util.JSON;
import jp.co.mintjams.osgi.service.jcr.script.ScriptingContext;

class Task {
	def context;
	def task;
	def processInstance;
	def processDefinition;

	Task(context) {
		this.context = context;
	}

	static def create(ScriptingContext context) {
		return new Task(context);
	}

	def with(org.camunda.bpm.engine.task.Task task) {
		this.task = task;
		return this;
	}

	def findByIdentifier(id) {
	    def ProcessAPI = context.getAttribute("ProcessAPI");
	    try {
    		return with(ProcessAPI.engine.taskService.createTaskQuery()
    			.taskId(id)
    			.initializeFormKeys()
    			.singleResult());
	    } catch (Throwable ignore) {}
	    return this;
	}

    def exists() {
        return (task != null);
    }

    def getIdentifier() {
        return task.id;
    }

	def getAssignee() {
		return task.assignee;
	}
	def setAssignee(assignee) {
	    task.setAssignee(assignee);
		return this;
	}

	def getDueDate() {
		return task.dueDate;
	}
	def setDueDate(dueDate) {
	    task.setDueDate(dueDate);
		return this;
	}

	def getFollowUpDate() {
		return task.followUpDate;
	}
	def setFollowUpDate(followUpDate) {
	    task.setFollowUpDate(followUpDate);
		return this;
	}

    def getVariable(name) {
	    def ProcessAPI = context.getAttribute("ProcessAPI");
		return ProcessAPI.engine.taskService.getVariable(task.id, name);
    }
    def setVariable(name, value) {
	    def ProcessAPI = context.getAttribute("ProcessAPI");
		ProcessAPI.engine.taskService.setVariable(task.id, name, value);
		return this;
    }

    def getVariables() {
	    def ProcessAPI = context.getAttribute("ProcessAPI");
		return ProcessAPI.engine.taskService.getVariables(task.id);
    }

    def removeVariable(name) {
	    def ProcessAPI = context.getAttribute("ProcessAPI");
		return ProcessAPI.engine.taskService.removeVariable(task.id, name);
    }
    def removeVariables(names) {
	    def ProcessAPI = context.getAttribute("ProcessAPI");
		return ProcessAPI.engine.taskService.removeVariables(task.id, names);
    }

    def getVariableLocal(name) {
	    def ProcessAPI = context.getAttribute("ProcessAPI");
		return ProcessAPI.engine.taskService.getVariableLocal(task.id, name);
    }
    def setVariableLocal(name, value) {
	    def ProcessAPI = context.getAttribute("ProcessAPI");
		ProcessAPI.engine.taskService.setVariableLocal(task.id, name, value);
		return this;
    }

    def getVariablesLocal() {
	    def ProcessAPI = context.getAttribute("ProcessAPI");
		return ProcessAPI.engine.taskService.getVariablesLocal(task.id);
    }

    def removeVariableLocal(name) {
	    def ProcessAPI = context.getAttribute("ProcessAPI");
		return ProcessAPI.engine.taskService.removeVariableLocal(task.id, name);
    }
    def removeVariablesLocal(names) {
	    def ProcessAPI = context.getAttribute("ProcessAPI");
		return ProcessAPI.engine.taskService.removeVariablesLocal(task.id, names);
    }

	def complete(variables = [:]) {
	    def ProcessAPI = context.getAttribute("ProcessAPI");
		ProcessAPI.engine.taskService.complete(task.id, variables);
		return this;
	}

	def getProcessInstance() {
	    if (!processInstance) {
    	    processInstance = ProcessInstance.create(context).findByIdentifier(task.processInstanceId);
	    }
		return processInstance;
	}

	def getProcessDefinition() {
	    if (!processDefinition) {
    	    processDefinition = ProcessDefinition.create(context).findByIdentifier(task.processDefinitionId);
	    }
		return processDefinition;
	}

	def toObject() {
	    def ProcessAPI = context.getAttribute("ProcessAPI");
		def taskService = ProcessAPI.engine.taskService;
		def o = [
			"assignee": getAssignee(),
			"caseDefinitionId": task.caseDefinitionId,
			"caseExecutionId": task.caseExecutionId,
			"caseInstanceId": task.caseInstanceId,
			"creationTime": ISO8601.formatDate(task.createTime),
			"description": task.description,
			"executionId": task.executionId,
			"formKey": task.formKey,
			"id": getIdentifier(),
			"name": task.name,
			"owner": task.owner,
			"parentTaskId": task.parentTaskId,
			"priority": task.priority,
			"taskDefinitionKey": task.taskDefinitionKey,
			"tenantId": task.tenantId,
			"isSuspended": task.isSuspended(),
			"variables": taskService.getVariables(task.id),
			"processInstance": getProcessInstance().toObject(),
			"processDefinition": getProcessDefinition().toObject(),
			"dueDate": null,
			"followUpDate": null
		];
		o.candidateUsers = [];
		o.candidateGroups = [];
		taskService.getIdentityLinksForTask(task.id).each { il ->
			if (il.type == "candidate") {
				if (il.userId) {
					o.candidateUsers.add(il.userId);
				}
				if (il.groupId) {
					o.candidateGroups.add(il.groupId);
				}
			}
		}
		if (task.dueDate) {
			o.dueDate = ISO8601.formatDate(task.dueDate);
		}
		if (task.followUpDate) {
			o.followUpDate = ISO8601.formatDate(task.followUpDate);
		}

		return o;
	}

	def toJson() {
		return JSON.stringify(toObject());
	}
}
