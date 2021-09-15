// Copyright (c) 2021 MintJams Inc. Licensed under MIT License.

package api.bpm;

import api.util.JSON;
import jp.co.mintjams.osgi.service.jcr.script.ScriptingContext;

class ProcessDefinition {
	def context;
	def processDefinition;
	def deployment;

	ProcessDefinition(context) {
		this.context = context;
	}

	static def create(ScriptingContext context) {
		return new ProcessDefinition(context);
	}

	def with(org.camunda.bpm.engine.repository.ProcessDefinition processDefinition) {
		this.processDefinition = processDefinition;
		return this;
	}

	def findByIdentifier(id) {
	    def ProcessAPI = context.getAttribute("ProcessAPI");
	    try {
    		return with(ProcessAPI.engine.repositoryService.getProcessDefinition(id));
	    } catch (Throwable ignore) {}
	    return this;
	}

	def findByKey(key) {
	    def ProcessAPI = context.getAttribute("ProcessAPI");
	    try {
    		return with(ProcessAPI.engine.repositoryService.createProcessDefinitionQuery()
    			.processDefinitionKey(key)
    			.latestVersion()
    			.singleResult());
	    } catch (Throwable ignore) {}
	    return this;
	}

    def exists() {
        return (processDefinition != null);
    }

    def getIdentifier() {
        return processDefinition.id;
    }

    def getKey() {
        return processDefinition.key;
    }

    def getVersion() {
        return processDefinition.version;
    }

    def getName() {
        return processDefinition.name;
    }

    def getResourceName() {
        return processDefinition.resourceName;
    }

    def getDiagramResourceName() {
        return processDefinition.diagramResourceName;
    }

    def getDescription() {
        return processDefinition.description;
    }

    def getVersionTag() {
        return processDefinition.versionTag;
    }

    def hasStartFormKey() {
        return processDefinition.hasStartFormKey();
    }

    def getStartFormKey() {
        if (!hasStartFormKey()) {
            return null;
        }
	    def ProcessAPI = context.getAttribute("ProcessAPI");
        return ProcessAPI.engine.formService.getStartFormKey(processDefinition.id);
    }

    def isStartableInTasklist() {
        return processDefinition.isStartableInTasklist();
    }

    def isSuspended() {
        return processDefinition.isSuspended();
    }

	def getDeployment() {
	    if (!deployment) {
    	    deployment = Deployment.create(context).findByIdentifier(processDefinition.deploymentId);
	    }
		return deployment;
	}

	def start(businessKey, variables = [:]) {
	    def ProcessAPI = context.getAttribute("ProcessAPI");
		def pi = ProcessAPI.engine.runtimeService.startProcessInstanceById(
    			processDefinition.id,
    			businessKey ? businessKey : null,
    			variables);
		return ProcessInstance.create(context).with(pi);
	}

	def activate() {
	    def ProcessAPI = context.getAttribute("ProcessAPI");
		ProcessAPI.engine.repositoryService.activateProcessDefinitionById(processDefinition.id);
		return with(ProcessAPI.engine.repositoryService.getProcessDefinition(processDefinition.id));
	}

	def suspend() {
	    def ProcessAPI = context.getAttribute("ProcessAPI");
		ProcessAPI.engine.repositoryService.suspendProcessDefinitionById(processDefinition.id);
		return with(ProcessAPI.engine.repositoryService.getProcessDefinition(processDefinition.id));
	}

    def countProcessInstances() {
	    def ProcessAPI = context.getAttribute("ProcessAPI");
		return ProcessAPI.engine.runtimeService.createProcessInstanceQuery()
			.processDefinitionId(processDefinition.id)
			.count();
    }

	def remove() {
	    def ProcessAPI = context.getAttribute("ProcessAPI");
		ProcessAPI.engine.repositoryService.deleteProcessDefinition(processDefinition.id);
		return this;
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
			"key": getKey(),
			"version": getVersion(),
			"name": getName(),
			"resourceName": getResourceName(),
			"diagramResourceName": getDiagramResourceName(),
			"description": getDescription(),
			"versionTag": getVersionTag(),
			"hasStartFormKey": hasStartFormKey(),
			"isStartableInTasklist": isStartableInTasklist(),
			"isSuspended": isSuspended(),
			"deployment": getDeployment().toObject(),
			"exists": true
		];
		if (hasStartFormKey()) {
			o.startFormKey = getStartFormKey();
		}

		return o;
	}

	def toJson() {
		return JSON.stringify(toObject());
	}
}
