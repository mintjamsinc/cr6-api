// Copyright (c) 2021 MintJams Inc. Licensed under MIT License.

package api.cms;

class ReferencedItemNotFoundException extends java.lang.RuntimeException {
	def identifier;
	def path;

	ReferencedItemNotFoundException(message) {
	    super(message);
	}
}
