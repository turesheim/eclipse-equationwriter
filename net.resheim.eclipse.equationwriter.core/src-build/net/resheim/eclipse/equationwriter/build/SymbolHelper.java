/*******************************************************************************
 * Copyright (c) 2016, 2017 Torkild U. Resheim.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Torkild U. Resheim - initial API and implementation
 *******************************************************************************/
package net.resheim.eclipse.equationwriter.build;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import net.resheim.eclipse.equationwriter.LaTeXCommand;
import net.resheim.eclipse.equationwriter.LaTeXCommand.Group;
import net.resheim.eclipse.equationwriter.LaTeXDictionary;

/**
 * Converts from the initial pure text format to the new "dictionary" format. This type can probably be deleted as soon
 * as the dictionary is completed.
 * 
 * @author Torkild U. Resheim
 */
public class SymbolHelper {

	public static void main(String[] args) {
		List<LaTeXCommand> commands = new ArrayList<>();
		try {
			readAllLines(Paths.get("latex/accents.txt"), Charset.forName("ISO-8859-1"), Group.ACCENT, commands);
			readAllLines(Paths.get("latex/arrows.txt"), Charset.forName("ISO-8859-1"), Group.ARROW, commands);
			readAllLines(Paths.get("latex/cumulative.txt"), Charset.forName("ISO-8859-1"), Group.CUMULATIVE, commands);
			readAllLines(Paths.get("latex/greek.txt"), Charset.forName("ISO-8859-1"), Group.GREEK, commands);
			readAllLines(Paths.get("latex/letters.txt"), Charset.forName("ISO-8859-1"), Group.LETTER, commands);
			readAllLines(Paths.get("latex/miscellaneous.txt"), Charset.forName("ISO-8859-1"), Group.MISCELLANEOUS,
					commands);
			readAllLines(Paths.get("latex/operators.txt"), Charset.forName("ISO-8859-1"), Group.OPERATOR, commands);
			readAllLines(Paths.get("latex/relations.txt"), Charset.forName("ISO-8859-1"), Group.RELATION, commands);
			readAllLines(Paths.get("latex/keywords.txt"), Charset.forName("ISO-8859-1"), Group.GENERAL, commands);
			readAllLines(Paths.get("latex/symbols.txt"), Charset.forName("ISO-8859-1"), Group.SYMBOL, commands);
			LaTeXDictionary.writeDictionary(commands);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static void readAllLines(Path path, Charset charset, Group group, List<LaTeXCommand> list)
			throws FileNotFoundException, IOException {
		try (BufferedReader br = new BufferedReader(
				new InputStreamReader(new FileInputStream(path.toFile()), charset))) {
			String in = null;
			while ((in = br.readLine()) != null) {
				if (in.startsWith("#")) {
					continue;
				}
				LaTeXCommand s = new LaTeXCommand(in.trim());
				s.setGroup(group);
				list.add(s);
			}
		}
	}

}
