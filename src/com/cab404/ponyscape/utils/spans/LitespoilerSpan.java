package com.cab404.ponyscape.utils.spans;

import android.graphics.Color;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;

/**
 * Скрывает текст фоном цвета текста, если нажать на него, то фон станет прозрачным.
 */
public class LitespoilerSpan extends ClickableSpan {
	boolean hidden = true;

	@SuppressWarnings("NullableProblems")
	@Override public void updateDrawState(TextPaint ds) {
		ds.bgColor = hidden ? ds.getColor() : Color.TRANSPARENT;
	}

	@Override public void onClick(View view) {
		hidden = !hidden;
		TextView text = (TextView) view;

		// Инвалидэйтим вот так вот.
		text.setText(text.getText());
	}
}
