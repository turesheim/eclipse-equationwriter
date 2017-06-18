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
package net.resheim.eclipse.equationwriter.build;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.mylyn.wikitext.parser.DocumentBuilder.SpanType;
import org.eclipse.mylyn.wikitext.parser.builder.HtmlDocumentBuilder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.osgi.framework.Bundle;

import net.resheim.eclipse.equationwriter.EditorPlugin;
import net.resheim.eclipse.equationwriter.LaTeXCommand;
import net.resheim.eclipse.equationwriter.LaTeXDictionary;

/**
 * This type is ised to create icons for the content assist feature. It will iterate over all LaTeX commands found in
 * the dictionaru, render it and produce PNG file in different sizes. Note that this has been found to work with Luna.
 * It appears there have been some changes in the browser widget.
 * 
 * @author Torkild U. Resheim
 */
public class IconBuilder {

	private static final int SIZE_1x = 16;

	private static final int SIZE_1_5x = 24;

	private static final int SIZE_2x = 32;

	/** The colour white */
	private final static int WHITE = 0xFFFFFF;

	private static Browser browser;

	/**
	 * JavaScript to render the current LaTeX command presentation and call back to Java code once this is done so that
	 * the image can be saved.
	 */
	public static final String JS = "" //
			+ "<script type=\"text/x-mathjax-config\">MathJax.Hub.Config({tex2jax: {inlineMath: [[\"$\",\"$\"],[\"\\\\(\",\"\\\\)\"]]}});</script>" //
			+ "<script type=\"text/javascript\" src=\"%MATHJAX%/MathJax.js?config=TeX-AMS_SVG\"></script>" //
			+ "<style>\n" + "  * {\n" + "    margin: 0;\n" //
			+ "    padding: 0;\n" + "  }\n" + "</style>\n" + "<script>\n" //
			+ "  MathJax.Hub.Config({\n" //
			+ "    SVG: {\n" //
			+ "      scale: 250,\n" //
			+ "      blacker: 10,\n" //
			+ "      font: \"STIX-Web\"\n" + "    }\n" //
			+ "  });\n" //
			+ "  (function () {\n" //
			+ "    var QUEUE = MathJax.Hub.queue;\n" //
			+ "    var NEXT = function () { loadFormula(); }\n" //
			+ "    var math = null;\n" //
			+ "    //\n" //
			+ "    QUEUE.Push(function () {\n" //
			+ "      math = MathJax.Hub.getAllJax(\"MathOutput\")[0];\n" //
			+ "    });\n" //
			+ "    window.UpdateMath = function (TeX) {\n" //
			+ "      QUEUE.Push([\"Text\",math,\"\\\\displaystyle{\"+TeX+\"}\"], NEXT);\n" //
			+ "    }\n" //
			+ "  })();\n" //
			+ "  MathJax.Hub.Register.StartupHook(\"End\",function () {\n" + " 	loadFormula();\n" + "  });\n"
			+ "</script>\n" //
			+ "<div id=\"MathOutput\" >${}$</div>\n" //
			+ "</div>"; //

	private static Display display;

	private static Shell shell;

	private static int i;

	private static class MathJaxReady extends BrowserFunction {

		public MathJaxReady(Browser browser, String name) {
			super(browser, name);
		}

		@Override
		public Object function(Object[] arguments) {
			// first call takes place when MathJaX has just loaded
			if (i > 0) {
				saveCurrentIcon();
			}
			setNextSymbol();
			return null;
		}
	}

	@SuppressWarnings("unused")
	public static void main(String[] args) {

		dictionary = LaTeXDictionary.readDictionary();

		display = new Display();
		shell = new Shell(display);
		shell.setLocation(100, 100);
		shell.setLayout(new FillLayout());
		browser = new Browser(shell, SWT.NONE);
		browser.setSize(400, 400);
		shell.pack(true);
		// call this method when the symbol has been correctly rendered
		new MathJaxReady(browser, "loadFormula");

		// load the initial HTML document
		final StringWriter sw = new StringWriter();
		final HtmlDocumentBuilder h = new HtmlDocumentBuilder(sw);
		h.beginDocument();
		Bundle bundle = Platform.getBundle(EditorPlugin.MATHJAX_BUNDLE_ID);
		URL url = FileLocator.find(bundle, Path.fromPortableString("MathJax"), null);
		try {
			URI uri = FileLocator.resolve(url).toURI();
			File f = new File(uri);
			h.charactersUnescaped(JS.replace("%MATHJAX%", f.toString()));
		} catch (URISyntaxException | IOException e) {
			h.beginSpan(SpanType.CODE, null);
			h.characters(e.getMessage());
			h.endSpan();
			e.printStackTrace();
		}
		h.endDocument();
		browser.setText(sw.toString());
		shell.open();

		// run the event loop as long as the window is open
		while (!shell.isDisposed()) {
			// read the next OS event queue and transfer it to a SWT event
			if (!display.readAndDispatch()) {
				// if there are currently no other OS event to process
				// sleep until the next OS event is available
				display.sleep();
			}
		}

		System.out.println("Done.");

		display.dispose();
	}

	private static LaTeXCommand currentSymbol;

	private static List<LaTeXCommand> dictionary;

	public static void setNextSymbol() {
		browser.getDisplay().asyncExec(new Runnable() {

			@Override
			public void run() {
				if (i < dictionary.size()) {
					currentSymbol = dictionary.get(i++);
					final String string = fixExpression(currentSymbol.getRender());
					browser.execute("UpdateMath(\"" + string + "\");");
				} else {
					shell.dispose();
				}
			}
		});
	}

	/**
	 * Save a screenshot (of the browser) having rendered the expression.
	 */
	private static void saveCurrentIcon() {
		File dir = new File("icons/content-assist");
		if (!dir.exists()) {
			dir.mkdir();
		}

		final Image image = new Image(display, shell.getBounds().x, shell.getBounds().y);
		// find edges and scale down
		GC gc = new GC(browser);
		gc.copyArea(image, 0, 0);
		Rectangle edges = detectEdges(image);

		// we need three different sizes for HiDPI displays.
		saveIcon(image, edges, SIZE_1x, currentSymbol.getToken(), dir, "");
		saveIcon(image, edges, SIZE_1_5x, currentSymbol.getToken(), dir, "@1.5x");
		saveIcon(image, edges, SIZE_2x, currentSymbol.getToken(), dir, "@2x");
	}

	private static void saveIcon(final Image image, final Rectangle edges, int size, String filename, File dir,
			String suffix) {

		// make sure that height and width are the same
		int width = edges.width;
		int height = edges.height;

		if (width > height) {
			height = width;
		}
		if (height > width) {
			width = height;
		}

		// never scale up, only down
		if (height < size) {
			height = size;
		}
		if (width < size) {
			width = size;
		}

		edges.width = width;
		edges.height = height;

		// crop and scale down
		final Image t = new Image(display, edges.width, edges.height);
		GC gc = new GC(image);
		gc.copyArea(t, edges.x, edges.y);
		gc.dispose();
		final Image scaled = new Image(display, t.getImageData().scaledTo(size, size));

		// change the colour to blue which looks a bit better
		RGB foreground = new RGB(0, 0, 255);
		// convert brightness (white) to degrees of transparent
		ImageData id = convertBrightnessToAlpha(scaled.getImageData(), foreground);

		// save the final image
		ImageLoader il = new ImageLoader();
		il.data = new ImageData[] { id };
		String imageName = dir.getAbsolutePath() + File.separator + getFilename(filename) + suffix + ".png";
		File imageFile = new File(imageName);
		if (imageFile.exists()) {
			imageFile.delete();
		}
		il.save(imageName, SWT.IMAGE_PNG);
		System.out.println("Created icon for " + currentSymbol.getToken() + ". Original size: " + edges);
	}

	/**
	 * Converts the string to something that can be used as the icon file name
	 * 
	 * @param keyword
	 *            the keyword
	 * @return the file name
	 */
	public static String getFilename(String keyword) {
		StringBuilder sb = new StringBuilder();
		char[] charArray = keyword.toCharArray();
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

	public static ImageData convertBrightnessToAlpha(ImageData data, RGB foreground) {
		for (int x = 0; x < data.width; x++) {
			for (int y = 0; y < data.height; y++) {
				int pixel = data.getPixel(x, y);
				RGB rgb = data.palette.getRGB(pixel);
				float[] rgBtoHSB = java.awt.Color.RGBtoHSB(rgb.red, rgb.green, rgb.blue, null);
				int alpha = 255 - (int) (rgBtoHSB[2] * 255); // brightness
				data.setPixel(x, y, data.palette.getPixel(foreground));
				data.setAlpha(x, y, alpha);
			}
		}

		return data;
	}

	private static Rectangle detectEdges(Image image) {
		// find left edge
		int left = 0;
		int right = image.getImageData().width - 1;
		while (isVerticalWhite(image, right) && right > 1) {
			right--;
		}
		int top = 0;
		int bottom = image.getImageData().height - 1;
		while (isHorizontalWhite(image, bottom) && bottom > 1) {
			bottom--;
		}
		int width = right - left;
		int height = bottom - top;

		Rectangle r = new Rectangle(left, top, width, height);
		return r;
	}

	private static boolean isVerticalWhite(Image image, int x) {
		boolean white = true;
		int height = image.getImageData().height;
		for (int y = 0; y < height; y++) {
			int i = image.getImageData().getPixel(x, y);
			if (i < WHITE) {
				white = false;
			}
		}
		return white;
	}

	private static boolean isHorizontalWhite(Image image, int y) {
		boolean white = true;
		int width = image.getImageData().width;
		for (int x = 0; x < width; x++) {
			int i = image.getImageData().getPixel(x, y);
			if (i < WHITE) {
				white = false;
			}
		}
		return white;
	}

	/**
	 * Use to escape backslash so that the equation from the editor will be properly read in the JavaScript code.
	 *
	 * @param text
	 *            the LaTeX expression
	 * @return expression with backslash escaped
	 */
	public static String fixExpression(String text) {
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
}
