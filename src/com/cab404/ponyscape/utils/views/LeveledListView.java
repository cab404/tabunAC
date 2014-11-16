package com.cab404.ponyscape.utils.views;
import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
/**
 * Well, sorry for no comments here!
 * Still you can send me your question to me@cab404.ru!
 * <p/>
 * Created at 22:37 on 16-11-2014
 *
 * @author cab404
 */
public class LeveledListView extends FollowableListView {

    private int
            rightMargin = 0,
            leftMargin = 0;

    public LeveledListView(Context context) {
        super(context);
    }
    public LeveledListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public LeveledListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        rightMargin = attrs.getAttributeIntValue(null, "rightXScrollMaximum", -1);
        leftMargin = attrs.getAttributeIntValue(null, "leftXScrollMaximum", -1);

    }

    public int getRightMargin() {
        return rightMargin;
    }
    public void setRightMargin(int rightMargin) {
        this.rightMargin = rightMargin;
    }
    public int getLeftMargin() {
        return leftMargin;
    }
    public void setLeftMargin(int leftMargin) {
        this.leftMargin = leftMargin;
    }
    Point last_point;
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        super.onTouchEvent(ev);

        if (ev.getAction() == MotionEvent.ACTION_DOWN || ev.getAction() == MotionEvent.ACTION_UP) {
            last_point = null;
            return true;
        }

        Point new_point = new Point((int) ev.getX(), (int) ev.getY());

        if (last_point != null) {
            int dX = new_point.x - last_point.x;
            int dY = new_point.y - last_point.y;

            if (dY == 0 || Math.abs((float) dX / (float) dY) > 0.5f)
                scrollTo(getScrollX() - dX, getScrollY());

            if (rightMargin != -1 && getWidth() + getScrollX() < rightMargin)
                scrollTo(rightMargin - getWidth(), getScrollY());

            if (leftMargin != -1 && getScrollX() < leftMargin)
                scrollTo(-leftMargin, getScrollY());

        }
        last_point = new_point;


        return true;
    }

    public void setLevel(int pxOffset) {
    }

    public int getLevel() {
        return 0;
    }

}
