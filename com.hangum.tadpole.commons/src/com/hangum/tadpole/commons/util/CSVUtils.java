/*******************************************************************************
 * Copyright (c) 2012 - 2015 hangum.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     hangum - initial API and implementation
 ******************************************************************************/
package com.hangum.tadpole.commons.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;


/**
 * CSV file util
 *
 *
 * @author hangum
 * @version 1.6.1
 * @since 2015. 5. 31.
 *
 */
public class CSVUtils {
	private static final Logger logger = Logger.getLogger(CSVUtils.class);
	
	public static String makeData(List<String[]> listContent) throws Exception {
		return makeData(listContent, CSVWriter.DEFAULT_SEPARATOR);
	}
			
	/**
	 * csv data
	 * @param seprator 
	 * 
	 * @param 
	 */
	public static String makeData(List<String[]> listContent, char seprator) throws Exception {
		String strReust = "";
		
		StringWriter sw = new StringWriter();
		CSVWriter writer = null;
//		// Excel does not recongize the UTF-8, add additional header (BOM) for excel
//		OutputStream outputStream = new ByteArrayOutputStream();
//		outputStream.write(0xEF);   
//		outputStream.write(0xBB);
//		outputStream.write(0xBF);   
//		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
		
		try {
			writer = new CSVWriter(sw, seprator);
			writer.writeAll(listContent);
			
			strReust = sw.getBuffer().toString();
		} finally {
			if(writer != null) try { writer.close(); } catch(Exception e) {}
			sw.close();
		}
		
		return strReust;
	}
	
	/**
	 * csv file reader
	 * 
	 * @param filename
	 * @param seprator
	 * @return
	 * @throws Exception
	 */
	public static List<String[]> readFile(String filename, char seprator) {
		List<String[]> returnMap = new ArrayList<String[]>();
		String[] nextLine = null;
		CSVReader csvReader = null;
		try {
			 csvReader = new CSVReader(new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF8")), seprator);
			 while((nextLine = csvReader.readNext()) != null) {
				returnMap.add(nextLine);
			 }
		} catch(Exception e) {
			logger.error("CSV read error", e);
		} finally {
			if(csvReader != null) try { csvReader.close(); } catch(Exception e) {}
		}
		
		return returnMap;
	}
	
	/**
	 * csv reader
	 * 
	 * @param filename
	 * @param seprator
	 * @return
	 * @throws Exception
	 */
	public static List<String[]> readData(String strData, char seprator) {
		List<String[]> returnMap = new ArrayList<String[]>();
		String[] nextLine = null;
		CSVReader csvReader = null;
		try {
			csvReader = new CSVReader(new BufferedReader(new StringReader(strData)), seprator);
			while((nextLine = csvReader.readNext()) != null) {
				returnMap.add(nextLine);
			}
		} catch(Exception e) {
			logger.error("CSV read error", e);
		} finally {
			if(csvReader != null) try { csvReader.close(); } catch(Exception e) {}
		}
		
		return returnMap;
	}
	
}
