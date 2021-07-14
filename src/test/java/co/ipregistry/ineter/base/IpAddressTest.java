/*
 * Copyright (c) 2020, ineter contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package co.ipregistry.ineter.base;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

@RunWith(JUnitPlatform.class)
public class IpAddressTest {

	@Test
	void ofArrayIPv4() {
		byte[] arr = new byte[] { 1, 2, 3, 4 };
		assertEquals(IpAddress.of(arr), Ipv4Address.of(arr));
		assertThrows(IllegalArgumentException.class, () -> IpAddress.of(new byte[] { 1, 2, 3 }));
		assertThrows(IllegalArgumentException.class, () -> IpAddress.of(new byte[] { 1, 2, 3, 4, 5 }));
	}

	@Test
	void ofStringIPv4() {
		String str = "1.2.3.4";
		assertEquals(IpAddress.of(str), Ipv4Address.of(str));
	}

	@Test
	void ofInetAddressV4() throws UnknownHostException {
		InetAddress addr = InetAddress.getByName("1.2.3.4");
		assertEquals(IpAddress.of(addr), Ipv4Address.of(addr.getAddress()));
	}

	@Test
	void ofArrayIPv6() {
		byte[] arr = new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 };
		assertEquals(IpAddress.of(arr), Ipv6Address.of(arr));
		assertThrows(IllegalArgumentException.class,
				() -> IpAddress.of(new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14 }));
		assertThrows(IllegalArgumentException.class,
				() -> IpAddress.of(new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16 }));
	}

	@Test
	void ofStringIPv6() {
		String str = "[1234:1234:1234:1234:1234:1234:1234:1234]";
		assertEquals(IpAddress.of(str), Ipv6Address.of(str));
	}

	@Test
	void ofInetAddressV6() throws UnknownHostException {
		InetAddress addr = InetAddress.getByName("::");
		assertEquals(IpAddress.of(addr), Ipv6Address.of(addr.getAddress()));
	}

	@Test
	void ofStringBadString() {
		assertThrows(IllegalArgumentException.class, () -> IpAddress.of(":"));
		assertThrows(IllegalArgumentException.class, () -> IpAddress.of(""));
		assertThrows(IllegalArgumentException.class,
				() -> IpAddress.of("this is some random long string that's not an ip address"));
	}
}
