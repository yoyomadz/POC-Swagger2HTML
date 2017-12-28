package com.mfec.apidoc.service;

import java.io.IOException;

public interface JSONPublisherService {
	String publishJSONValue(String inputFile, String markupFile) throws IOException;
}
