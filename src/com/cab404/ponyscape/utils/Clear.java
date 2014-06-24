package com.cab404.ponyscape.utils;

import com.cab404.jconsol.annotations.Command;
import com.cab404.jconsol.annotations.CommandClass;
import com.cab404.ponyscape.Static;

/**
 * @author cab404
 */
@CommandClass(prefix = "")
public class Clear {

    @Command(command = "clear")
    public void clear() {
        while (Static.list.size() > 0)
            Static.list.remove(Static.list.partAt(0));
    }
}
