// Copyright (c) 2021 MintJams Inc. Licensed under MIT License.
var $jscomp=$jscomp||{};$jscomp.scope={};$jscomp.checkStringArgs=function(a,c,d){if(null==a)throw new TypeError("The 'this' value for String.prototype."+d+" must not be null or undefined");if(c instanceof RegExp)throw new TypeError("First argument to String.prototype."+d+" must not be a regular expression");return a+""};$jscomp.ASSUME_ES5=!1;$jscomp.ASSUME_NO_NATIVE_MAP=!1;$jscomp.ASSUME_NO_NATIVE_SET=!1;
$jscomp.defineProperty=$jscomp.ASSUME_ES5||"function"==typeof Object.defineProperties?Object.defineProperty:function(a,c,d){a!=Array.prototype&&a!=Object.prototype&&(a[c]=d.value)};$jscomp.getGlobal=function(a){return"undefined"!=typeof window&&window===a?a:"undefined"!=typeof global&&null!=global?global:a};$jscomp.global=$jscomp.getGlobal(this);
$jscomp.polyfill=function(a,c,d,e){if(c){d=$jscomp.global;a=a.split(".");for(e=0;e<a.length-1;e++){var b=a[e];b in d||(d[b]={});d=d[b]}a=a[a.length-1];e=d[a];c=c(e);c!=e&&null!=c&&$jscomp.defineProperty(d,a,{configurable:!0,writable:!0,value:c})}};
$jscomp.polyfill("String.prototype.endsWith",function(a){return a?a:function(a,d){var e=$jscomp.checkStringArgs(this,a,"endsWith");a+="";void 0===d&&(d=e.length);d=Math.max(0,Math.min(d|0,e.length));for(var b=a.length;0<b&&0<d;)if(e[--d]!=a[--b])return!1;return 0>=b}},"es6","es3");$jscomp.SYMBOL_PREFIX="jscomp_symbol_";$jscomp.initSymbol=function(){$jscomp.initSymbol=function(){};$jscomp.global.Symbol||($jscomp.global.Symbol=$jscomp.Symbol)};$jscomp.symbolCounter_=0;
$jscomp.Symbol=function(a){return $jscomp.SYMBOL_PREFIX+(a||"")+$jscomp.symbolCounter_++};$jscomp.initSymbolIterator=function(){$jscomp.initSymbol();var a=$jscomp.global.Symbol.iterator;a||(a=$jscomp.global.Symbol.iterator=$jscomp.global.Symbol("iterator"));"function"!=typeof Array.prototype[a]&&$jscomp.defineProperty(Array.prototype,a,{configurable:!0,writable:!0,value:function(){return $jscomp.arrayIterator(this)}});$jscomp.initSymbolIterator=function(){}};
$jscomp.arrayIterator=function(a){var c=0;return $jscomp.iteratorPrototype(function(){return c<a.length?{done:!1,value:a[c++]}:{done:!0}})};$jscomp.iteratorPrototype=function(a){$jscomp.initSymbolIterator();a={next:a};a[$jscomp.global.Symbol.iterator]=function(){return this};return a};
$jscomp.iteratorFromArray=function(a,c){$jscomp.initSymbolIterator();a instanceof String&&(a+="");var d=0,e={next:function(){if(d<a.length){var b=d++;return{value:c(b,a[b]),done:!1}}e.next=function(){return{done:!0,value:void 0}};return e.next()}};e[Symbol.iterator]=function(){return e};return e};$jscomp.polyfill("Array.prototype.values",function(a){return a?a:function(){return $jscomp.iteratorFromArray(this,function(a,d){return d})}},"es6","es3");
var parameters=JSON.parse(parametersJson),item=parameters.item,facetDefinitions=parameters.facetDefinitions,Objects={clone:function(a){if(void 0!=a)return JSON.parse(JSON.stringify(a))}},Dates={toDate:function(a){if(void 0!=a){if(a instanceof Date)return a;if(a instanceof Number||"number"==typeof a)return new Date(a);if(-1!=a.indexOf("T")){var c=a.split(/[^0-9]/);if(5>c.length)throw"Invalid Date: "+a;for(;7>c.length;)c.push("0");for(var d=0;d<c.length;d++)c[d]=Number(c[d]);0==c[1]&&(c[1]=1);0==c[2]&&
(c[2]=1);c=new Date(c[0],c[1]-1,c[2],c[3],c[4],c[5],c[6])}else if(-1!=a.indexOf(":")){c=a.split(/[^0-9]/);if(2>c.length)throw"Invalid Date: "+a;for(;4>c.length;)c.push("0");c=new Date(1970,0,1,c[0],c[1],c[2],c[3])}else{c=a.split(/[^0-9]/);if(3>c.length)throw"Invalid Date: "+a;for(;7>c.length;)c.push("0");for(d=0;d<c.length;d++)c[d]=Number(c[d]);0==c[1]&&(c[1]=1);0==c[2]&&(c[2]=1);c=new Date(c[0],c[1]-1,c[2],c[3],c[4],c[5],c[6])}a.endsWith("Z")&&(c=new Date(c.getTime()-6E4*c.getTimezoneOffset()));
return c}}},Facets={toString:function(a){return Array.isArray(a)?(a.forEach(function(a,d,e){e[d]=Facets.toString(a)}),a):void 0==a?"":"string"==typeof a?a:""+a},toBoolean:function(a){return Array.isArray(a)?(a.forEach(function(a,d,e){e[d]=Facets.toBoolean(a)}),a):void 0==a?!1:"boolean"==typeof a?a:"true"==""+a},toNumber:function(a){return Array.isArray(a)?(a.forEach(function(a,d,e){e[d]=Facets.toNumber(a)}),a):void 0==a?0:"number"==typeof a?a:"boolean"==typeof a?a?1:0:new Number(a)},toDate:function(a){if(Array.isArray(a))return a.forEach(function(a,
d,e){e[d]=Facets.toDate(a)}),a;if(void 0!=a){if("object"==typeof a&&"Date"==a.constructor.name)return a;try{return Dates.toDate(a)}catch(c){}}},defaultString:function(a,c){void 0==c&&(c="");return void 0==a?c:a}};
(function(){var a={getGlobals:function(){var d={getProperty:function(b){return item.properties[b]},getFacetDefinition:function(b){return facetDefinitions[b]}},a={get:function(b){b=d.getProperty(b);if(void 0!=b&&void 0!=b.value)return b.value},getArray:function(b){return a.getStringArray(b)},getString:function(b){if(a.isCalculated(b))b=a.getCalculatedValue(b);else{b=d.getProperty(b);if(void 0==b||void 0==b.value)return;b=b.value}return Array.isArray(b)?0==b.length?void 0:a.toString(b[0]):a.toString(b)},
getStringArray:function(b){if(a.isCalculated(b))b=a.getCalculatedValue(b);else{b=d.getProperty(b);if(void 0==b||void 0==b.value)return[];b=b.value}if(Array.isArray(b)){if(0==b.length)return[];var e=[],c;for(c in b)e.push(a.toString(b[c]));return e}return[a.toString(b)]},getNumber:function(b){if(a.isCalculated(b))b=a.getCalculatedValue(b);else{b=d.getProperty(b);if(void 0==b||void 0==b.value)return;b=b.value}return Array.isArray(b)?0==b.length?void 0:a.toNumber(b[0]):a.toNumber(b)},getNumberArray:function(b){if(a.isCalculated(b))b=
a.getCalculatedValue(b);else{b=d.getProperty(b);if(void 0==b||void 0==b.value)return[];b=b.value}if(Array.isArray(b)){if(0==b.length)return[];var e=[],c;for(c in b)e.push(a.toNumber(b[c]));return e}return[a.toNumber(b)]},getBoolean:function(b){if(a.isCalculated(b))b=a.getCalculatedValue(b);else{b=d.getProperty(b);if(void 0==b||void 0==b.value)return;b=b.value}return Array.isArray(b)?0==b.length?void 0:a.toBoolean(b[0]):a.toBoolean(b)},getBooleanArray:function(b){if(a.isCalculated(b))b=a.getCalculatedValue(b);
else{b=d.getProperty(b);if(void 0==b||void 0==b.value)return[];b=b.value}if(Array.isArray(b)){if(0==b.length)return[];var e=[],c;for(c in b)e.push(a.toBoolean(b[c]));return e}return[a.toBoolean(b)]},getDate:function(b){if(a.isCalculated(b))b=a.getCalculatedValue(b);else{b=d.getProperty(b);if(void 0==b||void 0==b.value)return;b=b.value}return Array.isArray(b)?0==b.length?void 0:a.toDate(b[0]):a.toDate(b)},getDateArray:function(b){if(a.isCalculated(b))b=a.getCalculatedValue(b);else{b=d.getProperty(b);
if(void 0==b||void 0==b.value)return[];b=b.value}if(Array.isArray(b)){if(0==b.length)return[];var e=[],c;for(c in b)e.push(a.toDate(b[c]));return e}return[a.toDate(b)]},toString:function(b){return Facets.toString(b)},toNumber:function(b){return Facets.toNumber(b)},toBoolean:function(b){return Facets.toBoolean(b)},toDate:function(b){return Facets.toDate(b)},isCalculated:function(b){return(b=d.getFacetDefinition(b))&&"calculated"==b.type},getCalculatedValue:function(b){return d.getProperty(b).value},
references:[]};return a},evaluate:function(a){try{var d=this.getGlobals(),b=[],c=[],f;for(f in d)b.push(f),c.push(d[f]);b.push("facetDefinition");c.push(Objects.clone(this.facetDefinition));d.references=[this.propertyKey];return Function('"use strict"; return function('+b.join(",")+"){"+a+"};").call({}).apply({},c)}catch(l){this.errorText=l,log.error("An error occurred while evaluating the formula:"+l)}},normalizedValue:function(){var a=this,e=item.properties[a.propertyKey];if("calculated"==a.facetDefinition.type)return a.evaluate(a.facetDefinition.formula);
if("hyperlink"==a.facetDefinition.type||"picture"==a.facetDefinition.type)return function(){var b=e.value;if(void 0!=b){Array.isArray(b)&&0<b.length&&(b=b[0]);if("string"==typeof b){try{b=JSON.parse(b)}catch(f){}"string"==typeof b&&(b={url:b})}b.url=Facets.defaultString(b.url);b.text=Facets.defaultString(b.text);return b}}();if("query"==a.facetDefinition.type)return function(){if(void 0!=e.value&&"object"==typeof e.value){var b=e.value.properties,d={item:e.value},c=function(){if(a.facetDefinition.keyForValue){var c=
b[a.facetDefinition.keyForValue];if(c)return Facets.defaultString(c.value)}return d.item.path}(),h=function(){if(a.facetDefinition.keyForText){var c=b[a.facetDefinition.keyForText];if(c)return Facets.defaultString(c.value)}return d.item.name}();c&&(d.value=c);h&&(d.text=h);return d}}();var b="object"==typeof e.value?Objects.clone(e.value):e.value;return b},onStore:function(){var a={key:this.propertyKey,isMultiple:!1,isMixed:!1};-1!=["singlelinetext","multiplelinestext","hyperlink","picture"].indexOf(this.facetDefinition.valueType)&&
(a.type="String");-1!=["number","currency"].indexOf(this.facetDefinition.valueType)&&(a.type="Decimal");-1!=["datetime"].indexOf(this.facetDefinition.valueType)&&(a.type="Date");-1!=["boolean"].indexOf(this.facetDefinition.valueType)&&(a.type="Boolean");-1!=["hyperlink","picture"].indexOf(this.facetDefinition.valueType)?(a.value=this.normalizedValue(),"object"==typeof a.value&&(a.value=JSON.stringify({url:Facets.defaultString(a.value.url),text:Facets.defaultString(a.value.text)}))):a.value=this.normalizedValue();
return a},displayText:function(){try{var a=this.normalizedValue()}catch(g){this.errorText=g;log.error("An error occurred while formatting the value:"+g);return}var c=a;if(this.facetDefinition.displayFormat)try{var b=this.getGlobals(),k=[],f=[],l;for(l in b)k.push(l),f.push(b[l]);k.push("value");f.push(a);k.push("facetDefinition");f.push(Objects.clone(this.facetDefinition));b.references=[this.propertyKey];var h=Function('"use strict"; return function('+k.join(",")+"){"+this.facetDefinition.displayFormat+
"};");c=h.call({}).apply({},f)}catch(g){this.errorText=g;return}else if(h=this.defaultDisplayFormatFunction(),"function"==typeof h)try{c=h.call({},a,Objects.clone(this.facetDefinition))}catch(g){this.errorText=g;log.error("An error occurred while formatting the value:"+g);return}if(void 0!=c)return c},defaultDisplayFormatFunction:function(){var a=this;if("calculated"==a.facetDefinition.type)return function(c){if("number"==a.facetDefinition.valueType)try{var b={};void 0!=a.facetDefinition.numberFractionDigits&&
(b.minimumFractionDigits=a.facetDefinition.numberFractionDigits,b.maximumFractionDigits=a.facetDefinition.numberFractionDigits);a.facetDefinition.showAsPercentage&&(b.style="percent");return c.toLocaleString(void 0,b)}catch(f){return}if("currency"==a.facetDefinition.valueType)try{b={};if(a.facetDefinition.currencyFormat){var e=a.facetDefinition.currencyFormat.split("/");1<e.length&&(b.style="currency",b.currency=e[1])}void 0!=a.facetDefinition.currencyFractionDigits&&(b.minimumFractionDigits=a.facetDefinition.currencyFractionDigits,
b.maximumFractionDigits=a.facetDefinition.currencyFractionDigits);return c.toLocaleString(void 0,b)}catch(f){return}if("datetime"==a.facetDefinition.valueType){if(void 0==c)return;try{return c=Dates.toDate(c),"datetime"==a.facetDefinition.datetimeValueType&&(b={weekday:void 0,year:"numeric",month:"medium",day:"numeric",hour:"numeric",minute:"numeric"}),"date"==a.facetDefinition.datetimeValueType&&(b={weekday:void 0,year:"numeric",month:"medium",day:"numeric"}),"time"==a.facetDefinition.datetimeValueType&&
(b={hour:"numeric",minute:"numeric"}),c.toLocaleString(void 0,b)}catch(f){return}}if("boolean"==a.facetDefinition.valueType)return void 0==c?void 0:1==c?"Yes":"No";if("hyperlink"==a.facetDefinition.valueType)return void 0!=c&&c.url?c:void 0;if("picture"!=a.facetDefinition.valueType||void 0!=c&&c.url)return c};if("choice"==a.facetDefinition.type)return function(a,b){var c=function(a){if(!a)return"";for(var c=0;c<b.values.length;c++){var e=b.values[c];if(e.value==a){if(e.text)return e.text;break}}return a};
if(Array.isArray(a)){for(var e=0;e<a.length;e++)a[e]=c(a[e]);return a}return c(a)};if("datetime"==a.facetDefinition.type)return function(c){if(void 0!=c)try{c=Dates.toDate(c);var b;"datetime"==a.facetDefinition.valueType&&(b={weekday:void 0,year:"numeric",month:"medium",day:"numeric",hour:"numeric",minute:"numeric"});"date"==a.facetDefinition.valueType&&(b={weekday:void 0,year:"numeric",month:"medium",day:"numeric"});"time"==a.facetDefinition.valueType&&(b={hour:"numeric",minute:"numeric"});return c.toLocaleString(void 0,
b)}catch(k){}};if("currency"==a.facetDefinition.type)return function(c){try{var b={};if(a.facetDefinition.currencyFormat){var e=a.facetDefinition.currencyFormat.split("/");1<e.length&&(b.style="currency",b.currency=e[1])}void 0!=a.facetDefinition.fractionDigits&&(b.minimumFractionDigits=a.facetDefinition.fractionDigits,b.maximumFractionDigits=a.facetDefinition.fractionDigits);return c.toLocaleString(void 0,b)}catch(f){}};if("number"==a.facetDefinition.type)return function(c){try{var b={};void 0!=
a.facetDefinition.fractionDigits&&(b.minimumFractionDigits=a.facetDefinition.fractionDigits,b.maximumFractionDigits=a.facetDefinition.fractionDigits);a.facetDefinition.showAsPercentage&&(b.style="percent");return c.toLocaleString(void 0,b)}catch(k){}};if("hyperlink"==a.facetDefinition.type||"picture"==a.facetDefinition.type)return function(a){if(a.url)return a};if("boolean"==a.facetDefinition.type)return function(a){if(void 0!=a)return 1==a?"Yes":"No"};if("query"==a.facetDefinition.type)return function(a){if(a.value)return a}}},
c={key:parameters.key};a.propertyKey=parameters.key;a.facetDefinition=facetDefinitions[parameters.key];a.facetDefinition&&(c.value=a.displayText());return JSON.stringify(c)})();
