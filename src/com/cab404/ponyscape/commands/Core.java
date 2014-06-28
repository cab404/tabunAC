package com.cab404.ponyscape.commands;

import com.cab404.jconsol.annotations.Command;
import com.cab404.jconsol.annotations.CommandClass;
import com.cab404.ponyscape.Static;
import com.cab404.ponyscape.events.Events;
import com.cab404.ponyscape.parts.Credits;
import com.cab404.ponyscape.parts.Help;
import com.cab404.ponyscape.utils.Bus;

/**
 * @author cab404
 */
@CommandClass(prefix = "")
public class Core {


    @Command(command = "help")
    public void displayHelp() {
        Static.list.add(new Help());
    }

    @Command(command = "credits")
    public void displayCredits() {
        Static.list.add(new Credits());
    }


    @Command(command = "clear")
    public void clear() {
        while (Static.list.size() > 0)
            Static.list.remove(Static.list.partAt(0));
    }

    @Command(command = "login")
    public void login() {
        Bus.send(new Events.LoginRequested());
    }

}
