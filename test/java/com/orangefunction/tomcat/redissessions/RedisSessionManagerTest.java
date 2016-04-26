package com.orangefunction.tomcat.redissessions;

import java.io.IOException;

import org.apache.catalina.Session;
import org.junit.Assert;
import org.junit.Test;

public class RedisSessionManagerTest extends AbstractRedisSessionManagerTest {
	
	@Test
	public void testCreateSessionString() {
		createSession();
	}

	@Test
	public void testCreateEmptySession() {
		Session s = mgr.createEmptySession();
		checkSession(s);
	}

	@Test
	public void testAddSession() throws IOException {
		Session s = createSession();
		RedisSession ss = (RedisSession) s;
		ss.setAttribute(KEY1, VALUE1);
		Assert.assertTrue(ss.isDirty());
		Session s2 = mgr.findSession(s.getId());
		mgr.add(s);
		Assert.assertNotNull(s2);
		Assert.assertFalse(ss.isDirty());
	}

	@Test
	public void testFindSessionString() throws IOException {
		Session s1 = createSession();
		Session s2 = createSession();
		mgr.add(s1);
		mgr.add(s2);
		Session sB1 = mgr.findSession(s1.getId());
		Session sB2 = mgr.findSession(s2.getId());
		Assert.assertNotNull(sB1);
		Assert.assertNotNull(sB2);
	}
	
	@Test
	public void testLoadSessionDataFromRedis() throws IOException {
		Session s1 = createSession();
		Session s2 = createSession();
		mgr.add(s1);
		mgr.add(s2);
		byte[] b1 = mgr.loadSessionDataFromRedis(s1.getId());
		byte[] b2 = mgr.loadSessionDataFromRedis(s2.getId());
		Assert.assertNotNull(b1);
		Assert.assertNotNull(b2);
	}

	@Test
	public void testClearAndGetSize() throws IOException {
		Session s1 = createSession();
		mgr.add(s1);
		byte[] b1 = mgr.loadSessionDataFromRedis(s1.getId());
		Assert.assertNotNull(b1);
		Assert.assertEquals(1, mgr.getSize());
		mgr.clear();
		byte[] b2 = mgr.loadSessionDataFromRedis(s1.getId());
		Assert.assertNull(b2);
		Assert.assertEquals(0, mgr.getSize());
	}

	@Test
	public void testKeys() throws IOException {
		Session s1 = createSession();
		String[] keys = mgr.keys();
		Assert.assertEquals(1, keys.length);
		Assert.assertEquals(s1.getId(), keys[0]);
	}

	@Test
	public void testSessionFromSerializedData() throws IOException {
		RedisSession s1 = (RedisSession) createSession();
		s1.setAttribute(KEY1, VALUE1);
		mgr.add(s1);
		byte[] b1 = mgr.loadSessionDataFromRedis(s1.getId());
		DeserializedSessionContainer c = mgr.sessionFromSerializedData(s1.getId(), b1);
		Assert.assertEquals(s1.getAttribute(KEY1), c.session.getAttribute(KEY1));
	}
	
	@Test
	public void testPersistSerializableWithPotentialCircularReferences() throws IOException {
		S parent = new S("value2", null);
		S s = new S(VALUE1, parent);
		
		RedisSession s1 = (RedisSession) createSession();
		s1.setAttribute(KEY2, s);
		mgr.add(s1);
		
		RedisSession s2 = (RedisSession) mgr.findSession(s1.getId());
		Assert.assertTrue(s2.getAttribute(KEY2) instanceof S);
		S sB = (S) s2.getAttribute(KEY2);
		Assert.assertEquals(s.a, sB.a);
		Assert.assertEquals(s.s, sB.s);
		Assert.assertEquals(s.s.a, sB.s.a);
	}

	@Test
	public void testSaveSession() throws IOException {
		RedisSession s1 = (RedisSession) createSession();
		mgr.save(s1);
		RedisSession s2 = (RedisSession) mgr.findSession(s1.getId());
		Assert.assertNotNull(s2);
		Assert.assertEquals(s1.getId(), s2.getId());
	}

	@Test
	public void testSaveSessionNoForce() throws IOException {
		RedisSession s1 = (RedisSession) createSession();
		mgr.save(s1, false);
		RedisSession s2 = (RedisSession) mgr.findSession(s1.getId());
		Assert.assertNotNull(s2);
		Assert.assertEquals(s1.getId(), s2.getId());
	}
	
	@Test
	public void testSaveSessionForce() throws IOException {
		RedisSession s1 = (RedisSession) createSession();
		mgr.save(s1, true);
		RedisSession s2 = (RedisSession) mgr.findSession(s1.getId());
		Assert.assertNotNull(s2);
		Assert.assertEquals(s1.getId(), s2.getId());
	}

	@Test
	public void testRemoveSession() throws IOException {
		RedisSession s1 = (RedisSession) createSession();
		mgr.add(s1);
		RedisSession s2 = (RedisSession) mgr.findSession(s1.getId());
		Assert.assertNotNull(s2);
		mgr.afterRequest();
		mgr.remove(s1);
		RedisSession s3 = (RedisSession) mgr.findSession(s2.getId());
		Assert.assertNull(s3);
	}

	@Test
	public void testAfterRequestForInvalidObject() throws IOException {
		RedisSession s1 = (RedisSession) createSession();
		mgr.add(s1);
		s1.setValid(false);
		mgr.afterRequest();
		RedisSession s2 = (RedisSession) mgr.findSession(s1.getId());
		Assert.assertNull(s2);
	}
	
	@Test
	public void testAfterRequestForValidObject() throws IOException {
		RedisSession s1 = (RedisSession) createSession();
		mgr.add(s1);
		s1.setValid(true);
		mgr.afterRequest();
		RedisSession s2 = (RedisSession) mgr.findSession(s1.getId());
		Assert.assertNotNull(s2);
	}
	
}
