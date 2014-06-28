package com.cab404.ponyscape.parts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.cab404.libtabun.data.Topic;
import com.cab404.ponyscape.utils.TextEscaper;

/**
 * @author cab404
 */
public class TopicPart extends AbstractTextPart {

    private CharSequence text = null;
    private Topic topic;

    public TopicPart(Topic topic) {
        this.topic = topic;
    }

    @Override protected View create(LayoutInflater inflater, ViewGroup viewGroup, final Context context) {
        text = TextEscaper.simpleEscape(topic.text, context);
        updateText();
        return super.create(inflater, viewGroup, context);
    }

    @Override protected CharSequence getText() {
        return text;
    }

}
