// Copyright (c) 2021 MintJams Inc. Licensed under MIT License.

package api.cms;

import api.util.ISO8601;
import api.util.JSON;
import org.mintjams.script.ScriptingContext;
import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.jpeg.JpegDirectory;
import cr6.image.Images;

class ItemHelper {
	def context;
	def item;

	ItemHelper(context) {
		this.context = context;
	}

	static def create(ScriptingContext context) {
		return new ItemHelper(context);
	}

	def with(Item item) {
		this.item = item;
		return this;
	}

	def importContent(o, mimeType = "application/octet-stream") {
		def MimeTypeAPI = context.getAttribute("MimeTypeAPI");
		if (!mimeType || mimeType == "application/octet-stream") {
			def type = MimeTypeAPI.getMimeType(item.name);
			if (type) {
				mimeType = type;
			}
		}

		if (o instanceof MultipartUpload) {
			item.setContent(o.file).setContentType(mimeType);
			resetImageAttributes();
		} else if (o instanceof java.io.InputStream) {
			item.setContent(o).setContentType(mimeType);
			resetImageAttributes();
		}
	}

	def resetImageAttributes() {
		item.removeAttribute("mi:thumbnail");
		item.removeAttribute("mi:imageWidth");
		item.removeAttribute("mi:imageHeight");
		item.removeAttribute("mi:orientation");
		def mimeType = item.contentType;
		if (mimeType && (mimeType.startsWith("image/") || mimeType.startsWith("video/"))) {
			try {
				def thumb = Images.getThumbnailAsStream(item.contentAsStream, mimeType);
				if (thumb) {
					thumb.withCloseable { stream ->
						item.setAttribute("mi:thumbnail", stream);
					}
				} else {
					item.removeAttribute("mi:thumbnail");
				}
			} catch (Throwable ex) {
				item.removeAttribute("mi:thumbnail");
			}
		}
		if (mimeType && mimeType == "image/jpeg") {
			try {
				def metadata = ImageMetadataReader.readMetadata(o.file);

				def jpegDirectory = metadata.getFirstDirectoryOfType(JpegDirectory.class);
				item.setAttribute("mi:imageWidth", jpegDirectory.imageWidth);
				item.setAttribute("mi:imageHeight", jpegDirectory.imageHeight);

				def exifIFD0Directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
				item.setAttribute("mi:orientation", exifIFD0Directory.getInt(ExifIFD0Directory.TAG_ORIENTATION));
			} catch (Throwable ex) {}
		}
	}

	def importAttributes(properties) {
		if (properties instanceof Map) {
			properties = properties.collect { e ->
				def prop = e.value;
				prop.key = e.key;
				return prop;
			}
		}

		for (prop in properties) {
			def key = prop.key;
			if (item.contains(key)) {
				item.removeAttribute(key);
			}
			if (prop.value == null) {
				continue;
			}

			def type = prop.type;
			def value = prop.value;
			def mask = !!prop.mask;
			if (type == "String") {
				if (value instanceof Collection) {
					item.setAttribute(key, value as String[], mask);
				} else {
					item.setAttribute(key, value as String, mask);
				}
			} else if (type.equalsIgnoreCase("Binary")) {
				if (value instanceof Collection) {
					continue;
				} else {
					item.setAttribute(key, value.decodeBase64());
				}
			} else if (type == "Long") {
				if (value instanceof Collection) {
					item.setAttribute(key, value as Long[]);
				} else {
					item.setAttribute(key, value as Long);
				}
			} else if (type == "Double") {
				if (value instanceof Collection) {
					item.setAttribute(key, value as Double[]);
				} else {
					item.setAttribute(key, value as Double);
				}
			} else if (type == "Decimal") {
				if (value instanceof Collection) {
					item.setAttribute(key, value as BigDecimal[]);
				} else {
					item.setAttribute(key, value as BigDecimal);
				}
			} else if (type == "Date") {
				if (value instanceof Collection) {
					def values = [];
					for (def v in value) {
						values.add(ISO8601.parseDate(v));
					}
					item.setAttribute(key, values as Date[]);
				} else {
					item.setAttribute(key, ISO8601.parseDate(value));
				}
			} else if (type == "Boolean") {
				if (value instanceof Collection) {
					item.setAttribute(key, value as Boolean[]);
				} else {
					item.setAttribute(key, value as Boolean);
				}
			} else if (type == "Reference" || type == "WeakReference" || type == "Path") {
				def rr;
				if (value.path) {
					rr = context.resourceResolver.getResource(value.path);
				} else {
					rr = context.resourceResolver.getResourceByIdentifier(value.id);
				}
				if (!rr.exists()) {
					def ex = new ReferencedItemNotFoundException("Unable to import the item \"" + item.path + "\": The referenced item does not exist: " + (value.path ? "Path" : "Identifier") + "=" + (value.path ?: value.id));
					if (value.path) {
						ex.path = value.path;
					} else {
						ex.identifier = value.id;
					}
					throw ex;
				}
				item.setAttribute(key, rr);
			}
		}
	}
}
