package com.cab404.sjbus;

import java.util.concurrent.Executor;

/**
 * @author cab404
 */
public class BlockingExecutor implements Executor {
	@Override public void execute(Runnable runnable) {
		runnable.run();
	}
}
