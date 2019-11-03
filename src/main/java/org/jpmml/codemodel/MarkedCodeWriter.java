/*
 * Copyright (c) 2019 Villu Ruusmann
 */
package org.jpmml.codemodel;

import java.io.IOException;
import java.io.Writer;

import com.sun.codemodel.CodeWriter;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.writer.FilterCodeWriter;

public class MarkedCodeWriter extends FilterCodeWriter {

	private String header = null;


	public MarkedCodeWriter(CodeWriter codeWriter, String header){
		super(codeWriter);

		setHeader(header);
	}

	@Override
	public Writer openSource(JPackage _package, String name) throws IOException {
		Writer writer = super.openSource(_package, name);

		String header = getHeader();
		if(header != null){
			writer.write(header);
		}

		return writer;
	}

	public String getHeader(){
		return this.header;
	}

	private void setHeader(String header){
		this.header = header;
	}
}