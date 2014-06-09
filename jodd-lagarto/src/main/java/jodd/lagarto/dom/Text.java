// Copyright (c) 2003-2014, Jodd Team (jodd.org). All Rights Reserved.

package jodd.lagarto.dom;

import jodd.util.HtmlDecoder;
import jodd.util.HtmlEncoder;
import jodd.util.StringUtil;

import java.io.IOException;

public class Text extends Node {

	public Text(Document ownerDocument, String text) {
		super(ownerDocument, NodeType.TEXT, null);
		this.nodeValue = text;
	}

	@Override
	public Text clone() {
		return cloneTo(new Text(ownerDocument, nodeValue));
	}
	
	protected Boolean blank;

	/**
	 * Returns <code>true</code> if text content is blank.
	 */
	public boolean isBlank() {
		if (blank == null) {
			blank = Boolean.valueOf(StringUtil.isBlank(nodeValue));
		}
		return blank.booleanValue();
	}

	/**
	 * Sets HTML text, but decodes it first.
	 */
	public void setTextContent(String text) {
		nodeValue = HtmlDecoder.decode(text);
	}

	/**
	 * Returns encoded HTML text.
	 */
	@Override
	public String getTextContent() {
		return HtmlEncoder.text(nodeValue);
	}

	/**
	 * Appends the text content to <code>Appendable</code>.
	 */
	@Override
	public void appendTextContent(Appendable appendable) {
		try {
			appendable.append(getTextContent());
		} catch (IOException ioex) {
			throw new LagartoDOMException(ioex);
		}
	}

	@Override
	public void toHtml(Appendable appendable) throws IOException {
		ownerDocument.getRenderer().renderText(this, appendable);
	}
}
