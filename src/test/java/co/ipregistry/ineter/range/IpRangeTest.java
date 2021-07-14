/*
 * Copyright (c) 2020, ineter contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package co.ipregistry.ineter.range;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import co.ipregistry.ineter.base.Ipv4Address;

@RunWith(JUnitPlatform.class)
class IpRangeTest {

	static class TestIPRange extends Ipv4Range {
		private static final long serialVersionUID = 1L;
		boolean intLengthUsed = false;
		boolean lengthUsed = false;
		private int intLength;

		public TestIPRange(Ipv4Address firstAddress, Ipv4Address lastAddress, int intLength) {
			super(firstAddress, lastAddress);
			this.intLength = intLength;
		}

		@Override
		public int intLength() {
			this.intLengthUsed = true;
			return this.intLength;
		}

		@Override
		public Long length() {
			this.lengthUsed = true;
			return super.length();
		}
	}

	@Test
	void testListEqualToIter() {
		Ipv4Range range = Ipv4Range.of("10.0.0.0", "10.0.0.100");
		List<Ipv4Address> list = range.toList();
		assertEquals(range.intLength(), list.size());
		Iterator<Ipv4Address> iterator = range.iterator();
		int i = 0;
		while (iterator.hasNext()) {
			assertEquals(iterator.next(), list.get(i++));
		}
	}

	@Test
	void testToListUsesIntLength_NotLength() {
		TestIPRange range = new TestIPRange(Ipv4Address.of("10.0.0.0"), Ipv4Address.of("10.0.0.100"), 5);
		List<Ipv4Address> list = range.toList();
		assertEquals(range.intLength(), list.size());
		assertTrue(range.intLengthUsed);
		assertFalse(range.lengthUsed);
	}

}
