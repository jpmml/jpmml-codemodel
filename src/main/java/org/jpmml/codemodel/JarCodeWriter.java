/*
 * Copyright (c) 2018 Villu Ruusmann
 */
package org.jpmml.codemodel;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import com.sun.codemodel.CodeWriter;
import com.sun.codemodel.JPackage;

public class JarCodeWriter extends CodeWriter {

	private JarOutputStream jar = null;

	private OutputStream jarOs = null;


	public JarCodeWriter(OutputStream os, Manifest manifest) throws IOException {
		this.jar = new JarOutputStream(os, manifest);

		this.jarOs = new FilterOutputStream(this.jar){

			@Override
			public void close() throws IOException {
				JarOutputStream jar = (JarOutputStream)super.out;

				jar.closeEntry();
			}
		};
	}

	@Override
	public OutputStream openBinary(JPackage _package, String name) throws IOException {
		JarEntry entry = new JarEntry(FileObjectUtil.toResourceName(_package, name));

		this.jar.putNextEntry(entry);

		return this.jarOs;
	}

	@Override
	public void close() throws IOException {
		this.jar.close();
	}
}