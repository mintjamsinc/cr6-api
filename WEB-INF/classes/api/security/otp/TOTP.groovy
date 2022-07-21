// Copyright (c) 2021 MintJams Inc. Licensed under MIT License.

package api.security.otp;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.mintjams.script.ScriptingContext;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.lang3.StringUtils;

class TOTP {
	def context;

	TOTP(context) {
		this.context = context;
	}

	static def create(ScriptingContext context) {
		return new TOTP(context);
	}

	def verify(String secret, String code) {
		def timeNo = System.currentTimeMillis() / 1000 / 30;
		for (def i = -1; i <= 1; i++) {
			def hash = _code(new Base32().decode(secret), (timeNo + i) as long);
			def hashStr = StringUtils.leftPad(String.valueOf(hash), 6, '0');
			if (code.equals(hashStr)) {
				return true;
			}
		}
		return false;
	}

	def _code(byte[] key, long timeNo) {
		def data = new byte[8];
		def value = timeNo;
		for (def i = 8; i-- > 0; value >>>= 8) {
			data[i] = value as byte;
		}
		def signKey = new SecretKeySpec(key, "HmacSHA1");
		def mac = Mac.getInstance("HmacSHA1");
		mac.init(signKey);
		def hash = mac.doFinal(data);
		def offset = hash[20 - 1] & 0xF;
		def truncatedHash = 0;
		for (int i = 0; i < 4; ++i) {
			truncatedHash <<= 8;
			truncatedHash |= (hash[offset + i] & 0xFF);
		}
		truncatedHash &= 0x7FFFFFFF;
		truncatedHash %= 1000000;
		return truncatedHash as int;
	}
}
