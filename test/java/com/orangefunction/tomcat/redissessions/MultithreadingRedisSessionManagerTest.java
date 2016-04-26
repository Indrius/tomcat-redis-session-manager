package com.orangefunction.tomcat.redissessions;

import java.io.IOException;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MultithreadingRedisSessionManagerTest extends AbstractRedisSessionManagerTest {
	
	private static final long DELAY_PER_THREAD = 50L;

	private static final int NUMBER_OF_THREADS = 25;
	
	private ExecutorService service;
	private CyclicBarrier waitForThreadsToReadSession;
	private CyclicBarrier waitForCompletion;
	private RedisSession s1;
	
	@Before
	public void setup2() {
		service = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
		waitForThreadsToReadSession = new CyclicBarrier(NUMBER_OF_THREADS);
		waitForCompletion = new CyclicBarrier(1 + NUMBER_OF_THREADS);
		s1 = (RedisSession) createSession();
		s1.setAttribute(KEY1, VALUE1);
		mgr.add(s1);
		mgr.afterRequest();
	}
	
	@After
	public void tearDown2() {
		if (null != service) {
			service.shutdownNow();
		}
	}

	@Test
	public void testListWinPolicy() throws IOException, InterruptedException, BrokenBarrierException, TimeoutException {
		for (int i = 0; i < NUMBER_OF_THREADS; i++) {
			final Integer threadNumber = i;
			service.submit(new Runnable() {
				@Override
				public void run() {
					try {
						RedisSession s = (RedisSession) mgr.findSession(s1.getId());
						System.out.println("Thread: " + threadNumber + " read the session: " + s);
						// wait for all other threads to load the same session copy
						waitForThreadsToReadSession.await(5, TimeUnit.SECONDS);
						// make sure threads will update Redis in the same order they were started:
						Thread.sleep(DELAY_PER_THREAD * threadNumber);
						s.setAttribute(KEY1, threadNumber);
						mgr.save(s, true);
						mgr.afterRequest();
						System.out.println("Thread: " + threadNumber + " saved the session: " + s + " with value: " + s.getAttribute(KEY1));
						waitForCompletion.await();
					} catch (Exception e) {
						e.printStackTrace();
						throw new RuntimeException(e);
					}
				}
			});
		}
		
		waitForCompletion.await(/*double just in case*/2L * DELAY_PER_THREAD * NUMBER_OF_THREADS, TimeUnit.MILLISECONDS);
		
		RedisSession s = (RedisSession) mgr.findSession(s1.getId());
		Assert.assertEquals(Integer.valueOf(NUMBER_OF_THREADS - 1), s.getAttribute(KEY1));
	}

}
