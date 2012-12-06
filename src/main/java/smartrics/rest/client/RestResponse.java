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

/**
 * Wraps a REST response object
 */
public class RestResponse extends RestData{
	private String statusText;
	private Integer statusCode;
	
	private String responseCharset = "ISO-8859-1";

	/**
	 * @return the status code of this response
	 */
	public Integer getStatusCode() {
		return statusCode;
	}

	/**
	 * @param sCode the status code for this response
	 * @return this response
	 */
	public RestResponse setStatusCode(Integer sCode) {
		this.statusCode = sCode;
		return this;
	}

	/**
	 * @return the status text for this response
	 */
	public String getStatusText() {
		return statusText;
	}

	/**
	 * @param st the status text for this response
	 * @return this response
	 */
	public RestResponse setStatusText(String st) {
		this.statusText = st;
		return this;
	}

	/**
	 * @return string representation of this response
	 */
	public String toString() {
		StringBuilder builder = new StringBuilder();
		if (getStatusCode() != null)
			builder.append(String.format("[%s] %s", this.getStatusCode(), this.getStatusText()));
		builder.append(LINE_SEPARATOR);
		builder.append(super.toString());
		return builder.toString();
	}
	
    public void setResponseCharset(String responseCharset) {
        this.responseCharset = responseCharset;
    }

    public String getResponseCharset() {
        return responseCharset;
    }

}
