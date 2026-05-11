package com.viewlink.controller;

import com.viewlink.api.consumer.ResourceClient;
import feign.Response;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotEmpty;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

@RestController
@RequestMapping("/file")
public class FileController {

    @Resource
    private ResourceClient resourceClient;

    @RequestMapping("/downloadVideo/{fileId}")
    public void downloadVideo(HttpServletResponse httpServletResponse, @PathVariable @NotEmpty String fileId) {
        Response response = resourceClient.downloadVideo(fileId);
        writeResponse(httpServletResponse, response);
    }

    private void writeResponse(HttpServletResponse httpServletResponse, Response response) {
        if (response == null || response.body() == null) {
            return;
        }
        Map<String, Collection<String>> headers = response.headers();
        Collection<String> contentTypeValues = getHeaderValues(headers, "Content-Type");
        if (contentTypeValues != null && !contentTypeValues.isEmpty()) {
            httpServletResponse.setContentType(contentTypeValues.iterator().next());
        }
        Collection<String> contentDispositionValues = getHeaderValues(headers, "Content-Disposition");
        if (contentDispositionValues != null && !contentDispositionValues.isEmpty()) {
            httpServletResponse.setHeader("Content-Disposition", contentDispositionValues.iterator().next());
        }
        try (InputStream inputStream = response.body().asInputStream();
             ServletOutputStream outputStream = httpServletResponse.getOutputStream()) {
            byte[] bytes = new byte[1024];
            int len;
            while ((len = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, len);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Collection<String> getHeaderValues(Map<String, Collection<String>> headers, String targetHeader) {
        for (Map.Entry<String, Collection<String>> entry : headers.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(targetHeader)) {
                return entry.getValue();
            }
        }
        return null;
    }
}
