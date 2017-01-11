/*******************************************************************************
 * Copyright (c) 2016 Torkild U. Resheim.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Torkild U. Resheim - initial API and implementation
 *******************************************************************************/

package net.resheim.eclipse.equationwriter;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import net.resheim.eclipse.equationwriter.LaTeXCommand.Group;

public class LaTeXDictionary {

	private static Path path = Paths.get("dictionary.xml");

	public static List<LaTeXCommand> readDictionary() {
		List<LaTeXCommand> commands = new ArrayList<>();
		SAXParserFactory factory = SAXParserFactory.newInstance();
		try {
			SAXParser saxParser = factory.newSAXParser();
			DefaultHandler handler = new DefaultHandler() {

				@Override
				public void startElement(String uri, String localName, String qName, Attributes attributes) {
					if (qName.equals("command")) {
						LaTeXCommand command = new LaTeXCommand();
						String group = attributes.getValue("group");
						if (group != null) {
							command.setGroup(Group.valueOf(group));
						}
						command.setToken(attributes.getValue("token"));
						command.setRender(attributes.getValue("render"));
						command.setTemplate(attributes.getValue("template"));
						commands.add(command);
					}
				}
			};

			InputStream inputStream = new FileInputStream(path.toFile());
			Reader reader = new InputStreamReader(inputStream, "UTF-8");

			InputSource is = new InputSource(reader);
			is.setEncoding("UTF-8");

			saxParser.parse(is, handler);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return commands;
	}

	public static void writeDictionary(List<LaTeXCommand> commands) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(path.toFile()));
		bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		bw.write("<dictionary>\n");
		for (LaTeXCommand command : commands) {
			if (command.getRender() == null && command.getTemplate() == null) {
				bw.write("\t<command\n\t\tgroup=\"" + command.getGroup() + "\"\n\t\ttoken=\"" + command.getToken()
						+ "\" />\n");
			}
			if (command.getRender() != null && command.getTemplate() == null) {
				bw.write("\t<command\n\t\tgroup=\"" + command.getGroup() + "\"\n\t\ttoken=\"" + command.getToken()
						+ "\"\n\t\trender=\"" + command.getRender() + "\" />\n");
			}
			if (command.getRender() == null && command.getTemplate() != null) {
				bw.write("\t<command\n\t\tgroup=\"" + command.getGroup() + "\"\n\t\ttoken=\"" + command.getToken()
						+ "\"\n\t\ttemplate=\"" + command.getTemplate() + "\" />\n");
			}
			if (command.getRender() != null && command.getTemplate() != null) {
				bw.write("\t<command\n\t\tgroup=\"" + command.getGroup() + "\"\n\t\ttoken=\"" + command.getToken()
						+ "\"\n\t\trender=\"" + command.getRender() + "\"\n\t\ttemplate=\"" + command.getTemplate()
						+ "\" />\n");
			}
		}
		bw.write("</dictionary>\n");
		bw.close();
	}

}
