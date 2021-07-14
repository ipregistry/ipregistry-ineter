/*
 * Copyright (c) 2020, ineter contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package co.ipregistry.ineter.range;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import co.ipregistry.ineter.base.Ipv4Address;

@RunWith(JUnitPlatform.class)
public class Ipv4SubnetTest {

	@Test
	void constructors() {
		final Ipv4Subnet a = Ipv4Subnet.of("10.0.0.0/8");
		final Ipv4Subnet b = Ipv4Subnet.of(Ipv4Address.of("10.0.0.0"), 8);
		final Ipv4Subnet c = Ipv4Subnet.of("10.0.0.0", 8);

		Assertions.assertEquals(a.getFirst(), Ipv4Address.of("10.0.0.0"));
		Assertions.assertEquals(a.getLast(), Ipv4Address.of("10.255.255.255"));
		assertEquals(a, b);
		assertEquals(b, c);
		assertThrows(IllegalArgumentException.class, () -> Ipv4Subnet.of("0.0.0.0/-0"));
		assertThrows(IllegalArgumentException.class, () -> Ipv4Subnet.of("0.0.0.0"));
		assertThrows(IllegalArgumentException.class, () -> Ipv4Subnet.of("0.0.0.0/0/2"));
		assertThrows(IllegalArgumentException.class, () -> Ipv4Subnet.of("0.0.0.0/0/"));
	}

	@Test
	void getters() {
		final Ipv4Subnet subnet = Ipv4Subnet.of("192.168.1.0/24");

		Assertions.assertEquals(subnet.getFirst(), Ipv4Address.of("192.168.1.0"));
		Assertions.assertEquals(subnet.getLast(), Ipv4Address.of("192.168.1.255"));
		assertEquals(24, subnet.getNetworkBitCount());
		assertEquals(8, subnet.getHostBitCount());
		Assertions.assertEquals(subnet.getNetworkMask(), Ipv4Address.of("255.255.255.0"));
		Assertions.assertEquals(subnet.getNetworkAddress(), Ipv4Address.of("192.168.1.0"));
		assertEquals("192.168.1.0/24", subnet.toString());
	}

	@Test
	void equality() {
		final Ipv4Subnet subnet1 = Ipv4Subnet.of("192.168.1.0/24");
		final Ipv4Subnet subnet2 = Ipv4Subnet.of("192.168.1.0/24");
		assertEquals(subnet1, subnet2);
		assertEquals(subnet1.hashCode(), subnet2.hashCode());
	}

	@Test
	void unequal() {
		final List<Ipv4Subnet> l = Arrays.asList(Ipv4Subnet.of("192.168.1.0/24"), Ipv4Subnet.of("192.168.1.0/25"),
				Ipv4Subnet.of("192.168.0.0/24"), Ipv4Subnet.of("192.168.0.0/16"), Ipv4Subnet.of("192.168.0.0/32"));

		for (final Ipv4Subnet s1 : l) {
			for (final Ipv4Subnet s2 : l) {
				if (s1 != s2) {
					assertNotEquals(s1, s2);
				}
			}
		}
	}

	@Test
	void unequalToNull() {
		assertNotEquals(Ipv4Subnet.of("1.2.3.0/24"), null);
	}

	@Test
	void unequalToObject() {
		assertNotEquals(new Object(), Ipv4Subnet.of("1.2.3.0/24"));
	}

	@Test
	void equalToRangeWithSameAddresses() {
		final Ipv4Subnet subnet1 = Ipv4Subnet.of("192.168.1.0/24");
		final Ipv4Range subnet2 = Ipv4Range.parse("192.168.1.0-192.168.1.255");
		assertEquals(subnet1, subnet2);
		assertEquals(subnet2, subnet1);
		assertEquals(subnet1.hashCode(), subnet2.hashCode());
	}

	@Test
	void parseCidr() {
		final String cidr = "192.168.0.0/24";
		final Ipv4Subnet parsedSubnet = Ipv4Subnet.parse(cidr);
		final Ipv4Subnet cidrSubnet = Ipv4Subnet.of(cidr);

		assertEquals(cidrSubnet, parsedSubnet);
	}

	@Test
	void parseSingleAddress() {
		final String address = "172.20.0.1";
		final Ipv4Subnet parsedSubnet = Ipv4Subnet.parse(address);
		final Ipv4Subnet subnet = Ipv4Subnet.of(address, 32);
		assertEquals(subnet, parsedSubnet);
	}

	@Test
	void validAndInvalidMaskTest() {
		for (int i = 0; i <= 32; i++) {
			assertNotNull(Ipv4Subnet.IPv4SubnetMask.fromMaskLen(i));
		}
		for (int i = -100; i < 0; i++) {
			final int j = i;
			assertThrows(IllegalArgumentException.class, () -> Ipv4Subnet.IPv4SubnetMask.fromMaskLen(j));
		}
		for (int i = 33; i < 200; i++) {
			final int j = i;
			assertThrows(IllegalArgumentException.class, () -> Ipv4Subnet.IPv4SubnetMask.fromMaskLen(j));
		}
	}
}
