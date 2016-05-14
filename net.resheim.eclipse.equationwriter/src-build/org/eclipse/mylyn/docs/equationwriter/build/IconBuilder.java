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
package org.eclipse.mylyn.docs.equationwriter.build;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.mylyn.wikitext.core.parser.builder.HtmlDocumentBuilder;
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

/**
 * This type is ised to create icons for the content assist feature. It will iterate over all LaTeX commands found in
 * <i>./latex</i>, render it and produce a PNG file.
 * 
 * @author Torkild U. Resheim
 */
public class IconBuilder {

	private static final int SIZE = 16;

	/** The colour white */
	private final static int WHITE = 0xFFFFFF;

	private static Browser browser;

	public static final String JS = "" //
			+ "<script type=\"text/x-mathjax-config\">MathJax.Hub.Config({tex2jax: {inlineMath: [[\"$\",\"$\"],[\"\\\\(\",\"\\\\)\"]]}});</script>" //
			+ "<script type=\"text/javascript\" src=\"https://cdn.mathjax.org/mathjax/latest/MathJax.js?config=TeX-AMS_SVG\"></script>" //
			+ "<style>\n" + "  * {\n" + "    margin: 0;\n" //
			+ "    padding: 0;\n" + "  }\n" + "</style>\n" + "<script>\n" //
			+ "  MathJax.Hub.Config({\n" //
			+ "    SVG: {\n" //
			+ "      scale: 150,\n" //
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

	private static List<String> symbols = new ArrayList<>();

	private static List<String> ca = new ArrayList<>();

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
				saveScreenshot();
			}
			setNextSymbol();
			return null;
		}
	}

	@SuppressWarnings("unused")
	public static void main(String[] args) {

		try {
			symbols.addAll(Files.readAllLines(Paths.get("latex/accents.txt"), Charset.forName("ISO-8859-1")));
			symbols.addAll(Files.readAllLines(Paths.get("latex/arrows.txt"), Charset.forName("ISO-8859-1")));
			symbols.addAll(Files.readAllLines(Paths.get("latex/cumulative.txt"), Charset.forName("ISO-8859-1")));
			symbols.addAll(Files.readAllLines(Paths.get("latex/greek.txt"), Charset.forName("ISO-8859-1")));
			symbols.addAll(Files.readAllLines(Paths.get("latex/letters.txt"), Charset.forName("ISO-8859-1")));
			symbols.addAll(Files.readAllLines(Paths.get("latex/miscellaneous.txt"), Charset.forName("ISO-8859-1")));
			symbols.addAll(Files.readAllLines(Paths.get("latex/operators.txt"), Charset.forName("ISO-8859-1")));
			symbols.addAll(Files.readAllLines(Paths.get("latex/symbols.txt"), Charset.forName("ISO-8859-1")));
			symbols.addAll(Files.readAllLines(Paths.get("latex/relations.txt"), Charset.forName("ISO-8859-1")));
			ca.addAll(Files.readAllLines(Paths.get("src/org/eclipse/mylyn/docs/equationwriter/editors/keywords.txt"),
					Charset.forName("UTF-8")));
		} catch (IOException e) {
			e.printStackTrace();
		}

		display = new Display();
		shell = new Shell(display);
		shell.setLocation(100, 100);
		// shell.setSize(64, 64);
		shell.setLayout(new FillLayout());
		browser = new Browser(shell, SWT.NONE);
		shell.pack(true);
		// call this method when the symbol has been correctly rendered
		new MathJaxReady(browser, "loadFormula");

		// load the initial HTML document
		final StringWriter sw = new StringWriter();
		final HtmlDocumentBuilder h = new HtmlDocumentBuilder(sw);
		h.beginDocument();
		h.charactersUnescaped(JS);
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

		// disposes all associated windows and their components

		ca.stream().filter(t -> !symbols.contains(t)).forEach(t -> System.out.println(t));

		System.out.println("Done.");

		display.dispose();
	}

	private static String symbol;

	public static void setNextSymbol() {
		browser.getDisplay().asyncExec(new Runnable() {

			@Override
			public void run() {
				if (i < symbols.size()) {
					symbol = symbols.get(i++).trim();
					final String string = fixExpression(symbol);
					browser.execute("UpdateMath(\"" + string + "\");");
				} else {
					shell.dispose();
				}
			}
		});
	}

	/**
	 * Save a screenshot (of the browser) for the running test.
	 */
	private static void saveScreenshot() {
		String name = symbol.substring(1);
		File dir = new File("icons/content-assist");
		if (!dir.exists()) {
			dir.mkdir();
		}

		// find edges and scale down to 16px
		GC gc = new GC(browser);
		final Image image = new Image(display, shell.getBounds().x, shell.getBounds().y);
		gc.copyArea(image, 0, 0);
		Rectangle r = detectEdges(image);

		// crop and scale down to 16x16
		final Image t = new Image(display, r.width, r.height);
		gc.copyArea(t, r.x, r.y);
		gc.dispose();
		final Image scaled = new Image(display, t.getImageData().scaledTo(SIZE, SIZE));

		// convert brightness (white) to transparent
		ImageData id = convertBrightnessToAlpha(scaled.getImageData());

		// save the final image
		ImageLoader il = new ImageLoader();
		il.data = new ImageData[] { id };
		String imageName = dir.getAbsolutePath() + File.separator + getFilename(name) + ".png";
		File imageFile = new File(imageName);
		if (imageFile.exists()) {
			imageFile.delete();
		}
		il.save(imageName, SWT.IMAGE_PNG);
		System.out.println("Created icon for " + symbol + ". Original size: " + r);
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

	public static ImageData convertBrightnessToAlpha(ImageData data) {
		RGB foreground = new RGB(0, 0, 255);
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
		// while (isVerticalWhite(image, left) && left <
		// image.getBounds().width) {
		// left++;
		// }
		int right = image.getImageData().width - 1;
		while (isVerticalWhite(image, right) && right > 1) {
			right--;
		}
		int top = 0;
		// while (isHorizontalWhite(image, top) && top <
		// image.getBounds().height) {
		// top++;
		// }
		int bottom = image.getImageData().height - 1;
		while (isHorizontalWhite(image, bottom) && bottom > 1) {
			bottom--;
		}
		int width = right - left;
		int height = bottom - top;

		// make sure that height and width are the same
		if (width > height) {
			height = width;
		}
		if (height > width) {
			width = height;
		}
		// never scale up, only down
		if (height < SIZE) {
			height = SIZE;
		}
		if (width < SIZE) {
			width = SIZE;
		}

		Rectangle r = new Rectangle(left, top, width, height);
		return r;
	}

	private static boolean isVerticalWhite(Image image, int x) {
		boolean white = true;
		for (int y = 0; y < image.getImageData().height; y++) {
			int i = image.getImageData().getPixel(x, y);
			if (i < WHITE) {
				white = false;
			}
		}
		return white;
	}

	private static boolean isHorizontalWhite(Image image, int y) {
		boolean white = true;
		for (int x = 0; x < image.getImageData().width; x++) {
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
