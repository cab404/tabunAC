package com.cab404.ponyscape.parts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.cab404.acli.base.Part;
import com.cab404.jconsol.annotations.Command;
import com.cab404.jconsol.annotations.CommandClass;
import com.cab404.ponyscape.R;
import com.cab404.ponyscape.Static;

/**
 * @author cab404
 */

@CommandClass(prefix = "")
public class Credits extends Part implements View.OnClickListener {

    @Command(
            command = "credits"
    )
    public void displayCredits() {
        Static.list.add(this);
    }

    @Override protected View create(LayoutInflater inflater, ViewGroup viewGroup, Context context) {
        View view = inflater.inflate(R.layout.part_plain_text, viewGroup, false);
        view.setOnClickListener(this);
        return view;
    }

    @Override public void onClick(View view) {
        delete();
    }

}
