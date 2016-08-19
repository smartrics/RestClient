package smartrics.rest.client;

/**
 * Wraps a REST request multipart object used in {@code RestRequest}.
 */
public class RestMultipart {

    private String fileName;
    private String contentType;
    private String charset;

    public RestMultipart(String fileName) {
        this(fileName, (String)null, (String)null);
    }

    public RestMultipart(String fileName, String contentType) {
        this(fileName, contentType, (String)null);
    }

    public RestMultipart(String fileName, String contentType, String charset) {
        this.fileName = fileName;
        this.contentType = contentType;
        this.charset = charset;
    }

    /**
     * @return the upload file name for this request
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Sets the multipart upload file name for this request.
     *
     * @param fileName
     *            the multipart file name
     * @return this restMultipart
     */
    public RestMultipart setFileName(String fileName) {
        this.fileName = fileName;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RestMultipart)) return false;

        RestMultipart that = (RestMultipart) o;

        if (!fileName.equals(that.fileName)) return false;
        if (contentType != null ? !contentType.equals(that.contentType) : that.contentType != null) return false;
        return charset != null ? charset.equals(that.charset) : that.charset == null;

    }

    @Override
    public int hashCode() {
        int result = fileName.hashCode();
        result = 31 * result + (contentType != null ? contentType.hashCode() : 0);
        result = 31 * result + (charset != null ? charset.hashCode() : 0);
        return result;
    }


    @Override
    public String toString() {
        return "RestMultipart{" +
                "fileName='" + fileName + '\'' +
                ", contentType='" + contentType + '\'' +
                ", charset='" + charset + '\'' +
                '}';
    }

}
