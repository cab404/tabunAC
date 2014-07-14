package com.cab404.ponyscape.parts;

/**
 * @author cab404
 */
public class StaticTextPart extends AbstractTextPart {
	private CharSequence text = "";

	public void setText(CharSequence text) {
		this.text = text;
		updateText();
	}

	@Override protected CharSequence getText() {
		return text;
	}
}
