package com.cab404.ponyscape.parts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.cab404.acli.base.Part;
import com.cab404.jconsol.annotations.Command;
import com.cab404.jconsol.annotations.CommandClass;

/**
 * @author cab404
 */

@CommandClass(prefix = "")
public class Help extends Part {

    @Command(command = "help")
    public void displayHelp() {

    }

    @Override protected View create(LayoutInflater inflater, ViewGroup viewGroup, Context context) {
        return null;
    }


}
