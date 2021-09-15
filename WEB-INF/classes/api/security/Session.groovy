// Copyright (c) 2021 MintJams Inc. Licensed under MIT License.

package api.security;

import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import jp.co.mintjams.osgi.service.jcr.script.ScriptingContext;

class Session {
	def context;
	def key;

	Session(context) {
		this.context = context;
		def session = context.getAttribute("session");
		this.key = session.getAttribute("user.session.key");
		if (!this.key) {
			def id = session.getAttribute("user.session.id");
			def eTag = session.getAttribute("user.session.eTag");
			def md = MessageDigest.getInstance("SHA-256");
			md.update(id.getBytes("UTF-8"));
			def keySpec = new PBEKeySpec(eTag.toCharArray(), md.digest(), 10240, 128);
			this.key = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(keySpec);
			session.setAttribute("user.session.key", this.key);
		}
	}

	static def create(ScriptingContext context) {
		return new Session(context);
	}

	static def create(ScriptingContext context, String id, String eTag) {
		def session = context.getAttribute("session");
		session.setAttribute("user.session.id", id);
		session.setAttribute("user.session.eTag", eTag);
		session.removeAttribute("user.session.key");
		return new Session(context);
	}

	def mask(value) {
		def data;
		if (value instanceof String) {
			data = value.getBytes("UTF-8");
		} else {
			data = value as byte[];
		}
		def cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key.getEncoded(), "AES"));
		def out = new ByteArrayOutputStream();
 		out.write(cipher.getParameters().getParameterSpec(IvParameterSpec.class).getIV());
 		out.write(cipher.doFinal(data));
		return "{AES}" + out.toByteArray().encodeBase64();
	}
}
