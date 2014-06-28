package com.cab404.ponyscape.events;

/**
 * @author cab404
 */
public class Events {
    public static class LoginRequested { }
    public static class LoginSuccess { }
    public static class LoginFailure { }

    public static class RunCommand {
        public final String command;
        public RunCommand(String command) {
            this.command = command;
        }
    }
}
