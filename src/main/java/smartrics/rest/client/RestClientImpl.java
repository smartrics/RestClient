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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.FileRequestEntity;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A generic REST client based on {@code HttpClient}.
 */
public class RestClientImpl implements RestClient {

    private static Logger    LOG = LoggerFactory.getLogger(RestClientImpl.class);

    private final HttpClient client;

    private String           baseUrl;

    private boolean          allowRedirect;

    /**
     * Constructor allowing the injection of an {@code org.apache.commons.httpclient.HttpClient}.
     * 
     * @param client
     *            the client See {@link org.apache.commons.httpclient.HttpClient}
     */
    public RestClientImpl(final HttpClient client) {
        if (client == null) {
            throw new IllegalArgumentException("Null HttpClient instance");
        }
        allowRedirect = true;
        this.client = client;
    }

    /**
     * See {@link smartrics.rest.client.RestClient#setBaseUrl(java.lang.String)}
     */
    @Override
    public void setBaseUrl(final String bUrl) {
        baseUrl = bUrl;
    }

    /**
     * See {@link smartrics.rest.client.RestClient#getBaseUrl()}
     */
    @Override
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Returns the Http client instance used by this implementation.
     * 
     * @return the instance of HttpClient See {@link org.apache.commons.httpclient.HttpClient} See
     *         {@link smartrics.rest.client.RestClientImpl#RestClientImpl(HttpClient)}
     */
    public HttpClient getClient() {
        return client;
    }

    /**
     * See {@link smartrics.rest.client.RestClient#execute(smartrics.rest.client.RestRequest)}
     */
    @Override
    public RestResponse execute(final RestRequest request) {
        return execute(getBaseUrl(), request);
    }

    /**
     * See {@link smartrics.rest.client.RestClient#execute(java.lang.String, smartrics.rest.client.RestRequest)}
     */
    @Override
    public RestResponse execute(final String hostAddr, final RestRequest request) {
        if (request == null || !request.isValid()) {
            throw new IllegalArgumentException("Invalid request " + request);
        }
        if (request.getTransactionId() == null) {
            request.setTransactionId(Long.valueOf(System.currentTimeMillis()));
        }
        LOG.debug("request: {}", request);
        final HttpMethod m = createHttpClientMethod(request);
        configureHttpMethod(m, hostAddr, request);
        final RestResponse resp = new RestResponse();
        resp.setTransactionId(request.getTransactionId());
        resp.setResource(request.getResource());
        try {
            if (RestRequest.Method.Get.equals(request.getMethod())) {
                m.setFollowRedirects(allowRedirect);
            }
            client.executeMethod(m);
            for (final Header h : m.getResponseHeaders()) {
                resp.addHeader(h.getName(), h.getValue());
            }
            resp.setStatusCode(m.getStatusCode());
            resp.setStatusText(m.getStatusText());
            resp.setBody(m.getResponseBodyAsString());
        } catch (final HttpException e) {
            final String message = "Http call failed for protocol failure";
            throw new IllegalStateException(message, e);
        } catch (final IOException e) {
            final String message = "Http call failed for IO failure";
            throw new IllegalStateException(message, e);
        } finally {
            m.releaseConnection();
        }
        LOG.debug("response: {}", resp);
        return resp;
    }

    /**
     * Configures the instance of HttpMethod with the data in the request and the host address.
     * 
     * @param m
     *            the method class to configure
     * @param hostAddr
     *            the host address
     * @param request
     *            the rest request
     */
    protected void configureHttpMethod(final HttpMethod m, final String hostAddr, final RestRequest request) {
        addHeaders(m, request);
        setUri(m, hostAddr, request);
        m.setQueryString(request.getQuery());
        if (m instanceof EntityEnclosingMethod) {
            RequestEntity requestEntity = null;
            String fileName = request.getFileName();
            if (fileName != null) {
                requestEntity = configureFileUpload(fileName);
            } else {
                fileName = request.getMultipartFileName();
                if (fileName != null) {
                    requestEntity = configureMultipartFileUpload(m, request, requestEntity, fileName);
                } else {
                    requestEntity = new RequestEntity() {
                        @Override
                        public boolean isRepeatable() {
                            return true;
                        }

                        @Override
                        public void writeRequest(final OutputStream out) throws IOException {
                            final PrintWriter printer = new PrintWriter(out);
                            printer.print(request.getBody());
                            printer.flush();
                        }

                        @Override
                        public long getContentLength() {
                            return request.getBody().getBytes().length;
                        }

                        @Override
                        public String getContentType() {
                            final List<smartrics.rest.client.RestData.Header> values = request
                                    .getHeader("Content-Type");
                            String v = "text/xml";
                            if (values.size() != 0) {
                                v = values.get(0).getValue();
                            }
                            return v;
                        }
                    };
                }
            }
            ((EntityEnclosingMethod) m).setRequestEntity(requestEntity);
        }
    }

    private RequestEntity configureMultipartFileUpload(final HttpMethod m, final RestRequest request,
            RequestEntity requestEntity, final String fileName) {
        final File file = new File(fileName);
        try {
            requestEntity = new MultipartRequestEntity(new Part[] { new FilePart(
                    request.getMultipartFileParameterName(), file) }, ((EntityEnclosingMethod) m).getParams());
        } catch (final FileNotFoundException e) {
            throw new IllegalArgumentException("File not found: " + fileName, e);
        }
        return requestEntity;
    }

    private RequestEntity configureFileUpload(final String fileName) {
        final File file = new File(fileName);
        if (!file.exists()) {
            throw new IllegalArgumentException("File not found: " + fileName);
        }
        return new FileRequestEntity(file, "application/octet-stream");
    }

    public String getContentType(final RestRequest request) {
        final List<smartrics.rest.client.RestData.Header> values = request.getHeader("Content-Type");
        String v = "text/xml";
        if (values.size() != 0) {
            v = values.get(0).getValue();
        }
        return v;
    }

    private void setUri(final HttpMethod m, final String hostAddr, final RestRequest request) {
        final String host = hostAddr == null ? client.getHostConfiguration().getHost() : hostAddr;
        if (host == null) {
            throw new IllegalStateException("hostAddress is null: please config httpClient host configuration or "
                    + "pass a valid host address or config a baseUrl on this client");
        }
        final String uriString = host + request.getResource();
        final boolean escaped = request.isResourceUriEscaped();
        try {
            m.setURI(createUri(uriString, escaped));
        } catch (final URIException e) {
            throw new IllegalStateException("Problem when building URI: " + uriString, e);
        } catch (final NullPointerException e) {
            throw new IllegalStateException("Building URI with null string", e);
        }
    }

    protected URI createUri(final String uriString, final boolean escaped) throws URIException {
        return new URI(uriString, escaped);
    }

    /**
     * factory method that maps a string with a HTTP method name to an implementation class in Apache HttpClient.
     * Currently the name is mapped to <code>org.apache.commons.httpclient.methods.%sMethod</code> where <code>%s</code>
     * is the parameter mName.
     * 
     * @param mName
     *            the method name
     * @return the method class
     */
    protected String getMethodClassnameFromMethodName(final String mName) {
        return String.format("org.apache.commons.httpclient.methods.%sMethod", mName);
    }

    /**
     * Utility method that creates an instance of {@code org.apache.commons.httpclient.HttpMethod}.
     * 
     * @param request
     *            the rest request
     * @return the instance of {@code org.apache.commons.httpclient.HttpMethod} matching the method in RestRequest.
     */
    @SuppressWarnings("unchecked")
    protected HttpMethod createHttpClientMethod(final RestRequest request) {
        final String mName = request.getMethod().toString();
        final String className = getMethodClassnameFromMethodName(mName);
        try {
            final Class<HttpMethod> clazz = (Class<HttpMethod>) Class.forName(className);
            final HttpMethod m = clazz.newInstance();
            return m;
        } catch (final ClassNotFoundException e) {
            throw new IllegalStateException(className + " not found: you may be using a too old or "
                    + "too new version of HttpClient", e);
        } catch (final InstantiationException e) {
            throw new IllegalStateException("An object of type " + className + " cannot be instantiated", e);
        } catch (final IllegalAccessException e) {
            throw new IllegalStateException("The default ctor for type " + className + " cannot be invoked", e);
        } catch (final RuntimeException e) {
            throw new IllegalStateException("Exception when instantiating: " + className, e);
        }
    }

    private void addHeaders(final HttpMethod m, final RestRequest request) {
        for (final RestData.Header h : request.getHeaders()) {
            m.addRequestHeader(h.getName(), h.getValue());
        }
    }

    @Override
    public void allowRedirect(final boolean allowRedirect) {
        this.allowRedirect = allowRedirect;
    }
}
