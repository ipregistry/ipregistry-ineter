/*
 * Copyright (c) 2020, ineter contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package co.ipregistry.ineter.range;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import co.ipregistry.ineter.base.Ipv6Address;

import com.google.common.collect.ImmutableList;

@RunWith(JUnitPlatform.class)
public class Ipv6RangeTest {

	@Test
	void ofAddress() {
		final Ipv6Range range = Ipv6Range.of(Ipv6Address.of("::1"), Ipv6Address.of("1::"));
		Assertions.assertEquals(range.getFirst(), Ipv6Address.of("::1"));
		Assertions.assertEquals(range.getLast(), Ipv6Address.of("1::"));
	}

	@Test
	void ofString() {
		final Ipv6Range range = Ipv6Range.of("::1", "1::");
		Assertions.assertEquals(range.getFirst(), Ipv6Address.of("::1"));
		Assertions.assertEquals(range.getLast(), Ipv6Address.of("1::"));
	}

	@Test
	void ofInetAddress() throws UnknownHostException {
		final Ipv6Range range = Ipv6Range.of((Inet6Address) InetAddress.getByName("::1"),
				(Inet6Address) InetAddress.getByName("1::"));
		Assertions.assertEquals(range.getFirst(), Ipv6Address.of("::1"));
		Assertions.assertEquals(range.getLast(), Ipv6Address.of("1::"));
	}

	@Test
	void ofArray() {
		final Ipv6Range range = Ipv6Range.of(new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 },
				new byte[] { 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });
		Assertions.assertEquals(range.getFirst(), Ipv6Address.of("::1"));
		Assertions.assertEquals(range.getLast(), Ipv6Address.of("1::"));
	}

	@Test
	void invalidRange() {
		assertThrows(IllegalArgumentException.class, () -> Ipv6Range.of("1::", "::1"));
	}

	@Test
	void nullAddress() {
		assertThrows(NullPointerException.class, () -> new Ipv6Range(null, Ipv6Address.of("::1")));
		assertThrows(NullPointerException.class, () -> new Ipv6Range(Ipv6Address.of("::1"), null));
	}

	@Test
	void parse() {
		final Ipv6Range range = Ipv6Range.parse("::-1::");
		Assertions.assertEquals(range.getFirst(), Ipv6Address.of("::"));
		Assertions.assertEquals(range.getLast(), Ipv6Address.of("1::"));
		assertTrue(range.toString().contains("1:0:0:0:0:0:0:0"));
		assertTrue(range.toString().contains("0:0:0:0:0:0:0:0"));
	}

	@ParameterizedTest
	@CsvSource({ "::,::,::", "::,::1234,::1000", "1234::,1234::1234,1234::1000" })
	void contains(final String start, final String end, final String parse) {
		assertTrue(Ipv6Range.parse(start + "-" + end).contains(Ipv6Address.of(parse)));
	}

	@ParameterizedTest
	@CsvSource({ "::,::,::1", "::,::1234,::1235", "1234::,1234::1234,1234::1235" })
	void notContains(final String start, final String end, final String parse) {
		assertFalse(Ipv6Range.parse(start + "-" + end).contains(Ipv6Address.of(parse)));
	}

	@ParameterizedTest
	@CsvSource({ "::,::,::/128", "::,::1234,::1000/120", "1234::,1234::1234,1234::1230/126" })
	void containsRange(final String start, final String end, final String parse) {
		assertTrue(Ipv6Range.parse(start + "-" + end).contains(Ipv6Subnet.of(parse)));
	}

	@ParameterizedTest
	@CsvSource({ "::,::,::1/128", "::,::1234,::/112", "1234::,1234::1234,1235::/16" })
	void notContainsRange(final String start, final String end, final String parse) {
		assertFalse(Ipv6Range.parse(start + "-" + end).contains(Ipv6Subnet.of(parse)));
	}

	@ParameterizedTest
	@CsvSource({ "::,::,::-::", "::,::1234,::1-::2", "1234::,1234::1234,::-1234::", "1::,f::,::-ffff::" })
	void overlaps(final String start, final String end, final String parse) {
		assertTrue(Ipv6Range.parse(start + "-" + end).overlaps(Ipv6Range.parse(parse)));
	}

	@ParameterizedTest
	@CsvSource({ "::,::,::1-::2", "::,::1234,::1235-1::", "1234::,1234::1234,1234::1235-1234::1235" })
	void notOverlaps(final String start, final String end, final String parse) {
		assertFalse(Ipv6Range.parse(start + "-" + end).overlaps(Ipv6Range.parse(parse)));
	}

	@Test
	void equal() {
		final Ipv6Range range1 = Ipv6Range.parse("1234::1234-1234::ffff");
		final Ipv6Range range2 = Ipv6Range.of(Ipv6Address.of("1234::1234"), Ipv6Address.of("1234::ffff"));

		assertEquals(range1, range1);
		assertEquals(range2, range2);

		assertEquals(range1.hashCode(), range2.hashCode());
		assertEquals(range1, range2);
	}

	@Test
	void notEqual() {
		final Ipv6Range range1 = Ipv6Range.parse("1234::1234-1234::ffff");
		final Ipv6Range range2 = Ipv6Range.of(Ipv6Address.of("1234::"), Ipv6Address.of("1234::ffff"));

		assertNotEquals(range1, range2);
	}

	@Test
	void unequalToNull() {
		final Ipv6Range range1 = Ipv6Range.parse("1234::1234-1234::ffff");
		assertNotEquals(range1, null);
	}

	@Test
	void unequalToObject() {
		assertNotEquals(new Object(), Ipv6Range.parse("1234::1234-1234::ffff"));
	}

	@ParameterizedTest
	@CsvSource({ "::-::,1", "::-::fffe,ffff",
			"::-ffff:ffff:ffff:ffff:ffff:ffff:ffff:fffe,ffffffffffffffffffffffffffffffff",
			"::-0000:0000:0000:0000:ffff:ffff:ffff:fffe,0000000000000000ffffffffffffffff",
			"::-0000:0000:0000:0001:ffff:ffff:ffff:fffe,0000000000000001ffffffffffffffff" })
	void length(final String parse, final String length) {
		assertEquals(Ipv6Range.parse(parse).length(), new BigInteger(length, 16));
	}

	@Test
	void iterationOrder() {
		final ArrayList<Ipv6Address> itemList = new ArrayList<>();
		Ipv6Range.of("ffff:ffff:ffff:ffff:ffff:ffff:ffff:ff00", "ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff").iterator()
				.forEachRemaining(itemList::add);

		assertEquals(256, itemList.size());
		assertEquals(itemList.get(0), Ipv6Address.of("ffff:ffff:ffff:ffff:ffff:ffff:ffff:ff00"));
		assertEquals(itemList.get(itemList.size() - 1), Ipv6Address.of("ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff"));

		final ListIterator<Ipv6Address> listIterator = itemList.listIterator();
		Ipv6Address previous = listIterator.next();
		while (listIterator.hasNext()) {
			final Ipv6Address current = listIterator.next();
			assertTrue(current.compareTo(previous) > 0);
			previous = current;
		}
	}

	@Test
	void iterationOrderSkipEdges() {
		final ArrayList<Ipv6Address> itemList = new ArrayList<>();
		Ipv6Range.of("ffff:ffff:ffff:ffff:ffff:ffff:ffff:ff00", "ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff")
				.iterator(true).forEachRemaining(itemList::add);

		assertEquals(254, itemList.size());
		assertEquals(itemList.get(0), Ipv6Address.of("ffff:ffff:ffff:ffff:ffff:ffff:ffff:ff01"));
		assertEquals(itemList.get(itemList.size() - 1), Ipv6Address.of("ffff:ffff:ffff:ffff:ffff:ffff:ffff:fffe"));

		final ListIterator<Ipv6Address> listIterator = itemList.listIterator();
		Ipv6Address previous = listIterator.next();
		while (listIterator.hasNext()) {
			final Ipv6Address current = listIterator.next();
			assertTrue(current.compareTo(previous) > 0);
			previous = current;
		}
	}

	@Test
	void iterationLastElement() {
		final Iterator<Ipv6Address> i = Ipv6Range.of("1234::", "1234::").iterator();
		assertTrue(i.hasNext());
		assertEquals(i.next(), Ipv6Address.of("1234::"));
		assertThrows(NoSuchElementException.class, i::next);
	}

	@Test
	void iterationRemove() {
		final Iterator<Ipv6Address> i = Ipv6Range.of("1234::", "1234::").iterator();
		assertThrows(UnsupportedOperationException.class, i::remove);
	}

	@ParameterizedTest
	@CsvSource({ "::-ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff,0:0:0:0:0:0:0:0/0",
			"::-7fff:ffff:ffff:ffff:ffff:ffff:ffff:ffff,0:0:0:0:0:0:0:0/1",
			"::ffff:ffff:ffff:ffff-::1:0:0:0:1fff,::ffff:ffff:ffff:ffff/128 ::1:0:0:0:0/115",
			"::-1::0:0:0:1234, 0:0:0:0:0:0:0:0/16 1:0:0:0:0:0:0:0/116 1:0:0:0:0:0:0:1000/119 1:0:0:0:0:0:0:1200/123 1:0:0:0:0:0:0:1220/124 1:0:0:0:0:0:0:1230/126 1:0:0:0:0:0:0:1234/128",
			"::ffff:ffff:ffff:ffff-::ffff:ffff:ffff:ffff,0:0:0:0:ffff:ffff:ffff:ffff/128" })
	void toSubnets(final String range, final String subnets) {
		final List<Ipv6Subnet> generated = Ipv6Range.parse(range).toSubnets();
		final List<Ipv6Subnet> manual = Arrays.stream(subnets.split(" ")).map(Ipv6Subnet::of)
				.collect(Collectors.toList());
		assertEquals(generated, manual);
		// noinspection OptionalGetWithoutIsPresent
		assertEquals(manual.stream().map(Ipv6Subnet::length).reduce(BigInteger::add).get(),
				Ipv6Range.parse(range).length());
	}

	@Test
	void singleIPRangeParse() {
		final Ipv6Range explicitRange = Ipv6Range.parse("1234::1234-1234::1234");
		final Ipv6Range range = Ipv6Range.parse("1234::1234");
		assertEquals(explicitRange, range,
				"Single address range doesn't match explicit range with same addresses on both ends.");
	}

	@Test
	void singleIPRangeOfBytes() {
		final Ipv6Range explicitRange = Ipv6Range.of(new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 },
				new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 });
		final Ipv6Range range = Ipv6Range.of(new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 });
		assertEquals(explicitRange, range,
				"Single address range doesn't match explicit range with same addresses on both ends.");
	}

	@Test
	void singleIPRangeOfIPv6Address() {
		final Ipv6Range explicitRange = Ipv6Range.of(Ipv6Address.of("::1"), Ipv6Address.of("::1"));
		final Ipv6Range range = Ipv6Range.of(Ipv6Address.of("::1"));
		assertEquals(explicitRange, range,
				"Single address range doesn't match explicit range with same addresses on both ends.");
	}

	@Test
	void singleIPRangeOfString() {
		final Ipv6Range explicitRange = Ipv6Range.of("1234::1234", "1234::1234");
		final Ipv6Range range = Ipv6Range.of("1234::1234");
		assertEquals(explicitRange, range,
				"Single address range doesn't match explicit range with same addresses on both ends.");
	}

	@Test
	void singleIPRangeOfInet6Address() throws UnknownHostException {
		final Ipv6Range explicitRange = Ipv6Range.of((Inet6Address) InetAddress.getByName("::1"),
				(Inet6Address) InetAddress.getByName("::1"));
		final Ipv6Range range = Ipv6Range.of((Inet6Address) InetAddress.getByName("::1"));
		assertEquals(explicitRange, range,
				"Single address range doesn't match explicit range with same addresses on both ends.");
	}

	@Test
	void parseSubnet() {
		final Ipv6Range range = Ipv6Range.parse("1234::/16");
		Assertions.assertEquals(Ipv6Address.of("1234::"), range.getFirst());
		Assertions.assertEquals(Ipv6Address.of("1235::").previous(), range.getLast());
	}

	@Test
	void shouldMergeAdjacent() {
		final Ipv6Range first = Ipv6Range.of("::1", "::2");
		final Ipv6Range second = Ipv6Range.of("::3", "::4");
		final List<Ipv6Range> merge = Ipv6Range.merge(first, second);
		assertEquals(ImmutableList.of(Ipv6Range.of("::1", "::4")), merge);
	}

	@Test
	void shouldMergeOverlapping() {
		final Ipv6Range first = Ipv6Range.of("::1", "::3");
		final Ipv6Range second = Ipv6Range.of("::2", "::4");
		final List<Ipv6Range> merge = Ipv6Range.merge(first, second);
		assertEquals(ImmutableList.of(Ipv6Range.of("::1", "::4")), merge);
	}

	@Test
	void shouldMergeMixed() {
		final Ipv6Range first = Ipv6Range.of("::1", "::3");
		final Ipv6Range second = Ipv6Range.of("::2", "::4");
		final Ipv6Range third = Ipv6Range.of("::5", "::6");

		final Ipv6Range fourth = Ipv6Range.of("::8", "::10");
		final Ipv6Range fifth = Ipv6Range.of("::8", "::11");

		final Ipv6Range sixth = Ipv6Range.of("2001::", "2002::");

		final List<Ipv6Range> merge = Ipv6Range.merge(sixth, fifth, fourth, third, second, first);
		assertEquals(Arrays.asList(Ipv6Range.of("::1", "::6"), Ipv6Range.of("::8", "::11"),
				Ipv6Range.of("2001::", "2002::")), merge);
	}

	@Test
	void shouldNotMergeSeparated() {
		final Ipv6Range first = Ipv6Range.of("::1", "::3");
		final Ipv6Range second = Ipv6Range.of("::5", "::6");
		final List<Ipv6Range> merge = Ipv6Range.merge(first, second);
		assertEquals(ImmutableList.of(first, second), merge);
	}

	@Test
	void mergeShouldThrowOnNull() {
		assertThrows(NullPointerException.class, () -> Ipv6Range.merge((Ipv6Range) null));
	}

	@Test
	void shouldReturnEmptyOnEmpty() {
		assertTrue(Ipv6Range.merge(Collections.emptyList()).isEmpty());
	}

	@Test
	void testIntLength() {
		assertEquals(256, Ipv6Subnet.of("::/120").intLength());
		assertEquals(8, Ipv6Subnet.of("::/125").intLength());
		assertEquals(Integer.MAX_VALUE, Ipv6Subnet.of("::/64").intLength());
	}

	@Test
	void testWithLast() {
		assertEquals(Ipv6Range.of("::", "1234::"), Ipv6Subnet.of("::/120").withLast(Ipv6Address.of("1234::")));
		assertEquals(Ipv6Range.of("1234::", "1235::"), Ipv6Subnet.of("1234::/16").withLast(Ipv6Address.of("1235::")));
		assertThrows(IllegalArgumentException.class, () -> Ipv6Subnet.of("1234::/16").withLast(Ipv6Address.of("::")));
	}

	@Test
	void testWithFirst() {
		assertEquals(Ipv6Range.of("::5", "::00ff"), Ipv6Subnet.of("::/120").withFirst(Ipv6Address.of("::5")));
		assertEquals(Ipv6Range.of("::", "::123f"), Ipv6Subnet.of("::1230/124").withFirst(Ipv6Address.of("::")));
		assertThrows(IllegalArgumentException.class,
				() -> Ipv6Subnet.of("1234::/16").withFirst(Ipv6Address.of("2222::")));
	}

	@Test
	void withRemovedAll() {
		final Ipv6Range subnet = Ipv6Range.parse("2001::/16");
		assertEquals(Arrays.asList(subnet), subnet.withRemoved(emptyList()));
		assertEquals(emptyList(), subnet.withRemoved(subnet));
		assertEquals(emptyList(), subnet.withRemoved(Ipv6Range.parse("2000::/8")));
		assertEquals(emptyList(), subnet.withRemoved(Ipv6Range.parse("::/0")));
		assertEquals(emptyList(), Ipv6Range.parse("::/0").withRemoved(Ipv6Range.parse("::/0")));
	}

	@ParameterizedTest
	@CsvSource({
	//@formatter:off
            "::1234:1-::1234:ffff, ::1234:0/112, ::1234:0",
            "::1234:0-::1234:fffe, ::1234:0/112, ::1234:ffff",
            "::1234:0/113, ::1234:0/112, ::1234:8000/113",
            "::1234:8000/113, ::1234:0/112, ::1234:0/113",
            "::1234:0-::1234:fff ::1234:2001-::1234:ffff, ::1234:0/112, ::1234:1000-::1234:2000",
            "::-0fff:ffff:ffff:ffff:ffff:ffff:ffff:ffff 1001::-ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff, ::/0, 1000::/16"
            //@formatter:on
	})
	void withRemovedSingle(final String ans, final String original, final String toExclude) {
		final List<Ipv6Range> ansList = Arrays.stream(ans.trim().split(" ")).map(Ipv6Range::parse)
				.collect(Collectors.toList());
		assertEquals(ansList, Ipv6Range.parse(original).withRemoved(Ipv6Range.parse(toExclude)));
	}

	@Test
	void withRemovedCollectionEmpty() {
		assertEquals(singletonList(Ipv6Range.parse("1234::/16")),
				Ipv6Range.parse("1234::/16").withRemoved(emptyList()));
		assertEquals(emptyList(),
				Ipv6Range.parse("1234::/16").withRemoved(Arrays.asList(Ipv6Range.parse("1234::/16"))));
		assertEquals(emptyList(), Ipv6Range.parse("1234::/16").withRemoved(Arrays.asList(Ipv6Range.parse("::/0"))));
		assertEquals(emptyList(),
				Ipv6Range.parse("1234::/16").withRemoved(Arrays.asList(Ipv6Range.parse("1230::-1240::"))));
	}

	@ParameterizedTest
	@CsvSource({
	//@formatter:off
            "::1234:0/112 , ::1234:0/112 , ::1235:0/112 ::1236:0/112",
            "::1234:0/112 , ::1234:0/112 , ::1232:0/112 ::1233:0/112",
            "::1234:0/112 , ::1234:0/112 , 4321::",
            "::1234:1001-::1234:1fff ::1234:3001-::1234:efff, ::1234:0/112, ::1000:0/112 ::1233:f000-::1234:1000 ::1234:2000-::1234:3000 ::1234:f000-::1235:1000 ::1236:0/112",
            "::1234:1-::1234:0fff ::1234:1001-::1234:fffe,::1234:0/112, ::-::1234:0 ::1234:1000 ::1234:ffff-::1235:0",
            "::1234:0/112,::1234:0/112, ::1232:0/113 ::1233:0/113",
            "::1234:0/112,::1234:0/112, ::1232:0/113 ::1235:0/113",
            "::1234:0-::1234:ff ::1234:101-::1234:fff ::1234:1001-::1234:ffff,::1234:0/112, ::1234:100 ::1234:1000",
            "::1234:0-::1234:0fff,::1234:0/112, ::1232:0/113 ::1234:1000-::1235:1000"
            //@formatter:on
	})
	void withRemovedCollection(final String ans, final String original, final String toExclude) {
		final List<Ipv6Range> ansList = Arrays.stream(ans.trim().split(" ")).map(Ipv6Range::parse)
				.collect(Collectors.toList());
		final List<Ipv6Range> toExcludeList = toExclude == null ? emptyList()
				: Arrays.stream(toExclude.trim().split(" ")).map(Ipv6Range::parse).collect(Collectors.toList());

		assertEquals(ansList, Ipv6Range.parse(original).withRemoved(toExcludeList));
	}
}
