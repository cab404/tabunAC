package com.cab404.ponyscape.utils;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Thread pools for various things.
 *
 * @author cab404
 */
public class Pools {
	/**
	 * Операции над картинками - скалирование, блюр, всё такое.
	 */
	public ThreadPoolExecutor
			img_oper = new ThreadPoolExecutor(1, 8, 5, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>()),
			img_load = new ThreadPoolExecutor(2, 16, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>())
//			,http_oper = new ThreadPoolExecutor(1, 1, 5, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
					;

}
