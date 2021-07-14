/*
 * Copyright (c) 2020, ineter contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package co.ipregistry.ineter.range;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

@RunWith(JUnitPlatform.class)
class IpRangeUtilsTest {

	private static final BiFunction<String, String, Ipv4Range> IPv4_RANGE_PRODUCER = Ipv4Range::of;
	private static final Function<String, Ipv4Subnet> IPv4_SUBNET_PRODUCER = Ipv4Subnet::of;

	@Test
	void parseRange() {
		final String from = "127.0.0.1-127.0.0.2";
		final Ipv4Range iPv4Addresses = IpRangeUtils.parseRange(from, IPv4_RANGE_PRODUCER, IPv4_SUBNET_PRODUCER);

		final Ipv4Range range = Ipv4Range.of("127.0.0.1", "127.0.0.2");

		assertEquals(range, iPv4Addresses);
	}

	@Test
	void parseSubnetAsRange() {
		final String from = "172.20.88.0/24";
		final Ipv4Range iPv4Addresses = IpRangeUtils.parseRange(from, IPv4_RANGE_PRODUCER, IPv4_SUBNET_PRODUCER);

		final Ipv4Subnet range = Ipv4Subnet.of("172.20.88.0", 24);

		assertEquals(range, iPv4Addresses);
	}

	@Test
	void throwOnNonsenseOnRange() {
		final String from = "127-127-127";
		assertThrows(IllegalArgumentException.class,
				() -> IpRangeUtils.parseRange(from, IPv4_RANGE_PRODUCER, IPv4_SUBNET_PRODUCER));
	}

	@Test
	void parseSubnet() {
		final String from = "172.20.88.0/24";
		final Ipv4Subnet parsedSubnet = IpRangeUtils.parseSubnet(from, Ipv4Subnet::of, (byte) 32);

		final Ipv4Subnet subnet = Ipv4Subnet.of("172.20.88.0/24");
		assertEquals(subnet, parsedSubnet);
	}

	@Test
	void parseSingleAddressSubnet() {
		final String from = "172.20.88.1";
		final Ipv4Subnet parsedSubnet = IpRangeUtils.parseSubnet(from, Ipv4Subnet::of, (byte) 32);

		final Ipv4Subnet subnet = Ipv4Subnet.of("172.20.88.1/32");
		assertEquals(subnet, parsedSubnet);
	}

	@Test
	void throwOnNonsenseOnSubnet() {
		final String from = "127/127/127";
		assertThrows(IllegalArgumentException.class, () -> IpRangeUtils.parseSubnet(from, Ipv4Subnet::of, (byte) 32));
	}

	@Test
	void throwOnInvalidSplit() {
		assertThrows(IllegalArgumentException.class,
				() -> IpRangeUtils.parseRange("0.0.0.0-1.1.1.1-", Ipv4Range::of, Ipv4Subnet::of));
		assertThrows(IllegalArgumentException.class,
				() -> IpRangeUtils.parseRange("-0.0.0.0-1.1.1.1", Ipv4Range::of, Ipv4Subnet::of));
		assertThrows(IllegalArgumentException.class,
				() -> IpRangeUtils.parseSubnet("0.0.0.0/24/", Ipv4Subnet::of, (byte) 32));
		assertThrows(IllegalArgumentException.class,
				() -> IpRangeUtils.parseSubnet("/0.0.0.0/24", Ipv4Subnet::of, (byte) 32));
	}
}
