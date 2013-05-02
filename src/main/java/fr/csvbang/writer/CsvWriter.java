/**
 *  fr.csvbang.writer.CsvWriter
 * 
 *  Copyright (C) 2013  Tony EMMA
 *
 *  This file is part of Csvbang.
 *  
 *  Csvbang is a comma-separated values ( CSV ) API, written in JAVA and thread-safe.
 *
 *  Csvbang is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *   
 *  Csvbang is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *   
 *  You should have received a copy of the GNU General Public License
 *  along with Csvbang. If not, see <http://www.gnu.org/licenses/>.
 */
package fr.csvbang.writer;

import java.util.Collection;

import fr.csvbang.exception.CsvBangException;

/**
 * Writer of CSV file
 * @author Tony EMMA
 *
 */
public interface CsvWriter<T> {
	
	
	/**
	 * Create CSV file and write header
	 * @throws CsvBangException if a problem occurred during creation of file
	 * 
	 * @author Tony EMMA
	 */
	public void open() throws CsvBangException;
	
	/**
	 * Write a line in file
	 * @param line a line
	 * @throws CsvBangException if a problem occurred during writing file
	 * 
	 * @author Tony EMMA
	 */
	public void write(T line) throws CsvBangException;
	
	/**
	 * Write lines in file
	 * @param lines lines
	 * @throws CsvBangException if a problem occurred during writing file
	 * 
	 * @author Tony EMMA
	 */
	public void write(T[] lines) throws CsvBangException;
	
	/**
	 * Write lines in file
	 * @param lines lines
	 * @throws CsvBangException if a problem occurred during writing file
	 * 
	 * @author Tony EMMA
	 */
	public void write(Collection<T> lines) throws CsvBangException;
	
	/**
	 * Close file and write footer
	 * @throws CsvBangException if a problem occurred during closing file
	 * 
	 * @author Tony EMMA
	 */
	public void close() throws CsvBangException;
	
	
	

}