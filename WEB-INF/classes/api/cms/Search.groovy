// Copyright (c) 2021 MintJams Inc. Licensed under MIT License.

package api.cms;

import api.util.JSON;
import org.mintjams.script.ScriptingContext;
 
class Search {
	def context;

	Search(context) {
		this.context = context;
	}

	static def create(ScriptingContext context) {
		return new Search(context);
	}

	def execute(params) {
		def result = context.resourceResolver.session.workspace.queryManager.createQuery(params.statement, params.language)
			.offset(params.offset ?: 0)
			.limit(params.limit ?: 100)
			.execute();
		return new Result(result);
	}

	class Result {
		def result;

		Result(result) {
			this.result = result;
		}

		def getTotal() {
			return result.total;
		}

		def hasMore() {
			return result.hasMore();
		}

		def getItems() {
			def l = [];
			for (r in result.resources) {
				def item = Item.create(context).with(r);
				l.add(item);
			}
			return l;
		}

		def toObject() {
			def o = [
				"hasMore": result.hasMore(),
				"total": result.getTotal(),
				"items": []
			];
			for (r in result.resources) {
				def item = Item.create(context).with(r);
				o.items.add(item.toObject());
			}
			return o;
		}

		def toJson() {
			return JSON.stringify(toObject());
		}
	}
}
