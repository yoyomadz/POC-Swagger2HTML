package com.mfec.apidoc.service;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Response;
import io.swagger.models.Swagger;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import io.swagger.parser.SwaggerParser;

@Service("JSONPublisherService")
public class JSONPublisherServiceImpl implements JSONPublisherService {
	
	private static final Logger logger = LoggerFactory.getLogger(JSONPublisherServiceImpl.class);
	
	protected VelocityEngine velocity;
	
	final char START_TAG			= '{';
	final char END_TAG				= '}';
	final char START_ARRAY_TAG		= '[';
	final char END_ARRAY_TAG		= ']';
	final char COLON_SYMBOL			= ':';
	final char COMMA_SYMBOL			= ',';
	final String DEF_PREFIX_ARRAY	= "_array";
	final String DEF_PREFIX_REF		= "_ref";
	
	public JSONPublisherServiceImpl() {
		velocity = new VelocityEngine();
		velocity.init();
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public String publishJSONValue(String inputFile, String template) throws IOException {
		logger.debug(">>> START : JSONPublisherServiceImpl.publishJSONValue >>>");
		
		StringBuilder jsonBuilder = new StringBuilder();
		VelocityContext context   = new VelocityContext();
		
		Swagger swagger = new SwaggerParser().read(inputFile);
		
		context.put("swagger", swagger);
		
		Map defMap = new HashMap<String, Object>();
		
		definitionFirstLayer2JSON(defMap, swagger);
		definitionSecondLayerArray2JSON(defMap, swagger);
		definitionSecondLayerRef2JSON(defMap, swagger);
		
		for (Path path : swagger.getPaths().values()) {
			
			Operation operation = null;
			if (path.getGet() != null) {
				operation = path.getGet();
			} else if (path.getPost() != null) {
				operation = path.getPost();
			}
			
			if (operation != null) {
				/** Response */
				if (operation.getResponses() != null) {
					for (Response response : operation.getResponses().values()) {
						if (response != null) {
							if (response.getExamples() != null) {
								jsonBuilder.delete(0, jsonBuilder.length());
								
								for (Object example : response.getExamples().values()) {
									
									if (example instanceof HashMap) {
										HashMap exampleMap = (HashMap) example;
										
										for (Object exampleKey : exampleMap.keySet()) {
											
											if (exampleKey instanceof String) {
												jsonBuilder.append(START_TAG).append(appendDoubleQuote((String) exampleKey)).append(COLON_SYMBOL).append(START_TAG);
												
												Object exampleSub = exampleMap.get(exampleKey);
												
												if (exampleSub instanceof ArrayList) {
													List exampleSubLst = (ArrayList) exampleSub;
													
													for (Object exampleSubALst : exampleSubLst) {
														if (exampleSubALst instanceof HashMap) {
															Map exampleSubMap = (HashMap) exampleSubALst;
															
															Iterator iter = exampleSubMap.entrySet().iterator();
															Object elem = null;
															while (iter.hasNext()) {
																elem = iter.next();
																if (elem instanceof Entry) {
																	Entry elemEntry = (Entry) elem;
																	jsonBuilder.append(appendDoubleQuote((String) elemEntry.getKey())).append(COLON_SYMBOL);
																	jsonBuilder.append(appendDoubleQuote((String) elemEntry.getValue())).append(COMMA_SYMBOL);
																	
																} else if (elem instanceof HashMap) {
																	HashMap elemMap = (HashMap) elem;
																	
																	for (Object elemKey : elemMap.keySet()) {
																		jsonBuilder.append(START_TAG).append(appendDoubleQuote((String) elemKey)).append(COLON_SYMBOL);
																		jsonBuilder.append(elemMap.get(elemKey));
																	}
																}
															}
															
															jsonBuilder.delete(jsonBuilder.length()-1, jsonBuilder.length());
															jsonBuilder.append(END_TAG);
															jsonBuilder.append(END_TAG);
															
															exampleMap.put("json", jsonBuilder.toString());
															jsonBuilder.delete(0, jsonBuilder.length());
														}
													}
												}
											}
									    }
									}
								}
							}
						}
					}
				}
			}
		}
		
		context.put("defMap", defMap);
		
		Writer writer = new StringWriter();
		velocity.mergeTemplate(template, "UTF-8", context, writer);
		writer.flush();
		
		logger.debug("<<< END : JSONPublisherServiceImpl.publishJSONValue <<<");
		return writer.toString();
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void definitionSecondLayerRef2JSON(Map outDefMap, Swagger inputSwagger) {
		logger.debug(">>> START : JSONPublisherServiceImpl.publishJSONValue >>>");
		
		StringBuilder jsonBuilder = new StringBuilder();
		Model model = null;
		Property property = null;
		ArrayProperty arrayProp = null;
		RefProperty refProp = null;
		
		if (outDefMap != null && inputSwagger != null && inputSwagger.getDefinitions() != null) {
			
			for (String defKey : inputSwagger.getDefinitions().keySet()) {
				
				if (outDefMap.get(defKey + DEF_PREFIX_REF) != null) {
					
					model = inputSwagger.getDefinitions().get(defKey);
					
					if (model.getProperties() != null) {
						
						jsonBuilder.delete(0, jsonBuilder.length());
						jsonBuilder.append(START_TAG);
						
						for (String propKey : model.getProperties().keySet()) {
							property = model.getProperties().get(propKey);
							
							if (property instanceof RefProperty) {
								refProp = (RefProperty) property;
								jsonBuilder.append(appendDoubleQuote(propKey)).append(COLON_SYMBOL);
								if (outDefMap.get(refProp.getSimpleRef()) != null) {
									jsonBuilder.append(outDefMap.get(refProp.getSimpleRef()));
								}
								jsonBuilder.append(COMMA_SYMBOL);
								
							} else if (property instanceof ArrayProperty) {
								arrayProp = (ArrayProperty) property;
								jsonBuilder.append(appendDoubleQuote(propKey)).append(COLON_SYMBOL);
								jsonBuilder.append(START_ARRAY_TAG);
								if (arrayProp.getItems() instanceof RefProperty) {
									refProp = (RefProperty) arrayProp.getItems();
									
									if (outDefMap.get(refProp.getSimpleRef()) != null) {
										jsonBuilder.append(outDefMap.get(refProp.getSimpleRef()));
									}
									
								}
								jsonBuilder.append(END_ARRAY_TAG).append(COMMA_SYMBOL);
								
							} else {
								jsonBuilder.append(appendDoubleQuote(propKey)).append(COLON_SYMBOL);
								jsonBuilder.append(appendDoubleQuote(property.getExample())).append(COMMA_SYMBOL);
							}
						}
					}
					if (jsonBuilder.length() > 0 && jsonBuilder.charAt(jsonBuilder.length()-1) == COMMA_SYMBOL) {
						jsonBuilder.delete(jsonBuilder.length()-1, jsonBuilder.length());
					}
					jsonBuilder.append(END_TAG);
					outDefMap.put(defKey, jsonBuilder.toString());
				}
			}
		}
		
		logger.debug("<<< END : JSONPublisherServiceImpl.publishJSONValue <<<");
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map definitionFirstLayer2JSON(Map outDefMap, Swagger inputSwagger) {
		logger.debug(">>> START : JSONPublisherServiceImpl.definitionFirstLayer2JSON >>>");
		
		StringBuilder jsonBuilder = new StringBuilder();
		Model model = null;
		Property property = null;
		ArrayProperty arrayProp = null;
		RefProperty refProp = null;
		
		if (outDefMap != null && inputSwagger != null && inputSwagger.getDefinitions() != null) {
			
			for (String defKey : inputSwagger.getDefinitions().keySet()) {
				jsonBuilder.delete(0, jsonBuilder.length());
				jsonBuilder.append(START_TAG);
				
				model = inputSwagger.getDefinitions().get(defKey);
				if (model.getProperties() != null) {
					for (String propKey : model.getProperties().keySet()) {
						property = model.getProperties().get(propKey);
						
						jsonBuilder.append(appendDoubleQuote(propKey)).append(COLON_SYMBOL);
						jsonBuilder.append(appendDoubleQuote(property.getExample())).append(COMMA_SYMBOL);
						
						if (property instanceof ArrayProperty) {
							arrayProp = (ArrayProperty) property;
							
							if (arrayProp.getItems() instanceof RefProperty) {
								refProp = (RefProperty) arrayProp.getItems();
								
								if (refProp.getSimpleRef() != null) {
									outDefMap.put(defKey + DEF_PREFIX_ARRAY, refProp.getSimpleRef());
								}
								
							}
							
						}
						
						else if (property instanceof RefProperty) {
							refProp = (RefProperty) property;
							
							if (refProp.getSimpleRef() != null) {
								outDefMap.put(defKey + DEF_PREFIX_REF, refProp.getSimpleRef());
							}
						}
						
					}
				}
				jsonBuilder.delete(jsonBuilder.length()-1, jsonBuilder.length());
				jsonBuilder.append(END_TAG);
				outDefMap.put(defKey, jsonBuilder.toString());
			}
		}
		
		logger.debug("<<< END : JSONPublisherServiceImpl.definitionFirstLayer2JSON <<<");
		return outDefMap;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map definitionSecondLayerArray2JSON(Map outDefMap, Swagger inputSwagger) {
		logger.debug(">>> START : JSONPublisherServiceImpl.definitionSecondLayerArray2JSON >>>");
		
		StringBuilder jsonBuilder = new StringBuilder();
		Model model = null;
		Property property = null;
		ArrayProperty arrayProp = null;
		RefProperty refProp = null;
		
		if (outDefMap != null && inputSwagger != null && inputSwagger.getDefinitions() != null) {
			
			for (String defKey : inputSwagger.getDefinitions().keySet()) {
				
				if (outDefMap.get(defKey + DEF_PREFIX_ARRAY) != null) {
					
					model = inputSwagger.getDefinitions().get(defKey);
					
					if (model.getProperties() != null) {
						
						jsonBuilder.delete(0, jsonBuilder.length());
						jsonBuilder.append(START_TAG);
						
						for (String propKey : model.getProperties().keySet()) {
							property = model.getProperties().get(propKey);
							
							if (property instanceof RefProperty) {
								refProp = (RefProperty) property;
								jsonBuilder.append(appendDoubleQuote(propKey)).append(COLON_SYMBOL);
								if (outDefMap.get(refProp.getSimpleRef()) != null) {
									jsonBuilder.append(outDefMap.get(refProp.getSimpleRef()));
								}
								jsonBuilder.append(COMMA_SYMBOL);
								
							} else if (property instanceof ArrayProperty) {
								arrayProp = (ArrayProperty) property;
								jsonBuilder.append(appendDoubleQuote(propKey)).append(COLON_SYMBOL);
								jsonBuilder.append(START_ARRAY_TAG);
								if (arrayProp.getItems() instanceof RefProperty) {
									refProp = (RefProperty) arrayProp.getItems();
									
									if (outDefMap.get(refProp.getSimpleRef()) != null) {
										jsonBuilder.append(outDefMap.get(refProp.getSimpleRef()));
									}
									
								}
								jsonBuilder.append(END_ARRAY_TAG).append(COMMA_SYMBOL);
								
							} else {
								jsonBuilder.append(appendDoubleQuote(propKey)).append(COLON_SYMBOL);
								jsonBuilder.append(appendDoubleQuote(property.getExample())).append(COMMA_SYMBOL);
							}
						}
					}
					if (jsonBuilder.length() > 0 && jsonBuilder.charAt(jsonBuilder.length()-1) == COMMA_SYMBOL) {
						jsonBuilder.delete(jsonBuilder.length()-1, jsonBuilder.length());
					}
					jsonBuilder.append(END_TAG);
					outDefMap.put(defKey, jsonBuilder.toString());
				}
			}
		}
		
		logger.debug("<<< END : JSONPublisherServiceImpl.definitionSecondLayerArray2JSON <<<");
		return outDefMap;
	}
	
	public static Object appendDoubleQuote(Object input) {
		if (input instanceof String) {
			return "\"" + input + "\"";
		}
		return input;
	}

}
