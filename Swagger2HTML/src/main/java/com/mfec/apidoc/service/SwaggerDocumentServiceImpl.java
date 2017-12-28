package com.mfec.apidoc.service;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.mfec.apidoc.utils.ApplicationConfiguration;
import com.mfec.apidoc.utils.ApplicationConstants;
import com.mfec.apidoc.utils.FileUtils;

@Service("SwaggerDocumentService")
public class SwaggerDocumentServiceImpl implements SwaggerDocumentService {

	private static final Logger logger = LoggerFactory.getLogger(SwaggerDocumentServiceImpl.class);
	
	@Autowired
	private JSONPublisherService publisherService;
	
	@Autowired
	private ApplicationConfiguration appConfig;
	
	private List<File> swaggerFileList;
	
	public void swaggerScan(String fileFilter) throws IOException {
		logger.debug(">>> START : SwaggerDocumentServiceImpl.swaggerScan >>>");
		String INBOUND = appConfig.getInbound();
		swaggerFileList = StringUtils.isEmpty(fileFilter) ? FileUtils.listAllFile(INBOUND) : FileUtils.listAllFileByFilter(INBOUND, fileFilter);
		logger.debug("<<< END : SwaggerDocumentServiceImpl.swaggerScan <<<");
	}
	
	@Override
	public void generateAll() throws IOException {
		swaggerScan(null);
		convertSwagger2HTML();
	}

	@Override
	public void generateByName(String fileFilter) throws IOException {
		swaggerScan(fileFilter);
		convertSwagger2HTML();
	}
	
	private void convertSwagger2HTML() throws IOException {
		logger.debug(">>> START : SwaggerDocumentServiceImpl.convertSwagger2HTML >>>");
		
		String jsonFile = null;
		String INBOUND  = appConfig.getInbound();
		
		logger.info("##### START : Swagger2HTML Application : Generate the json swagger file to HTML. #####");
		for (File file : swaggerFileList) {
			jsonFile = INBOUND.concat(file.getName());
			ApplicationConstants.COUNT_IN_FILE ++;
			generateApiDocument(jsonFile, file);
			generateApiDictionary(jsonFile, file);
			ApplicationConstants.COUNT_OUT_FILE ++;
			logger.info(" ##### No." + ApplicationConstants.COUNT_IN_FILE + " :::INPUT : " + file.getName() + ".json::: :::OUTPUT : Generated API Ducment & API Dictionary [SUCCESS] #####");
		}
		
		logger.debug("<<< END : SwaggerDocumentServiceImpl.convertSwagger2HTML <<<");
	}

	private void generateApiDocument(String jsonFile, File file) throws IOException {
		String PREFIX        = "DOC_";
		String TEMPLATE      = appConfig.getTemplateApiDocs();
		String OUTBOUND      = appConfig.getOutbound();
		String MarkupFile    = file.getName().contains(".json") ? OUTBOUND.concat(PREFIX).concat(file.getName().replace(".json", ".html")) : OUTBOUND.concat(file.getName().concat(".html"));
		String MarkupContent = publisherService.publishJSONValue(jsonFile, TEMPLATE);
		FileUtils.writeContentToHTML(MarkupContent, MarkupFile);
	}
	
	private void generateApiDictionary(String jsonFile, File file) throws IOException {
		String PREFIX        = "DICT_";
		String TEMPLATE      = appConfig.getTemplateApiDict();
		String OUTBOUND      = appConfig.getOutbound();
		String MarkupFile    = file.getName().contains(".json") ? OUTBOUND.concat(PREFIX).concat(file.getName().replace(".json", ".html")) : OUTBOUND.concat(file.getName().concat(".html"));
		String MarkupContent = publisherService.publishJSONValue(jsonFile, TEMPLATE);
		FileUtils.writeContentToHTML(MarkupContent, MarkupFile);
	}
	
}
