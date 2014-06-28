package com.cab404.ponyscape.commands;

import com.cab404.jconsol.annotations.Command;
import com.cab404.jconsol.annotations.CommandClass;
import com.cab404.jconsol.converters.Int;
import com.cab404.libtabun.data.Comment;
import com.cab404.libtabun.data.Topic;
import com.cab404.libtabun.pages.TopicPage;
import com.cab404.ponyscape.Static;
import com.cab404.ponyscape.parts.AbstractTextPart;
import com.cab404.ponyscape.parts.CommentPart;
import com.cab404.ponyscape.parts.TopicPart;

/**
 * @author cab404
 */
@CommandClass(prefix = "post")
public class Load {

    @Command(command = "load", params = Int.class)
    public void post(final Integer id) {
        final AbstractTextPart loading = new AbstractTextPart() {
            @Override protected CharSequence getText() {
                return "Загружаю пост...";
            }
        };
        Static.list.add(loading);
        new Thread(new Runnable() {
            @Override public void run() {
                new TopicPage(id) {
                    @Override public void handle(final Object object, final int key) {
                        super.handle(object, key);
                        switch (key) {
                            case BLOCK_TOPIC_HEADER:
                                Static.handler.post(new Runnable() {
                                    @Override public void run() {
                                        Static.list.add(new TopicPart((Topic) object));
                                    }
                                });
                                break;
                            case BLOCK_COMMENT:
                                Static.handler.post(new Runnable() {
                                    @Override public void run() {
                                        Static.list.add(new CommentPart((Comment) object));
                                    }
                                });
                                break;
                        }
                    }
                }.fetch(Static.user);
                Static.handler.post(new Runnable() {
                    @Override public void run() {
                        loading.delete();
                    }
                });
            }
        }).start();
    }


}
