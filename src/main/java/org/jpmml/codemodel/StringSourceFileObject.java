/*
 * Copyright (c) 2018 Villu Ruusmann
 */
package org.jpmml.codemodel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Objects;

import javax.tools.SimpleJavaFileObject;

public class StringSourceFileObject extends SimpleJavaFileObject {

	private String encoding = null;

	private ByteArrayOutputStream buffer = new ByteArrayOutputStream(){

		@Override
		public void close() throws IOException {
			super.close();
		}
	};


	public StringSourceFileObject(String name){
		this(name, null);
	}

	public StringSourceFileObject(String name, String encoding){
		super(URI.create("string:///" + Objects.requireNonNull(name)), Kind.SOURCE);

		setEncoding(encoding);
	}

	@Override
	public String getCharContent(boolean ignoreEncodingErrors) throws IOException {
		String string;

		String encoding = getEncoding();
		if(encoding != null){
			string = this.buffer.toString(encoding);
		} else

		{
			string = this.buffer.toString();
		}

		return string;
	}

	@Override
	public ByteArrayOutputStream openOutputStream(){
		this.buffer.reset();

		return this.buffer;
	}

	public String getEncoding(){
		return this.encoding;
	}

	public void setEncoding(String encoding){
		this.encoding = encoding;
	}
}