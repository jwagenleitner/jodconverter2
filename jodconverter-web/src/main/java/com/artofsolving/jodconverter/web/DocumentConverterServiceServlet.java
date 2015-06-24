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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.artofsolving.jodconverter.DocumentConverter;
import com.artofsolving.jodconverter.DocumentFormat;
import com.artofsolving.jodconverter.DocumentFormatRegistry;

/**
 * This servlet offers a document converter service suitable for remote invocation
 * by HTTP clients written in any language.
 * <p>
 * To be valid a request to service must:
 * <ul>
 *   <li>use the POST method and send the input document data as the request body</li>
 *   <li>specify the correct <b>Content-Type</b> of the input document</li>
 *   <li>specify an <b>Accept</b> header with the mime-type of the desired output</li>
 * </ul>
 * <p>
 * As a very simple example, a request to convert a text document into PDF would
 * look something like
 * <pre>
 * POST /jooconverter/service HTTP/1.1
 * Host: x.y.z
 * Content-Type: text/plain
 * Accept: application/pdf
 * 
 * Hello world!
 * </pre>
 */
public class DocumentConverterServiceServlet extends HttpServlet {

    private static final long serialVersionUID = -6698481065322400366L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    	ApplicationContext applicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
    	DocumentConverter converter = (DocumentConverter) applicationContext.getBean("documentConverter");
    	DocumentFormatRegistry registry = (DocumentFormatRegistry) applicationContext.getBean("documentFormatRegistry");

		String inputMimeType = request.getContentType();
		if (inputMimeType == null) {
			throw new IllegalArgumentException("Content-Type not set in request");
		}
		DocumentFormat inputFormat = registry.getFormatByMimeType(inputMimeType);
		if (inputFormat == null) {
			throw new IllegalArgumentException("unsupported input mime-type: " + inputMimeType);
		}

		String outputMimeType = request.getHeader("Accept");
		if (outputMimeType == null) {
			throw new IllegalArgumentException("Accept header not set in request");
		}
		DocumentFormat outputFormat = registry.getFormatByMimeType(outputMimeType);
		if (outputFormat == null) {
			throw new IllegalArgumentException("unsupported output mime-type: " + outputMimeType);
		}

        response.setContentType(outputFormat.getMimeType());
        //response.setContentLength(???); - should cache result in a buffer first

        try {
            converter.convert(request.getInputStream(), inputFormat, response.getOutputStream(), outputFormat);
        } catch (Exception exception) {
            throw new ServletException("conversion failed", exception);
        }
    }

}
