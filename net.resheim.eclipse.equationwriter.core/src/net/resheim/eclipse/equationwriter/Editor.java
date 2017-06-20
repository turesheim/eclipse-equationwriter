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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.mylyn.wikitext.parser.Attributes;
import org.eclipse.mylyn.wikitext.parser.DocumentBuilder.SpanType;
import org.eclipse.mylyn.wikitext.parser.builder.HtmlDocumentBuilder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.progress.WorkbenchJob;
import org.osgi.framework.Bundle;

public class Editor extends TextEditor {

	private Browser browser;

	// javascript for loading MathJaX. The original is at
	// https://docs.mathjax.org/en/v2.5-latest/typeset.html. This version has
	// been modified to show the rendered math only when ready. This appears
	// to be a bit easier on the eyes.
	private static final String JS = "" //
			+ "<script type=\"text/x-mathjax-config\">MathJax.Hub.Config({tex2jax: {inlineMath: [[\"$\",\"$\"],[\"\\\\(\",\"\\\\)\"]]}});</script>" //
			+ "<script type=\"text/javascript\" src=\"%MATHJAX%/MathJax.js?config=TeX-AMS-MML_SVG\"></script>" //
			+ "<script>\n" //
			+ "  (function () {\n" //
			+ "    var QUEUE = MathJax.Hub.queue;\n" //
			+ "    var math = null, box = null;\n" //
			+ "    //\n" //
			+ "    var HIDEBOX = function () {box.style.visibility = \"hidden\"}\n" //
			+ "    var SHOWBOX = function () {box.style.visibility = \"visible\"}\n" //
			+ "    QUEUE.Push(function () {\n" //
			+ "      math = MathJax.Hub.getAllJax(\"MathOutput\")[0];\n" //
			+ "      box = document.getElementById(\"box\");\n" //
			+ "      SHOWBOX();\n" //
			+ "    });\n" //
			+ "    window.UpdateMath = function (TeX) {\n" //
			+ "      QUEUE.Push(HIDEBOX,[\"Text\",math,\"\\\\displaystyle{\"+TeX+\"}\"],SHOWBOX);\n" //
			+ "    }\n" //
			+ "  })();\n" //
			+ "	 MathJax.Hub.Queue(function () {\n" //
			+ "    	loadFormula();\n" //
			+ "	 });" //
			+ "</script>\n" //
			+ "<div id=\"box\" style=\"visibility:hidden\">\n" //
			+ "<div id=\"MathOutput\" >$${}$$</div>\n" //
			+ "</div>"; //

	/** Use to handle that browser/MathJax may not be ready to do any rendering */
	private final Lock readyLock = new ReentrantLock();

	/**
	 * Callback method for the browser. It will be called when MathJax is ready to be used.
	 */
	class MathJaxReady extends BrowserFunction {

		public MathJaxReady(Browser browser, String name) {
			super(browser, name);
		}

		@Override
		public Object function(Object[] arguments) {
			String latex = getSourceViewer().getDocument().get();
			updatePreview(latex);
			readyLock.unlock();
			return null;
		}
	}

	/**
	 * Job for refreshing the LaTeX preview after a certain delay.
	 */
	private class RefreshJob extends WorkbenchJob {

		public RefreshJob() {
			super("Refresh Job");
			setSystem(true); // set to false to show progress to user
		}

		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			monitor.beginTask("Refreshing LaTeX preview", IProgressMonitor.UNKNOWN);
			updatePreview(getSourceViewer().getDocument().get());
			monitor.done();
			return Status.OK_STATUS;
		}
	}

	public Editor() {
		super();
		setSourceViewerConfiguration(new Configuration());
		setDocumentProvider(new DocumentProvider());
		readyLock.lock();
	}

	@Override
	public void createPartControl(Composite parent) {

		// create the editor with source viewer
		super.createPartControl(parent);

		// ugly hack to get the root composite of the editor
		Composite parent2 = getSourceViewer().getTextWidget().getParent().getParent().getParent().getParent();
		((FillLayout) parent2.getLayout()).type = SWT.VERTICAL;

		// preview
		browser = new Browser(parent2, SWT.NONE);

		initializePreview();

		final RefreshJob job = new RefreshJob();
		getSourceViewer().addTextListener(new ITextListener() {

			@Override
			public void textChanged(TextEvent event) {
				job.cancel();
				job.schedule(200);
			}
		});
	}

	@SuppressWarnings("unused")
	private void initializePreview() {
		new MathJaxReady(browser, "loadFormula");

		Bundle bundle = Platform.getBundle(EditorPlugin.MATHJAX_BUNDLE_ID);
		final StringWriter sw = new StringWriter();
		final HtmlDocumentBuilder h = new HtmlDocumentBuilder(sw);
		h.beginDocument();
		URL url = FileLocator.find(bundle, Path.fromPortableString("MathJax"), null);
		try {
			String f = FileLocator.resolve(url).toExternalForm();
			h.charactersUnescaped(JS.replace("%MATHJAX%", f));
		} catch (IOException e) {
			h.beginSpan(SpanType.CODE, new Attributes());
			h.characters(e.getMessage());
			h.endSpan();
			e.printStackTrace();
		}
		h.endDocument();
		browser.setText(sw.toString());
	}

	/**
	 * Use to escape backslash so that the equation from the editor will be properly read in the JavaScript code.
	 *
	 * @param text
	 *            the LaTeX expression
	 * @return expression with backslash escaped
	 */
	private String fixExpression(String text) {
		if (text == null) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		BufferedReader br = new BufferedReader(new StringReader(text));
		int c = 0;
		try {
			while ((c = br.read()) != -1) {
				// Add extra backslash
				if (c == '\\') {
					sb.append('\\');
				}
				// Remove line feed characters
				if (c != '\n' && c != '\r') {
					sb.append((char) c);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}

	private void updatePreview(final String t) {
		browser.getDisplay().asyncExec(new Runnable() {

			@Override
			public void run() {
				final String string = fixExpression(t);
				browser.execute("UpdateMath(\"" + string + "\");");
			}
		});
	}
}
