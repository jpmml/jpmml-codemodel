/*
 * Copyright (c) 2019 Villu Ruusmann
 */
package org.jpmml.codemodel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JResourceFile;

public class JCodeModelClassLoader extends ClassLoader {

	private JCodeModel codeModel = null;


	public JCodeModelClassLoader(JCodeModel codeModel){
		super();

		setCodeModel(codeModel);
	}

	public JCodeModelClassLoader(ClassLoader parent, JCodeModel codeModel){
		super(parent);

		setCodeModel(codeModel);
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		JCodeModel codeModel = getCodeModel();

		JPackage _package;

		String simpleName;

		int dot = name.lastIndexOf('.');
		if(dot > -1){
			_package = codeModel._package(name.substring(0, dot));

			simpleName = name.substring(dot + 1);
		} else

		{
			_package = codeModel.rootPackage();

			simpleName = name;
		}

		JClassFile definedClazzClassFile = (JClassFile)findResourceFile(_package, simpleName + ".class");
		if(definedClazzClassFile == null){
			throw new ClassNotFoundException(name);
		}

		byte[] bytes = definedClazzClassFile.getBytes();

		return defineClass(name, bytes, 0, bytes.length);
	}

	@Override
	public URL findResource(String name){
		JCodeModel codeModel = getCodeModel();

		if(name.startsWith("/")){
			name = name.substring(1);
		}

		JPackage _package;

		int slash = name.lastIndexOf('/');
		if(slash > -1){
			_package = codeModel._package(name.substring(0, slash));

			name = name.substring(slash + 1);
		} else

		{
			_package = codeModel.rootPackage();
		}

		JResourceFile resourceFile = findResourceFile(_package, name);
		if(resourceFile == null){
			return null;
		}

		URLStreamHandler resourceFileHandler = new URLStreamHandler(){

			@Override
			public URLConnection openConnection(URL url) throws IOException {
				URLConnection result = new URLConnection(url){

					@Override
					public void connect(){
					}

					@Override
					public InputStream getInputStream() throws IOException {

						if(resourceFile instanceof Streamable){
							Streamable streamable = (Streamable)resourceFile;

							return streamable.getInputStream();
						}

						byte[] bytes;

						try {
							bytes = toByteArray(resourceFile);
						} catch(Exception e){
							throw new IOException(e);
						}

						return new ByteArrayInputStream(bytes);
					}
				};

				return result;
			}
		};

		try {
			return new URL((URL)null, "codemodel:" + name, resourceFileHandler);
		} catch(MalformedURLException mue){
			throw new RuntimeException(mue);
		}
	}

	@Override
	public Enumeration<URL> findResources(String name){
		URL url = findResource(name);

		if(url != null){
			return Collections.enumeration(Collections.singleton(url));
		}

		return Collections.emptyEnumeration();
	}

	public JCodeModel getCodeModel(){
		return this.codeModel;
	}

	private void setCodeModel(JCodeModel codeModel){
		this.codeModel = codeModel;
	}

	static
	private JResourceFile findResourceFile(JPackage _package, String name){
		Iterator<JResourceFile> it = _package.propertyFiles();

		while(it.hasNext()){
			JResourceFile resourceFile = it.next();

			if((resourceFile.name()).equals(name)){
				return resourceFile;
			}
		}

		return null;
	}

	static
	private byte[] toByteArray(JResourceFile resourceFile) throws Exception {
		Class<?> clazz = resourceFile.getClass();

		Method method = clazz.getDeclaredMethod("build", OutputStream.class);
		if(!method.isAccessible()){
			method.setAccessible(true);
		}

		ByteArrayOutputStream os = new ByteArrayOutputStream();

		try {
			method.invoke(resourceFile, os);

			return os.toByteArray();
		} finally {
			os.close();
		}
	}

	static {
		ClassLoader.registerAsParallelCapable();
	}
}