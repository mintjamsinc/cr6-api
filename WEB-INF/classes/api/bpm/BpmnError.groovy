// Copyright (c) 2021 MintJams Inc. Licensed under MIT License.

package api.bpm;

class BpmnError {
	static org.camunda.bpm.engine.delegate.BpmnError create(errorCode) {
		return new org.camunda.bpm.engine.delegate.BpmnError(errorCode);
	}

	static org.camunda.bpm.engine.delegate.BpmnError create(errorCode, errorMessage) {
		return new org.camunda.bpm.engine.delegate.BpmnError(errorCode, errorMessage);
	}
}
