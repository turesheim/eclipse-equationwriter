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

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.ITextViewerExtension;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class LaTeXSourceViewer extends SourceViewer {

	public LaTeXSourceViewer(Composite parent, int styles) {
		super(parent, null, styles);
	}

	@Override
	protected void createControl(Composite parent, int styles) {
		super.createControl(parent, styles);
	}

	@SuppressWarnings("cast")
	public void initializeViewerFont() {
		SourceViewer sourceViewer = this;
		Font font = JFaceResources.getTextFont();
		if (sourceViewer.getDocument() != null) {
			ISelectionProvider provider = sourceViewer.getSelectionProvider();
			ISelection selection = provider.getSelection();
			int topIndex = sourceViewer.getTopIndex();
			StyledText styledText = sourceViewer.getTextWidget();
			Control parent = styledText;
			if (sourceViewer instanceof ITextViewerExtension) {
				ITextViewerExtension extension = sourceViewer;
				parent = extension.getControl();
			}
			parent.setRedraw(false);
			styledText.setFont(font);
			provider.setSelection(selection);
			sourceViewer.setTopIndex(topIndex);
			if (parent instanceof Composite) {
				Composite composite = (Composite) parent;
				composite.layout(true);
			}
			parent.setRedraw(true);
		} else {
			StyledText styledText = sourceViewer.getTextWidget();
			styledText.setFont(font);
		}
	}
}
