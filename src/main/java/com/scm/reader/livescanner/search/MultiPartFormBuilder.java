package com.scm.reader.livescanner.search;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * Created by franco on 17/06/16.
 */
public class MultiPartFormBuilder {

    private static final String MULTIPART_BOUNDARY = "----------ThIs_Is_tHe_bouNdaRY_";
    private static final String CRLF = "\r\n";

    public byte[] build(byte[] image, Map<String, String> params) throws IOException {

        ByteArrayOutputStream bodyOutputStream = new ByteArrayOutputStream();

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            sb.append("--" + MULTIPART_BOUNDARY + CRLF);
            sb.append("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"" + CRLF);
            sb.append(CRLF);
            sb.append(entry.getValue());
            sb.append(CRLF);
        }

        sb.append("--" + MULTIPART_BOUNDARY + CRLF);
        sb.append("Content-Disposition: form-data; name=\"image\"" + CRLF);
        sb.append("Content-Type: appliation/octet-stream" + CRLF);
        sb.append(CRLF);

        bodyOutputStream.write(sb.toString().getBytes());
        bodyOutputStream.write(image);
        bodyOutputStream.write((CRLF + "--" + MULTIPART_BOUNDARY + "--").getBytes());
        return bodyOutputStream.toByteArray();
    }

    public String getMultipartBoundary() {
        return MULTIPART_BOUNDARY;
    }
}
