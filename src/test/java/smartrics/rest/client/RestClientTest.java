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
import java.util.stream.Stream;

import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import smartrics.rest.client.RestRequest.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RestClientTest {

    private static final Logger LOG = LoggerFactory.getLogger(RestClientTest.class);

    @Mock
    private HttpRequestBase mockRequestBase;
    @Mock
    private HttpClient httpClient;
    private RestClientImpl restClient;
    @BeforeEach
    public void setUp() {
        restClient = new RestClientImpl(httpClient);
        validRestRequest.setQuery("aQuery");
        validRestRequest.addHeader("a", "v");
    }
    private final RestRequest validRestRequest = (RestRequest) new RestRequest().setMethod(RestRequest.Method.Get).setResource("/a/resource");

    @ParameterizedTest
    @MethodSource("providesArgumentsForHttpMethods")
    public void mustHandleAllHttpMethods(Method method, String result) {
        RestRequest r = new RestRequest();
        r.setMethod(method);
        HttpRequestBase m = restClient.createHttpClientMethod(r);
        assertEquals(result, m.getMethod());
    }

    private static Stream<Arguments> providesArgumentsForHttpMethods() {
        return Stream.of(
                Arguments.of(Method.Get, "GET"),
                Arguments.of(Method.Post, "POST"),
                Arguments.of(Method.Delete, "DELETE"),
                Arguments.of(Method.Patch, "PATCH"),
                Arguments.of(Method.Put, "PUT"),
                Arguments.of(Method.Trace, "TRACE"),
                Arguments.of(Method.Options, "OPTIONS")
        );
    }
    
    @Test
    public void mustBeConstructedWithAValidHttpClient() {
        assertSame(httpClient, restClient.getClient());
    }

    @Test
    public void shouldFailConstructionWithAnInvalidHttpClient() {
        assertThrows(IllegalArgumentException.class, () -> new RestClientImpl(null));
    }

    @Test
    public void mustNotExecuteAnInvalidRequest() {
        assertThrows(IllegalArgumentException.class, () -> restClient.execute(new RestRequest()));
    }

    @Test
    public void mustNotExecuteANullRestRequest() {
        assertThrows(IllegalArgumentException.class, () -> restClient.execute(null));
    }

    @Test
    public void mustNotifyCallerIfHttpCallFailsDueToAnIoFailure() throws IOException {
        when(httpClient.execute(any())).thenThrow(IOException.class);
        assertThrows(IllegalStateException.class, () ->restClient.execute(validRestRequest));
    }

    @Test
    public void mustNotifyCallerIfHttpCallFailsDueToAProtocolFailure() throws IOException {
        when(httpClient.execute(any())).thenThrow(HttpException.class);
        assertThrows(IllegalStateException.class, () ->restClient.execute(validRestRequest));
    }

    @Test
    public void responseShouldContainTheResultCodeOfASuccessfullHttpCall() throws IOException {
        HttpResponse response = new BasicHttpResponse(new BasicStatusLine(new ProtocolVersion("protocol",1,2), 200, ""));
        when(httpClient.execute(any())).thenReturn(response);
        RestResponse restResponse = restClient.execute(validRestRequest);
        assertEquals(Integer.valueOf(200), restResponse.getStatusCode());
    }

    @Test
    public void shouldNotifyCallerThatNullHostAddressesAreNotHandled() {
        assertThrows(IllegalStateException.class, () -> restClient.execute(null, validRestRequest));
    }

    @Test
    public void shouldNotifyCallerThatInvalidResourceUriAreNotHandled() {
        validRestRequest.setResource("http://resource/shoud/not/include/the/abs/path");
        assertThrows(IllegalStateException.class, () -> restClient.execute("http://basehostaddress:8080", validRestRequest));
    }

/*
    @Test
    public void shouldCreateMultipartEntityIfRestRequestHasNonEmptyMultiparts() throws Exception {
        String filename1 = "multiparttest-file1";
        File file1 = File.createTempFile(filename1, null);
        file1.deleteOnExit();
        PrintWriter writer1 = new PrintWriter(file1);
        writer1.println("File1_Content");
        writer1.close();

        String filename2 = "multiparttest-file2";
        File file2 = File.createTempFile(filename2, null);
        file1.deleteOnExit();
        PrintWriter writer2 = new PrintWriter(file2);
        writer2.println("File2_Content");
        writer2.close();

        String json1 = " {\"lastname\":\"Boby\",\"firstname\":\"Bob\"}";


        mockHttpMethod = new MockHttpMethod("mock");
        validRestRequest.addHeader("a", "header");
        validRestRequest.addMultipart("file1", new RestMultipart(RestMultipart.RestMultipartType.FILE, file1.getAbsolutePath()));
        validRestRequest.addMultipart("file2", new RestMultipart(RestMultipart.RestMultipartType.FILE, file2.getAbsolutePath(), "application/octet-stream"));
        validRestRequest.addMultipart("json1", new RestMultipart(RestMultipart.RestMultipartType.STRING, json1, "application/json"));

        RestClientImpl client = new RestClientImpl(new MockHttpClient(200));
        client.configureHttpMethod(mockHttpMethod, "localhost", validRestRequest);
        assertTrue(mockHttpMethod.isMultipartRequest());
        Header[] headers = mockHttpMethod.getRequestHeaders();
        // Test Request Entity
        assertNotNull(mockHttpMethod.getRequestEntity());
        assertTrue( mockHttpMethod.getRequestEntity().getContentType().startsWith("multipart/form-data; boundary="));
        String boundary =  mockHttpMethod.getRequestEntity().getContentType().substring("multipart/form-data; boundary=".length());
        LOG.debug("Request boundary = {}" ,  boundary );
        // Test Request Body
        ByteArrayOutputStream requestOut = new ByteArrayOutputStream();
        mockHttpMethod.getRequestEntity().writeRequest(requestOut);
            String requestBodyAsString = requestOut.toString();
        //System.err.println("-------- " +  requestBodyAsString );
        LOG.debug("requestBodyAsString = {}", requestBodyAsString);
        // Request Body Assert Params
        assertTrue(requestBodyAsString.contains("name=\"file1\""));
        assertTrue(requestBodyAsString.contains("name=\"file2\""));
        assertTrue(requestBodyAsString.contains("name=\"json1\""));
        // Request Body Assert Contents
        assertTrue(requestBodyAsString.contains("File1_Content"));
        assertTrue(requestBodyAsString.contains("File2_Content"));
        assertTrue(requestBodyAsString.contains("Boby"));
        // Test boundary body
        String[] bodySplit = requestBodyAsString.split("--" + boundary);
        assertEquals(5, bodySplit.length);
        LOG.debug("-------- Split Idx 1" +  bodySplit[1] );
        LOG.debug("-------- Split Idx 2" +  bodySplit[2] );
        LOG.debug("-------- Split Idx 3" +  bodySplit[3] );
    }
*/

/*
    @Test
    public void shouldCreateMultipartEntityIfRestRequestHasNonNullMultipartFileNames() throws Exception {
        String filename = "multiparttest";
        File f = File.createTempFile(filename, null);
        f.deleteOnExit();

        mockHttpMethod = new MockHttpMethod("mock");
        validRestRequest.addHeader("a", "header");
        validRestRequest.addMultipartFileName(f.getAbsolutePath());
        RestClientImpl client = new RestClientImpl(new MockHttpClient(200));
        client.configureHttpMethod(mockHttpMethod, "localhost", validRestRequest);
        // Could not be the same as mock :  assertTrue(mockHttpMethod.isMultipartRequest());
    }

    @Test
    public void shouldCreateMultipartEntityIfRestRequestHasNonNullTwoMultipartFileNames() throws Exception {
        String filename = "multiparttest";
        File f = File.createTempFile(filename, null);
        f.deleteOnExit();

        mockHttpMethod = new MockHttpMethod("mock");
        validRestRequest.addHeader("a", "header");
        validRestRequest.addMultipartFileName(f.getAbsolutePath(), "file1");
        validRestRequest.addMultipartFileName(f.getAbsolutePath(), "file2");
        RestClientImpl client = new RestClientImpl(new MockHttpClient(200));
        client.configureHttpMethod(mockHttpMethod, "localhost", validRestRequest);
       // Could not be the same as mock :  assertTrue(mockHttpMethod.isMultipartRequest());
    }
*/


/*
    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfMultipartFileNameDoesNotExist() throws Exception {
        mockHttpMethod = new MockHttpMethod("mock");
        validRestRequest.addHeader("a", "header");
        validRestRequest.setMultipartFileName("multiparttest");
        RestClientImpl client = new RestClientImpl(new MockHttpClient(200));
        client.configureHttpMethod(mockHttpMethod, "localhost", validRestRequest);
    }

    @Test
    public void shouldCreateFileRequestEntityIfRestRequestHasNonNullFileName() throws Exception {
        String filename = "filetest";
        File f = File.createTempFile(filename, null);
        f.deleteOnExit();

        mockHttpMethod = new MockHttpMethod("mock");
        validRestRequest.addHeader("a", "header");
        validRestRequest.setFileName(f.getAbsolutePath());
        RestClientImpl client = new RestClientImpl(new MockHttpClient(200));
        client.configureHttpMethod(mockHttpMethod, "localhost", validRestRequest);
        assertTrue(mockHttpMethod.isFileRequest());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExeptionIfFileNameDoesNotExist() throws Exception {
        mockHttpMethod = new MockHttpMethod("mock");
        validRestRequest.addHeader("a", "header");
        validRestRequest.setFileName("somefilethatdoesntexist");
        RestClientImpl client = new RestClientImpl(new MockHttpClient(200));
        client.configureHttpMethod(mockHttpMethod, "localhost", validRestRequest);
    }
*/
}
