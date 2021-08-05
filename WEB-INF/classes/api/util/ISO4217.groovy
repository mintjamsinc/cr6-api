// Copyright (c) 2021 MintJams Inc. Licensed under MIT License.

package api.util;

class ISO4217 {
	static def currencies(displayLocale) {
		if (!displayLocale) {
			displayLocale = Locale.getDefault();
		}

		def countries = [:];
		for (def locale in Locale.availableLocales) {
			if (!locale.country) {
				continue;
			}

			if (!countries[locale.country]) {
				countries[locale.country] = [];
			}
			countries[locale.country].add(locale);
		}

		def results = [];
		for (def e in countries) {
			def locale = countries[e.key][0];
			def cc = Currency.getInstance(locale);
			def r = [:];
			r["Ctry"] = e.key;
			r["CtryNm"] = locale.getDisplayCountry(displayLocale);
			r["Ccy"] = cc.currencyCode;
			r["CcyNm"] = cc.getDisplayName(displayLocale);
			r["isDefault"] = false;
			r["languages"] = [];
			for (def l in countries[e.key]) {
				r["languages"].add(l.language);
			}
			results.add(r);
		}

		results.sort { o1, o2 -> o1["CtryNm"] <=> o2["CtryNm"] };

		setDefaultCurrency(results, displayLocale);

		for (def r in results) {
			r.remove("languages");
		}
		return results;
	}

	static def setDefaultCurrency(currencies, displayLocale) {
		// by country
		for (def r in currencies) {
			if (r["Ctry"] == displayLocale.country) {
				r["isDefault"] = true;
				return;
			}
		}

		// by language
		for (def r in currencies) {
			if (r["languages"].contains(displayLocale.language)) {
				r["isDefault"] = true;
				return;
			}
		}
	}
}
