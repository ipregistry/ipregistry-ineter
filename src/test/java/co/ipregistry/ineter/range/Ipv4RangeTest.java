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

import java.net.Inet4Address;
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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import co.ipregistry.ineter.base.Ipv4Address;

import com.google.common.collect.ImmutableList;

@RunWith(JUnitPlatform.class)
public class Ipv4RangeTest {

	@Test
	void ofAddress() {
		final Ipv4Range range = Ipv4Range.of(Ipv4Address.of("1.2.3.4"), Ipv4Address.of("5.4.3.2"));
		assertEquals(range.getFirst(), Ipv4Address.of("1.2.3.4"));
		assertEquals(range.getLast(), Ipv4Address.of("5.4.3.2"));
	}

	@Test
	void ofString() {
		final Ipv4Range range = Ipv4Range.of("1.2.3.4", "5.4.3.2");
		assertEquals(range.getFirst(), Ipv4Address.of("1.2.3.4"));
		assertEquals(range.getLast(), Ipv4Address.of("5.4.3.2"));
	}

	@Test
	void ofInetAddress() throws UnknownHostException {
		final Ipv4Range range = Ipv4Range.of((Inet4Address) InetAddress.getByName("1.2.3.4"),
				(Inet4Address) InetAddress.getByName("5.4.3.2"));
		assertEquals(range.getFirst(), Ipv4Address.of("1.2.3.4"));
		assertEquals(range.getLast(), Ipv4Address.of("5.4.3.2"));
	}

	@Test
	void ofArray() {
		final Ipv4Range range = Ipv4Range.of(new byte[] { 1, 2, 3, 4 }, new byte[] { 5, 4, 3, 2 });
		assertEquals(range.getFirst(), Ipv4Address.of("1.2.3.4"));
		assertEquals(range.getLast(), Ipv4Address.of("5.4.3.2"));
	}

	@Test
	void invalidRange() {
		assertThrows(IllegalArgumentException.class, () -> Ipv4Range.of("5.4.3.2", "1.2.3.4"));
	}

	@Test
	void nullAddress() {
		assertThrows(NullPointerException.class, () -> new Ipv4Range(null, Ipv4Address.of("1.2.3.4")));
		assertThrows(NullPointerException.class, () -> new Ipv4Range(Ipv4Address.of("1.2.3.4"), null));
	}

	@Test
	void parse() {
		final Ipv4Range range = Ipv4Range.parse("1.2.3.4-5.4.3.2");
		assertEquals(range.getFirst(), Ipv4Address.of("1.2.3.4"));
		assertEquals(range.getLast(), Ipv4Address.of("5.4.3.2"));
		assertTrue(range.toString().contains("1.2.3.4"));
		assertTrue(range.toString().contains("5.4.3.2"));
	}

	@ParameterizedTest
	@CsvSource({ "1.2.3.4,2.3.4.5,1.2.3.4", "0.0.0.0,255.255.255.255,255.255.255.255",
			"127.0.0.0,127.255.255.255,127.1.2.3" })
	void contains(final String start, final String end, final String parse) {
		assertTrue(Ipv4Range.parse(start + "-" + end).contains(Ipv4Address.of(parse)));
	}

	@ParameterizedTest
	@CsvSource({ "1.2.3.4,2.3.4.5,1.2.3.2", "0.0.0.0,127.255.255.255,255.255.255.255",
			"127.0.0.0,127.255.255.255,128.0.0.0" })
	void notContains(final String start, final String end, final String parse) {
		assertFalse(Ipv4Range.parse(start + "-" + end).contains(Ipv4Address.of(parse)));
	}

	@ParameterizedTest
	@CsvSource({ "1.2.3.4,2.3.4.5,1.2.4.0/24", "0.0.0.0,255.255.255.255,0.0.0.0/0",
			"127.0.0.0,127.255.255.255,127.0.0.0/16" })
	void containsRange(final String start, final String end, final String parse) {
		assertTrue(Ipv4Range.parse(start + "-" + end).contains(Ipv4Subnet.of(parse)));
	}

	@ParameterizedTest
	@CsvSource({ "1.2.3.4,2.3.4.5,1.2.3.0/24", "0.0.0.1,255.255.255.255,0.0.0.0/0",
			"127.0.0.0,127.255.255.255,127.0.0.0/7" })
	void notContainsRange(final String start, final String end, final String parse) {
		assertFalse(Ipv4Range.parse(start + "-" + end).contains(Ipv4Subnet.of(parse)));
	}

	@ParameterizedTest
	@CsvSource({ "1.2.3.4,2.3.4.5,1.0.0.0-1.2.3.5", "0.0.0.0,255.255.255.255,1.2.3.4-1.2.3.4",
			"127.0.0.0,127.255.255.255,0.0.0.0-128.0.0.0" })
	void overlaps(final String start, final String end, final String parse) {
		assertTrue(Ipv4Range.parse(start + "-" + end).overlaps(Ipv4Range.parse(parse)));
	}

	@ParameterizedTest
	@CsvSource({ "1.2.3.4,2.3.4.5,128.0.0.0-128.2.3.5", "0.0.0.1,255.255.255.255,0.0.0.0-0.0.0.0",
			"127.0.0.0,127.255.255.255,128.0.0.0-255.255.255.255" })
	void notOverlaps(final String start, final String end, final String parse) {
		assertFalse(Ipv4Range.parse(start + "-" + end).overlaps(Ipv4Range.parse(parse)));
	}

	@Test
	void equal() {
		final Ipv4Range range1 = Ipv4Range.parse("192.168.0.0-192.168.255.255");
		final Ipv4Range range2 = Ipv4Range.of(Ipv4Address.of("192.168.0.0"), Ipv4Address.of("192.168.255.255"));

		assertEquals(range1, range1);
		assertEquals(range2, range2);

		assertEquals(range1.hashCode(), range2.hashCode());
		assertEquals(range1, range2);
	}

	@Test
	void notEqual() {
		final Ipv4Range range1 = Ipv4Range.parse("192.168.0.0-192.168.255.255");
		final Ipv4Range range2 = Ipv4Range.of(Ipv4Address.of("10.0.0.0"), Ipv4Address.of("10.255.255.255"));

		assertNotEquals(range1, range2);
	}

	@Test
	void unequalToNull() {
		final Ipv4Range range1 = Ipv4Range.parse("192.168.0.0-192.168.255.255");
		assertFalse(range1.equals(null));
	}

	@Test
	void unequalToObject() {
		assertFalse(Ipv4Range.parse("192.168.0.0-192.168.255.255").equals(new Object()));
	}

	@ParameterizedTest
	@CsvSource({ "0.0.0.0-255.255.255.255,100000000", "10.0.0.0-10.0.0.255,100", "10.0.0.1-10.0.0.1,1" })
	void length(final String parse, final String length) {
		assertEquals(Ipv4Range.parse(parse).length().longValue(), Long.parseLong(length, 16));
	}

	@Test
	void iterationOrder() {
		final ArrayList<Ipv4Address> itemList = new ArrayList<>();
		Ipv4Range.of("127.255.255.0", "128.0.0.1").iterator().forEachRemaining(itemList::add);

		assertEquals(258, itemList.size());
		assertEquals(itemList.get(0), Ipv4Address.of("127.255.255.0"));
		assertEquals(itemList.get(itemList.size() - 1), Ipv4Address.of("128.0.0.1"));

		final ListIterator<Ipv4Address> listIterator = itemList.listIterator();
		Ipv4Address previous = listIterator.next();
		while (listIterator.hasNext()) {
			final Ipv4Address current = listIterator.next();
			assertTrue(current.compareTo(previous) > 0);
			previous = current;
		}
	}

	@Test
	void iterationOrderSkipEdges() {
		final ArrayList<Ipv4Address> itemList = new ArrayList<>();
		Ipv4Range.of("127.255.255.0", "128.0.0.1").iterator(true).forEachRemaining(itemList::add);

		assertEquals(256, itemList.size());
		assertEquals(itemList.get(0), Ipv4Address.of("127.255.255.1"));
		assertEquals(itemList.get(itemList.size() - 1), Ipv4Address.of("128.0.0.0"));

		final ListIterator<Ipv4Address> listIterator = itemList.listIterator();
		Ipv4Address previous = listIterator.next();
		while (listIterator.hasNext()) {
			final Ipv4Address current = listIterator.next();
			assertTrue(current.compareTo(previous) > 0);
			previous = current;
		}
	}

	@Test
	void iterationLastElement() {
		final Iterator<Ipv4Address> i = Ipv4Range.of("127.255.255.0", "127.255.255.0").iterator();
		assertTrue(i.hasNext());
		assertEquals(i.next(), Ipv4Address.of("127.255.255.0"));
		assertThrows(NoSuchElementException.class, () -> i.next());
	}

	@Test
	void iterationRemove() {
		final Iterator<Ipv4Address> i = Ipv4Range.of("127.255.255.0", "127.255.255.0").iterator();
		assertThrows(UnsupportedOperationException.class, () -> i.remove());
	}

	@ParameterizedTest
	@CsvSource({
			"0.0.0.0-255.255.255.1,0.0.0.0/1 128.0.0.0/2 192.0.0.0/3 224.0.0.0/4 240.0.0.0/5 248.0.0.0/6 252.0.0.0/7"
					+ " 254.0.0.0/8 255.0.0.0/9 255.128.0.0/10 255.192.0.0/11 255.224.0.0/12 255.240.0.0/13 255.248.0.0/14"
					+ " 255.252.0.0/15 255.254.0.0/16 255.255.0.0/17 255.255.128.0/18 255.255.192.0/19 255.255.224.0/20"
					+ " 255.255.240.0/21 255.255.248.0/22 255.255.252.0/23 255.255.254.0/24 255.255.255.0/31",
			"0.0.0.0-255.255.255.255,0.0.0.0/0", "127.255.255.255-128.0.0.1,127.255.255.255/32 128.0.0.0/31",
			"10.100.0.0-10.255.255.255,10.100.0.0/14 10.104.0.0/13 10.112.0.0/12 10.128.0.0/9",
			"123.45.67.89-123.45.68.4, 123.45.67.89/32 123.45.67.90/31 123.45.67.92/30 123.45.67.96/27 123.45.67.128/25 123.45.68.0/30 123.45.68.4/32" })
	void toSubnets(final String range, final String subnets) {
		final List<Ipv4Subnet> generated = Ipv4Range.parse(range).toSubnets();
		final List<Ipv4Subnet> manual = Arrays.stream(subnets.split(" ")).map(Ipv4Subnet::of)
				.collect(Collectors.toList());
		assertEquals(generated, manual);
		assertEquals(manual.stream().mapToLong(Ipv4Subnet::length).sum(), Ipv4Range.parse(range).length().longValue());
	}

	@Test
	void singleIPRangeParse() {
		final Ipv4Range explicitRange = Ipv4Range.parse("127.0.0.1-127.0.0.1");
		final Ipv4Range range = Ipv4Range.parse("127.0.0.1");
		assertEquals(explicitRange, range,
				"Single address range doesn't match explicit range with same addresses on both ends.");
	}

	@Test
	void singleIPRangeOfBytes() {
		final Ipv4Range explicitRange = Ipv4Range.of(new byte[] { 1, 2, 3, 4 }, new byte[] { 1, 2, 3, 4 });
		final Ipv4Range range = Ipv4Range.of(new byte[] { 1, 2, 3, 4 });
		assertEquals(explicitRange, range,
				"Single address range doesn't match explicit range with same addresses on both ends.");
	}

	@Test
	void singleIPRangeOfString() {
		final Ipv4Range explicitRange = Ipv4Range.of("1.2.3.4", "1.2.3.4");
		final Ipv4Range range = Ipv4Range.of("1.2.3.4");
		assertEquals(explicitRange, range,
				"Single address range doesn't match explicit range with same addresses on both ends.");
	}

	@Test
	void singleIPRangeOfIPv4Address() {
		final Ipv4Range explicitRange = Ipv4Range.of(Ipv4Address.of("1.2.3.4"), Ipv4Address.of("1.2.3.4"));
		final Ipv4Range range = Ipv4Range.of(Ipv4Address.of("1.2.3.4"));
		assertEquals(explicitRange, range,
				"Single address range doesn't match explicit range with same addresses on both ends.");
	}

	@Test
	void singleIPRangeOfInetAddress() throws UnknownHostException {
		final Ipv4Range explicitRange = Ipv4Range.of((Inet4Address) InetAddress.getByName("1.2.3.4"),
				(Inet4Address) InetAddress.getByName("1.2.3.4"));
		final Ipv4Range range = Ipv4Range.of((Inet4Address) InetAddress.getByName("1.2.3.4"));
		assertEquals(explicitRange, range,
				"Single address range doesn't match explicit range with same addresses on both ends.");
	}

	@Test
	void parsedSubnet() {
		final Ipv4Range range = Ipv4Range.parse("127.0.0.0/24");
		assertEquals(Ipv4Address.of("127.0.0.0"), range.getFirst());
		assertEquals(Ipv4Address.of("127.0.0.255"), range.getLast());
	}

	@Test
	void shouldMergeAdjacent() {
		final Ipv4Range first = Ipv4Range.of("127.0.0.1", "127.0.0.2");
		final Ipv4Range second = Ipv4Range.of("127.0.0.3", "127.0.0.4");
		final List<Ipv4Range> merge = Ipv4Range.merge(first, second);
		assertEquals(Collections.singletonList(Ipv4Range.of("127.0.0.1", "127.0.0.4")), merge);
	}

	@Test
	void shouldMergeOverlapping() {
		final Ipv4Range first = Ipv4Range.of("127.0.0.1", "127.0.0.3");
		final Ipv4Range second = Ipv4Range.of("127.0.0.2", "127.0.0.4");
		final List<Ipv4Range> merge = Ipv4Range.merge(first, second);
		assertEquals(Collections.singletonList(Ipv4Range.of("127.0.0.1", "127.0.0.4")), merge);
	}

	@Test
	void shouldMergeMixed() {
		final Ipv4Range first = Ipv4Range.of("127.0.0.1", "127.0.0.3");
		final Ipv4Range second = Ipv4Range.of("127.0.0.2", "127.0.0.4");
		final Ipv4Range third = Ipv4Range.of("127.0.0.5", "127.0.0.6");

		final Ipv4Range fourth = Ipv4Range.of("127.0.0.8", "127.0.0.10");
		final Ipv4Range fifth = Ipv4Range.of("127.0.0.8", "127.0.0.11");

		final Ipv4Range sixth = Ipv4Range.of("128.0.0.0", "255.255.255.255");
		final List<Ipv4Range> merge = Ipv4Range.merge(sixth, fifth, fourth, third, second, first);

		assertEquals(Arrays.asList(Ipv4Range.of("127.0.0.1", "127.0.0.6"), Ipv4Range.of("127.0.0.8", "127.0.0.11"),
				Ipv4Range.of("128.0.0.0", "255.255.255.255")), merge);
	}

	@Test
	void shouldNotMergeSeparated() {
		final Ipv4Range first = Ipv4Range.of("127.0.0.1", "127.0.0.3");
		final Ipv4Range second = Ipv4Range.of("127.0.0.5", "127.0.0.6");
		final List<Ipv4Range> merge = Ipv4Range.merge(first, second);
		assertEquals(ImmutableList.of(first, second), merge);
	}

	@Test
	void mergeShouldThrowOnNull() {
		assertThrows(NullPointerException.class, () -> Ipv4Range.merge((Ipv4Range) null));
	}

	@Test
	void shouldReturnEmptyOnEmpty() {
		assertTrue(Ipv4Range.merge(Collections.emptyList()).isEmpty());
	}

	@Test
	void testIntLength() {
		assertEquals(256, Ipv4Subnet.of("192.168.0.0/24").intLength());
		assertEquals(8, Ipv4Subnet.of("10.0.0.0/29").intLength());
		assertEquals(Integer.MAX_VALUE, Ipv4Subnet.of("0.0.0.0/0").intLength());
	}

	@Test
	void testWithLast() {
		assertEquals(Ipv4Range.of("0.0.0.0", "1.2.3.4"),
				Ipv4Subnet.of("0.0.0.0/0").withLast(Ipv4Address.of("1.2.3.4")));
		assertEquals(Ipv4Range.of("1.2.3.0", "1.2.3.4"),
				Ipv4Subnet.of("1.2.3.0/24").withLast(Ipv4Address.of("1.2.3.4")));
		assertThrows(IllegalArgumentException.class,
				() -> Ipv4Subnet.of("1.2.3.0/24").withLast(Ipv4Address.of("0.0.0.0")));
	}

	@Test
	void testWithFirst() {
		assertEquals(Ipv4Range.of("0.0.0.0", "255.255.255.255"),
				Ipv4Subnet.of("128.0.0.0/1").withFirst(Ipv4Address.of("0.0.0.0")));
		assertEquals(Ipv4Range.of("10.0.0.255", "10.0.0.255"),
				Ipv4Subnet.of("10.0.0.0/24").withFirst(Ipv4Address.of("10.0.0.255")));
		assertThrows(IllegalArgumentException.class,
				() -> Ipv4Subnet.of("10.0.0.0/8").withFirst(Ipv4Address.of("12.12.12.12")));
	}

	@Test
	void withRemovedAll() {
		final Ipv4Range subnet = Ipv4Range.parse("10.0.0.0/24");
		assertEquals(Arrays.asList(subnet), subnet.withRemoved(emptyList()));
		assertEquals(emptyList(), subnet.withRemoved(subnet));
		assertEquals(emptyList(), subnet.withRemoved(Ipv4Range.parse("10.0.0.0/8")));
		assertEquals(emptyList(), subnet.withRemoved(Ipv4Range.parse("0.0.0.0/0")));
		assertEquals(emptyList(), Ipv4Range.parse("0.0.0.0/0").withRemoved(Ipv4Range.parse("0.0.0.0/0")));
	}

	@ParameterizedTest
	@CsvSource({
	//@formatter:off
            "10.0.0.1-10.0.0.255, 10.0.0.0/24,  10.0.0.0",
            "10.0.0.0-10.0.0.254, 10.0.0.0/24 ,10.0.0.255",
            "10.0.0.128-10.0.0.255, 10.0.0.0/24,10.0.0.0/25",
            "10.0.0.0-10.0.0.127, 10.0.0.0/24,10.0.0.128/25",
            "10.0.0.0-10.0.0.99 10.0.0.201-10.0.0.255, 10.0.0.0/24, 10.0.0.100-10.0.0.200",
            "0.0.0.0-127.255.255.255 129.0.0.0-255.255.255.255, 0.0.0.0/0, 128.0.0.0/8"
            //@formatter:on
	})
	void withRemovedSingle(final String ans, final String original, final String toExclude) {
		final List<Ipv4Range> ansList = Arrays.stream(ans.trim().split(" ")).map(Ipv4Range::parse)
				.collect(Collectors.toList());
		assertEquals(ansList, Ipv4Range.parse(original).withRemoved(Ipv4Range.parse(toExclude)));
	}

	@Test
	void withRemovedCollectionEmpty() {
		assertEquals(singletonList(Ipv4Range.parse("10.0.0.0/24")),
				Ipv4Range.parse("10.0.0.0/24").withRemoved(emptyList()));
		assertEquals(emptyList(),
				Ipv4Range.parse("10.0.0.0/24").withRemoved(Arrays.asList(Ipv4Range.parse("10.0.0.0/24"))));
		assertEquals(emptyList(),
				Ipv4Range.parse("10.0.0.0/24").withRemoved(Arrays.asList(Ipv4Range.parse("0.0.0.0/0"))));
		assertEquals(emptyList(),
				Ipv4Range.parse("10.0.0.0/24").withRemoved(Arrays.asList(Ipv4Range.parse("9.0.0.0-11.0.0.0"))));
	}

	@ParameterizedTest
	@CsvSource({
	//@formatter:off
            "10.0.0.128-10.0.0.255, 10.0.0.0/24, 10.0.0.0/25",
            "10.0.0.0-10.0.0.127, 10.0.0.0/24, 10.0.0.128/25",
            "10.0.0.0/24, 10.0.0.0/24, 11.0.0.0/24 12.0.0.0/24",
            "10.0.0.0/24, 10.0.0.0/24, 8.0.0.0/24 7.0.0.0/24",
            "10.0.0.0/24, 10.0.0.0/24, ",
            "10.0.0.0/24, 10.0.0.0/24, 1.2.3.4",
            "10.0.0.1-10.0.0.9 10.0.0.20-10.0.0.249 , 10.0.0.0/24 , 8.0.0.0/24 9.0.0.0-10.0.0.0 10.0.0.10-10.0.0.19 10.0.0.250-255.255.255.255",
            "10.0.0.20-10.0.0.249, 10.0.0.0/24 , 9.0.0.0-10.0.0.19 10.0.0.250-10.0.0.255",
            "10.0.0.0-10.0.0.99 10.0.0.101-10.0.0.199 10.0.0.201-10.0.0.255 , 10.0.0.0/24 , 10.0.0.100 10.0.0.200",
            "10.0.0.0-10.0.0.9 10.0.0.20-10.0.0.249, 10.0.0.0/24, 10.0.0.10-10.0.0.19 10.0.0.250-10.0.0.255 12.0.0.0/24",
            "10.0.0.0 10.0.0.2 10.0.0.4-10.0.0.199, 10.0.0.0/24, 10.0.0.1 10.0.0.3 10.0.0.200-10.0.1.100"
            //@formatter:on
	})
	void withRemovedCollection(final String ans, final String original, final String toExclude) {
		final List<Ipv4Range> ansList = Arrays.stream(ans.trim().split(" ")).map(Ipv4Range::parse)
				.collect(Collectors.toList());
		final List<Ipv4Range> toExcludeList = toExclude == null ? emptyList()
				: Arrays.stream(toExclude.trim().split(" ")).map(Ipv4Range::parse).collect(Collectors.toList());

		assertEquals(ansList, Ipv4Range.parse(original).withRemoved(toExcludeList));
	}
}
