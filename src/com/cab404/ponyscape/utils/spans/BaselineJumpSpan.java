package com.cab404.ponyscape.utils.spans;

import android.graphics.Paint;
import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;

/**
 * Sorry for no comments!
 * Created at 8:59 on 16.12.14
 *
 * @author cab404
 */
public class BaselineJumpSpan extends MetricAffectingSpan {
    private Gravity g;

    public static enum Gravity {
        TOP, BOTTOM, CENTER
    }

    public BaselineJumpSpan(Gravity g) {
        this.g = g;
    }

    private void u(TextPaint p) {
        Paint.FontMetricsInt fm = p.getFontMetricsInt();
        switch (g) {
            case TOP:
                p.baselineShift = fm.ascent / 2;
                break;
            case CENTER:
                p.baselineShift = (fm.ascent / 2 + fm.descent) / 2;
                break;
            case BOTTOM:
                p.baselineShift = fm.descent;
                break;
        }

    }

    @Override
    public void updateMeasureState(TextPaint p) {
        u(p);
    }

    @Override
    public void updateDrawState(TextPaint tp) {
        u(tp);
    }
}
