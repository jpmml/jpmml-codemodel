/*
 * Copyright (c) 2018 Villu Ruusmann
 */
package org.jpmml.codemodel;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.sun.codemodel.JResourceFile;

public class JClassFile extends JResourceFile implements Streamable {

	private byte[] bytes = null;


	public JClassFile(String name, byte[] bytes){
		super(name);

		setBytes(bytes);
	}

	@Override
	public void build(OutputStream os) throws IOException {
		byte[] bytes = getBytes();

		os.write(bytes, 0, bytes.length);
	}

	@Override
	public InputStream getInputStream(){
		byte[] bytes = getBytes();

		return new ByteArrayInputStream(bytes);
	}

	public byte[] getBytes(){
		return this.bytes;
	}

	private void setBytes(byte[] bytes){
		this.bytes = bytes;
	}
}