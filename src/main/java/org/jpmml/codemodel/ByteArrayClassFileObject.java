/*
 * Copyright (c) 2018 Villu Ruusmann
 */
package org.jpmml.codemodel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ByteArrayClassFileObject extends ClassFileObject {

	private ByteArrayOutputStream buffer = new ByteArrayOutputStream();


	public ByteArrayClassFileObject(String name){
		super("byte-array", name);
	}

	public JClassFile toClassFile(){
		return new JClassFile(getSimpleName() + ".class", toByteArray());
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