package com.cab404.ponyscape.parts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.cab404.acli.base.Part;
import com.cab404.jconsol.annotations.Command;
import com.cab404.jconsol.annotations.CommandClass;
import com.cab404.jconsol.converters.Int;

/**
 * @author cab404
 */
@CommandClass(prefix = "post")
public class PostPart extends Part {

    @Command(command = "load", params = Int.class)
    public void fetch(Integer id) {

    }


    @Override protected View create(LayoutInflater inflater, ViewGroup viewGroup, Context context) {
        return null;
    }


}
