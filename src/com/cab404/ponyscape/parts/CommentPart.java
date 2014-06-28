package com.cab404.ponyscape.parts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.cab404.libtabun.data.Comment;
import com.cab404.ponyscape.utils.TextEscaper;

/**
 * @author cab404
 */
public class CommentPart extends AbstractTextPart {
    private CharSequence text;
    private Comment comment;

    public CommentPart(Comment comment) {
        this.comment = comment;
    }

    @Override protected View create(LayoutInflater inflater, ViewGroup viewGroup, Context context) {
        text = TextEscaper.simpleEscape(comment.text, context);
        updateText();

        return super.create(inflater, viewGroup, context);
    }

    @Override protected CharSequence getText() {
        return text;
    }
}
