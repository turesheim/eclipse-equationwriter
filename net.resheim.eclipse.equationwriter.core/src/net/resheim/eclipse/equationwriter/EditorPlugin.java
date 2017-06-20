/*******************************************************************************
 * Copyright (c) 2016, 2017 Torkild U. Resheim.
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

import java.net.URL;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class EditorPlugin extends AbstractUIPlugin {

	private static EditorPlugin plugin;

	public static final String PLUGIN_ID = "net.resheim.eclipse.equationwriter"; //$NON-NLS-1$

	private List<LaTeXCommand> symbols = null;

	public static final String MATHJAX_BUNDLE_ID = "net.resheim.eclipse.equationwriter.mathjax";

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		symbols = LaTeXDictionary.readDictionary();
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
			} else if (c != '\\') {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	@Override
	protected void initializeImageRegistry(ImageRegistry reg) {
		super.initializeImageRegistry(reg);

		for (LaTeXCommand symbol : getSymbols()) {
			// build the filename and remove the prefixing backslash
			try {
				if (symbol.getIcon() == null) {
					break;
				}
				URL url = FileLocator.find(getBundle(), new Path(symbol.getIcon().toString()), null);
				ImageDescriptor imageDescriptor = ImageDescriptor.createFromURL(url);
				reg.put(symbol.getToken(), imageDescriptor);
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("Could not read " + symbol);
			}
		}
	}

	public List<LaTeXCommand> getSymbols() {
		return symbols;
	}

}
