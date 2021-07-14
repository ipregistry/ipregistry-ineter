/*
 * Copyright (c) 2020, ineter contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package co.ipregistry.ineter.base;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import co.ipregistry.ineter.range.Ipv4Range;
import co.ipregistry.ineter.range.Ipv4Subnet;

@SuppressWarnings("JUnit5Platform")
@RunWith(JUnitPlatform.class)
public class Ipv4AddressTest {

	@ParameterizedTest
	@ValueSource(strings = { "255.255.255.255", "0.0.0.0", "1.2.3.4" })
	void equality(String ipStr) {
		Ipv4Address ip1 = Ipv4Address.of(ipStr);
		Ipv4Address ip2 = Ipv4Address.of(ipStr);
		assertEquals(ip1, ip2);
		assertEquals(ip1.hashCode(), ip2.hashCode());
		assertNotSame(ip1, ip2);
	}

	@Test
	void version() {
		Ipv4Address ip1 = Ipv4Address.of("1.2.3.4");
		assertEquals(4, ip1.version());
	}

	@Test
	void unequalToObject() {
		Ipv4Address ip1 = Ipv4Address.of("1.2.3.4");
		assertNotEquals(new Object(), ip1);
	}

	@Test
	void unequalToNull() {
		Ipv4Address ip1 = Ipv4Address.of("1.2.3.4");
		assertNotEquals(ip1, null);
	}

	@ParameterizedTest
	@CsvSource({ "255.255.255.255,127.255.255.255", "0.0.0.0,255.255.255.255", "1.2.3.4,0.0.0.0",
			"127.255.255.255,1.2.3.4" })
	void inequality(String ipStr1, String ipStr2) {
		Ipv4Address ip1 = Ipv4Address.of(ipStr1);
		Ipv4Address ip2 = Ipv4Address.of(ipStr2);
		assertNotEquals(ip1, ip2);
		assertNotSame(ip1, ip2);
	}

	@ParameterizedTest
	@CsvSource({ "0.0.0.0,255.255.255.255", "127.255.255.0, 128.0.0.0" })
	void ordering(String ipStr1, String ipStr2) {
		Ipv4Address ip1 = Ipv4Address.of(ipStr1);
		Ipv4Address ip2 = Ipv4Address.of(ipStr2);
		assertEquals(-1, ip1.compareTo(ip2));
		assertEquals(1, ip2.compareTo(ip1));
		// noinspection EqualsWithItself
		assertEquals(0, ip1.compareTo(ip1));
		// noinspection EqualsWithItself
		assertEquals(0, ip2.compareTo(ip2));

		assertEquals(1, ip1.compareTo(null));
		assertEquals(1, ip2.compareTo(null));
	}

	@ParameterizedTest
	@ValueSource(strings = { "asdfasf", "260.52.123.260", "1.2.3.4.5", "1.2.3", "1.2.3.4.", "1.2.-0.4", ".10.20.30",
			"10.20.30.", "10.20..30", "123", "12.34.56.ab", "100.100.100" })
	void badStringConstructor(String ipStr) {
		assertThrows(IllegalArgumentException.class, () -> Ipv4Address.of(ipStr));
	}

	@Test
	void nullStringConstructor() {
		String a = null;
		// noinspection ConstantConditions
		assertThrows(NullPointerException.class, () -> Ipv4Address.of(a));
	}

	@Test
	void inetAddressConstructor() {
		try {
			assertEquals("8.8.8.8", Ipv4Address.of((Inet4Address) InetAddress.getByName("8.8.8.8")).toString());
		} catch (UnknownHostException e) {
			fail(e);
		}
	}

	@Test
	void inet4AddressConstructor() {
		try {
			Inet4Address a = (Inet4Address) InetAddress.getByName("8.8.8.8");
			Ipv4Address.of(a);
		} catch (UnknownHostException e) {
			fail(e);
		}
	}

	@Test
	void byteArrayConstructor() {
		Ipv4Address ip = Ipv4Address.of(new byte[] { 1, 2, 3, 4 });
		assertEquals("1.2.3.4", ip.toString());
		assertThrows(IllegalArgumentException.class, () -> Ipv4Address.of(new byte[] { 1, 2, 3 }));
		assertThrows(IllegalArgumentException.class, () -> Ipv4Address.of(new byte[] { 1, 2, 3, 4, 5 }));
		assertThrows(NullPointerException.class, () -> Ipv4Address.of((byte[]) null));
	}

	@ParameterizedTest
	@CsvSource({ "10.0.0.0,1,10.0.0.1", "0.0.0.0,7fffffff,127.255.255.255", "127.255.255.255,1,128.0.0.0",
			"255.255.255.255,1,0.0.0.0" })
	void plusMinus(String ipStr1, String i, String ipStr2) {
		Ipv4Address ip1 = Ipv4Address.of(ipStr1);
		Ipv4Address ip2 = Ipv4Address.of(ipStr2);
		int j = Integer.parseInt(i, 16);
		assertEquals(ip1.plus(j), ip2);
		assertEquals(ip2.minus(j), ip1);
	}

	@ParameterizedTest
	@CsvSource({ "10.0.0.0,10.0.0.1", "0.0.0.0,0.0.0.1", "127.255.255.255,128.0.0.0", "255.255.255.255,0.0.0.0" })
	void nextPrev(String ipStr1, String ipStr2) {
		Ipv4Address ip1 = Ipv4Address.of(ipStr1);
		Ipv4Address ip2 = Ipv4Address.of(ipStr2);
		assertEquals(ip1.next(), ip2);
		assertEquals(ip2.previous(), ip1);
	}

	@Test
	void toStr() {
		Ipv4Address ip = Ipv4Address.of("130.123.1.2");
		assertEquals("130.123.1.2", ip.toString());
	}

	@Test
	void toInetAddress() {
		Ipv4Address ip = Ipv4Address.of("130.123.1.2");
		try {
			assertEquals(InetAddress.getByName("130.123.1.2"), ip.toInet4Address());
		} catch (UnknownHostException e) {
			fail(e);
		}
	}

	@Test
	void toInt() {
		Ipv4Address ip = Ipv4Address.of("130.123.1.2");
		assertEquals(0x827b0102, ip.toInt());
		assertEquals(BigInteger.valueOf(0x827b0102L), ip.toBigInteger());
		assertEquals(BigInteger.valueOf(0xffffffff827b0102L), ip.toSignedBigInteger());
	}

	@Test
	void toArray() {
		Ipv4Address ip = Ipv4Address.of("130.123.1.2");
		assertArrayEquals(ip.toArray(), new byte[] { (byte) 130, 123, 1, 2 });
		assertArrayEquals(ip.toBigEndianArray(), new byte[] { (byte) 130, 123, 1, 2 });
		assertArrayEquals(ip.toLittleEndianArray(), new byte[] { 2, 1, 123, (byte) 130 });
	}

	@Test
	void distanceTo() {
		assertEquals(2, Ipv4Address.of("127.0.0.1").distanceTo(Ipv4Address.of("127.0.0.3")));
		assertEquals(-2, Ipv4Address.of("127.0.0.3").distanceTo(Ipv4Address.of("127.0.0.1")));
		assertEquals(1, Ipv4Address.of("127.255.255.255").distanceTo(Ipv4Address.of("128.0.0.0")));
		assertEquals(-1, Ipv4Address.of("128.0.0.0").distanceTo(Ipv4Address.of("127.255.255.255")));
		assertEquals(0, Ipv4Address.of("1.2.3.4").distanceTo(Ipv4Address.of("1.2.3.4")));
		assertEquals(0x0ffffffffL, Ipv4Address.of("0.0.0.0").distanceTo(Ipv4Address.of("255.255.255.255")));
		assertEquals(-0x0ffffffffL, Ipv4Address.of("255.255.255.255").distanceTo(Ipv4Address.of("0.0.0.0")));
	}

	@Test
	void isAdjacent() {
		assertTrue(Ipv4Address.of("127.0.0.1").isAdjacentTo(Ipv4Address.of("127.0.0.2")));
		assertFalse(Ipv4Address.of("127.0.0.1").isAdjacentTo(Ipv4Address.of("127.0.0.1")));
		assertTrue(Ipv4Address.of("0.0.0.0").isAdjacentTo(Ipv4Address.of("0.0.0.1")));
		assertTrue(Ipv4Address.of("255.255.255.255").isAdjacentTo(Ipv4Address.of("255.255.255.254")));
		assertFalse(Ipv4Address.of("127.0.0.1").isAdjacentTo(Ipv4Address.of("127.0.0.3")));
	}

	@Test
	void toRange() {
		Assertions.assertEquals(Ipv4Range.of("0.0.0.0", "1.2.3.4"),
				Ipv4Address.of("0.0.0.0").toRange(Ipv4Address.of("1.2.3.4")));
		assertEquals(Ipv4Range.of("0.0.0.0", "1.2.3.4"), Ipv4Address.of("1.2.3.4").toRange(Ipv4Address.of("0.0.0.0")));
	}

	@Test
	void toSubnet() {
		Assertions.assertEquals(Ipv4Subnet.of("1.2.3.4/32"), Ipv4Address.of("1.2.3.4").toSubnet());
	}

	@Test
	void and() {
		assertEquals(Ipv4Address.of("1.2.3.0"), Ipv4Address.of("1.2.3.4").and(Ipv4Address.of("255.255.255.0")));
	}

	@Test
	void or() {
		assertEquals(Ipv4Address.of("1.2.3.255"), Ipv4Address.of("1.2.3.0").or(Ipv4Address.of("0.0.0.255")));
	}

	@Test
	void xor() {
		assertEquals(Ipv4Address.of("1.2.0.255"), Ipv4Address.of("1.2.255.0").xor(Ipv4Address.of("0.0.255.255")));
	}

	@Test
	void not() {
		assertEquals(Ipv4Address.of("0.255.0.255"), Ipv4Address.of("255.0.255.0").not());
	}
}
