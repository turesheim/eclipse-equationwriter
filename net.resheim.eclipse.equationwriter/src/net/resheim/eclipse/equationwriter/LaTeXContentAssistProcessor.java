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
import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.swt.graphics.Image;

/**
 * @author Torkild U. Resheim
 * @since 1.0
 */
class LaTeXContentAssistProcessor implements IContentAssistProcessor {

	private static final char BACKSLASH = '\\';

	private static String[] keywords = new String[0];

	public LaTeXContentAssistProcessor() {
		if (keywords.length == 0) {
			try {
				BufferedInputStream bs = new BufferedInputStream(
						LaTeXContentAssistProcessor.this.getClass().getResourceAsStream("keywords.txt"));
				StringBuilder sb = new StringBuilder();
				try {
					while (bs.available() != 0) {
						char read = (char) bs.read();
						// Add everything except space
						if (read != ' ') {
							sb.append(read);
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				keywords = sb.toString().split("\n");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int documentOffset) {
		ArrayList<ICompletionProposal> props = new ArrayList<ICompletionProposal>();
		IDocument doc = viewer.getDocument();
		String qualifier = getQualifier(doc, documentOffset);
		int qlen = qualifier.length();
		boolean restart = false;
		for (String keyword : keywords) {
			if (qualifier.equals(keyword)) {
				restart = true;
				qlen = 0;
			}
		}
		for (String keyword : keywords) {
			if ((restart || keyword.startsWith(qualifier)) && (documentOffset - qlen >= 0)) {
				Image image = EditorPlugin.getDefault().getImageRegistry().get(keyword);
				addProposal(keyword, documentOffset, qlen, image, props);
			}
		}
		return props.toArray(new ICompletionProposal[props.size()]);
	}

	private void addProposal(String prop, int offset, int qlen, Image image, ArrayList<ICompletionProposal> props) {
		CompletionProposal proposal = new CompletionProposal(prop, // replacement string
				offset - qlen, // offset of the text
				qlen, // length of the text
				prop.length(), // cursor position after
				image, // the image
				prop, // displayed string
				null, // context info
				null); // extra information
		props.add(proposal);
	}

	@Override
	public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
		return null;
	}

	@Override
	public char[] getCompletionProposalAutoActivationCharacters() {
		return new char[] { BACKSLASH };
	}

	@Override
	public char[] getContextInformationAutoActivationCharacters() {
		return new char[] { BACKSLASH };
	}

	@Override
	public String getErrorMessage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IContextInformationValidator getContextInformationValidator() {
		// TODO Auto-generated method stub
		return null;
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
}