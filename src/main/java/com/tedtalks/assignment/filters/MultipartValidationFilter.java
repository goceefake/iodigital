package com.tedtalks.assignment.filters;

import com.tedtalks.assignment.exception.InvalidFileTypeException;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class MultipartValidationFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (request instanceof HttpServletRequest) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;

            String requestURI = httpRequest.getRequestURI();
            if (httpRequest.getContentType() != null &&
                    httpRequest.getContentType().startsWith(MediaType.MULTIPART_FORM_DATA_VALUE) &&
                    requestURI.startsWith("/api/v1/ted-talks/import")) {

                for (Part part : httpRequest.getParts()) {

                    String contentType = part.getContentType();
                    if (!"text/csv".equals(contentType)) {
                        throw new InvalidFileTypeException("Invalid file type: " + contentType);
                    }
                }
            }
        }

        chain.doFilter(request, response);
    }
}