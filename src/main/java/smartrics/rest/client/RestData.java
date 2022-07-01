/*  Copyright 2008 Fabrizio Cannizzo
 *
 *  This file is part of RestFixture.
 *
 *  RestFixture (http://code.google.com/p/rest-fixture/) is free software:
 *  you can redistribute it and/or modify it under the terms of the
 *  GNU Lesser General Public License as published by the Free Software Foundation,
 *  either version 3 of the License, or (at your option) any later version.
 *
 *  RestFixture is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with RestFixture.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  If you want to contact the author please leave a comment here
 *  http://smartrics.blogspot.com/2008/08/get-fitnesse-with-some-rest.html
 */
package smartrics.rest.client;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Base class for holding shared data between {@code RestRequest} and {@code RestResponse}.
 */
public abstract class RestData {
	public final static String LINE_SEPARATOR = System.getProperty("line.separator");
	public static String DEFAULT_ENCODING = "UTF-8";

	/**
	 * Holds an Http Header.
	 */
	public static class Header {
		private final String name;
		private final String value;

		public Header(String name, String value) {
			if (name == null || value == null)
				throw new IllegalArgumentException("Name or Value is null");
			this.name = name;
			this.value = value;
		}

		public String getName() {
			return name;
		}

		public String getValue() {
			return value;
		}

		@Override
		public int hashCode() {
			return getName().hashCode() + 37 * getValue().hashCode();
		}

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof Header))
				return false;
			Header h = (Header) o;
			return getName().equals(h.getName())
					&& getValue().equals(h.getValue());
		}

		@Override
		public String toString() {
			return String.format("%s:%s", getName(), getValue());
		}
	}

	private final List<Header> headers = new ArrayList<>();
	private byte[] raw;
	private String resource;
	private Long transactionId;

	/**
	 * @return the body of this http request/response
	 */
	public String getBody() {
		if(raw == null) {
			return null;
		}
		try {
			return new String(raw, getCharset());
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException("Unsupported encoding: " + getCharset());
		}
	}

	public byte[] getRawBody() {
		return raw;
	}
	
	public RestData setBody(String body) {
		if(body == null) {
			setRawBody(null);
		} else {
			setRawBody(body.getBytes());
		}
		return this;
	}

	/**
	 * @param rawBody
	 *            the body
	 * @return this RestData
	 */
	public RestData setRawBody(byte[] rawBody) {
		this.raw = rawBody;
		return this;
	}

	/**
	 * @return the resource type (for example {@code /resource-type}) for this
	 *         request/response
	 */
	public String getResource() {
		return resource;
	}

	/**
	 * @param resource
	 *            the resource type
	 * @return this RestData
	 */
	public RestData setResource(String resource) {
		this.resource = resource;
		return this;
	}

	/**
	 * @param txId
	 *            the transaction id
	 * @return this RestData
	 */
	public RestData setTransactionId(Long txId) {
		this.transactionId = txId;
		return this;
	}

	/**
	 * A transaction Id is a unique long for this transaction.
	 *
	 * It can be used to tie request and response, especially when debugging or parsing logs.
	 *
	 * @return the unique value that ties request and response.
	 */
	public Long getTransactionId() {
		return transactionId;
	}

	/**
	 * @return the list of headers for this request/response
	 */
	public List<Header> getHeaders() {
		return Collections.unmodifiableList(headers);
	}

	/**
	 *
	 * @param name
	 *            the header name
	 * @return the sub-list of headers with the same name
	 */
	public List<Header> getHeader(String name) {
		List<Header> headersWithTheSameName = new ArrayList<>();
		for (Header h : headers) {
			if (h.getName().equalsIgnoreCase(name)) {
				headersWithTheSameName.add(h);
			}
		}
		return headersWithTheSameName;
	}

	/**
	 * Adds an HTTP header to the current list.
	 *
	 * @param name
	 *            the header name
	 * @param value
	 *            the header value
	 * @return this RestData
	 */
	public RestData addHeader(String name, String value) {
		this.headers.add(new Header(name, value));
		return this;
	}

	/**
	 * Adds a collection of HTTP headers to the current list of headers.
	 *
	 * @param headers
	 *            the collection of headers
	 * @return this RestData
	 */
	public RestData addHeaders(Map<String, String> headers) {
		for (Map.Entry<String, String> e : headers.entrySet()) {
			addHeader(e.getKey(), e.getValue());
		}
		return this;
	}

	/**
	 * Adds a collection of HTTP headers to the current list of headers.
	 * 
	 * @param headers
	 *            the list of headers
	 * @return this RestData
	 */
	public RestData addHeaders(List<Header> headers) {
		for (Header e : headers) {
			addHeader(e.getName(), e.getValue());
		}
		return this;
	}

	/**
	 * A visually easy to read representation of this {@code RestData}.
	 * 
	 * It tryes to match the typical Http Request/Response
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (Header h : getHeaders()) {
			builder.append(h).append(LINE_SEPARATOR);
		}
		if (raw != null) {
			builder.append(LINE_SEPARATOR);
			builder.append(this.getBody());
		} else {
			builder.append("[empty/null body]");
		}
		return builder.toString();
	}
	
	public String getContentType() {
		return getHeaderValue("Content-Type");
	}
	
	public String getCharset() {
		String v = getHeaderValue("Content-Type");
		if(v == null || !v.contains("charset")) {
			return DEFAULT_ENCODING;
		}
		int pos = v.indexOf("charset");
		pos = v.indexOf("=", pos);
		try {
			String substring = v.substring(pos + 1);
			return substring.trim();
		} catch(RuntimeException e) {
			return DEFAULT_ENCODING;
		}
	}
	
	public String getContentLength() {
		return getHeaderValue("Content-Length");
	}
	
	public String getHeaderValue(String name) {
		List<Header> headers = getHeader(name);
		if(headers.size() > 0) {
			return headers.get(0).getValue();
		}
		return null;
	}
	
}
