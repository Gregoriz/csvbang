/**
 *  com.github.lecogiteur.csvbang.test.formatter.DateCsvFormatterTest
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
package com.github.lecogiteur.csvbang.test.formatter;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import com.github.lecogiteur.csvbang.formatter.CsvFormatter;
import com.github.lecogiteur.csvbang.formatter.DateCsvFormatter;


/**
 * @author Tony EMMA
 *
 */
@RunWith(BlockJUnit4ClassRunner.class)
public class DateCsvFormatterTest {

	SimpleDateFormat sf = new SimpleDateFormat("dd/MM/yyyy");
	
	@Test
	public void dateTest() throws ParseException{
		CsvFormatter format = new DateCsvFormatter();
		format.init();
		
		Date date = sf.parse("11/05/2013");
		Calendar calendar = Calendar.getInstance(Locale.US);
		calendar.setTime(date);
		
		Timestamp timestamp = new Timestamp(date.getTime());
		Long longValue = date.getTime();
		
		Assert.assertEquals("default", format.format(null, "default"));
		
		try{
			Assert.assertEquals("default", format.format("string", "default"));
			Assert.fail();
		}catch(Exception e){
			//good
		}
		Assert.assertEquals("05/11/2013", format.format(date, "default"));
		Assert.assertEquals("05/11/2013", format.format(calendar, "default"));
		Assert.assertEquals("05/11/2013", format.format(timestamp, "default"));
		Assert.assertEquals("05/11/2013", format.format(longValue, "default"));
	}
	
	@Test
	public void dateWithPatternTest() throws ParseException{
		CsvFormatter format = new DateCsvFormatter();
		format.setPattern("dd-MM-yyyy");
		format.init();
		
		Date date = sf.parse("11/05/2013");
		Calendar calendar = Calendar.getInstance(Locale.US);
		calendar.setTime(date);
		
		Timestamp timestamp = new Timestamp(date.getTime());
		Long longValue = date.getTime();
		
		Assert.assertEquals("default", format.format(null, "default"));
		Assert.assertEquals("11-05-2013", format.format(date, "default"));
		Assert.assertEquals("11-05-2013", format.format(calendar, "default"));
		Assert.assertEquals("11-05-2013", format.format(timestamp, "default"));
		Assert.assertEquals("11-05-2013", format.format(longValue, "default"));
	}
	
	@Test
	public void dateParseTest() throws ParseException{
		CsvFormatter format = new DateCsvFormatter();
		format.init();
		
		Date date = sf.parse("11/05/2013");
		Calendar calendar = Calendar.getInstance(Locale.US);
		calendar.setTime(date);
		
		Timestamp timestamp = new Timestamp(date.getTime());
		Long longValue = date.getTime();
		
		Assert.assertNull(format.parse(null, Calendar.class));
		Assert.assertNull(format.parse(null, Timestamp.class));
		Assert.assertNull(format.parse(null, Date.class));
		
		try{
			format.parse("string", Calendar.class);
			Assert.fail();
		}catch(Exception e){
			//good
		}
		Assert.assertEquals(date, format.parse("05/11/2013", Date.class));
		Assert.assertEquals(calendar, format.parse("05/11/2013", Calendar.class));
		Assert.assertEquals(timestamp, format.parse("05/11/2013", Timestamp.class));
		Assert.assertEquals(longValue, format.parse("05/11/2013", Long.class));
	}
	
	@Test
	public void dateParseWithPatternTest() throws ParseException{
		CsvFormatter format = new DateCsvFormatter();
		format.setPattern("dd-MM-yyyy");
		format.init();
		
		Date date = sf.parse("11/05/2013");
		Calendar calendar = Calendar.getInstance(Locale.US);
		calendar.setTime(date);
		
		Timestamp timestamp = new Timestamp(date.getTime());
		Long longValue = date.getTime();
		
		Assert.assertNull(format.parse(null, Calendar.class));
		Assert.assertNull(format.parse("", Timestamp.class));
		Assert.assertNull(format.parse("  ", Date.class));
		

		try{
			format.parse("string", Calendar.class);
			Assert.fail();
		}catch(Exception e){
			//good
		}
		
		Assert.assertEquals(date, format.parse("11-05-2013", Date.class));
		Assert.assertEquals(calendar, format.parse("11-05-2013", Calendar.class));
		Assert.assertEquals(timestamp, format.parse("11-05-2013", Timestamp.class));
		Assert.assertEquals(longValue, format.parse("11-05-2013", Long.class));
	}

}
