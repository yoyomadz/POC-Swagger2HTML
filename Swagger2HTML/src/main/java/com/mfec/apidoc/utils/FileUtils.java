package com.mfec.apidoc.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.output.FileWriterWithEncoding;
import org.springframework.util.StringUtils;

public class FileUtils {
	
	public static List<String> listAllFileName(String path) {
		return listFileName(new File(path), null, ".json");
	}
	
	public static List<String> listAllFileNameByFilter(String path, String filter) {
		return listFileName(new File(path), filter, ".json");
	}
	
	public static List<File> listAllFile(String path) {
		return listFiles(new File(path), null, ".json");
	}
	
	public static List<File> listAllFileByFilter(String path, String filter) {
		return listFiles(new File(path), filter, ".json");
	}

	/**
	 * Get all file name in current path
	 * @param dir
	 * @param filter
	 * @param extension
	 * @return file name
	 */
	private static List<String> listFileName(final File dir, String filter, String extension) {
		List<String> files = new ArrayList<String>();
		for (final File file : dir.listFiles()) {
			if (file.isDirectory()) {
				listFileName(file, filter, extension);
			} else if (matchFilter(file.getName(), filter) && matchExtension(file.getName(), extension)) {
				files.add(file.getName());
			}
		}
		return files;
	}
	
	/**
	 * Get all file in current path
	 * @param dir
	 * @param filter
	 * @param extension
	 * @return file
	 */
	private static List<File> listFiles(final File dir, String filter, String extension) {
		List<File> files = new ArrayList<File>();
		for (final File file : dir.listFiles()) {
			if (file.isDirectory()) {
				listFiles(dir, filter, extension);
			} else if (matchFilter(file.getName(), filter) && matchExtension(file.getName(), extension)) {
				files.add(file);
			}
		}
		return files;
	}
	
	private static boolean matchFilter(String fileName, String filter) {
		return StringUtils.isEmpty(filter) || fileName.contains(filter);
	}
	
	private static boolean matchExtension(String fileName, String extension) {
		return StringUtils.isEmpty(extension) ? fileName.endsWith(".json") : fileName.endsWith(extension);
	}
	
	/**
	 * Generate swagger contents to markup (HTML)
	 * @param contents
	 * @param fileName
	 * @throws IOException
	 */
	public static void writeContentToHTML(String contents, String fileName) throws IOException {
		FileWriterWithEncoding fw = null;
		BufferedWriter bw = null;
		
		try {
			fw = new FileWriterWithEncoding(fileName, Charset.forName("UTF-8"));
			bw = new BufferedWriter(fw);
			bw.write(contents);
			
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		} finally {
			try {
				if (bw != null) bw.close();
				if (fw != null ) fw.close();
			} catch (IOException ex) {
				ex.printStackTrace();
				throw ex;
			}
		}
		
	}
}
