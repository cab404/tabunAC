package com.cab404.ponyscape.utils.views;

import android.view.View;

/**
 * Sorry for no comments!
 * Created at 6:14 on 05.02.15
 *
 * @author cab404
 */
public abstract class DoubleClickListener implements View.OnClickListener {
    long last_clicked = 0;

    byte num_clicked = 0;

    static final byte num_to_click = 2;
    static final int max_delay = 300;


    @Override
    public void onClick(View v) {
        long c_time = System.currentTimeMillis();

        if (last_clicked + max_delay > c_time)
            num_clicked++;
        else
            num_clicked = 1;

        last_clicked = c_time;

        if (num_clicked == num_to_click) {
            num_clicked = 0;
            act(v);
        }

    }

    public abstract void act(View v);

}
