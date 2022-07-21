// Copyright (c) 2021 MintJams Inc. Licensed under MIT License.

package api.bpm;

import api.util.ISO8601;
import api.util.JSON;
import org.mintjams.script.ScriptingContext;

class ProcessInstanceHelper {
	def context;
	def processInstance;

	ProcessInstanceHelper(context) {
		this.context = context;
	}

	static def create(ScriptingContext context) {
		return new ProcessInstanceHelper(context);
	}

	def with(ProcessInstance processInstance) {
		this.processInstance = processInstance;
		return this;
	}

	def detectType(value) {
		def type = value.class.getSimpleName();
		if (type == "ArrayList") {
			if (value.size() > 0) {
				return detectType(value.get(0)) + "[]";
			}
		}
		return type;
	}

	def isArray(value) {
		def type = value.class.getSimpleName();
		if (type == "ArrayList") {
			return true;
		}
		if (type.endsWith("[]")) {
			return true;
		}
		return false;
	}

	def exportVariables() {
		def m = [:];
		def variables = processInstance.variables;
		for (name in variables.keySet()) {
			def v = [
				"name": name
			];
			def value = variables[name];
			v.value = value;
			if (value == null) {
				v.type = null;
			} else {
				v.type = detectType(value);
			}
			if (v.type == null) {
				v.value = null;
			} else if (v.type == "String") {
				v.value = value as String;
			} else if (v.type == "String[]") {
				v.type = "String";
				v.value = value as String[];
			} else if (v.type == "BigDecimal") {
				v.value = value as BigDecimal;
			} else if (v.type == "BigDecimal[]") {
				v.type = "BigDecimal";
				v.value = value as BigDecimal[];
			} else if (v.type == "Double") {
				v.value = value as Double;
			} else if (v.type == "Double[]") {
				v.type = "Double";
				v.value = value as Double[];
			} else if (v.type == "Long") {
				v.value = value as Long;
			} else if (v.type == "Long[]") {
				v.type = "Long";
				v.value = value as Long[];
			} else if (v.type == "Integer") {
				v.value = value as Integer;
			} else if (v.type == "Integer[]") {
				v.type = "Integer";
				v.value = value as Integer[];
			} else if (v.type == "Date") {
				v.value = ISO8601.parse(value);
			} else if (v.type == "Date[]") {
				v.type = "Date";
				v.value = value as String[];
				for (def i = 0; i < v.value.length; i++) {
					v.value[i] = ISO8601.parse(v.value[i]);
				}
			} else if (v.type == "Calendar") {
				v.value = ISO8601.parse(value).toCalendar();
			} else if (v.type == "Calendar[]") {
				v.type = "Calendar";
				v.value = value as String[];
				for (def i = 0; i < v.value.length; i++) {
					v.value[i] = ISO8601.parse(v.value[i]).toCalendar();
				}
			} else if (v.type == "Boolean") {
				v.value = value as Boolean;
			} else if (v.type == "Boolean[]") {
				v.type = "Boolean";
				v.value = value as Boolean[];
			}
			m[name] = v;
		}
		return m;
	}

	def importVariables(variables) {
		if (variables instanceof Map) {
			variables = variables.collect { e ->
				return e.value;
			}
		}

		for (v in variables) {
			if (v.isRemoved) {
				processInstance.removeVariable(v.name);
				continue;
			}

			def value = v.value;
			def type = v.type;
			if (type == null) {
				type = detectType(value);
			}
			if (type == null) {
				throw new java.lang.IllegalArgumentException(v.name);
			} else if (type == "String") {
				if (!isArray(value)) {
					value = value as String;
				} else {
					value = value as String[];
				}
			} else if (type == "BigDecimal") {
				if (!isArray(value)) {
					value = value as BigDecimal;
				} else {
					value = value as BigDecimal[];
				}
			} else if (type == "Double") {
				if (!isArray(value)) {
					value = value as Double;
				} else {
					value = value as Double[];
				}
			} else if (type == "Long") {
				if (!isArray(value)) {
					value = value as Long;
				} else {
					value = value as Long[];
				}
			} else if (type == "Integer") {
				if (!isArray(value)) {
					value = value as Integer;
				} else {
					value = value as Integer[];
				}
			} else if (type == "Date") {
				if (!isArray(value)) {
					value = ISO8601.parse(value);
				} else {
					value = value as String[];
					for (def i = 0; i < value.length; i++) {
						value[i] = ISO8601.parse(value[i]);
					}
				}
			} else if (type == "Calendar") {
				if (!isArray(value)) {
					value = ISO8601.parse(value).toCalendar();
				} else {
					value = value as String[];
					for (def i = 0; i < value.length; i++) {
						value[i] = ISO8601.parse(value[i]).toCalendar();
					}
				}
			} else if (type == "Boolean") {
				if (!isArray(value)) {
					value = value as Boolean;
				} else {
					value = value as Boolean[];
				}
			}
			processInstance.setVariable(v.name, value);
		}
	}
}
