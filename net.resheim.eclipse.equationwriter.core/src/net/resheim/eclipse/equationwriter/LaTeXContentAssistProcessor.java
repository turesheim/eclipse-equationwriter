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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateCompletionProcessor;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.jface.text.templates.TemplateProposal;
import org.eclipse.swt.graphics.Image;

/**
 * @author Torkild U. Resheim
 * @since 1.0
 */
class LaTeXContentAssistProcessor extends TemplateCompletionProcessor {

	private static final char BACKSLASH = '\\';

	TemplateContextType tcp = new TemplateContextType("net.resheim.eclipse.equationwriter");

	@Override
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int documentOffset) {
		ArrayList<ICompletionProposal> props = new ArrayList<ICompletionProposal>();
		IDocument doc = viewer.getDocument();
		String qualifier = getQualifier(doc, documentOffset);
		int qlen = qualifier.length();
		boolean restart = false;
		for (LaTeXCommand keyword : EditorPlugin.getDefault().getSymbols()) {
			if (qualifier.equals(keyword.getToken())) {
				restart = true;
				qlen = 0;
			}
		}
		for (LaTeXCommand symbol : EditorPlugin.getDefault().getSymbols()) {
			if ((restart || symbol.getToken().startsWith(qualifier)) && (documentOffset - qlen >= 0)) {
				int position = symbol.getToken().length();
				// Create a template if one has been specified
				if (symbol.getTemplate() != null) {
					Region region = new Region(documentOffset - qlen, qlen);
					TemplateContext context = createContext(viewer, region);
					for (Template template : getTemplates(tcp.getId())) {
						try {
							context.getContextType().validate(template.getPattern());
							if (template.getName().startsWith(qualifier)) {
								if (template.matches(qualifier, context.getContextType().getId())) {
									Image image = EditorPlugin.getDefault().getImageRegistry().get(symbol.getToken());
									TemplateProposal tp = new TemplateProposal(template, context, region, image);
									props.add(tp);
								}
							}
						} catch (TemplateException e) {
							e.printStackTrace();
						}
					}
				} else {
					Image image = EditorPlugin.getDefault().getImageRegistry().get(symbol.getToken());
					addBasicProposal(symbol.getToken(), documentOffset, qlen, image, position, props);
				}
			}
		}
		return props.toArray(new ICompletionProposal[props.size()]);
	}

	private void addBasicProposal(String prop, int offset, int qlen, Image image, int position,
			ArrayList<ICompletionProposal> props) {
		CompletionProposal proposal = new CompletionProposal(prop, // replacement string
				offset - qlen, // offset of the text
				qlen, // length of the text
				position, // cursor position after
				image, // the image
				prop, // displayed string
				null, // context info
				null); // extra information
		props.add(proposal);
	}

	@Override
	public char[] getCompletionProposalAutoActivationCharacters() {
		return new char[] { BACKSLASH };
	}

	@Override
	public char[] getContextInformationAutoActivationCharacters() {
		return new char[] { BACKSLASH };
	}

	/**
	 * Calculates the LaTeX content assist qualifier.
	 * 
	 * @param doc
	 *            the document
	 * @param documentOffset
	 *            the cursor position
	 * @return the qualifier for content assist
	 */
	private String getQualifier(IDocument doc, int documentOffset) {
		int offset = documentOffset;
		String qualifier = "";
		// use string buffer to collect characters
		StringBuffer buf = new StringBuffer();
		while (true) {
			try {
				// read character backwards
				char c = doc.getChar(--offset);
				if (Character.isWhitespace(c) || (c == BACKSLASH)) {
					qualifier = buf.reverse().toString().trim();
					break;
				}
				buf.append(c);
			} catch (BadLocationException e) {
				break;
			}
		}
		return BACKSLASH + qualifier;
	}

	@Override
	protected Template[] getTemplates(String contextTypeId) {
		List<Template> templates = new ArrayList<>();
		for (LaTeXCommand symbol : EditorPlugin.getDefault().getSymbols()) {
			if (symbol.getTemplate() != null) {
				Template template = new Template(symbol.getToken(), "", tcp.getId(), symbol.getTemplate(), false);
				templates.add(template);
			}
		}
		return templates.toArray(new Template[0]);
	}

	@Override
	protected TemplateContextType getContextType(ITextViewer viewer, IRegion region) {
		return tcp;
	}

	@Override
	protected Image getImage(Template template) {
		return EditorPlugin.getDefault().getImageRegistry().get(template.getName());
	}

}