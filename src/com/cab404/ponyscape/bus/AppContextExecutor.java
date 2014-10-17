package com.cab404.ponyscape.bus;

import com.cab404.ponyscape.utils.Static;

import java.util.concurrent.Executor;

/**
 * @author cab404
 */
public class AppContextExecutor implements Executor {

	@Override public void execute(Runnable runnable) {
		Static.handler.post(runnable);
	}

}
