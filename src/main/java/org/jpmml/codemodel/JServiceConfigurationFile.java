/*
 * Copyright (c) 2018 Villu Ruusmann
 */
package org.jpmml.codemodel;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.Objects;
import java.util.ServiceLoader;

import com.sun.codemodel.JResourceFile;
import com.sun.codemodel.JType;

/**
 * @see ServiceLoader
 */
public class JServiceConfigurationFile extends JResourceFile {

	private JType service = null;

	private Collection<? extends JType> serviceProviders = null;


	public JServiceConfigurationFile(JType service, Collection<? extends JType> serviceProviders){
		super(service != null ? service.binaryName() : null);

		setService(service);
		setServiceProviders(serviceProviders);
	}

	@Override
	public void build(OutputStream os) throws IOException {
		Writer writer = new OutputStreamWriter(os, "UTF-8");

		String sep = "";

		Collection<? extends JType> serviceProviders = getServiceProviders();
		for(JType serviceProvider : serviceProviders){
			writer.write(sep);

			sep = "\n";

			writer.write(serviceProvider.binaryName());
		}

		writer.flush();
	}

	public JType getService(){
		return this.service;
	}

	private void setService(JType service){
		this.service = Objects.requireNonNull(service);
	}

	public Collection<? extends JType> getServiceProviders(){
		return this.serviceProviders;
	}

	private void setServiceProviders(Collection<? extends JType> serviceProviders){
		this.serviceProviders = Objects.requireNonNull(serviceProviders);
	}
}