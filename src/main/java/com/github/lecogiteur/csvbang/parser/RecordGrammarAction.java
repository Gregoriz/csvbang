/**
 *  com.github.lecogiteur.csvbang.parser.RecordGrammarAction
 * 
 *  Copyright (C) 2013-2015  Tony EMMA
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
package com.github.lecogiteur.csvbang.parser;

import java.util.ArrayList;
import java.util.List;

import com.github.lecogiteur.csvbang.configuration.CsvBangConfiguration;
import com.github.lecogiteur.csvbang.configuration.CsvFieldConfiguration;
import com.github.lecogiteur.csvbang.exception.CsvBangException;
import com.github.lecogiteur.csvbang.util.CsvbangUti;
import com.github.lecogiteur.csvbang.util.ReflectionUti;

/**
 * Generate a CSV record
 * @author Tony EMMA
 * @version 1.0.0
 * @since 1.0.0
 */
public class RecordGrammarAction<T> implements CsvGrammarAction<T> {
	
	/**
	 * Start offset of this action in CSV file
	 * @since 1.0.0
	 */
	private long startOffset = -1;
	
	/**
	 * End offset of this action in CSV file
	 * @since 1.0.0
	 */
	private long endOffset = -1;
	
	/**
	 * List of fields
	 * @since 1.0.0
	 */
	private final List<FieldGrammarAction> fields;
	
	/**
	 * CSV bean class
	 * @since 1.0.0
	 */
	private final Class<T> beanClass;
	
	/**
	 * CsvBang configuration for the CSV bean
	 * @since 1.0.0
	 */
	private final CsvBangConfiguration conf;
	
	/**
	 * Last record of file. Warning: if file have a footer or comment after last record, this variable is at false.  
	 * @since 1.0.0
	 */
	private boolean isTerminatedRecord = false;

	/**
	 * Constructor
	 * @param beanClass
	 * @param conf
	 * @since 1.0.0
	 */
	public RecordGrammarAction(final Class<T> beanClass, final CsvBangConfiguration conf) {
		super();
		this.beanClass = beanClass;
		this.conf = conf;
		this.fields = new ArrayList<FieldGrammarAction>();
		this.fields.add(new FieldGrammarAction(100));
	}

	/**
	 * {@inheritDoc}
	 * @see com.github.lecogiteur.csvbang.parser.CsvGrammarAction#getType()
	 * @since 1.0.0
	 */
	@Override
	public CsvGrammarActionType getType() {
		return CsvGrammarActionType.RECORD;
	}

	/**
	 * {@inheritDoc}
	 * @see com.github.lecogiteur.csvbang.parser.CsvGrammarAction#add(byte)
	 * @since 1.0.0
	 */
	@Override
	public void add(byte b) throws CsvBangException {
		if (fields.size() == 1){
			fields.get(0).add(b);
			return;
		}
		throw new CsvBangException(String.format("You can't add byte [%s] to a record.", b));
	}

	/**
	 * {@inheritDoc}
	 * @see com.github.lecogiteur.csvbang.parser.CsvGrammarAction#add(com.github.lecogiteur.csvbang.parser.CsvGrammarAction)
	 * @since 1.0.0
	 */
	@Override
	public boolean add(final CsvGrammarAction<?> word) throws CsvBangException {
		if (word != null){
			switch (word.getType()) {
			case FIELD:
				//add a field
				fields.add((FieldGrammarAction)word);
				endOffset = word.getEndOffset();
				isTerminatedRecord = isTerminatedRecord || word.isLastAction();
				return true;
			case QUOTE:
				if (fields.size() == 1){
					fields.get(0).add(word);
					endOffset = word.getEndOffset();
					return true;
				}
				return false;
			case FOOTER:
				final FooterGrammarAction foot = (FooterGrammarAction) word;
				if (fields.size() < conf.fields.size()){
					//the number of field of last record of file is invalid. So it's a part of footer.
					final StringBuilder s = new StringBuilder();
					for (final FieldGrammarAction field:fields){
						final String c = field.execute();
						if (c!=null){
							s.append(conf.delimiter).append(c);
						}
					}
					s.delete(0, conf.delimiter.length());
					foot.addBefore(s);
					foot.setStartOffset(fields.get(0).getStartOffset());
					endOffset = foot.getEndOffset();
					fields.clear();
				}else if (fields.size() == conf.fields.size()){
					//we verify if the last field of last record can be set
					final T bean = newInstance();
					boolean b = true;
					int size = 0;
					final int idx = fields.size()-1;
					while(b){
						try{
							setField(idx, bean);
							b = false;
						}catch(CsvBangException e){
							final Character a = fields.get(idx).deleteLastChar();
							if (a == null){
								b = false;
							}else{
								size++;
								foot.addBefore(a);
							}
						}
					}
					foot.setStartOffset(fields.get(idx).getEndOffset() - size);
					fields.get(idx).setEndOffset(foot.getEndOffset());
					endOffset = foot.getEndOffset();
				}
				foot.terminateSetFooterContent();
				return false;
			case END:
				isTerminatedRecord = isTerminatedRecord || word.isLastAction();
				return true;
			default:
				return false;
			}
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 * @see com.github.lecogiteur.csvbang.parser.CsvGrammarAction#isActionCompleted(com.github.lecogiteur.csvbang.parser.CsvGrammarActionType)
	 * @since 1.0.0
	 */
	@Override
	public boolean isActionCompleted(final CsvGrammarActionType next) {
		return (isTerminatedRecord || conf.fields.size() == fields.size() || CsvGrammarActionType.RECORD.equals(next) 
				|| CsvGrammarActionType.END.equals(next) || CsvGrammarActionType.COMMENT.equals(next)) 
				&& !CsvGrammarActionType.FOOTER.equals(next);
	}

	/**
	 * {@inheritDoc}
	 * @see com.github.lecogiteur.csvbang.parser.CsvGrammarAction#execute()
	 * @since 1.0.0
	 */
	@Override
	//TODO vérifier que les beans CSV ont un constructeur par défaut
	//TODO créer un paramètre pour désactiver la lecture des commentaires
	public T execute() throws CsvBangException {
		T bean = null;
		boolean isNull = true;
		if (CsvbangUti.isCollectionNotEmpty(fields)){
			if (fields.size() == 1 && fields.get(0).execute() == null){
				return null;
			}
			if (conf.fields.size() != fields.size()){
				throw new CsvBangException(String.format("A problem has occurred between offset [%s] and [%s] for a record. The number of fields configurated [%s] is different than the number of field find in file [%s] for type [%s].", 
						startOffset, endOffset, conf.fields.size(), fields.size(), beanClass));
			}
			
			//create new CSV bean
			bean = newInstance();
			
			//set all field to CSV bean
			for (int i=0; i<conf.fields.size(); i++){
				isNull = setField(i, bean) && isNull;
			}
		}
		return isNull?null:bean;
	}
	
	/**
	 * Set a field to the CSV bean
	 * @param fieldIndex field index
	 * @param bean the CSV bean
	 * @return true if the field is null
	 * @throws CsvBangException if a problem has occurred when we set tthe field to the CSV bean
	 * @since 1.0.0
	 */
	private boolean setField(final int fieldIndex, final T bean) throws CsvBangException{
		final CsvFieldConfiguration confField = conf.fields.get(fieldIndex);
		final CsvGrammarAction<?> field = fields.get(fieldIndex);
		final Object content = field.execute();
		ReflectionUti.setValue(confField.setter, confField.generator, bean, field.execute());
		return content == null;
	}
	
	/**
	 * Return a new instance of CSV bean
	 * @return the CSV bean
	 * @throws CsvBangException if a problem has occurred whe we create the new instance.
	 * @since 1.0.0
	 */
	private T newInstance() throws CsvBangException{
		try {
			return beanClass.newInstance();
		} catch (InstantiationException e) {
			throw new CsvBangException(String.format("A problem has occurred when we instantiate the CSV type [%s]. Record between offset [%s] and [%s].", startOffset, endOffset, beanClass));
		} catch (IllegalAccessException e) {
			throw new CsvBangException(String.format("A problem has occurred when we instantiate the CSV type [%s]. Record between offset [%s] and [%s].", startOffset, endOffset, beanClass));
		}
	}

	/**
	 * {@inheritDoc}
	 * @see com.github.lecogiteur.csvbang.parser.CsvGrammarAction#getStartOffset()
	 * @since 1.0.0
	 */
	@Override
	public long getStartOffset() {
		return startOffset;
	}

	/**
	 * {@inheritDoc}
	 * @see com.github.lecogiteur.csvbang.parser.CsvGrammarAction#getEndOffset()
	 * @since 1.0.0
	 */
	@Override
	public long getEndOffset() {
		return endOffset;
	}

	/**
	 * {@inheritDoc}
	 * @see com.github.lecogiteur.csvbang.parser.CsvGrammarAction#setStartOffset(long)
	 * @since 1.0.0
	 */
	@Override
	public void setStartOffset(long offset) {
		startOffset = offset;
	}

	/**
	 * {@inheritDoc}
	 * @see com.github.lecogiteur.csvbang.parser.CsvGrammarAction#setEndOffset(long)
	 * @since 1.0.0
	 */
	@Override
	public void setEndOffset(long offset) {
		endOffset = offset;
	}

	/**
	 * {@inheritDoc}
	 * @see com.github.lecogiteur.csvbang.parser.CsvGrammarAction#isLastAction()
	 * @since 1.0.0
	 */
	@Override
	public boolean isLastAction() {
		return isTerminatedRecord;
	}


	/**
	 * {@inheritDoc}
	 * @see com.github.lecogiteur.csvbang.parser.CsvGrammarAction#isChuck(com.github.lecogiteur.csvbang.parser.CsvGrammarActionType, byte[])
	 * @since 1.0.0
	 */
	@Override
	public boolean isChuck(final CsvGrammarActionType next, final byte[] keyword) {
		return false;
	}
}
