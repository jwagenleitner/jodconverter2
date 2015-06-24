//
// JODConverter - Java OpenDocument Converter
// Copyright (C) 2004-2007 - Mirko Nasato <mirko@artofsolving.com>
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
// http://www.gnu.org/copyleft/lesser.html
//
package com.artofsolving.jodconverter.web;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.artofsolving.jodconverter.DocumentConverter;
import com.artofsolving.jodconverter.DocumentFormat;
import com.artofsolving.jodconverter.DocumentFormatRegistry;

public class DocumentConverterServlet extends HttpServlet {

    private static final long serialVersionUID = 2069882175819537711L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    	ApplicationContext applicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
    	ServletFileUpload fileUpload = (ServletFileUpload) applicationContext.getBean("fileUpload");
    	DocumentConverter converter = (DocumentConverter) applicationContext.getBean("documentConverter");
    	DocumentFormatRegistry registry = (DocumentFormatRegistry) applicationContext.getBean("documentFormatRegistry");
    	
        if (!ServletFileUpload.isMultipartContent(request)) {
            throw new IllegalArgumentException("request is not multipart");
        }

        // determine output format based on the request uri
        String outputExtension = FilenameUtils.getExtension(request.getRequestURI());
        DocumentFormat outputFormat = registry.getFormatByFileExtension(outputExtension);
        if (outputFormat == null) {
            throw new IllegalArgumentException("invalid outputFormat: "+ outputExtension);
        }

        FileItem inputFileUpload = getInputFileUpload(request, fileUpload);
        if (inputFileUpload == null) {
            throw new IllegalArgumentException("inputDocument is null");
        }
        String inputExtension = FilenameUtils.getExtension(inputFileUpload.getName());
        DocumentFormat inputFormat = registry.getFormatByFileExtension(inputExtension);

        response.setContentType(outputFormat.getMimeType());
        String fileName = FilenameUtils.getBaseName(inputFileUpload.getName()) +"."+ outputFormat.getFileExtension();
        response.setHeader("Content-Disposition", "inline; filename="+ fileName);
        //response.setContentLength(???);

        converter.convert(inputFileUpload.getInputStream(), inputFormat, response.getOutputStream(), outputFormat);
    }

    private FileItem getInputFileUpload(HttpServletRequest request, ServletFileUpload fileUpload) throws ServletException {
        FileItem inputFileUpload = null;
        try {
            List fileItems = fileUpload.parseRequest(request);
            for (Iterator it = fileItems.iterator(); it.hasNext();) {
                FileItem item = (FileItem) it.next();
                if ("inputDocument".equals(item.getFieldName())) {
                    inputFileUpload = item;
                }
            }
        } catch (FileUploadException e) {
            throw new ServletException("file upload failed", e);
        }
        return inputFileUpload;
    }
}
