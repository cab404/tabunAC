package com.cab404.ponyscape.utils.images;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.util.Log;
import com.cab404.ponyscape.R;

/**
 * Для обозначения уровня комментария.
 *
 * @author cab404
 */
public class LevelDrawable extends Drawable {
	private int alpha = 255;

	int section_size;
	int limit;
	int gradient_start, gradient_end;
	private int start;

	public LevelDrawable(Resources res, int start) {
		this.start = start;
		section_size = res.getDimensionPixelSize(R.dimen.comment_ladder);
		limit = res.getInteger(R.integer.maximum_gradient_length);

		gradient_start = res.getColor(R.color.comment_ladder_gradient_start);
		gradient_end = res.getColor(R.color.comment_ladder_gradient_end);
	}

	private int gradient(int start, int end, float process) {
		int result = 0;

		for (int shift = 0, section = 0xFF; section != 0; shift += 8, section <<= 8) {
			int start_section = (start & section) >> shift;
			int end_section = (end & section) >> shift;
			result |= start_section + (int) ((end_section - start_section) * process) << shift;
		}

		return result;
	}

	public int getLastColor(int width) {
		int sector = width / section_size;

		if (sector > limit)
			return gradient_end;
		else
			return gradient(gradient_start, gradient_end, (float) sector / limit);

	}

	@Override public void draw(Canvas canvas) {
		int width = canvas.getWidth(), height = canvas.getHeight();

		int section_num = width / section_size;
		Paint level = new Paint(0);

		for (int i = 0; i < section_num; i++) {

			if (i > limit)
				level.setColor(gradient_end);
			else
				level.setColor(gradient(gradient_start, gradient_end, (float) (i + start) / limit));

			level.setAlpha(alpha);

			canvas.drawRect(i * section_size, 0, (i + 1) * section_size, height, level);

		}

		Log.v("Levels", "Drawing self");
	}

	@Override public void setAlpha(int alpha) {
		this.alpha = alpha;
	}

	@Override public void setColorFilter(ColorFilter cf) {}

	@Override public int getOpacity() {
		return PixelFormat.OPAQUE;
	}
}
