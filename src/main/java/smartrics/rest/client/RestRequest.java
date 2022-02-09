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


import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Wraps a REST request object.
 */
public class RestRequest extends RestData {
    /**
     * An http verb (those supported).
     */
    public enum Method {
        Get, Post, Put, Delete, Head, Options, Trace, Patch
    }

    private static final String FILE = "file";
    private String fileName;
    @Deprecated
    private String multipartFileName;
    @Deprecated
    private String multipartFileParameterName = FILE;
    private Map<String,RestMultipart> multipartFileByParamName = new LinkedHashMap<String, RestMultipart>();
    private String query;
    private Method method;
    private boolean followRedirect = true;
    private boolean resourceUriEscaped = false;

    /**
     * @return the method for this request
     */
    public Method getMethod() {
        return method;
    }

    /**
     * Sets the method for this request.
     * 
     * @param method
     *            the method
     * @return this request
     * @see smartrics.rest.client.RestRequest.Method
     */
    public RestRequest setMethod(Method method) {
        this.method = method;
        return this;
    }

    /**
     * @return the query for this request
     */
    public String getQuery() {
        return query;
    }

    /**
     * Sets the query for this request.
     * 
     * @param query
     *            the query
     * @return this request
     */
    public RestRequest setQuery(String query) {
        this.query = query;
        return this;
    }

    /**
     * @return the upload file name for this request
     */
    public String getFileName() {
        return fileName;
    }

//    /**
//     * @return the multipart upload file name for this request
//     */
//    public String getMultipartFileName() {
//        return multipartFileName;
//    }

    /**
     * @return the multipart upload files name for this request
     */
    public Map<String,RestMultipart> getMultipartFileNames() {
        // Add History Api
        if  ( (this.multipartFileName!=null) && (!this.multipartFileName.trim().isEmpty()) )  {
            RestMultipart restMultipart = new RestMultipart(RestMultipart.RestMultipartType.FILE, this.multipartFileName);
            this.addMultipart(   this.multipartFileParameterName, restMultipart);
        }
        // Return the Map
        return multipartFileByParamName;
    }

    /**
     * @return the multipart file form parameter name for this request
     */
    @Deprecated
    public String getMultipartFileParameterName() {
        return multipartFileParameterName;
    }

    /**
     * Sets the multipart file form parameter name for this request.
     * 
     * @param multipartFileParameterName
     *            the multipart file form parameter name
     * @return this request
     */
    @Deprecated
    public RestRequest setMultipartFileParameterName(String multipartFileParameterName) {
        this.multipartFileParameterName = multipartFileParameterName;
        return this;
    }

    /**
     * Sets the multipart upload file name for this request.
     * 
     * @param multipartFileName
     *            the multipart file name
     * @return this request
     */
    @Deprecated
    public RestRequest setMultipartFileName(String multipartFileName) {
        this.multipartFileName = multipartFileName;
        return this;
    }


    /**
     * Add the multipart upload file name for this request.
     *
     * @param multipartFileName
     *            the multipart file name
     * @return this request
     */
    public RestRequest addMultipartFileName(String multipartFileName ) {
        RestMultipart restMultipart = new RestMultipart(RestMultipart.RestMultipartType.FILE, multipartFileName);
        return this.addMultipart(FILE, restMultipart);
    }

    /**
     * Add the multipart upload file name for this request.
     *
     * @param multipartFileName
     *            the multipart file name
     * @param contentType
     *            the multipart contentType
     * @return this request
     */
    public RestRequest addMultipartFileName(String multipartFileName, String contentType ) {
        RestMultipart restMultipart = new RestMultipart(RestMultipart.RestMultipartType.FILE,multipartFileName, contentType);
        return this.addMultipart(FILE, restMultipart);
    }


    /**
     * Add the multipart upload file name for this request.
     *
     * @param multipartFileName
     *            the multipart file name
     * @param contentType
     *            the multipart contentType
     * @param charSet
     *            the multipart charSet
     * @return this request
     */
    public RestRequest addMultipartFileName(String multipartFileName, String contentType, String charSet ) {
        RestMultipart restMultipart = new RestMultipart(RestMultipart.RestMultipartType.FILE, multipartFileName, contentType, charSet);
        return this.addMultipart(FILE, restMultipart);
    }

    /**
     * Add the multipart upload file name for this request.
     *
     * @param multiParamName
     *            the multipart file form parameter name
     * @param restMultipart
     *            the multipart restMultipart data
     * @return this request
     */
    public RestRequest addMultipart(String multiParamName, RestMultipart restMultipart) {
        multipartFileByParamName.put(multiParamName, restMultipart);
        return this;
    }

    public RestRequest setFollowRedirect(boolean v) {
    	this.followRedirect = v;
    	return this;
    }
    
    public boolean isFollowRedirect() {
    	return followRedirect;
    }
    
    /**
     * Sets the upload file name for this request.
     * 
     * @param fileName
     *            the upload file name
     * @return this request
     */
    public RestRequest setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    /**
     * Checks validity of this request.
     * 
     * In this implementation a request is valid if both the method and the
     * resource Uri not null
     * 
     * @return true if valid
     */
    public boolean isValid() {
        return getMethod() != null && getResource() != null;
    }

	/**
	 * @return whether resource uri is % escaped (true) or not (false). Defaults to false.
	 */
	public boolean isResourceUriEscaped() {
		return resourceUriEscaped;
	}

	/**
	 * @param escaped whether resource uri is % escaped or not
     * @return this request
	 */
	public RestRequest setResourceUriEscaped(boolean escaped) {
		this.resourceUriEscaped = escaped;
		return this;
	}
	
    
    /**
     * String representation of this request.
     * 
     * @return the string representation
     */
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (getMethod() != null)
            builder.append(getMethod().toString()).append(" ");
        if (getResource() != null)
            builder.append(this.getResource());
        if (getQuery() != null)
            builder.append("?").append(this.getQuery());
        builder.append(LINE_SEPARATOR);
        builder.append(super.toString());
        return builder.toString();
    }
}
