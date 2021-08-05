// Copyright (c) 2021 MintJams Inc. Licensed under MIT License.

package api.bpm;

import api.util.ISO8601;
import api.util.JSON;
import jp.co.mintjams.osgi.service.jcr.script.ScriptingContext;

class Deployment {
	def context;
	def deployment;

	Deployment(context) {
		this.context = context;
	}

	static def create(ScriptingContext context) {
		return new Deployment(context);
	}

	def with(org.camunda.bpm.engine.repository.Deployment deployment) {
		this.deployment = deployment;
		return this;
	}

	def findByIdentifier(id) {
	    def ProcessAPI = context.getAttribute("ProcessAPI");
	    try {
    		return with(ProcessAPI.engine.repositoryService.createDeploymentQuery()
    			.deploymentId(id)
    			.singleResult());
	    } catch (Throwable ignore) {}
	    return this;
	}

    def exists() {
        return (deployment != null);
    }

    def getIdentifier() {
        return deployment.id;
    }

    def getName() {
        return deployment.name;
    }

    def getSource() {
        return deployment.source;
    }

    def getDeploymentTime() {
        return deployment.deploymentTime;
    }

	def toObject() {
	    if (!exists()) {
    		return [
    			"exists": false
    		];
	    }

		def o = [
			"deploymentTime": ISO8601.formatDate(getDeploymentTime()),
			"id": getIdentifier(),
			"name": getName(),
			"source": getSource(),
			"tenantId": deployment.tenantId,
			"exists": true
		];

		return o;
	}

	def toJson() {
		return JSON.stringify(toObject());
	}
}
