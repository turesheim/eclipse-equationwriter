/*******************************************************************************
 * Copyright (c) 2016 Torkild U. Resheim.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *     Torkild U. Resheim - initial API and implementation
 *******************************************************************************/
package net.resheim.eclipse.equationwriter;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class EditorPlugin extends AbstractUIPlugin {

	private static EditorPlugin plugin;

	public static final String PLUGIN_ID = "net.resheim.eclipse.equationwriter"; //$NON-NLS-1$

	private final List<String> symbols = new ArrayList<>();

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		if (plugin == this) {
			plugin = null;
		}
		super.stop(context);
	}

	public static EditorPlugin getDefault() {
		return plugin;
	}

	public static String getFilename(String latex) {
		StringBuilder sb = new StringBuilder();
		char[] charArray = latex.toCharArray();
		for (char c : charArray) {
			if (Character.isUpperCase(c)) {
				sb.append("_");
				sb.append(Character.toLowerCase(c));
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	@Override
	protected void initializeImageRegistry(ImageRegistry reg) {
		super.initializeImageRegistry(reg);
		Enumeration<URL> findEntries = getBundle().findEntries("latex", "*.txt", true);
		while (findEntries.hasMoreElements()) {
			URL url;
			try {
				url = FileLocator.toFileURL(findEntries.nextElement());
				try (BufferedInputStream bs = new BufferedInputStream(new FileInputStream(new File(url.toURI())));
						BufferedReader br = new BufferedReader(
								new InputStreamReader(bs, Charset.forName("ISO-8859-1")))) {
					String in = null;
					while ((in = br.readLine()) != null) {
						String trim = in.trim();
						symbols.add(trim);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}

		for (String string : symbols) {
			// build the filename and remove the prefixing backslash
			try {
				String imageFilePath = "icons/content-assist/" + getFilename(string.substring(1)) + ".png";
				ImageDescriptor imageDescriptorFromPlugin = AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID,
						imageFilePath);
				reg.put(string, imageDescriptorFromPlugin);
			} catch (Exception e) {
				System.err.println("Could not read " + string);
			}
		}
	}

}
