/*
 * Copyright (c) 2020, ineter contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package co.ipregistry.ineter.base;

import java.io.Serial;
import java.net.Inet4Address;

import co.ipregistry.ineter.range.Ipv4Range;
import co.ipregistry.ineter.range.Ipv4Subnet;

public class Ipv4Address implements IpAddress {

	public enum IPv4KnownRange {

		//@formatter:off
        /**
         * 127.0.0.0/8 - RFC 990
         */
        LOOPBACK(Ipv4Subnet.of("127.0.0.0/8")),
        /**
         * 0.0.0.0/8 - RFC 1700
         */
        UNSPECIFIED(Ipv4Subnet.of("0.0.0.0/8")),
        /**
         * 10.0.0.0/8 - RFC 1918
         */
        PRIVATE_10(Ipv4Subnet.of("10.0.0.0/8")),
        /**
         * 172.16.0.0/12 - RFC 1918
         */
        PRIVATE_172_16(Ipv4Subnet.of("172.16.0.0/12")),
        /**
         * 192.168.0.0/16 - RFC 1918
         */
        PRIVATE_192_168(Ipv4Subnet.of("192.168.0.0/16")),
        /**
         * 198.18.0.0/15 - RFC 2544
         */
        TESTING(Ipv4Subnet.of("198.18.0.0/15")),
        /**
         * 192.88.99.0/24 - RFC 3068
         */
        TRANSLATION_6_TO_4(Ipv4Subnet.of("192.88.99.0/24")),
        /**
         * 169.254.0.0/16 - RFC 3927
         */
        LINK_LOCAL(Ipv4Subnet.of("169.254.0.0/16")),
        /**
         * 192.0.0.0/24 - RFC 5736
         */
        SPECIAL_PURPOSE(Ipv4Subnet.of("192.0.0.0/24")),
        /**
         * 192.0.2.0/24 - RFC 5737
         */
        TEST_NET1(Ipv4Subnet.of("192.0.2.0/24")),
        /**
         * 198.51.100.0/24 - RFC 5737
         */
        TEST_NET2(Ipv4Subnet.of("198.51.100.0/24")),
        /**
         * 203.0.113.0/24 - RFC 5737
         */
        TEST_NET3(Ipv4Subnet.of("203.0.113.0/24")),
        /**
         * 224.0.0.0/4 - RFC 5771
         */
        MULTICAST(Ipv4Subnet.of("224.0.0.0/4")),
        /**
         * "100.64.0.0/10 - RFC 6598
         */
        CGNAT(Ipv4Subnet.of("100.64.0.0/10")),
        /**
         * 240.0.0.0/4 - RFC 6890
         */
        RESERVED_240(Ipv4Subnet.of("240.0.0.0/4")),
        /**
         * 255.255.255.255/32 - RFC 6890
         */
        BROADCAST(Ipv4Subnet.of("255.255.255.255/32"));
        //@formatter:on

		private final Ipv4Range range;

		IPv4KnownRange(final Ipv4Range range) {
			this.range = range;
		}

		public boolean contains(final Ipv4Address address) {
			return this.range.contains(address);
		}

		public Ipv4Range range() {
			return this.range;
		}
	}

	protected enum Ip4Octet {
		OCTET_A(0), OCTET_B(1), OCTET_C(2), OCTET_D(3);

		private final int mask;
		private final int shift;

		Ip4Octet(final int byteShift) {
			this.shift = 24 - (byteShift << 3);
			this.mask = 0xff000000 >>> (byteShift << 3);
		}

		public int isolateAsInt(final int ip) {
			return (ip & this.mask) >>> this.shift;
		}

		public byte isolateAsByte(final int ip) {
			return (byte) isolateAsInt(ip);
		}
	}

	public static final int ADDRESS_BITS = 32;
	public static final int ADDRESS_BYTES = 4;
	public static final Ipv4Address MIN_ADDR = Ipv4Address.of("0.0.0.0");
	public static final Ipv4Address MAX_ADDR = Ipv4Address.of("255.255.255.255");

	@Serial
	private static final long serialVersionUID = 2L;

	/**
	 * Build an IPv4Address from a 4 byte long big-endian (highest byte first) byte
	 * array
	 *
	 * @param bigEndianByteArr 4 byte big-endian byte array
	 * @return new IPv4Address instance
	 */
	public static Ipv4Address of(final byte[] bigEndianByteArr) {
		if (bigEndianByteArr == null) {
			throw new NullPointerException("The given array is null");
		}
		if (bigEndianByteArr.length != ADDRESS_BYTES) {
			throw new IllegalArgumentException(String.format(
					"The array has to be 4 bytes long, the given array is %d bytes long", bigEndianByteArr.length));
		}

		return new Ipv4Address(
				shiftToInt(bigEndianByteArr[0], bigEndianByteArr[1], bigEndianByteArr[2], bigEndianByteArr[3]));
	}

	/**
	 * Build an IPv4Address from an int (32 bit)
	 *
	 * @param intIp
	 * @return new IPv4Address instance
	 */
	public static Ipv4Address of(final int intIp) {
		return new Ipv4Address(intIp);
	}

	/**
	 * Build an IPv4Address from a literal String representations such as
	 * "192.168.1.1"
	 *
	 * @param ip literal IP address String
	 * @return new IPv4Address instance
	 */
	public static Ipv4Address of(final String ip) {
		if (ip == null) {
			throw new NullPointerException("String IP address is null");
		}
		if (ip.length() < 7 || ip.length() > 15) {
			throw new IllegalArgumentException("Invalid IP address length in " + ip);
		}
		boolean octetEmpty = true;
		int ipInt = 0;
		int octet = 0;
		int dots = 0;
		for (final char c : ip.toCharArray()) {
			if (c >= '0' && c <= '9') {
				octetEmpty = false;
				octet *= 10;
				octet += c - '0';
				if (octet > 255) {
					throw new IllegalArgumentException("Invalid octet in " + ip);
				}
				continue;
			}
			if (c == '.') {
				if (octetEmpty) {
					throw new IllegalArgumentException("Empty octet in " + ip);
				}
				dots++;
				if (dots > 3) {
					throw new IllegalArgumentException("Too many dots in " + ip);
				}
				ipInt = (ipInt << 8) | octet;
				octet = 0;
				octetEmpty = true;
				continue;
			}
			throw new IllegalArgumentException("Unexpected character " + c + " in " + ip);
		}
		if (dots != 3) {
			throw new IllegalArgumentException("Too few dots in" + ip);
		}
		if (octetEmpty) {
			throw new IllegalArgumentException("Empty octet in " + ip);
		}
		ipInt = (ipInt << 8) | octet;

		return of(ipInt);
	}

	/**
	 * Build an IPv4Address from an java.net.Inet4Address
	 *
	 * @param address
	 * @return new IPv4Address instance
	 */
	public static Ipv4Address of(final Inet4Address address) {
		return of(address.getAddress());
	}

	protected static int shiftToInt(final int a, final int b, final int c, final int d) {
		return (a << 24 | b << 16 | c << 8 | d);
	}

	protected static int shiftToInt(final byte a, final byte b, final byte c, final byte d) {
		return shiftToInt(a & 0xff, b & 0xff, c & 0xff, d & 0xff);
	}

	protected final int ip;

	/**
	 * IPv4Address int constructor
	 *
	 * @param intIp the integer representation of the IPv4 address.
	 */
	public Ipv4Address(final int intIp) {
		this.ip = intIp;
	}

	@Override
	public int version() {
		return 4;
	}

	@Override
	public int compareTo(final IpAddress o) {
		if (o == null) {
			return 1;
		}

		final Ipv4Address other = (Ipv4Address) o;

		if (this.ip == other.ip) {
			return 0;
		}
		return (this.ip + Integer.MIN_VALUE) < (other.toInt() + Integer.MIN_VALUE) ? -1 : 1;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Ipv4Address)) {
			return false;
		}
		return ((Ipv4Address) obj).ip == this.ip;
	}

	@Override
	public int hashCode() {
		return this.ip;
	}

	@Override
	public boolean is6To4() {
		return IPv4KnownRange.TRANSLATION_6_TO_4.contains(this);
	}

	@Override
	public boolean isAnyLocal() {
		return ip == 0;
	}

	/**
	 * Is this the broadcast address?
	 *
	 * @return true if the address is 255.255.255.255
	 */
	public boolean isBroadcast() {
		return IPv4KnownRange.BROADCAST.contains(this);
	}

	@Override
	public boolean isMartian() {
		return isReserved() || isPrivate() || is6To4() || isBroadcast() || isLinkLocal() || isMulticast()
				|| isLoopback() || isUnspecified();
	}

	@Override
	public boolean isLinkLocal() {
		return IPv4KnownRange.LINK_LOCAL.contains(this);
	}

	@Override
	public boolean isLoopback() {
		return IPv4KnownRange.LOOPBACK.contains(this);
	}

	@Override
	public boolean isMulticast() {
		return IPv4KnownRange.MULTICAST.contains(this);
	}

	@Override
	public boolean isPrivate() {
		return IPv4KnownRange.PRIVATE_10.contains(this) || IPv4KnownRange.PRIVATE_172_16.contains(this)
				|| IPv4KnownRange.PRIVATE_192_168.contains(this) || IPv4KnownRange.CGNAT.contains(this);

	}

	@Override
	public boolean isReserved() {
		return IPv4KnownRange.RESERVED_240.contains(this) || IPv4KnownRange.SPECIAL_PURPOSE.contains(this)
				|| IPv4KnownRange.TEST_NET1.contains(this) || IPv4KnownRange.TEST_NET2.contains(this)
				|| IPv4KnownRange.TEST_NET3.contains(this) || IPv4KnownRange.TESTING.contains(this);
	}

	@Override
	public boolean isUnspecified() {
		return IPv4KnownRange.UNSPECIFIED.contains(this);
	}

	@Override
	public Ipv4Address next() {
		return plus(1);
	}

	@Override
	public Ipv4Address plus(final int n) {
		return new Ipv4Address((int) (toLong() + n));
	}

	@Override
	public Ipv4Address previous() {
		return minus(1);
	}

	@Override
	public Ipv4Address minus(final int n) {
		return new Ipv4Address((int) (toLong() - n));
	}

	@Override
	public byte[] toBigEndianArray() {
		return new byte[] { Ip4Octet.OCTET_A.isolateAsByte(this.ip), Ip4Octet.OCTET_B.isolateAsByte(this.ip),
				Ip4Octet.OCTET_C.isolateAsByte(this.ip), Ip4Octet.OCTET_D.isolateAsByte(this.ip) };
	}

	/**
	 * Return a copy of this address, in Inet4Address form
	 *
	 * @return new Inet4Address instance
	 */
	public Inet4Address toInet4Address() {
		return (Inet4Address) toInetAddress();
	}

	@Override
	public byte[] toLittleEndianArray() {
		return new byte[] { Ip4Octet.OCTET_D.isolateAsByte(this.ip), Ip4Octet.OCTET_C.isolateAsByte(this.ip),
				Ip4Octet.OCTET_B.isolateAsByte(this.ip), Ip4Octet.OCTET_A.isolateAsByte(this.ip) };
	}

	@Override
	public String toString() {
		return Integer.toString(Ip4Octet.OCTET_A.isolateAsInt(this.ip)) + '.' + Ip4Octet.OCTET_B.isolateAsInt(this.ip)
				+ '.' + Ip4Octet.OCTET_C.isolateAsInt(this.ip) + '.' + Ip4Octet.OCTET_D.isolateAsInt(this.ip);
	}

	/**
	 * Return a copy of this address in int form
	 *
	 * @return int representation of this address
	 */
	public int toInt() {
		return this.ip;
	}

	/**
	 * Return a copy of this address in long form
	 *
	 * @return long representation of this address
	 */
	public long toLong() {
		return this.ip & 0x00000000ffffffffL;
	}

	/**
	 * Return this address in /32 subnet form. Note that {@link Ipv4Subnet} is a
	 * type of {@link Ipv4Range}, so the returned value is also a single address
	 * range
	 *
	 * @return This address as a single /32 subnet
	 */
	public Ipv4Subnet toSubnet() {
		return Ipv4Subnet.of(this, ADDRESS_BITS);
	}

	/**
	 * Returns a range between this address and an arbitrary one This method takes
	 * care of comparing the addresses so they're always passed to the range factory
	 * in the right order
	 *
	 * @return an IPv4Range between this address and a given one
	 */
	public Ipv4Range toRange(final Ipv4Address address) {
		return this.compareTo(address) < 0 ? Ipv4Range.of(this, address) : Ipv4Range.of(address, this);
	}

	/**
	 * Returns true iff the given address is adjacent (above or below) the current
	 * one
	 *
	 * @return true iff the given address is adjacent to this one
	 */
	public boolean isAdjacentTo(final Ipv4Address other) {
		final Long distance = distanceTo(other);
		return distance == 1 || distance == -1;
	}

	/**
	 * Returns the distance to the given address. If the provided address is bigger,
	 * the result will be positive. If it's smaller, the result will be negative.
	 * <p>
	 * For example, the distance from 10.0.0.1 to 10.0.0.3 is 2, the distance from
	 * 10.0.0.3 to 10.0.0.1 is -2
	 *
	 * @return the distance between this address and the given one
	 */
	public Long distanceTo(final Ipv4Address other) {
		return other.toLong() - this.toLong();
	}

	/**
	 * Returns the address which is the results of a bitwise AND between this
	 * address and the given one. This operation is useful for masking and various
	 * low level bit manipulation
	 *
	 * @return a bitwise AND between this address and the given one
	 */
	public Ipv4Address and(final Ipv4Address other) {
		return Ipv4Address.of(this.ip & other.ip);
	}

	/**
	 * Returns the address which is the results of a bitwise OR between this address
	 * and the given one. This operation is useful for masking and various low level
	 * bit manipulation
	 *
	 * @return a bitwise OR between this address and the given one
	 */
	public Ipv4Address or(final Ipv4Address other) {
		return Ipv4Address.of(this.ip | other.ip);
	}

	/**
	 * Returns the address which is the results of a bitwise XOR between this
	 * address and the given one. This operation is useful for masking and various
	 * low level bit manipulation
	 *
	 * @return a bitwise XOR between this address and the given one
	 */
	public Ipv4Address xor(final Ipv4Address other) {
		return Ipv4Address.of(this.ip ^ other.ip);
	}

	/**
	 * Returns the address which is the results of a bitwise NOT of this address
	 * This operation is useful for masking and various low level bit manipulation
	 *
	 * @return a bitwise NOT of this address
	 */
	public Ipv4Address not() {
		return Ipv4Address.of(~this.ip);
	}
}
