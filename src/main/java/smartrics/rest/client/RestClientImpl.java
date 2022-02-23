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

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/*
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
import org.apache.commons.httpclient.methods.multipart.StringPart;
*/
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.message.BasicHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.print.URIException;

/**
 * A generic REST client based on {@code HttpClient}.
 */
public class RestClientImpl implements RestClient {

    private static final Logger LOG = LoggerFactory.getLogger(RestClientImpl.class);

    private final HttpClient client;

    private String baseUrl;

    /**
     * Constructor allowing the injection of an {@code
     * org.apache.commons.httpclient.HttpClient}.
     *
     * @param client the client
     *               See {@link HttpClient}
     */
    public RestClientImpl(HttpClient client) {
        if (client == null)
            throw new IllegalArgumentException("Null HttpClient instance");
        this.client = client;
    }

    /**
     * See {@link smartrics.rest.client.RestClient#setBaseUrl(java.lang.String)}
     */
    public void setBaseUrl(String bUrl) {
        this.baseUrl = bUrl;
    }

    /**
     * See {@link smartrics.rest.client.RestClient#getBaseUrl()}
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Returns the Http client instance used by this implementation.
     *
     * @return the instance of HttpClient
     * See {@link HttpClient}
     * See {@link smartrics.rest.client.RestClientImpl#RestClientImpl(HttpClient)}
     */
    public HttpClient getClient() {
        return client;
    }

    /**
     * See {@link smartrics.rest.client.RestClient#execute(smartrics.rest.client.RestRequest)}
     */
    public RestResponse execute(RestRequest request) {
        return execute(getBaseUrl(), request);
    }

    /**
     * See {@link smartrics.rest.client.RestClient#execute(java.lang.String, smartrics.rest.client.RestRequest)}
     */
    public RestResponse execute(String hostAddr, final RestRequest request) {
        if (request == null || !request.isValid())
            throw new IllegalArgumentException("Invalid request " + request);
        if (request.getTransactionId() == null)
            request.setTransactionId(System.currentTimeMillis());
        LOG.info("request: {}", request);
        HttpRequestBase method = createHttpClientMethod(request);
        configureHttpMethod(method, hostAddr, request);
        // Debug Client
        if (LOG.isInfoEnabled()) {
            LOG.info("Http Request URI : {}", method.getURI());
            // Request Header
            LOG.info("Http Request Method Class : {} ",    method.getClass()  );
            LOG.info("Http Request Header : {} ",    Arrays.toString( method.getAllHeaders()) );
            // Request Body
            if (method instanceof HttpEntityEnclosingRequestBase) {
                try {
                    ByteArrayOutputStream requestOut = new ByteArrayOutputStream();
                    ((HttpEntityEnclosingRequestBase) method).getEntity().writeTo(requestOut);
                    LOG.info("Http Request Body : {}", requestOut);
                } catch (IOException e) {
                    LOG.error("Error in reading request body in debug : " + e.getMessage(), e);
                }
            }
        }
        // Prepare Response
        RestResponse resp = new RestResponse();
        resp.setTransactionId(request.getTransactionId());
        resp.setResource(request.getResource());
        try {
            HttpResponse response = client.execute(method);
            for (Header h : response.getAllHeaders()) {
                resp.addHeader(h.getName(), h.getValue());
            }
            resp.setStatusCode(response.getStatusLine().getStatusCode());
            resp.setStatusText(response.getStatusLine().toString());
            int nRead;
            byte[] bytes = new byte[4];
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            response.getEntity().writeTo(buffer);
            resp.setRawBody(buffer.toByteArray());
            // Debug
            if (LOG.isDebugEnabled()) {
                LOG.debug("Http Request Path : {}", method.toString());
                LOG.debug("Http Request Header : {} ", Arrays.toString( method.getAllHeaders()) );
                LOG.debug("Http Response Status : {}", response.getStatusLine() );
                LOG.debug("Http Response Body : {}", new String(resp.getRawBody()));
            }

        } catch (IOException e) {
            String message = "Http call failed for IO failure";
            throw new IllegalStateException(message, e);
        } finally {
            method.releaseConnection();
        }
        LOG.debug("response: {}", resp);
        return resp;
    }

    /**
     * Configures the instance of HttpMethod with the data in the request and
     * the host address.
     *
     * @param method        the method class to configure
     * @param hostAddr the host address
     * @param request  the rest request
     */
    protected void configureHttpMethod(HttpRequestBase method, String hostAddr, final RestRequest request) {
        addHeaders(method, request);
        setUri(method, hostAddr, request);
        if (method instanceof HttpEntityEnclosingRequestBase) {
            HttpEntity requestEntity = null;
            String fileName = request.getFileName();
            if (fileName != null) {
                requestEntity = configureFileUpload(fileName);
            } else {
                // Add Multipart
                Map<String, RestMultipart> multipartFiles = request.getMultipartFileNames();
                if ((multipartFiles != null) && (!multipartFiles.isEmpty())) {
                    requestEntity = configureMultipartFileUpload(method, request, requestEntity, multipartFiles);
                } else {
                    requestEntity = new ByteArrayEntity(request.getBody().getBytes()) {
                        @Override
                        public Header getContentType() {
                            List<smartrics.rest.client.RestData.Header> values = request.getHeader("Content-Type");
                            String v = "text/xml";
                            if (values.size() != 0)
                                v = values.get(0).getValue();
                            return new BasicHeader("Content-Type", v);
                        }
                    };
                }
            }
            ((HttpEntityEnclosingRequestBase) method).setEntity(requestEntity);
        } else {
            //TODO what about redirects
            //method.setFollowRedirects(request.isFollowRedirect());
        }

    }


    private HttpEntity configureMultipartFileUpload(HttpRequestBase m, final RestRequest request, HttpEntity requestEntity, Map<String, RestMultipart> multipartFiles) {
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        // Current File Name reading for tracking missing file
        String fileName = null;

        // Read File Part
        for (Map.Entry<String, RestMultipart> multipartFile : multipartFiles.entrySet()) {
            ContentBody filePart = createMultipart(multipartFile.getKey(), multipartFile.getValue());
            builder.addPart(multipartFile.getKey(), filePart);
        }
        return builder.build();
    }

    private ContentBody createMultipart(String fileParamName, RestMultipart restMultipart) {
        RestMultipart.RestMultipartType type = restMultipart.getType();
        switch (type) {
            case FILE:
                String fileName;
                fileName = restMultipart.getValue();
                File file = new File(fileName);
                FileBody fileBody = new FileBody(file); //TODO what about content type
                //FilePart filePart = new FilePart(fileParamName, file, restMultipart.getContentType(), restMultipart.getCharset());
                LOG.info("Configure Multipart file upload paramName={} :  ContentType={} for  file={} ", new String[]{ fileParamName,  restMultipart.getContentType(), fileName});
                return fileBody;
            case STRING:
                StringBody stringPart = new StringBody(restMultipart.getValue(), ContentType.MULTIPART_FORM_DATA);
                //stringPart.setContentType(restMultipart.getContentType()); TODO whata about content type
                LOG.info("Configure Multipart String upload paramName={} :  ContentType={} ", fileParamName, stringPart.getContentType());
                return stringPart;
            default:
                throw new IllegalArgumentException("Unknonw Multipart Type : " + type);
        }

    }



    private HttpEntity configureFileUpload(String fileName) {
        final File file = new File(fileName);
        if (!file.exists()) {
            throw new IllegalArgumentException("File not found: " + fileName);
        }
        return new FileEntity(file); //TODO what about the content type
    }

    private void setUri(HttpRequestBase m, String hostAddr, RestRequest request) {
        //TODO what about gethostconfiguration
        //String host = hostAddr == null ? client.getHostConfiguration().getHost() : hostAddr;
        if (hostAddr == null) {
            throw new IllegalStateException("hostAddress is null: please config httpClient host configuration or " + "pass a valid host address or config a baseUrl on this client");
        }
        String uriString = hostAddr + request.getResource();
        boolean escaped = request.isResourceUriEscaped();
        try {
            URIBuilder builder = new URIBuilder(uriString);
            builder.setQuery(request.getQuery());
            m.setURI(builder.build());
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Problem when building URI: " + uriString, e);
        } catch (NullPointerException e) {
            throw new IllegalStateException("Building URI with null string", e);
        }
    }

    protected URI createUri(String uriString, boolean escaped) throws URISyntaxException, UnsupportedEncodingException {
        if (escaped) {
            return new URI(URLDecoder.decode(uriString, StandardCharsets.UTF_8.toString()));
        } else {
            return new URI(uriString);
        }
    }
    /**
     * factory method that maps a string with a HTTP method name to an
     * implementation class in Apache HttpClient. Currently the name is mapped
     * to <code>org.apache.commons.httpclient.methods.%sMethod</code> where
     * <code>%s</code> is the parameter mName.
     *
     * @param mName the method name
     * @return the method class
     */
    protected String getMethodClassnameFromMethodName(String mName) {
        //TODO fix this for new http client
        return String.format("org.apache.http.client.methods.Http%s", mName);
    }

    /**
     * Utility method that creates an instance of {@code
     * org.apache.commons.httpclient.HttpMethod}.
     *
     * @param request the rest request
     * @return the instance of {@code org.apache.commons.httpclient.HttpMethod}
     * matching the method in RestRequest.
     */
    @SuppressWarnings("unchecked")
    protected HttpRequestBase createHttpClientMethod(RestRequest request) {
        String mName = request.getMethod().toString();
        String className = getMethodClassnameFromMethodName(mName);
        try {
            Class<HttpRequestBase> clazz = (Class<HttpRequestBase>) Class.forName(className);
            return clazz.newInstance();
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(className + " not found: you may be using a too old or " + "too new version of HttpClient", e);
        } catch (InstantiationException e) {
            throw new IllegalStateException("An object of type " + className + " cannot be instantiated", e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("The default ctor for type " + className + " cannot be accessed", e);
        } catch (RuntimeException e) {
            throw new IllegalStateException("Exception when instantiating: " + className, e);
        }
    }

    private void addHeaders(HttpRequestBase method, RestRequest request) {
        for (RestData.Header h : request.getHeaders()) {
            method.addHeader(h.getName(), h.getValue());
        }
    }
}
