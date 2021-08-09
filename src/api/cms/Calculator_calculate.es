var parameters = JSON.parse(parametersJson);
var item = parameters.item;
var facetDefinitions = parameters.facetDefinitions;

var Objects = {
	clone(o) {
		if (o == undefined) {
			return undefined;
		}
		return JSON.parse(JSON.stringify(o));
	}
};

var Dates = {
	toDate(text) {
		if (text == undefined) {
			return undefined;
		}
		if (text instanceof Date) {
			return text;
		}
		if ((text instanceof Number) || (typeof text == 'number')) {
			return new Date(text);
		}

		if (text.indexOf('T') != -1) {
			// datetime
			var n = text.split(/[^0-9]/);
			if (n.length < 5) {
				throw 'Invalid Date: ' + text;
			}

			while (n.length < 7) {
				n.push('0');
			}
			for (var i = 0; i < n.length; i++) {
				n[i] = Number(n[i]);
			}
			if (n[1] == 0) {
				n[1] = 1;
			}
			if (n[2] == 0) {
				n[2] = 1;
			}
			var d = new Date(n[0], n[1] - 1, n[2], n[3], n[4], n[5], n[6]);
			if (text.endsWith('Z')) {
				d = new Date(d.getTime() - (d.getTimezoneOffset() * 60000));
			}
			return d;
		} else if (text.indexOf(':') != -1) {
			// time
			var n = text.split(/[^0-9]/);
			if (n.length < 2) {
				throw 'Invalid Date: ' + text;
			}

			while (n.length < 4) {
				n.push('0');
			}
			var d = new Date(1970, 0, 1, n[0], n[1], n[2], n[3]);
			if (text.endsWith('Z')) {
				d = new Date(d.getTime() - (d.getTimezoneOffset() * 60000));
			}
			return d;
		} else {
			// date
			var n = text.split(/[^0-9]/);
			if (n.length < 3) {
				throw 'Invalid Date: ' + text;
			}

			while (n.length < 7) {
				n.push('0');
			}
			for (var i = 0; i < n.length; i++) {
				n[i] = Number(n[i]);
			}
			if (n[1] == 0) {
				n[1] = 1;
			}
			if (n[2] == 0) {
				n[2] = 1;
			}
			var d = new Date(n[0], n[1] - 1, n[2], n[3], n[4], n[5], n[6]);
			if (text.endsWith('Z')) {
				d = new Date(d.getTime() - (d.getTimezoneOffset() * 60000));
			}
			return d;
		}
	}
};

var Facets = {
	toString(v) {
		if (Array.isArray(v)) {
			v.forEach(function(currentValue, index, array) {
				array[index] = Facets.toString(currentValue);
			});
			return v;
		}

		if (v == undefined) {
			return '';
		}
		if (typeof v == 'string') {
			return v;
		}
		return '' + v;
	},
	toBoolean(v) {
		if (Array.isArray(v)) {
			v.forEach(function(currentValue, index, array) {
				array[index] = Facets.toBoolean(currentValue);
			});
			return v;
		}

		if (v == undefined) {
			return false;
		}
		if (typeof v == 'boolean') {
			return v;
		}
		return (('' + v) == 'true');
	},
	toNumber(v) {
		if (Array.isArray(v)) {
			v.forEach(function(currentValue, index, array) {
				array[index] = Facets.toNumber(currentValue);
			});
			return v;
		}

		if (v == undefined) {
			return 0;
		}
		if (typeof v == 'number') {
			return v;
		}
		if (typeof v == 'boolean') {
			return (v) ? 1 : 0;
		}
		return new Number(v);
	},
	toDate(v) {
		if (Array.isArray(v)) {
			v.forEach(function(currentValue, index, array) {
				array[index] = Facets.toDate(currentValue);
			});
			return v;
		}

		if (v == undefined) {
			return undefined;
		}
		if (typeof v == 'object' && v.constructor.name == 'Date') {
			return v;
		}
		try {
			return Dates.toDate(v);
		} catch (e) {
			// ignore
		}
		return undefined;
	},
	defaultString(v, defaultValue) {
		if (defaultValue == undefined) {
			defaultValue = '';
		}

		if (v == undefined) {
			return defaultValue;
		}

		return v;
	}
};

(() => {
	var op = {
		getGlobals() {
			var vm = this;
			var env = {
				getProperty(key) {
					return item.properties[key];
				},
				getFacetDefinition(key) {
					return facetDefinitions[key];
				}
			};

			var calculated = {};
			var calculatedIndex = [];

			var globals = {
				get(key) {
					var p = env.getProperty(key);
					if (p == undefined || p.value == undefined) {
						return undefined;
					}
					return p.value;
				},
				getArray(key) {
					return globals.getStringArray(key);
				},
				getString(key) {
					var value;
					if (globals.isCalculated(key)) {
						value = globals.getCalculatedValue(key);
					} else {
						var p = env.getProperty(key);
						if (p == undefined || p.value == undefined) {
							return undefined;
						}
						value = p.value;
					}

					if (Array.isArray(value)) {
						if (value.length == 0) {
							return undefined;
						} else {
							return globals.toString(value[0]);
						}
					}
					return globals.toString(value);
				},
				getStringArray(key) {
					var value;
					if (globals.isCalculated(key)) {
						value = globals.getCalculatedValue(key);
					} else {
						var p = env.getProperty(key);
						if (p == undefined || p.value == undefined) {
							return [];
						}
						value = p.value;
					}

					if (Array.isArray(value)) {
						if (value.length == 0) {
							return [];
						} else {
							var values = [];
							for (var i in value) {
							    var v = value[i];
								values.push(globals.toString(v));
							}
							return values;
						}
					}
					return [globals.toString(value)];
				},
				getNumber(key) {
					var value;
					if (globals.isCalculated(key)) {
						value = globals.getCalculatedValue(key);
					} else {
						var p = env.getProperty(key);
						if (p == undefined || p.value == undefined) {
							return undefined;
						}
						value = p.value;
					}

					if (Array.isArray(value)) {
						if (value.length == 0) {
							return undefined;
						} else {
							return globals.toNumber(value[0]);
						}
					}
					return globals.toNumber(value);
				},
				getNumberArray(key) {
					var value;
					if (globals.isCalculated(key)) {
						value = globals.getCalculatedValue(key);
					} else {
						var p = env.getProperty(key);
						if (p == undefined || p.value == undefined) {
							return [];
						}
						value = p.value;
					}

					if (Array.isArray(value)) {
						if (value.length == 0) {
							return [];
						} else {
							var values = [];
							for (var i in value) {
							    var v = value[i];
								values.push(globals.toNumber(v));
							}
							return values;
						}
					}
					return [globals.toNumber(value)];
				},
				getBoolean(key) {
					var value;
					if (globals.isCalculated(key)) {
						value = globals.getCalculatedValue(key);
					} else {
						var p = env.getProperty(key);
						if (p == undefined || p.value == undefined) {
							return undefined;
						}
						value = p.value;
					}

					if (Array.isArray(value)) {
						if (value.length == 0) {
							return undefined;
						} else {
							return globals.toBoolean(value[0]);
						}
					}
					return globals.toBoolean(value);
				},
				getBooleanArray(key) {
					var value;
					if (globals.isCalculated(key)) {
						value = globals.getCalculatedValue(key);
					} else {
						var p = env.getProperty(key);
						if (p == undefined || p.value == undefined) {
							return [];
						}
						value = p.value;
					}

					if (Array.isArray(value)) {
						if (value.length == 0) {
							return [];
						} else {
							var values = [];
							for (var i in value) {
							    var v = value[i];
								values.push(globals.toBoolean(v));
							}
							return values;
						}
					}
					return [globals.toBoolean(value)];
				},
				getDate(key) {
					var value;
					if (globals.isCalculated(key)) {
						value = globals.getCalculatedValue(key);
					} else {
						var p = env.getProperty(key);
						if (p == undefined || p.value == undefined) {
							return undefined;
						}
						value = p.value;
					}

					if (Array.isArray(value)) {
						if (value.length == 0) {
							return undefined;
						} else {
							return globals.toDate(value[0]);
						}
					}
					return globals.toDate(value);
				},
				getDateArray(key) {
					var value;
					if (globals.isCalculated(key)) {
						value = globals.getCalculatedValue(key);
					} else {
						var p = env.getProperty(key);
						if (p == undefined || p.value == undefined) {
							return [];
						}
						value = p.value;
					}

					if (Array.isArray(value)) {
						if (value.length == 0) {
							return [];
						} else {
							var values = [];
							for (var i in value) {
							    var v = value[i];
								values.push(globals.toDate(v));
							}
							return values;
						}
					}
					return [globals.toDate(value)];
				},
				toString(v) {
					return Facets.toString(v);
				},
				toNumber(v) {
					return Facets.toNumber(v);
				},
				toBoolean(v) {
					return Facets.toBoolean(v);
				},
				toDate(v) {
					return Facets.toDate(v);
				},
				isCalculated(key) {
					var facetDefinition = env.getFacetDefinition(key);
					return (facetDefinition && facetDefinition.type == 'calculated')
				},
				getCalculatedValue(key) {
					if (calculatedIndex.indexOf(key) != -1) {
						return calculated[key];
					}
					if (globals.references.indexOf(key) != -1) {
						throw "Circular References: " + key;
					}
					globals.references.push(key);

					var facetDefinition = env.getFacetDefinition(key);
					if (!facetDefinition) {
						return undefined;
					}

					var argNames = [];
					var args = [];
					for (var key in globals) {
						argNames.push(key);
						args.push(globals[key]);
					}
					argNames.push('facetDefinition');
					args.push(Objects.clone(facetDefinition));
					var f = Function('"use strict"; return function(' + argNames.join(',') + '){' + facetDefinition.formula + '};');
					var result = f.call({}).apply({}, args);
					calculated[key] = result;
					return result;
				},
				references: []
			};

			return globals;
		},
		evaluate(formula) {
			var vm = this;
			try {
				var globals = vm.getGlobals();
				var argNames = [];
				var args = [];
				for (var key in globals) {
					argNames.push(key);
					args.push(globals[key]);
				}
				argNames.push('facetDefinition');
				args.push(Objects.clone(vm.facetDefinition));
				globals.references = [vm.propertyKey];
				var f = Function('"use strict"; return function(' + argNames.join(',') + '){' + formula + '};');
				var result = f.call({}).apply({}, args);
				return result;
			} catch (ex) {
				vm.errorText = ex;
				log.error('An error occurred while evaluating the formula:' + ex);
			}
			return undefined;
		},
		normalizedValue() {
			var vm = this;
			var property = item.properties[vm.propertyKey];

			if (vm.facetDefinition.type == 'calculated') {
				return (function() {
					return vm.evaluate(vm.facetDefinition.formula);
				})();
			}

			if (vm.facetDefinition.type == 'hyperlink' || vm.facetDefinition.type == 'picture') {
				return (function() {
					var v = property.value;
					if (v == undefined) {
						return undefined;
					}
					if (Array.isArray(v) && v.length > 0) {
						v = v[0];
					}
					if (typeof v == 'string') {
						try {
							v = JSON.parse(v);
						} catch (e) {
							// ignore
						}
						if (typeof v == 'string') {
							v = {
								'url': v
							};
						}
					}
					v.url = Facets.defaultString(v.url);
					v.text = Facets.defaultString(v.text);
					return v;
				})();
			}

			if (vm.facetDefinition.type == 'query') {
				return (function() {
					var v = property.value;
					if (v == undefined) {
						return undefined;
					}
					if (typeof property.value != 'object') {
						return undefined;
					}
					var prop = property.value.properties;
					var result = {
						'item': property.value
					};
					var value = (function() {
						if (vm.facetDefinition.keyForValue) {
							var p = prop[vm.facetDefinition.keyForValue];
							if (p) {
								return Facets.defaultString(p.value);
							}
						}
						return result.item.path;
					})();
					var text = (function() {
						if (vm.facetDefinition.keyForText) {
							var p = prop[vm.facetDefinition.keyForText];
							if (p) {
								return Facets.defaultString(p.value);
							}
						}
						return result.item.name;
					})();
					if (value) {
						result.value = value;
					}
					if (text) {
						result.text = text;
					}
					return result;
				})();
			}

			return (function() {
				if (typeof property.value == 'object') {
					return Objects.clone(property.value);
				}
				return property.value;
			})();
		},
		onStore() {
			var vm = this;
			var prop = {
				'key': vm.propertyKey,
				'isMultiple': false,
				'isMixed': false
			};
			if (['singlelinetext', 'multiplelinestext', 'hyperlink', 'picture'].indexOf(vm.facetDefinition.valueType) != -1) {
				prop.type = 'String';
			}
			if (['number', 'currency'].indexOf(vm.facetDefinition.valueType) != -1) {
				prop.type = 'Decimal';
			}
			if (['datetime'].indexOf(vm.facetDefinition.valueType) != -1) {
				prop.type = 'Date';
			}
			if (['boolean'].indexOf(vm.facetDefinition.valueType) != -1) {
				prop.type = 'Boolean';
			}

			if (['hyperlink', 'picture'].indexOf(vm.facetDefinition.valueType) != -1) {
				prop.value = vm.normalizedValue();
				if (typeof prop.value == 'object') {
					prop.value = JSON.stringify({
						'url': Facets.defaultString(prop.value.url),
						'text': Facets.defaultString(prop.value.text)
					});
				}
			} else {
				prop.value = vm.normalizedValue();
			}
			return prop;
		},
		displayText() {
			var vm = this;

			var value;
			try {
				value = vm.normalizedValue();
			} catch (ex) {
				vm.errorText = ex;
				log.error('An error occurred while formatting the value:' + ex);
				return undefined;
			}

			var result = value;
			if (vm.facetDefinition.displayFormat) {
				try {
					var globals = vm.getGlobals();
					var argNames = [];
					var args = [];
					for (var key in globals) {
						argNames.push(key);
						args.push(globals[key]);
					}
					argNames.push('value');
					args.push(value);
					argNames.push('facetDefinition');
					args.push(Objects.clone(vm.facetDefinition));
					globals.references = [vm.propertyKey];
					var fn = Function('"use strict"; return function(' + argNames.join(',') + '){' + vm.facetDefinition.displayFormat + '};');
					result = fn.call({}).apply({}, args);
				} catch (ex) {
					vm.errorText = ex;
					return undefined;
				}
			} else {
				var fn = vm.defaultDisplayFormatFunction();
				if (typeof fn == 'function') {
					try {
						result = fn.call({}, value, Objects.clone(vm.facetDefinition));
					} catch (ex) {
						vm.errorText = ex;
						log.error('An error occurred while formatting the value:' + ex);
						return undefined;
					}
				}
			}

			if (result == undefined) {
				return undefined;
			}
			if (vm.facetDefinition.type == 'number' || vm.facetDefinition.type == 'currency') {
				if (isNaN(result)) {
					return undefined;
				}
			}
			if (vm.facetDefinition.type == 'calculated') {
				if (vm.facetDefinition.valueType == 'number' || vm.facetDefinition.valueType == 'currency') {
					if (isNaN(result)) {
						return undefined;
					}
				}
			}
			return result;
		},
		defaultDisplayFormatFunction() {
			var vm = this;
			if (vm.facetDefinition.type == 'calculated') {
				return function(value) {
					if (vm.facetDefinition.valueType == 'number') {
						try {
							var options = {};
							if (vm.facetDefinition.numberFractionDigits != undefined) {
								options.minimumFractionDigits = vm.facetDefinition.numberFractionDigits;
								options.maximumFractionDigits = vm.facetDefinition.numberFractionDigits;
							}
							if (vm.facetDefinition.showAsPercentage) {
								options.style = 'percent';
							}
							var v = value.toLocaleString(undefined, options);
							return v;
						} catch (e) {
							return undefined;
						}
					}

					if (vm.facetDefinition.valueType == 'currency') {
						try {
							var options = {};
							if (vm.facetDefinition.currencyFormat) {
								var cc = vm.facetDefinition.currencyFormat.split('/');
								if (cc.length > 1) {
									options.style = 'currency';
									options.currency = cc[1];
								}
							}
							if (vm.facetDefinition.currencyFractionDigits != undefined) {
								options.minimumFractionDigits = vm.facetDefinition.currencyFractionDigits;
								options.maximumFractionDigits = vm.facetDefinition.currencyFractionDigits;
							}
							return value.toLocaleString(undefined, options);
						} catch (e) {
							return undefined;
						}
					}

					if (vm.facetDefinition.valueType == 'datetime') {
						if (value == undefined) {
							return undefined;
						}
						try {
							value = Dates.toDate(value);
							var options;
							if (vm.facetDefinition.datetimeValueType == 'datetime') {
								options = {
									weekday: undefined,
									year: 'numeric',
									month: 'medium',
									day: 'numeric',
									hour: 'numeric',
									minute: 'numeric'
								};
							}
							if (vm.facetDefinition.datetimeValueType == 'date') {
								options = {
									weekday: undefined,
									year: 'numeric',
									month: 'medium',
									day: 'numeric'
								};
							}
							if (vm.facetDefinition.datetimeValueType == 'time') {
								options = {
									hour: 'numeric',
									minute: 'numeric'
								};
							}
							return value.toLocaleString(undefined, options);
						} catch (e) {
							return undefined;
						}
					}

					if (vm.facetDefinition.valueType == 'boolean') {
						if (value == undefined) {
							return undefined;
						}
						return (value == true) ? 'Yes' : 'No';
					}

					if (vm.facetDefinition.valueType == 'hyperlink') {
						if (value == undefined) {
							return undefined;
						}
						if (!value.url) {
							return undefined;
						}

						return value;
					}

					if (vm.facetDefinition.valueType == 'picture') {
						if (value == undefined) {
							return undefined;
						}
						if (!value.url) {
							return undefined;
						}

						return value;
					}

					return value;
				};
			} else if (vm.facetDefinition.type == 'choice') {
				return function(value, facetDefinition) {
					var getLabel = function(value) {
						if (!value) {
							return '';
						}
						for (var i = 0; i < facetDefinition.values.length; i++) {
							var e = facetDefinition.values[i];
							if (e.value != value) {
								continue;
							}
							if (e.text) {
								return e.text;
							}
							return value;
						}
						return value;
					};

					if (Array.isArray(value)) {
						for (var i = 0; i < value.length; i++) {
							value[i] = getLabel(value[i]);
						}
						return value;
					} else {
						return getLabel(value);
					}
				};
			} else if (vm.facetDefinition.type == 'datetime') {
				return function(value) {
					if (value == undefined) {
						return undefined;
					}

					try {
						value = Dates.toDate(value);
						var options;
						if (vm.facetDefinition.valueType == 'datetime') {
							options = {
								weekday: undefined,
								year: 'numeric',
								month: 'medium',
								day: 'numeric',
								hour: 'numeric',
								minute: 'numeric'
							};
						}
						if (vm.facetDefinition.valueType == 'date') {
							options = {
								weekday: undefined,
								year: 'numeric',
								month: 'medium',
								day: 'numeric'
							};
						}
						if (vm.facetDefinition.valueType == 'time') {
							options = {
								hour: 'numeric',
								minute: 'numeric'
							};
						}
						return value.toLocaleString(undefined, options);
					} catch (e) {
						return undefined;
					}
				};
			} else if (vm.facetDefinition.type == 'currency') {
				return function(value) {
					try {
						var options = {};
						if (vm.facetDefinition.currencyFormat) {
							var cc = vm.facetDefinition.currencyFormat.split('/');
							if (cc.length > 1) {
								options.style = 'currency';
								options.currency = cc[1];
							}
						}
						if (vm.facetDefinition.fractionDigits != undefined) {
							options.minimumFractionDigits = vm.facetDefinition.fractionDigits;
							options.maximumFractionDigits = vm.facetDefinition.fractionDigits;
						}
						return value.toLocaleString(undefined, options);
					} catch (e) {
						return undefined;
					}
				};
			} else if (vm.facetDefinition.type == 'number') {
				return function(value) {
					try {
						var options = {};
						if (vm.facetDefinition.fractionDigits != undefined) {
							options.minimumFractionDigits = vm.facetDefinition.fractionDigits;
							options.maximumFractionDigits = vm.facetDefinition.fractionDigits;
						}
						if (vm.facetDefinition.showAsPercentage) {
							options.style = 'percent';
						}
						var v = value.toLocaleString(undefined, options);
						return v;
					} catch (e) {
						return undefined;
					}
				};
			} else if (vm.facetDefinition.type == 'hyperlink' || vm.facetDefinition.type == 'picture') {
				return function(value) {
					if (!value.url) {
						return undefined;
					}

					return value;
				};
			} else if (vm.facetDefinition.type == 'boolean') {
				return function(value) {
					if (value == undefined) {
						return undefined;
					}
					return (value == true) ? 'Yes' : 'No';
				};
			} else if (vm.facetDefinition.type == 'query') {
				return function(value) {
					if (!value.value) {
						return undefined;
					}

					return value;
				};
			}

			return undefined;
		}
	};

	var calculated = {};
	for (var key in item.properties) {
		var facetDefinition = facetDefinitions[key];
		if (!facetDefinition) {
			continue;
		}

		op.propertyKey = key;
		op.facetDefinition = facetDefinition;

		if (op.facetDefinition.type == 'calculated') {
			calculated[key] = op.onStore();
		}
	}

	return JSON.stringify(calculated);
})();