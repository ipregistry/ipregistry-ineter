/*
 * Copyright (c) 2020, ineter contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package co.ipregistry.ineter.range;

import java.io.Serial;
import java.math.BigInteger;
import java.net.Inet6Address;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicLong;

import co.ipregistry.ineter.base.IpAddress;
import co.ipregistry.ineter.base.Ipv4Address;
import co.ipregistry.ineter.base.Ipv6Address;

public class Ipv6Range implements IpRange<Ipv6Range, Ipv6Subnet, Ipv6Address, BigInteger> {

	private static final BigInteger INTEGER_MAX_VALUE = new BigInteger(
			new byte[] { 0x7f, (byte) 0xff, (byte) 0xff, (byte) 0xff });
	@Serial
	private static final long serialVersionUID = 3L;

	public static Ipv6Range of(final Ipv6Address firstAddress, final Ipv6Address lastAddress) {
		return new Ipv6Range(firstAddress, lastAddress);
	}

	static Ipv6Range of(final Ipv6Address firstAddress, final Ipv6Address lastAddress, final boolean unchecked) {
		return new Ipv6Range(firstAddress, lastAddress, unchecked);
	}

	public static Ipv6Range of(final Ipv6Address address) {
		return Ipv6Range.of(address, address);
	}

	public static Ipv6Range of(final String firstAddress, final String lastAddress) {
		return new Ipv6Range(Ipv6Address.of(firstAddress), Ipv6Address.of(lastAddress));
	}

	public static Ipv6Range of(final String address) {
		return Ipv6Range.of(address, address);
	}

	public static Ipv6Range of(final byte[] firstAddress, final byte[] lastAddress) {
		return new Ipv6Range(Ipv6Address.of(firstAddress), Ipv6Address.of(lastAddress));
	}

	public static Ipv6Range of(final byte[] address) {
		return Ipv6Range.of(address, address);
	}

	public static Ipv6Range of(final Inet6Address firstAddress, final Inet6Address lastAddress) {
		return new Ipv6Range(Ipv6Address.of(firstAddress), Ipv6Address.of(lastAddress));
	}

	public static Ipv6Range of(final Inet6Address address) {
		return Ipv6Range.of(address, address);
	}

	public static Ipv6Range of(final Ipv6Subnet subnet) {
		return Ipv6Range.of(subnet.firstAddress, subnet.lastAddress, true);
	}

	/**
	 * merges the given {@link Ipv6Range} instances to a minimal list of
	 * non-overlapping ranges
	 *
	 * @return a list of {@link Ipv6Range}
	 */
	public static List<Ipv6Range> merge(final Ipv6Range... ranges) {
		return merge(Arrays.asList(ranges));
	}

	/**
	 * merges the given collection of {@link Ipv6Range} instances to a minimal list
	 * of non-overlapping ranges
	 *
	 * @return a list of {@link Ipv6Range}
	 */
	public static List<Ipv6Range> merge(final Collection<Ipv6Range> ranges) {
		return IpRangeUtils.merge(ranges, Ipv6Range::of);
	}

	/**
	 * Parses the given String into an {@link Ipv6Range} The String can be either a
	 * single address, a range such as "2001::-2002::" or a subnet such as
	 * "2001::/16"
	 *
	 * @param from - a String representation of a single IPv6 address, a range or a
	 *             subnet
	 * @return An {@link Ipv6Range}
	 */
	public static Ipv6Range parse(final String from) {
		return IpRangeUtils.parseRange(from, Ipv6Range::of, Ipv6Subnet::of);
	}

	final Ipv6Address firstAddress;
	final Ipv6Address lastAddress;

	public Ipv6Range(final Ipv6Address firstAddress, final Ipv6Address lastAddress) {
		this(firstAddress, lastAddress, false);
	}

	Ipv6Range(final Ipv6Address firstAddress, final Ipv6Address lastAddress, final boolean unchecked) {
		this.firstAddress = firstAddress;
		this.lastAddress = lastAddress;

		if (!unchecked) {
			if (this.firstAddress == null || this.lastAddress == null) {
				throw new NullPointerException("Neither the first nor the last address can be null");
			}

			if (this.firstAddress.compareTo(lastAddress) > 0) {
				throw new IllegalArgumentException(
						String.format("The first address in the range (%s) has to be lower than the last address (%s)",
								firstAddress, lastAddress));
			}
		}
	}

	@Override
	public Ipv6Address getFirst() {
		return this.firstAddress;
	}

	@Override
	public Ipv6Address getLast() {
		return this.lastAddress;
	}

	@Override
	public BigInteger length() {
		return this.lastAddress.toBigInteger().subtract(this.firstAddress.toBigInteger()).add(BigInteger.ONE);
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
		if (!(obj instanceof Ipv6Range))
			return false;
		final Ipv6Range other = (Ipv6Range) obj;
		if (this.firstAddress == null) {
			if (other.firstAddress != null)
				return false;
		} else if (!this.firstAddress.equals(other.firstAddress))
			return false;
		if (this.lastAddress == null) {
			return other.lastAddress == null;
		} else
			return this.lastAddress.equals(other.lastAddress);
	}

	@Override
	public String toString() {
		return this.getFirst().toString() + ',' + this.getLast().toString();
	}

	@Override
	public Iterator<Ipv6Address> iterator(final boolean skipFirst, final boolean skipLast) {
		return new Iterator<Ipv6Address>() {

			final AtomicLong nextAddition = new AtomicLong(skipFirst ? 1 : 0);
			// Will throw exception if length is greater than max long
			final long totalCount = skipLast ? length().longValueExact() - 1 : length().longValueExact();

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean hasNext() {
				return this.nextAddition.get() < this.totalCount;
			}

			@Override
			public Ipv6Address next() {
				final long tempNext;
				if ((tempNext = this.nextAddition.getAndIncrement()) < this.totalCount) {
					return Ipv6Range.this.firstAddress.plus(tempNext);
				}
				throw new NoSuchElementException();
			}
		};
	}

	protected int numberOfTrailingOnes(final Ipv6Address a) {
		final long notLower = ~a.getLower();
		return (notLower == 0) ? Ipv6Address.HOLDER_BITS + Long.numberOfTrailingZeros(~a.getUpper())
				: Long.numberOfTrailingZeros(notLower);
	}

	protected int numberOfTrailingZeros(final Ipv6Address a) {
		return (a.getLower() == 0) ? Ipv6Address.HOLDER_BITS + Long.numberOfTrailingZeros(a.getUpper())
				: Long.numberOfTrailingZeros(a.getLower());
	}

	protected int numberOfLeadingEq(final Ipv6Address a, final Ipv6Address b) {
		final long upperXOR = a.getUpper() ^ b.getUpper();
		if (upperXOR == 0) {
			return Ipv6Address.HOLDER_BITS + Long.numberOfLeadingZeros(a.getLower() ^ b.getLower());
		}
		return Long.numberOfLeadingZeros(upperXOR);
	}

	protected Ipv6Subnet maxSubnetInRange(final Ipv6Address addr) {
		final int addrHostBits = numberOfTrailingZeros(addr);
		final int networkBitsEq = numberOfLeadingEq(this.lastAddress, addr);
		int hostBitsMax = Ipv6Address.ADDRESS_BITS - networkBitsEq;
		if (numberOfTrailingOnes(this.lastAddress) < hostBitsMax) {
			hostBitsMax--;
		}

		final int hostBits = Math.min(addrHostBits, hostBitsMax);
		return Ipv6Subnet.of(addr, Ipv6Address.ADDRESS_BITS - hostBits);
	}

	@Override
	public List<Ipv6Subnet> toSubnets() {
		final ArrayList<Ipv6Subnet> result = new ArrayList<>();
		Ipv6Address lastAddress = this.firstAddress.previous();
		do {
			final Ipv6Subnet nextSubnet = maxSubnetInRange(lastAddress.next());
			result.add(nextSubnet);
			lastAddress = nextSubnet.lastAddress;
		} while (lastAddress.compareTo(this.lastAddress) < 0);

		return result;
	}

	@Override
	public int intLength() {
		return this.length().compareTo(INTEGER_MAX_VALUE) >= 0 ? Integer.MAX_VALUE : this.length().intValue();
	}

	@Override
	public Ipv6Range withFirst(final Ipv6Address address) {
		return Ipv6Range.of(address, this.getLast());
	}

	@Override
	public Ipv6Range withLast(final Ipv6Address address) {
		return Ipv6Range.of(this.getFirst(), address);
	}

	public List<Ipv6Range> withRemoved(final Collection<Ipv6Range> ranges) {
		final List<Ipv6Range> ret = new ArrayList<>(ranges.size() + 1);
		final List<Ipv6Range> merged = Ipv6Range.merge(ranges);
		ret.add(this);
		for (final Ipv6Range toRemove : merged) {
			if (ret.isEmpty()) {
				break;
			}
			final Ipv6Range next = ret.remove(ret.size() - 1);
			// a bit faster than calling withRemoved() one range at a time
			if (toRemove.getFirst().compareTo(next.getFirst()) > 0) {
				if (toRemove.getLast().compareTo(next.getLast()) < 0) {
					ret.add(Ipv6Range.of(next.getFirst(), toRemove.getFirst().previous()));
					ret.add(Ipv6Range.of(toRemove.getLast().next(), next.getLast()));
					continue;
				}
				ret.add(Ipv6Range.of(next.getFirst(), IpAddress.min(next.getLast(), toRemove.getFirst().previous())));
				break;
			}
			if (toRemove.getLast().compareTo(next.getLast()) < 0) {
				ret.add(Ipv6Range.of(IpAddress.max(toRemove.getLast().next(), next.getFirst()), next.getLast()));
			}
		}
		return ret;
	}

	public List<Ipv6Range> withRemoved(final Ipv6Range r) {
		if (r.getFirst().compareTo(this.getFirst()) > 0) {
			if (r.getLast().compareTo(this.getLast()) < 0) {
				return Arrays.asList(Ipv6Range.of(this.getFirst(), r.getFirst().previous()),
						Ipv6Range.of(r.getLast().next(), this.getLast()));
			}
			return Arrays.asList(Ipv6Range.of(this.getFirst(), IpAddress.min(this.getLast(), r.getFirst().previous())));
		}
		if (r.getLast().compareTo(this.getLast()) < 0) {
			return Arrays.asList(Ipv6Range.of(IpAddress.max(r.getLast().next(), this.getFirst()), this.getLast()));
		}

		return Collections.emptyList();
	}
}
