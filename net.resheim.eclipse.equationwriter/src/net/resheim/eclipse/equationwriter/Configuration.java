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

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.IWhitespaceDetector;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.text.rules.WordRule;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

/**
 * 
 * @author Torkild U. Resheim
 * @since 1.0
 */
public class Configuration extends SourceViewerConfiguration {

	private Color latexTokenColor;
	private Color groupTokenColor;

	public Configuration() {
		super();
		latexTokenColor = Display.getDefault().getSystemColor(SWT.COLOR_BLUE);
		groupTokenColor = Display.getDefault().getSystemColor(SWT.COLOR_RED);
	}

	@Override
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
		ContentAssistant assistant = new ContentAssistant();
		assistant.setContentAssistProcessor(new LaTeXContentAssistProcessor(), IDocument.DEFAULT_CONTENT_TYPE);
		assistant.setInformationControlCreator(getInformationControlCreator(sourceViewer));
		assistant.enableAutoActivation(true);
		assistant.enableAutoInsert(true);
		return assistant;
	}

	@Override
	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
		PresentationReconciler reconciler = new PresentationReconciler();
		reconciler.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));
		DefaultDamagerRepairer dr = new DefaultDamagerRepairer(getTagScanner());
		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);
		return reconciler;
	}

	private ITokenScanner getTagScanner() {
		RuleBasedScanner scanner = new RuleBasedScanner();
		IRule[] rules = new IRule[3];
		IToken latexToken = new Token(new TextAttribute(latexTokenColor));
		IToken groupToken = new Token(new TextAttribute(groupTokenColor));
		WordRule wr = new WordRule(new IWordDetector() {

			@Override
			public boolean isWordStart(char c) {
				return (c == '\\');
			}

			@Override
			public boolean isWordPart(char c) {
				return (c != ' ' && c != '{' && c != '}' && c != '(' && c != ')');
			}
		}, latexToken);
		rules[0] = wr;
		rules[1] = new WordRule(new IWordDetector() {

			@Override
			public boolean isWordStart(char c) {
				return (c == '(' || c == ')' || c == '{' || c == '}');
			}

			@Override
			public boolean isWordPart(char c) {
				return false;
			}
		}, groupToken);
		rules[2] = new WhitespaceRule(new IWhitespaceDetector() {

			@Override
			public boolean isWhitespace(char c) {
				return Character.isWhitespace(c);
			}
		});
		scanner.setRules(rules);
		return scanner;
	}
}