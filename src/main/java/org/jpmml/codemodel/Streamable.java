/*
 * Copyright (c) 2019 Villu Ruusmann
 */
package org.jpmml.codemodel;

import java.io.IOException;
import java.io.InputStream;

public interface Streamable {

	InputStream getInputStream() throws IOException;
}