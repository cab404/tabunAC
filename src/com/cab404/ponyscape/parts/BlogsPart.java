package com.cab404.ponyscape.parts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import com.cab404.acli.Part;
import com.cab404.libtabun.data.Blog;
import com.cab404.libtabun.pages.BlogListPage;
import com.cab404.moonlight.util.exceptions.MoonlightFail;
import com.cab404.ponyscape.R;
import com.cab404.ponyscape.bus.E;
import com.cab404.ponyscape.utils.Static;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Sorry for no comments!
 * Created at 15:37 on 16.12.14
 *
 * @author cab404
 */
public class BlogsPart extends Part {
    BlogListPage page = new BlogListPage();
    List<Blog> blogs = Collections.emptyList();
    ListView list;
    boolean last = false;

    private void fetch(final int pnum) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    page.page = pnum;
                    page.blogs.clear();
                    page.fetch(Static.user);
                    Static.bus.send(new E.Commands.Success("Страница " + pnum));
                    if (page.blogs.size() < 20)
                        last = true;
                    Static.handler.post(new Runnable() {
                        @Override
                        public void run() {
                            blogs = new ArrayList<>(page.blogs);
                            ((BaseAdapter) list.getAdapter()).notifyDataSetChanged();
                            Static.bus.send(new E.Commands.Finished());
                        }
                    });
                } catch (MoonlightFail e) {
                    Static.bus.send(new E.Commands.Failure("Не вышло загрузить блоги ._."));
                }
            }
        }).start();
    }

    @Override
    protected View create(LayoutInflater inflater, ViewGroup viewGroup, Context context) {
        View view = inflater.inflate(R.layout.part_blog_list, viewGroup, false);

        list = (ListView) view.findViewById(R.id.blogs);
        list.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return blogs.size();
            }

            @Override
            public Object getItem(int position) {
                return blogs.get(position);
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                final Blog blog = blogs.get(position);
                BlogPart part = new BlogPart(blog);

                if (convertView == null)
                    convertView = part.create(LayoutInflater.from(parent.getContext()), parent, parent.getContext());
                else
                    part.convert(convertView);
                convertView.findViewById(R.id.title).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Static.bus.send(new E.Commands.Run("page load " + blog.resolveURL()));
                    }
                });

                return convertView;
            }
        });

        view.findViewById(R.id.fwd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!last)
                    fetch(page.page + 1);
                System.out.println(page.page);
            }
        });


        view.findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (page.page > 1)
                    fetch(page.page - 1);
                System.out.println(page.page);
            }
        });
        fetch(1);
        return view;
    }


}
