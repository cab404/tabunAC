package com.cab404.ponyscape.parts;

import android.view.View;

/**
 * @author cab404
 */

public class Credits extends AbstractTextPart {

    @Override protected CharSequence getText() {
        return " — Всю эту байду написал cab404. Кроме Android, да. Android написала команда архиняш. А, и Apache HTTP Library, тоже няши, да. Вот.";
    }

    @Override public void onClick(View view) {
        delete();
    }
}
