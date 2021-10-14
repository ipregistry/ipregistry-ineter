/*
 * Copyright (c) 2020, ineter contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package co.ipregistry.ineter.range;

import java.net.Inet4Address;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import co.ipregistry.ineter.base.IpAddress;
import co.ipregistry.ineter.base.Ipv4Address;

public class Ipv4Range implements IpRange<Ipv4Range, Ipv4Subnet, Ipv4Address, Long> {

	private static final long serialVersionUID = 3L;

	public static Ipv4Range of(final Ipv4Address firstAddress, final Ipv4Address lastAddress) {
		return new Ipv4Range(firstAddress, lastAddress);
	}

	public static Ipv4Range of(final Ipv4Address address) {
		return Ipv4Range.of(address, address);
	}

	public static Ipv4Range of(final String firstAddress, final String lastAddress) {
		return new Ipv4Range(Ipv4Address.of(firstAddress), Ipv4Address.of(lastAddress));
	}

	public static Ipv4Range of(final String address) {
		return Ipv4Range.of(address, address);
	}

	public static Ipv4Range of(final byte[] firstAddress, final byte[] lastAddress) {
		return new Ipv4Range(Ipv4Address.of(firstAddress), Ipv4Address.of(lastAddress));
	}

	public static Ipv4Range of(final byte[] address) {
		return Ipv4Range.of(address, address);
	}

	public static Ipv4Range of(final Inet4Address firstAddress, final Inet4Address lastAddress) {
		return new Ipv4Range(Ipv4Address.of(firstAddress), Ipv4Address.of(lastAddress));
	}

	public static Ipv4Range of(final Inet4Address address) {
		return Ipv4Range.of(address, address);
	}

	/**
	 * merges the given {@link Ipv4Range} instances to a minimal list of
	 * non-overlapping ranges
	 *
	 * @return a list of {@link Ipv4Range}
	 */
	public static List<Ipv4Range> merge(final Ipv4Range... ranges) {
		return merge(Arrays.asList(ranges));
	}

	/**
	 * merges the given collection of {@link Ipv4Range} instances to a minimal list
	 * of non-overlapping ranges
	 *
	 * @return a list of {@link Ipv4Range}
	 */
	public static List<Ipv4Range> merge(final Collection<Ipv4Range> ranges) {
		return IpRangeUtils.merge(ranges, Ipv4Range::of);
	}

	/**
	 * Parses the given String into an {@link Ipv4Range} The String can be either a
	 * single address, a range such as "192.168.0.0-192.168.1.2" or a subnet such as
	 * "192.168.0.0/16"
	 *
	 * @param from - a String representation of a single IPv4 address, a range or a
	 *             subnet
	 * @return An {@link Ipv4Range}
	 */
	public static Ipv4Range parse(final String from) {
		return IpRangeUtils.parseRange(from, Ipv4Range::of, Ipv4Subnet::of);
	}

	protected final Ipv4Address firstAddress;
	protected final Ipv4Address lastAddress;

	public Ipv4Range(final Ipv4Address firstAddress, final Ipv4Address lastAddress) {
		this.firstAddress = firstAddress;
		this.lastAddress = lastAddress;
		if (this.firstAddress == null || this.lastAddress == null) {
			throw new NullPointerException("Neither the first nor the last address can be null");
		}

		if (this.firstAddress.compareTo(lastAddress) > 0) {
			throw new IllegalArgumentException(
					String.format("The first address in the range (%s) has to be lower than the last address (%s)",
							firstAddress, lastAddress));
		}
	}

	@Override
	public Ipv4Address getFirst() {
		return this.firstAddress;
	}

	@Override
	public Ipv4Address getLast() {
		return this.lastAddress;
	}

	@Override
	public Long length() {
		return this.lastAddress.toLong() - this.firstAddress.toLong() + 1;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.firstAddress == null) ? 0 : this.firstAddress.hashCode());
		result = prime * result + ((this.lastAddress == null) ? 0 : this.lastAddress.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Ipv4Range))
			return false;
		final Ipv4Range other = (Ipv4Range) obj;
		return this.firstAddress != null && other.firstAddress != null && this.lastAddress != null
				&& other.lastAddress != null && this.firstAddress.equals(other.firstAddress)
				&& this.lastAddress.equals(other.lastAddress);
	}

	@Override
	public String toString() {
		return this.getFirst().toString() + ',' + this.getLast().toString();
	}

	@Override
	public Iterator<Ipv4Address> iterator(final boolean skipFirst, final boolean skipLast) {
		return new Iterator<Ipv4Address>() {

			final AtomicLong next = new AtomicLong(
					skipFirst ? Ipv4Range.this.firstAddress.next().toLong() : Ipv4Range.this.firstAddress.toLong());
			final long last = skipLast ? Ipv4Range.this.lastAddress.previous().toLong()
					: Ipv4Range.this.lastAddress.toLong();

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean hasNext() {
				return this.next.get() <= this.last;
			}

			@Override
			public Ipv4Address next() {
				final long tempNext;
				if ((tempNext = this.next.getAndIncrement()) <= this.last) {
					return Ipv4Address.of((int) tempNext);
				}
				throw new NoSuchElementException();
			}
		};
	}

	protected Ipv4Subnet maxSubnetInRange(final Ipv4Address addr) {
		final int addrHostBits = Integer.numberOfTrailingZeros(addr.toInt());
		final int networkBitsEq = Integer.numberOfLeadingZeros(this.lastAddress.toInt() ^ addr.toInt());
		int hostBitsMax = Ipv4Address.ADDRESS_BITS - networkBitsEq;
		if (Integer.numberOfTrailingZeros(~this.lastAddress.toInt()) < hostBitsMax) {
			hostBitsMax--;
		}

		final int hostBits = Math.min(addrHostBits, hostBitsMax);
		return Ipv4Subnet.of(addr, 32 - hostBits);
	}

	@Override
	public List<Ipv4Subnet> toSubnets() {
		final ArrayList<Ipv4Subnet> result = new ArrayList<>();
		Ipv4Address lastAddress = this.firstAddress.previous();
		do {
			final Ipv4Subnet nextSubnet = maxSubnetInRange(lastAddress.next());
			result.add(nextSubnet);
			lastAddress = nextSubnet.lastAddress;
		} while (lastAddress.compareTo(this.lastAddress) < 0);

		return result;
	}

	@Override
	public int intLength() {
		return this.length() >= Integer.MAX_VALUE ? Integer.MAX_VALUE : this.length().intValue();
	}

	@Override
	public Ipv4Range withFirst(final Ipv4Address address) {
		return Ipv4Range.of(address, this.getLast());
	}

	@Override
	public Ipv4Range withLast(final Ipv4Address address) {
		return Ipv4Range.of(this.getFirst(), address);
	}

	public List<Ipv4Range> withRemoved(final Collection<Ipv4Range> ranges) {
		final List<Ipv4Range> ret = new ArrayList<>(ranges.size() + 1);
		final List<Ipv4Range> merged = Ipv4Range.merge(ranges);
		ret.add(this);
		for (final Ipv4Range toRemove : merged) {
			final Ipv4Range next = ret.remove(ret.size() - 1);
			// a bit faster than calling withRemoved() one range at a time
			if (toRemove.getFirst().compareTo(next.getFirst()) > 0) {
				if (toRemove.getLast().compareTo(next.getLast()) < 0) {
					ret.add(Ipv4Range.of(next.getFirst(), toRemove.getFirst().previous()));
					ret.add(Ipv4Range.of(toRemove.getLast().next(), next.getLast()));
					continue;
				}
				ret.add(Ipv4Range.of(next.getFirst(), IpAddress.min(next.getLast(), toRemove.getFirst().previous())));
				break;
			}
			if (toRemove.getLast().compareTo(next.getLast()) < 0) {
				ret.add(Ipv4Range.of(IpAddress.max(toRemove.getLast().next(), next.getFirst()), next.getLast()));
			}
		}
		return ret;
	}

	public List<Ipv4Range> withRemoved(final Ipv4Range r) {
		if (r.getFirst().compareTo(this.getFirst()) > 0) {
			if (r.getLast().compareTo(this.getLast()) < 0) {
				return Arrays.asList(Ipv4Range.of(this.getFirst(), r.getFirst().previous()),
						Ipv4Range.of(r.getLast().next(), this.getLast()));
			}
			// noinspection ArraysAsListWithZeroOrOneArgument
			return Arrays.asList(Ipv4Range.of(this.getFirst(), IpAddress.min(this.getLast(), r.getFirst().previous())));
		}
		if (r.getLast().compareTo(this.getLast()) < 0) {
			// noinspection ArraysAsListWithZeroOrOneArgument
			return Arrays.asList(Ipv4Range.of(IpAddress.max(r.getLast().next(), this.getFirst()), this.getLast()));
		}

		return Collections.emptyList();
	}
}
