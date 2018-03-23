/*
 * Copyright (c) 2018 Villu Ruusmann
 */
package org.jpmml.codemodel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

import javax.tools.SimpleJavaFileObject;

public class ByteArrayClassFileObject extends SimpleJavaFileObject {

	private ByteArrayOutputStream buffer = new ByteArrayOutputStream();


	public ByteArrayClassFileObject(String name){
		super(URI.create("byte-array:///" + name), Kind.CLASS);
	}

	public byte[] toByteArray(){
		return this.buffer.toByteArray();
	}

	@Override
	public OutputStream openOutputStream() throws IOException {
		this.buffer.reset();

		return this.buffer;
	}
}