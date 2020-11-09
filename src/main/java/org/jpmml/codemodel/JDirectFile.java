/*
 * Copyright (c) 2018 Villu Ruusmann
 */
package org.jpmml.codemodel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

import com.google.common.io.ByteStreams;
import com.sun.codemodel.JResourceFile;

public class JDirectFile extends JResourceFile implements Streamable {

	private File file = null;


	public JDirectFile(String name, File file){
		super(name);

		setFile(file);
	}

	@Override
	public void build(OutputStream os) throws IOException {

		try(InputStream is = getInputStream()){
			ByteStreams.copy(is, os);
		}

		os.flush();
	}

	@Override
	public InputStream getInputStream() throws IOException {
		File file = getFile();

		return new FileInputStream(file);
	}

	public File getFile(){
		return this.file;
	}

	private void setFile(File file){
		this.file = Objects.requireNonNull(file);
	}
}