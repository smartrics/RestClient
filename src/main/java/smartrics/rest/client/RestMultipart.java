package smartrics.rest.client;

/**
 * Wraps a REST request multipart object used in {@code RestRequest}.
 */
public class RestMultipart {

    /**
     * An http verb (those supported).
     */
    public enum RestMultipartType {
        FILE, STRING
    }

    private RestMultipartType type;
    private String value;
    private String contentType;
    private String charset;

    /**
     *
     * @param type Type of the stringValue
     * @param stringValue The String content or the file Path
     */
    public RestMultipart(RestMultipartType type, String stringValue) {
        this(type, stringValue, null, null);
    }

    public RestMultipart(RestMultipartType type,String stringValue, String contentType) {
        this(type, stringValue, contentType, null);
    }

    public RestMultipart(RestMultipartType type, String stringValue, String contentType, String charset) {
        this.type = type;
        this.value = stringValue;
        this.contentType = contentType;
        this.charset = charset;
    }

    /**
     * @return the upload file name for this request
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the multipart upload file name for this request.
     *
     * @param value
     *            the multipart file name
     * @return this restMultipart
     */
    public RestMultipart setValue(String value) {
        this.value = value;
        return this;
    }

    /**
     * @return the Content Type for upload file name for this request
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Sets the content type of multipart upload file name for this request.
     *
     * @param contentType
     *            the content type
     * @return this restMultipart
     */
    public RestMultipart setContentType(String contentType) {
        this.contentType = contentType;
        return this;

    }

    /**
     * @return the Charset for upload file name for this request
     */
    public String getCharset() {
        return charset;
    }

    /**
     * Sets the charset of multipart upload file name for this request.
     *
     * @param charset
     *            the charset
     * @return this restMultipart
     */

    public RestMultipart setCharset(String charset) {
        this.charset = charset;
        return this;
    }

    public RestMultipartType getType() {
        return type;
    }

    public void setType(RestMultipartType type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RestMultipart)) return false;

        RestMultipart that = (RestMultipart) o;

        if (!value.equals(that.value)) return false;
        if (contentType != null ? !contentType.equals(that.contentType) : that.contentType != null) return false;
        return charset != null ? charset.equals(that.charset) : that.charset == null;

    }



    @Override
    public int hashCode() {
        int result = value.hashCode();
        result = 31 * result + (contentType != null ? contentType.hashCode() : 0);
        result = 31 * result + (charset != null ? charset.hashCode() : 0);
        return result;
    }


    @Override
    public String toString() {
        return "RestMultipart{" +
                "value='" + value + '\'' +
                ", contentType='" + contentType + '\'' +
                ", charset='" + charset + '\'' +
                '}';
    }

}
