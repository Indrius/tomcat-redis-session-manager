package com.orangefunction.tomcat.redissessions;

import java.io.Serializable;
import java.util.UUID;

import org.apache.catalina.Container;
import org.apache.catalina.Session;
import org.apache.catalina.core.StandardContext;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

public abstract class AbstractRedisSessionManagerTest {
	
	protected static final String KEY1 = "key1";
	protected static final String KEY2 = "key2";
	protected static final String VALUE1 = "value1";
	
	protected RedisSessionManager mgr;

	@Before
	public void setUp() throws Exception {
		final Container ctx = new StandardContext();
		ctx.setName("Ctx");
		final StandardContext parent = new StandardContext();
		parent.setName("Parent");
		ctx.setParent(parent);
		ctx.getPipeline().addValve(new RedisSessionHandlerValve());
		mgr = new RedisSessionManager();
		mgr.setContainer(ctx);
		mgr.start();
	}
	
	@After
	public void tearDown() throws Exception {
		mgr.clear();
		mgr.stop();
	}
	
	protected Session createSession() {
		Session s = mgr.createSession(UUID.randomUUID().toString());
		checkSession(s);
		return s;
	}
	
	protected void checkSession(Session s) {
		Assert.assertNotNull(s);
		RedisSession ss = (RedisSession) s;
		Assert.assertFalse(ss.isDirty());
	}
	
	protected static class S implements Serializable {
		private static final long serialVersionUID = -7235285032834685357L;
		protected final String a;
		protected final S s;
		protected S(String a, S s) {
			this.a = a;
			this.s = s;
		}
	}

}
