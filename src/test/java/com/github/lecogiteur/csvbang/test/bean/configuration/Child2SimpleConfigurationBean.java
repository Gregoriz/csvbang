/**
 *  com.github.lecogiteur.csvbang.test.bean.configuration.Child2SimpleConfigurationBean
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
package com.github.lecogiteur.csvbang.test.bean.configuration;

import java.util.Calendar;

import com.github.lecogiteur.csvbang.annotation.CsvComment;
import com.github.lecogiteur.csvbang.annotation.CsvComment.DIRECTION;
import com.github.lecogiteur.csvbang.annotation.CsvField;
import com.github.lecogiteur.csvbang.annotation.CsvFile;
import com.github.lecogiteur.csvbang.annotation.CsvFooter;
import com.github.lecogiteur.csvbang.annotation.CsvFormat;
import com.github.lecogiteur.csvbang.annotation.CsvHeader;
import com.github.lecogiteur.csvbang.annotation.CsvType;
import com.github.lecogiteur.csvbang.annotation.CsvFormat.TYPE_FORMAT;

/**
 * @author Tony EMMA
 *
 */
@CsvType(charsetName="ISO-8859-1", delimiter="||", 
		endRecord="\nEND\n", quoteCharacter="'", 
		quoteEscapeCharacter='\'', startRecord="*", commentCharacter='%')
@CsvHeader(header=true)
@CsvFooter(noEndRecordOnLastRecord=true)
@CsvFile(append=true, asynchronousWriter=true, blocksize=20, fileName="test.csv")
public class Child2SimpleConfigurationBean extends SimpleConfigurationBean{

	/**
	 * {@inheritDoc}
	 * @see com.github.lecogiteur.csvbang.test.bean.configuration.SimpleConfigurationBean#getName()
	 */
	@Override
	@CsvComment(direction=DIRECTION.AFTER_RECORD)
	@CsvField(name="The Name", position=5)
	public String getName() {
		return super.getName();
	}

	
	
	/**
	 * {@inheritDoc}
	 * @see com.github.lecogiteur.csvbang.test.bean.configuration.SimpleConfigurationBean#getDate()
	 */
	@Override
	@CsvField(position=-1, deleteIfNull=true, defaultIfNull="No Date")
	@CsvFormat(type=TYPE_FORMAT.NONE)
	public Calendar getDate() {
		return super.getDate();
	}



	public boolean isOld(){
		return old;
	}



	/**
	 * {@inheritDoc}
	 * @see com.github.lecogiteur.csvbang.test.bean.configuration.SimpleConfigurationBean#customMethod()
	 */
	@Override
	@CsvField(defaultIfNull="0")
	public double customMethod() {
		return super.customMethod();
	}
	
}
