/*******************************************************************************
 * Copyright (c) 2017 Torkild U. Resheim.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Torkild U. Resheim - initial API and implementation
 *******************************************************************************/

package net.resheim.eclipse.equationwriter.build;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.resheim.eclipse.equationwriter.LaTeXCommand;
import net.resheim.eclipse.equationwriter.LaTeXDictionary;

public class SymbolFixer {

	public static void main(String[] args) {
		List<LaTeXCommand> dictionary = LaTeXDictionary.readDictionary();
		List<LaTeXCommand> newDictionary = new ArrayList<>();
		Map<String, Path> map = new HashMap<>();
		try {
			Files.list(Paths.get("icons", "content-assist")).forEach(a -> map.put(a.getFileName().toString(), a));
			for (LaTeXCommand laTeXCommand : dictionary) {
				String filename = getFilename(laTeXCommand.getToken());
				if (map.containsKey(filename + ".png")) {
					laTeXCommand.setIcon(map.get(filename + ".png"));
					newDictionary.add(laTeXCommand);
				}
			}
			LaTeXDictionary.writeDictionary(newDictionary);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

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

}
