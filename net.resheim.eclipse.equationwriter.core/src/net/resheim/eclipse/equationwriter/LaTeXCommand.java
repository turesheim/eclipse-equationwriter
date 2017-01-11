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

/**
 * Represents a LaTeX math symbol. Can be an expression or a simple symbol, letter or sign.
 * 
 * @author Torkild U. Resheims
 */
public class LaTeXCommand {

	public enum Group {
		ACCENT, ARROW, CUMULATIVE, GREEK, LETTER, MISCELLANEOUS, OPERATOR, RELATION, SYMBOL, GENERAL
	}

	/** The token to use for content assist */
	private String token;

	/** The LaTeX code to use when rendering a representation image */
	private String render;

	/** The template to offer the user in content assist etc */
	private String template;

	private Group group;

	public LaTeXCommand(String input) {
		String trim = input.trim();
		String[] split = trim.split("\\t+");
		// use the "template" column if specified
		if (split.length == 3) {
			this.template = split[2];
		} else {
			this.template = split[0];
		}
		this.token = split[0];
		this.render = split[1];
	}

	public LaTeXCommand() {
	}

	public String getRender() {
		return render;
	}

	public String getTemplate() {
		return template;
	}

	public void setRender(String render) {
		this.render = render;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Group getGroup() {
		return group;
	}

	public void setGroup(Group group) {
		this.group = group;
	}

}
