package com.orangefunction.tomcat.redissessions;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

public class SessionSerializationMetadataTest {
	
	private final static byte[] HASH = "TestHash123".getBytes();
	
	SessionSerializationMetadata serialization, serialization2;

	@Before
	public void setUp() throws Exception {
		serialization = new SessionSerializationMetadata();
		serialization2 = new SessionSerializationMetadata();
	}

	@Test
	public void testGet() {
		assertEquals(0, serialization.getSessionAttributesHash().length);
	}
	
	@Test
	public void testSet() {
		serialization.setSessionAttributesHash(HASH);
		assertTrue(Arrays.equals(HASH, serialization.getSessionAttributesHash()));
	}
	
	@Test
	public void testCopy() {
		serialization.setSessionAttributesHash(HASH);
		serialization2.copyFieldsFrom(serialization);
		assertTrue(Arrays.equals(serialization.getSessionAttributesHash(), serialization2.getSessionAttributesHash()));
	}

}
