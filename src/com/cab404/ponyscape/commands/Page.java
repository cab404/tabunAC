package com.cab404.ponyscape.commands;

import com.cab404.jconsol.annotations.Command;
import com.cab404.jconsol.annotations.CommandClass;
import com.cab404.jconsol.converters.Str;
import com.cab404.libtabun.data.Topic;
import com.cab404.libtabun.modules.TopicModule;
import com.cab404.libtabun.pages.TabunPage;
import com.cab404.moonlight.framework.ModularBlockParser;
import com.cab404.ponyscape.utils.Static;
import com.cab404.ponyscape.parts.StaticTextPart;
import com.cab404.ponyscape.parts.TopicPart;

/**
 * @author cab404
 */
@CommandClass(prefix = "page")
public class Page {

    @Command(command = "load", params = Str.class)
    public void load(String str) {
        final StaticTextPart loading = new StaticTextPart();
        final String address = str.contains("/") ? str : "/blog/" + str;

        Static.list.add(loading);
        loading.setText("Загружаю список...");

        new Thread(new Runnable() {
            @Override public void run() {
                new TabunPage() {

                    @Override public String getURL() {
                        return address;
                    }

                    @Override protected void bindParsers(ModularBlockParser base) {
                        super.bindParsers(base);
                        base.bind(new TopicModule(TopicModule.Mode.LIST), BLOCK_TOPIC_HEADER);
                    }

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
