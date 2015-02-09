package com.cab404.ponyscape.utils.views;

import android.view.View;
import com.cab404.ponyscape.bus.E;
import com.cab404.ponyscape.utils.Static;

/**
 * Sorry for no comments!
 * Created at 10:57 on 05.02.15
 *
 * @author cab404
 */
public class RunCommandOnClick implements View.OnClickListener {

    private String command;

    public RunCommandOnClick(String command) {
        this.command = command;
    }

    @Override
    public void onClick(View v) {
        Static.bus.send(new E.Commands.Run(command));
    }
}
