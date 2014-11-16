package com.cab404.ponyscape.parts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import com.cab404.acli.Part;
import com.cab404.ponyscape.R;
import com.cab404.ponyscape.utils.Static;
import org.json.simple.JSONArray;

import java.util.ArrayList;

/**
 * @author cab404
 */
public class StashPart extends Part {
    @Override
    protected View create(LayoutInflater inflater, ViewGroup viewGroup, Context context) {
        View view = inflater.inflate(R.layout.part_stash, viewGroup, false);

        ListView list = ((ListView) view.findViewById(R.id.data));
        list.setAdapter(new StashedAdapter());

        return view;

    }

    private class StashedAdapter extends BaseAdapter {
        ArrayList<String> stashed;

        public StashedAdapter() {

            JSONArray data = Static.obscure.ensure("stash.storage", new JSONArray());
            stashed = new ArrayList<>();

            for (Object data_unit : data)
                stashed.add(String.valueOf(data_unit));

        }

        @Override
        public int getCount() {
            return stashed.size();
        }

        @Override
        public Object getItem(int i) {
            return stashed.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {


            return null;
        }
    }

}


