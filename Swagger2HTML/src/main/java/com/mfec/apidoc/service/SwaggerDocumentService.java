package com.mfec.apidoc.service;

import java.io.IOException;

public interface SwaggerDocumentService {
	void generateAll() throws IOException;
	void generateByName(String filter) throws IOException;
}
