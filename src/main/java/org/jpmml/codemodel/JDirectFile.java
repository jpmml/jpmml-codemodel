/*
 * Copyright (c) 2018 Villu Ruusmann
 */
package org.jpmml.codemodel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.google.common.io.ByteStreams;
import com.sun.codemodel.JResourceFile;

public class JDirectFile extends JResourceFile {

	private File file = null;


	public JDirectFile(String name, File file){
		super(name);

		setFile(file);
	}

	@Override
	public void build(OutputStream os) throws IOException {
		File file = getFile();

		try(InputStream is = new FileInputStream(file)){
			ByteStreams.copy(is, os);
		}

		os.flush();
	}

	public File getFile(){
		return this.file;
	}

	private void setFile(File file){
		this.file = file;
	}
}