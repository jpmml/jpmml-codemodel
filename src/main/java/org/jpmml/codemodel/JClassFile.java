/*
 * Copyright (c) 2018 Villu Ruusmann
 */
package org.jpmml.codemodel;

import java.io.IOException;
import java.io.OutputStream;

import com.sun.codemodel.JResourceFile;

public class JClassFile extends JResourceFile {

	private byte[] bytes = null;


	public JClassFile(String name){
		super(name);
	}

	@Override
	public void build(OutputStream os) throws IOException {
		byte[] bytes = getBytes();

		os.write(bytes, 0, bytes.length);
	}

	public byte[] getBytes(){
		return this.bytes;
	}

	public void setBytes(byte[] bytes){
		this.bytes = bytes;
	}
}