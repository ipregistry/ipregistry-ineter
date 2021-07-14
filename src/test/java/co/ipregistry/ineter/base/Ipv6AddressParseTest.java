/*
 * Copyright (c) 2020, ineter contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package co.ipregistry.ineter.base;

import static org.junit.jupiter.api.Assertions.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

@RunWith(JUnitPlatform.class)
public class Ipv6AddressParseTest {

	// Do not rename used by @MethodSource below
	public static List<String> generateIP6AddressStrings() {
		return generateIP6AddressStrings(0, 10_000, true);
	}

	public static List<String> generateIP6AddressStrings(final int seed, final int count, final boolean brackets) {
		final List<String> addresses = new ArrayList<>(count);
		final Random r = new Random(seed);

		for (int i = 0; i < count; i++) {
			final List<String> currentAddress = new ArrayList<>(8);

			for (int j = 0; j < 8; j++) {
				currentAddress.add(Integer.toHexString(r.nextInt(Short.MAX_VALUE + 1)));
			}

			// 50% Chance of double colon
			if (r.nextBoolean()) {
				int first = r.nextInt(8); // First part to erase
				final int last = first + r.nextInt(8 - first); // Last part to erase

				// Set one part to either "", ":" or "::"
				currentAddress.set(first, (first == 0 ? ":" : "") + (last == 7 ? ":" : ""));
				for (int j = ++first; j <= last; j++) { // Delete parts
					currentAddress.remove(first);
				}
			}
			if (brackets && r.nextBoolean()) {
				addresses.add('[' + String.join(":", currentAddress) + ']');
			} else {
				addresses.add(String.join(":", currentAddress));
			}
		}
		return addresses;
	}

	@ParameterizedTest
	@ValueSource(strings = { "abcd:dbca:1234:4321:aabb:bbaa:ccdd:ddcc", "A:B:C:D:E:F:a:B", "::1", "1::", "1::1",
			"1:2:3:4:5:6:7:8", "12:34:56::abcd", "::", "1:02:003:0004::", "1000:200:30:4::" })
	void passeAndToString(final String addressStr) throws UnknownHostException {
		final String java = InetAddress.getByName(addressStr).getHostAddress();
		final Ipv6Address ineter = Ipv6Address.of(addressStr);
		assertFalse(ineter.isZoned());
		assertEquals(java, ineter.toString());
	}

	@ParameterizedTest
	@MethodSource("generateIP6AddressStrings")
	void randomAddressesCompareParsing(final String addressStr) throws UnknownHostException {
		final String java = InetAddress.getByName(addressStr).getHostAddress();
		final String ineter = Ipv6Address.of(addressStr).toString();
		assertEquals(java, ineter);
	}

	@ParameterizedTest
	@ValueSource(strings = { "[::1]", "[1::]", "[1::1]", "[1:2:3:4:5:6:7:8]", "[12:34:56::abcd]", "[::]",
			"[abcd:dbca:1234:4321:aabb:bbaa:ccdd:ddcc]", "[A:B:C:D:E:F:a:B]" })
	void validBrackets(final String addressStr) throws UnknownHostException {
		final String java = InetAddress.getByName(addressStr).getHostAddress();
		final String ineter = Ipv6Address.of(addressStr).toString();
		assertEquals(java, ineter);
	}

	@ParameterizedTest
	@ValueSource(strings = { "::1]", "[1::" })
	void invalidBrackets(final String addressStr) {
		assertThrows(IllegalArgumentException.class, () -> Ipv6Address.of(addressStr));
	}

	@ParameterizedTest
	@ValueSource(strings = { "", "1", "[0000:0000:0000:0000:0000:0000:0000:00001]", })
	void badLength(final String addressStr) {
		try {
			Ipv6Address.of(addressStr);
		} catch (final IllegalArgumentException e) {
			assertTrue(e.getMessage().contains("length"));
			return;
		}
		fail("Exception expected!");
	}

	@ParameterizedTest
	@ValueSource(strings = { "00001:0000:0000:0000:0000:0000:0000:000", "0000:00001:0000:0000:0000:0000:0000:000",
			"0000:0000:00001:0000:0000:0000:0000:000", "0000:0000:0000:00001:0000:0000:0000:000",
			"0000:0000:0000:0000:00001:0000:0000:000", "0000:0000:0000:0000:0000:00001:0000:000",
			"0000:0000:0000:0000:0000:0000:00001:000", "000:0000:0000:0000:0000:0000:0000:00001", "00001:",
			"0000:00001:", "0000:0000:00001::", "0000:0000:0000:00001::", "::00001:0000:0000:0000", "::00001:0000:0000",
			"::00001:0000", "::00001" })
	void tooManyDigitsInPart(final String addressStr) {
		try {
			Ipv6Address.of(addressStr);
		} catch (final IllegalArgumentException e) {
			assertTrue(e.getMessage().contains("digits"));
			return;
		}
		fail("Exception expected!");
	}

	@ParameterizedTest
	@ValueSource(strings = { "1:1", "[1:1]", "1:1:1", "::1:1:1:1:1:1:1:1", "1:1:1:1:1:1:1:1:1", "1:1:1:1:1:1:1:1:1:1",
			"1:1:1:1:1:1:1:1:1:1:1", "1:1:1:1:1:1:1:1:1:1:1:1", "::1:1:1:1:1:1:1:1:1", "1::1:1:1:1:1:1:1:1",
			"1:1::1:1:1:1:1:1:1", "1:1:1::1:1:1:1:1:1", "1:1:1:1::1:1:1:1:1", "1:1:1:1:1::1:1:1:1",
			"1:1:1:1:1:1::1:1:1", "1:1:1:1:1:1:1::1:1", "1:1:1:1:1:1:1:1::1", "1:1:1:1:1:1:1:1:1::", })
	void numberOfParts(final String addressStr) {
		try {
			Ipv6Address.of(addressStr);
		} catch (final IllegalArgumentException e) {
			assertTrue(e.getMessage().contains("parts"));
			return;
		}
		fail("Exception expected!");
	}

	@ParameterizedTest
	@ValueSource(strings = { "1:::1:1:1:1:1:1:1", "1:1:::1:1:1:1:1:1", "1:1:1:::1:1:1:1:1", "1:1:1:1:::1:1:1:1",
			"1:1:1:1:1:::1:1:1", "1:1:1:1:1:1:::1:1", "1:1:1:1:1:1:::1:1", "1:1:1:1:1:1:1:::1", "1:1:1:1:1:1:1:1:::",
			"1::1:1:1:1:1:1::1", "1:1::1:1:1:1:1::1", "1:1:1::1:1:1:1::1", "1:1:1:1::1:1:1::1", "1:1:1:1:1::1:1::1",
			"1:1:1:1:1:1::1::1", "1:1:1:1:1:1::1::1", "::1:1:1:1:1:1:1:1::", })
	void badColons(final String addressStr) {
		try {
			Ipv6Address.of(addressStr);
		} catch (final IllegalArgumentException e) {
			assertTrue(e.getMessage().contains("colon"));
			return;
		}
		fail("Exception expected!");
	}

	@ParameterizedTest
	@CsvSource({ "::1%eth0,eth0", "::%eth0,eth0", "1234:1234:1234:1234:1234:1234:1234:1234%blah,blah" })
	void zoned(final String addressStr, final String zone) {
		final Ipv6Address of = Ipv6Address.of(addressStr);
		assertTrue(of instanceof ZonedIpv6Address);
		assertTrue(of.isZoned());
		final ZonedIpv6Address zoned = (ZonedIpv6Address) of;
		assertEquals(zoned.getZone(), zone);
	}

	@Test
	void illegalChar() {
		final List<Character> charsNoDigits = IntStream.range(0, 128).mapToObj(c -> (char) c)
				.filter(c -> Character.digit(c, 16) == -1)
				.filter(c -> !(c.equals(':') || c.equals('%') || c.equals(']') || c.equals('[')))
				.collect(Collectors.toList());
		for (final Character c : charsNoDigits) {
			try {
				Ipv6Address.of("1::" + c); // After colons
			} catch (final IllegalArgumentException e) {
				assertTrue(e.getMessage().contains("character"));
				continue;
			}
			fail("Exception expected!");
		}

		for (final Character c : charsNoDigits) {
			try {
				Ipv6Address.of(c + "::1"); // Before colons
			} catch (final IllegalArgumentException e) {
				assertTrue(e.getMessage().contains("character"));
				continue;
			}
			fail("Exception expected!");
		}
	}
}
