package com.cab404.ponyscape.parts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.cab404.acli.Part;
import com.cab404.ponyscape.R;

/**
 * Sorry for no comments!
 * Created at 15:37 on 16.12.14
 *
 * @author cab404
 */
public class BlogsPart extends Part {

    @Override
    protected View create(LayoutInflater inflater, ViewGroup viewGroup, Context context) {
        return inflater.inflate(R.layout.part_blog_list, viewGroup, false);
    }


}
