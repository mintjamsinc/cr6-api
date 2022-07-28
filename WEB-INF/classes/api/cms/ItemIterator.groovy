// Copyright (c) 2021 MintJams Inc. Licensed under MIT License.

package api.cms;

import api.security.Session;
import api.util.ISO8601;
import api.util.JSON;
import java.io.ByteArrayInputStream; 
import java.io.IOException;
import java.util.Locale;
import org.mintjams.script.ScriptingContext;

class ItemIterator implements java.util.Iterator<Item> {
	def context;
	def resourceIterator;

	ItemIterator(context) {
		this.context = context;
	}

	static def create(ScriptingContext context) {
		return new ItemIterator(context);
	}

	def with(org.mintjams.script.resource.Resource.ResourceIterator resourceIterator) {
		this.resourceIterator = resourceIterator;
		return this;
	}

	void skip(long skipNum) {
		resourceIterator.skip(skipNum);
	}

	boolean hasNext() {
		return resourceIterator.hasNext();
	}

	Item next() {
		return Item.create(context).with(resourceIterator.next());
	}
}
