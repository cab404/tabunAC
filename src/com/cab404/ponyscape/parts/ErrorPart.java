package com.cab404.ponyscape.parts;

import com.cab404.libtabun.data.TabunError;
import com.cab404.ponyscape.R;
import com.cab404.ponyscape.parts.raw_text.AbstractTextPart;
import com.cab404.ponyscape.utils.Static;

/**
 * @author cab404
 */
public class ErrorPart extends AbstractTextPart {

	private TabunError error;

	public ErrorPart(TabunError error) {
		this.error = error;
	}

	@Override protected CharSequence getText() {
		switch (error) {
			case ACCESS_DENIED:
				return Static.ctx.getResources().getText(R.string.err403);
			case NOT_FOUND:
				return Static.ctx.getResources().getText(R.string.err404);
			case UNKNOWN:
			default:
				return Static.ctx.getResources().getText(R.string.errUNKNOWN);
		}
	}
}
