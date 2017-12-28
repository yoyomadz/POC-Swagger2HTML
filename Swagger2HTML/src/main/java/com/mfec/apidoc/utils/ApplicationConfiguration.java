package com.mfec.apidoc.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ApplicationConfiguration {
	@Value("${" + ApplicationConstants.TEMPLATE_API_DOCS + "}")
	private String templateApiDocs;
	
	@Value("${" + ApplicationConstants.TEMPLATE_API_DICT + "}")
	private String templateApiDict;
	
	@Value("${" + ApplicationConstants.ROOT + "}")
	private String root;
	
	@Value("${" + ApplicationConstants.INBOUND + "}")
	private String inbound;
	
	@Value("${" + ApplicationConstants.OUTBOUND + "}")
	private String outbound;
	
	@Value("${" + ApplicationConstants.ARCHIVED + "}")
	private String archived;

	public String getTemplateApiDocs() {
		return templateApiDocs;
	}

	public String getTemplateApiDict() {
		return templateApiDict;
	}

	public String getRoot() {
		return root;
	}

	public String getInbound() {
		return inbound;
	}

	public String getOutbound() {
		return outbound;
	}

	public String getArchived() {
		return archived;
	}
	
}
