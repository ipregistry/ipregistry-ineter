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

import co.ipregistry.ineter.base.Ipv6Address;

@RunWith(JUnitPlatform.class)
public class Ipv6SubnetTest {

	@Test
	void constructors() {
		final Ipv6Subnet a = Ipv6Subnet.of("::/16");
		final Ipv6Subnet b = Ipv6Subnet.of(Ipv6Address.of("::"), (byte) 16);
		final Ipv6Subnet c = Ipv6Subnet.of("::", (byte) 16);

		Assertions.assertEquals(a.getFirst(), Ipv6Address.of("::"));
		Assertions.assertEquals(a.getLast(), Ipv6Address.of("1::").previous());
		assertEquals(a, b);
		assertEquals(b, c);
		assertThrows(IllegalArgumentException.class, () -> Ipv6Subnet.of("::/-0"));
		assertThrows(IllegalArgumentException.class, () -> Ipv6Subnet.of("::"));
		assertThrows(IllegalArgumentException.class, () -> Ipv6Subnet.of("::/0/4"));
		assertThrows(IllegalArgumentException.class, () -> Ipv6Subnet.of("::/0/"));
	}

	@Test
	void getters() {
		final Ipv6Subnet subnet = Ipv6Subnet.of("1234::/16");

		Assertions.assertEquals(subnet.getFirst(), Ipv6Address.of("1234::"));
		Assertions.assertEquals(subnet.getLast(), Ipv6Address.of("1235::").previous());
		assertEquals(16, subnet.getNetworkBitCount());
		assertEquals(112, subnet.getHostBitCount());
		assertEquals(subnet.getNetworkMask(), Ipv6Address.of("ffff::"));
		assertEquals(subnet.getNetworkAddress(), Ipv6Address.of("1234::"));
		assertEquals("1234:0:0:0:0:0:0:0/16", subnet.toString());
	}

	@Test
	void equality() {
		final Ipv6Subnet subnet1 = Ipv6Subnet.of("1234::/16");
		final Ipv6Subnet subnet2 = Ipv6Subnet.of("1234::/16");
		assertEquals(subnet1, subnet2);
		assertEquals(subnet1.hashCode(), subnet2.hashCode());
	}

	@Test
	void unequal() {
		final List<Ipv6Subnet> l = Arrays.asList(Ipv6Subnet.of("1234::/16"), Ipv6Subnet.of("1234::/17"),
				Ipv6Subnet.of("1234::/15"), Ipv6Subnet.of("::1234/128"), Ipv6Subnet.of("::1234/127"));

		for (final Ipv6Subnet s1 : l) {
			for (final Ipv6Subnet s2 : l) {
				if (!(s1 == s2)) {
					assertNotEquals(s1, s2);
				}
			}
		}
	}

	@Test
	void unequalToNull() {
		assertNotEquals(Ipv6Subnet.of("::/24"), null);
	}

	@Test
	void unequalToObject() {
		assertNotEquals(new Object(), Ipv6Subnet.of("::/24"));
	}

	@Test
	void equalToRangeWithSameAddresses() {
		final Ipv6Subnet subnet1 = Ipv6Subnet.of("1234::/16");
		final Ipv6Range subnet2 = Ipv6Range.parse("1234::-1234:ffff:ffff:ffff:ffff:ffff:ffff:ffff");
		assertEquals(subnet1, subnet2);
		assertEquals(subnet2, subnet1);
		assertEquals(subnet1.hashCode(), subnet2.hashCode());
	}

	@Test
	void parseSingleAddress() {
		final String address = "1234::";
		final Ipv6Subnet parsedSubnet = Ipv6Subnet.parse(address);
		final Ipv6Subnet subnet = Ipv6Subnet.of("1234::/128");
		assertEquals(subnet, parsedSubnet);
	}

	@Test
	void parseCidr() {
		final String address = "1234::/64";
		final Ipv6Subnet parsedSubnet = Ipv6Subnet.parse(address);
		final Ipv6Subnet subnet = Ipv6Subnet.of("1234::", 64);
		assertEquals(subnet, parsedSubnet);
	}

	@Test
	void validAndInvalidMaskTest() {
		for (int i = 0; i <= 128; i++) {
			assertNotNull(Ipv6Subnet.IPv6SubnetMask.fromMaskLen(i));
		}
		for (int i = -100; i < 0; i++) {
			final int j = i;
			assertThrows(IllegalArgumentException.class, () -> Ipv6Subnet.IPv6SubnetMask.fromMaskLen(j));
		}
		for (int i = 129; i < 200; i++) {
			final int j = i;
			assertThrows(IllegalArgumentException.class, () -> Ipv6Subnet.IPv6SubnetMask.fromMaskLen(j));
		}
	}
}
