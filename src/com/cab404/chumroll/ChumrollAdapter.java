package com.cab404.chumroll;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Dynamic adapter, but named Chumroll because of reasons
 * Supports adding multiple types of views.
 * <p/>
 * And yup, it manages itself.
 * <p/>
 * Well, besides it's MAGICAL ability to create BEAUTIFUL and GORGEOUS views,
 * caches are still on ListView, or wherever you've plugged this WONDERFUL adapter.
 * So if you are adding views of some new TYPE, it's better to first UNPLUG adapter
 * from view, then ADD and then REPLUG it.
 * <p/>
 * Or simply add it with {@link #prepareFor(com.cab404.chumroll.ViewConverter[])} before initial setAdapter().
 *
 * @author cab404
 */
public class ChumrollAdapter extends BaseAdapter {

    /**
     * Contains list of types of ViewConverters present in this adapter.
     */
    protected List<Class<? extends ViewConverter>> typed;

    /**
     * Contains instances of ViewConverters
     */
    protected ConverterPool converters;

    /**
     * Data set of this adapter
     */
    protected List<ViewBinder> list;

    /**
     * Cached layout inflater
     */
    protected LayoutInflater inf;

    {
        converters = new ConverterPool();
        typed = new ArrayList<>();
        list = new ArrayList<>();
    }

    public ChumrollAdapter() {
        super();
    }

    /**
     * Returns converter pool used by this adapter;
     */
    public ConverterPool getConverters() {
        return converters;
    }

    /**
     * Simple entry type.
     */
    protected /*struct*/ class ViewBinder<From> {
        ViewConverter<? extends From> converter;
        From data;

        public ViewBinder(ViewConverter<? extends From> converter, From data) {
            this.converter = converter;
            this.data = data;
        }

    }

    /**
     * Returns first index in adapter, which has given data.
     */
    public int indexOf(Object data) {
        for (int $i = 0; $i < list.size(); $i++)
            if (list.get($i).data == data)
                return $i;
        return -1;
    }

    /**
     * Adds new entry into adapter.
     */
    @SuppressWarnings("unchecked")
    public <Data> void add(Class<? extends ViewConverter<Data>> converter, Data data) {
        ViewConverter<Data> instance = (ViewConverter<Data>) converters.getInstance(converter);
        if (!typed.contains(converter))
            typed.add(converter);
        list.add(new ViewBinder<>(instance, data));
    }

    /**
     * Adds new entry into adapter.
     */
    @SuppressWarnings("unchecked")
    public <Data> void addAll(Class<? extends ViewConverter<Data>> converter, Collection<? extends Data> data_set) {
        ViewConverter<Data> instance = (ViewConverter<Data>) converters.getInstance(converter);
        if (!typed.contains(converter))
            typed.add(converter);
        for (Data data : data_set)
            list.add(new ViewBinder<Data>(instance, data));
    }

    public Object getData(int at) {
        return list.get(at).data;
    }

    /**
     * Removes everything from adapter.
     */
    public void clear() {
        list.clear();
        notifyDataSetChanged();
    }

    /**
     * Removes entry at given index
     */
    public void remove(int at) {
        list.remove(at);
    }

    /**
     * Removes first occurrence of entry with given data.
     */
    public <Data> void remove(Data what) {
        remove(indexOf(what));
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public int getViewTypeCount() {
        return converters.instances.size();
    }

    @Override
    public int getItemViewType(int position) {
        ViewConverter converter = list.get(position).converter;
        return typed.indexOf(converter.getClass());
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        ViewBinder binder = list.get(position);
        return binder.converter.enabled(binder.data);
    }

    @SuppressWarnings("unchecked")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewBinder binder = list.get(position);

        if (convertView == null) {

            if (inf == null || inf.getContext() != parent.getContext())
                inf = LayoutInflater.from(parent.getContext());

            convertView = binder.converter.createView(parent, inf);
        }

        binder.converter.convert(convertView, binder.data, parent);

        return convertView;
    }

    /**
     * You can stuff a bunch of converters inside right here.
     */
    public void prepareFor(ViewConverter... prepare_to_what) {
        for (ViewConverter thing : prepare_to_what) {
            converters.enforceInstance(thing);
        }
    }

}

