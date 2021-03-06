/**
 *  com.github.lecogiteur.csvbang.test.pool.MultiCsvFilePoolTest
 * 
 *  Copyright (C) 2013-2014  Tony EMMA
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
package com.github.lecogiteur.csvbang.test.pool;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import com.github.lecogiteur.csvbang.configuration.CsvBangConfiguration;
import com.github.lecogiteur.csvbang.exception.CsvBangException;
import com.github.lecogiteur.csvbang.factory.CsvFilePoolFactory;
import com.github.lecogiteur.csvbang.file.CsvFileContext;
import com.github.lecogiteur.csvbang.pool.CsvFilePool;
import com.github.lecogiteur.csvbang.pool.MultiCsvFilePool;
import com.github.lecogiteur.csvbang.pool.OneByOneCsvFilePool;
import com.github.lecogiteur.csvbang.pool.SimpleCsvFilePool;

/**
 * @author Tony EMMA
 * @version 0.1.0
 * @since 0.1.0
 */
@RunWith(BlockJUnit4ClassRunner.class)
public class CsvFilePoolTest {
	
	private class Getter implements Runnable{

		private final Integer millis;
		private final CsvFilePool pool;
		private final Map<Integer, Integer> count = new HashMap<Integer, Integer>();
		
		public Getter(Integer millis, CsvFilePool pool) {
			super();
			this.millis = millis;
			this.pool = pool;
		}

		@Override
		public void run() {
			try {
				Thread.sleep(millis);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			for (int i=0; i<10000; i++){
				try {
					CsvFileContext file = pool.getFile(10, 100);
					Assert.assertNotNull(file);
					int key = file.hashCode();
					int total = 10;
					if (count.containsKey(key)){
						total += count.get(key);
					}
					count.put(key, total);
				} catch (CsvBangException e) {
					Assert.fail("" + count.size());
				}
			}
		}

		public Map<Integer, Integer> getCount() {
			return count;
		}
		
		
	}
	
	private void threadSafeTest(CsvBangConfiguration conf, CsvFilePool pool, String name, boolean isNotSimple, long nbFile){
		System.out.println("Test name: " + name);
		Getter g1 = new Getter(20, pool);
		Getter g2 = new Getter(10, pool);
		Getter g3 = new Getter(0, pool);
		Thread t1 = new Thread(g1);
		Thread t2 = new Thread(g2);
		Thread t3 = new Thread(g3);
		
		t1.start();
		t2.start();
		t3.start();
		
		while (t1.isAlive() || t2.isAlive() || t3.isAlive()){};
		
		Map<Integer, Integer> c1 = g1.getCount();
		Map<Integer, Integer> c2 = g2.getCount();
		Map<Integer, Integer> c3 = g3.getCount();
		
		Assert.assertEquals(nbFile, pool.getAllFiles().size());
		List<Integer> nbRecords = new ArrayList<Integer>();
		for (CsvFileContext c:pool.getAllFiles()){
			int nbRecord = 0;
			if (c1.containsKey(c.hashCode())){
				nbRecord += c1.get(c.hashCode());
				System.out.println(c.hashCode() + " --> t1 : " + c1.get(c.hashCode()));
			}
			if (c2.containsKey(c.hashCode())){
				nbRecord += c2.get(c.hashCode());
				System.out.println(c.hashCode() + " --> t2 : " + c2.get(c.hashCode()));
			}
			if (c3.containsKey(c.hashCode())){
				nbRecord += c3.get(c.hashCode());
				System.out.println(c.hashCode() + " --> t3 : " + c3.get(c.hashCode()));
			}
			nbRecords.add(nbRecord);
		}
		

		if(isNotSimple){
			for (int nbRecord:nbRecords){
				Assert.assertEquals(conf.maxRecordByFile, nbRecord);
			}

			try{
				pool.getFile(54, 800000);
			}catch(CsvBangException e){
				return;
			}
			Assert.fail("Maximum of file");
		}else{
			for (int nbRecord:nbRecords){
				Assert.assertEquals(300000, nbRecord);
			}
		}
	}

	@Test
	public void threadsafeMultiFileTest() throws CsvBangException{
		CsvBangConfiguration conf = new CsvBangConfiguration();
		conf.maxFile = 3;
		conf.maxRecordByFile = 100000l;
		conf.maxFileSize = 100000000l;
		conf.isFileByFile = false;
		conf.init();
		CsvFilePool pool = CsvFilePoolFactory.createPool(conf, (File)null, null, null);
		Assert.assertNotNull(pool);
		Assert.assertTrue(pool instanceof MultiCsvFilePool);
		Assert.assertEquals(0, pool.getAllFiles().size());
		
		threadSafeTest(conf, pool, "MultiCsvFilePool", true, conf.maxFile);
	}
	
	@Test
	public void threadsafeOneByOneTest() throws CsvBangException{
		final CsvBangConfiguration conf = new CsvBangConfiguration();
		conf.maxRecordByFile = 100000l;
		conf.maxFileSize = 100000000l;
		conf.maxFile = 3;
		conf.init();
		final CsvFilePool pool = CsvFilePoolFactory.createPool(conf, (File)null, null, null);
		Assert.assertNotNull(pool);
		Assert.assertTrue(pool instanceof OneByOneCsvFilePool);
		Assert.assertEquals(0, pool.getAllFiles().size());
		
		
		threadSafeTest(conf, pool, "OneByOneCsvFilePool", true, conf.maxFile);
	}
	
	@Test
	public void threadsafeSimpleTest() throws CsvBangException{
		final CsvBangConfiguration conf = new CsvBangConfiguration();
		conf.init();
		final CsvFilePool pool = CsvFilePoolFactory.createPool(conf, (File)null, null, null);
		Assert.assertNotNull(pool);
		Assert.assertTrue(pool instanceof SimpleCsvFilePool);
		Assert.assertEquals(1, pool.getAllFiles().size());
		
		
		threadSafeTest(conf, pool, "SimpleCsvFilePool", false, 1);
	}
}
