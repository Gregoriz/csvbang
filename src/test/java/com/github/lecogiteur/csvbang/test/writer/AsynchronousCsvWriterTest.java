package com.github.lecogiteur.csvbang.test.writer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import com.github.lecogiteur.csvbang.exception.CsvBangException;
import com.github.lecogiteur.csvbang.factory.FactoryCsvWriter;
import com.github.lecogiteur.csvbang.test.bean.writer.AsynchronousCsvWriterBean;
import com.github.lecogiteur.csvbang.writer.AsynchronousCsvWriter;
import com.github.lecogiteur.csvbang.writer.CsvWriter;

@RunWith(BlockJUnit4ClassRunner.class)
public class AsynchronousCsvWriterTest {

	private class Writer<T> implements Runnable{
		private final CsvWriter<T> w;
		private final Integer millis;
		private final Integer nbSamples;
		private final T[] samples;
		private long nbWriting = 0;
		private boolean fail = false;

		public Writer(CsvWriter<T> w, Integer millis, Integer nbSamples, T[] samples) {
			super();
			this.w = w;
			this.millis = millis;
			this.nbSamples = nbSamples;
			this.samples = samples;
		}


		@Override
		public void run() {
			try {
				Thread.sleep(millis);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			for(int i=0; i<nbSamples; i++){
				try {
					w.write(samples);
					nbWriting += samples.length;
				} catch (CsvBangException e) {
					e.printStackTrace();
					fail = true;
				}
			}
		}

		public long getNbWriting() {
			return nbWriting;
		}

		public boolean isFail() {
			return fail;
		}
		
		
	}

	@Rule
	public TemporaryFolder testFolder = new TemporaryFolder();
	
	@Test
	public void simpleWrite() throws CsvBangException, IOException{
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
		Calendar c1 = Calendar.getInstance();
		String f1 = format.format(c1.getTime());
		FactoryCsvWriter factory = new FactoryCsvWriter("com.github.lecogiteur.csvbang.test.bean.writer");
		File folder = testFolder.newFolder();
		System.out.println("Folder: " + folder.getAbsolutePath());
		Calendar c2 = Calendar.getInstance();
		String f2 = format.format(c2.getTime());
		
		CsvWriter<AsynchronousCsvWriterBean> writer = factory.createCsvWriter(AsynchronousCsvWriterBean.class, folder);
		Assert.assertNotNull(writer);
		Assert.assertTrue(writer instanceof AsynchronousCsvWriter);
		Calendar c3 = Calendar.getInstance();
		String f3 = format.format(c3.getTime());
		
		Writer<AsynchronousCsvWriterBean> w1 = new Writer<AsynchronousCsvWriterBean>(writer, 20, 10000, 
				new AsynchronousCsvWriterBean[]{new AsynchronousCsvWriterBean("name1W1", "value1W1", c1), 
				new AsynchronousCsvWriterBean("name2W1", "value2W1", c1)});
		
		Writer<AsynchronousCsvWriterBean> w2 = new Writer<AsynchronousCsvWriterBean>(writer, 10, 5000, 
				new AsynchronousCsvWriterBean[]{new AsynchronousCsvWriterBean("name1W2", "value1W2", c2), 
				new AsynchronousCsvWriterBean("name2W2", "value2W2", c2), new AsynchronousCsvWriterBean("name3W2", "value3W2", c2)});
		
		Writer<AsynchronousCsvWriterBean> w3 = new Writer<AsynchronousCsvWriterBean>(writer, 0, 15000, 
				new AsynchronousCsvWriterBean[]{new AsynchronousCsvWriterBean("name1W3", "value1W3", c3)});
		
		Thread t1 = new Thread(w1);
		Thread t2 = new Thread(w2);
		Thread t3 = new Thread(w3);
		
		t1.start();
		t2.start();
		t3.start();
		
		while (t1.isAlive() || t2.isAlive() || t3.isAlive()){}
		
		writer.close();
		
		Assert.assertFalse(w1.isFail());
		Assert.assertFalse(w2.isFail());
		Assert.assertFalse(w3.isFail());
		
		Assert.assertEquals(50000, w1.getNbWriting() +  w2.getNbWriting() + w3.getNbWriting() );
		File[] files = folder.listFiles();
		Assert.assertNotNull(files);
		Assert.assertEquals(3, files.length);
		
		//file name
		ArrayList<String> names = new ArrayList<String>();
		Map<String, Integer> count = new HashMap<String, Integer>();
		long nbLines = 0;
		for (File f:files){
			names.add(f.getName());
			FileReader file = new FileReader(f);
			BufferedReader br = new BufferedReader(file);
			int nb = 0;
			//header
			Assert.assertEquals("Name,Value,Date", br.readLine());
			
			String line = br.readLine();
			while (line != null){
				++nbLines;
				++nb;
				int value = 1;
				if (count.containsKey(line)){
					value += count.get(line);
				}
				count.put(line, value);
				line = br.readLine();
			}
			
			Assert.assertTrue(18000 >=  nb);
			br.close();
			file.close();
		}
		
		Assert.assertEquals(50000, nbLines);
		Assert.assertEquals(6, count.size());
		Assert.assertEquals(5000, count.get("name1W2,value1W2,"+f2).intValue());
		Assert.assertEquals(5000, count.get("name2W2,value2W2,"+f2).intValue());
		Assert.assertEquals(5000, count.get("name3W2,value3W2,"+f2).intValue());
		Assert.assertEquals(10000, count.get("name1W1,value1W1,"+f1).intValue());
		Assert.assertEquals(10000, count.get("name2W1,value2W1,"+f1).intValue());
		Assert.assertEquals(15000, count.get("name1W3,value1W3,"+f3).intValue());
		
		Assert.assertTrue(names.contains("async-1.csv"));
		Assert.assertTrue(names.contains("async-2.csv"));
		Assert.assertTrue(names.contains("async-3.csv"));
	}
}
