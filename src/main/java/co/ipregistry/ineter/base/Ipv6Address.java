/*
 * Copyright (c) 2020, ineter contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package co.ipregistry.ineter.base;

import java.math.BigInteger;
import java.net.Inet6Address;

import co.ipregistry.ineter.range.Ipv6Range;
import co.ipregistry.ineter.range.Ipv6Subnet;

public class Ipv6Address implements IpAddress {

	public enum IPv6KnownRange {

		//@formatter:off
        /**
         * ::/128 - RFC 4291
         */
        UNSPECIFIED(Ipv6Subnet.of("::/128")),
        /**
         * ::1/128 - RFC 4291
         */
        LOOPBACK(Ipv6Subnet.of("::1/128")),

        /**
         * 100::/64 - RFC 6666
         */
        DISCARD(Ipv6Subnet.of("100::/64")),
        /**
         * 2001:10::/28 - RFC 4843
         */
        ORCHID(Ipv6Subnet.of("2001:10::/28")),
        /**
         * 2001:20::/28
         */
        ORCHID_2(Ipv6Subnet.of("2001:20::/28")),
        /**
         * 2001:db8::/32 - RFC 3849
         */
        DOCUMENTATION(Ipv6Subnet.of("2001:db8::/32")),

        /**
         * ::/96 - RFC 4291
         */
        IPV4_COMPATIBLE_IPV6_DEPRECATED(Ipv6Subnet.of("::/96")),
        /**
         * ::ffff:0:0/96 - RFC 4291
         */
        IPV4_MAPPED_IPV6(Ipv6Subnet.of("::ffff:0:0/96")),
        /**
         * 64:ff9b::/96 - RFC 6052
         */
        IPV4_IPV6_TRANSLATION_WELL_KNOWN(Ipv6Subnet.of("64:ff9b::/96")),
        /**
         * 2002::/16 - RFC 3056
         */
        TRANSLATION_6_TO_4(Ipv6Subnet.of("2002::/16")),
        /**
         * 2001:0000:/32 - RFC 4380
         */
        TEREDO(Ipv6Subnet.of("2001::/32")),

        /**
         * fc00::/7 - RFC 4193
         */
        ULA(Ipv6Subnet.of("fc00::/7")),

        /**
         * ff00::/8 - RFC 4291
         */
        MULTICAST(Ipv6Subnet.of("ff00::/8")),
        /**
         * ff0e::/16 - RFC 4291
         */
        GLOBAL_MULTICAST(Ipv6Subnet.of("ff0e::/16")),
        /**
         * ff05::/16 - RFC 4291
         */
        SITE_LOCAL_MULTICAST(Ipv6Subnet.of("ff05::/16")),
        /**
         * ff02::/16 - RFC 4291
         */
        LINK_LOCAL_MULTICAST(Ipv6Subnet.of("ff02::/16")),
        /**
         * ff01::/16 - RFC 4291
         */
        INTERFACE_LOCAL_MULTICAST(Ipv6Subnet.of("ff01::/16")),

        /**
         * 2000::/3 - RFC 3587
         */
        GLOBAL_UNICAST(Ipv6Subnet.of("2000::/3")),
        /**
         * fe80::/10 - RFC 4291
         */
        LINK_LOCAL_UNICAST(Ipv6Subnet.of("fe80::/10")),
        /**
         * fec::/10 - RFC 4291
         */
        SITE_LOCAL_UNICAST_DEPRECATED(Ipv6Subnet.of("fec::/10"));
        //@formatter:on

		private final Ipv6Range range;

		IPv6KnownRange(final Ipv6Range subnet) {
			this.range = subnet;
		}

		public boolean contains(final Ipv6Address address) {
			return this.range.contains(address);
		}

		public Ipv6Range range() {
			return this.range;
		}
	}

	/**
	 * Enum for extracting 16-bit shorts from 64-bit longs
	 */
	protected enum LongShort {
		SHORT_A(0), SHORT_B(1), SHORT_C(2), SHORT_D(3);

		private final long mask;
		private final int shift;

		LongShort(final int shortShift) {
			this.shift = 48 - (shortShift << 4);
			this.mask = 0xffff000000000000L >>> (shortShift << 4);
		}

		public long isolateAsLong(final long l) {
			return (l & this.mask) >>> this.shift;
		}

		public int isolateAsInt(final long l) {
			return (int) isolateAsLong(l);
		}
	}

	/**
	 * Enum for extracting bytes from 64-bit longs
	 */
	protected enum LongByte {
		BYTE_A(0), BYTE_B(1), BYTE_C(2), BYTE_D(3), BYTE_E(4), BYTE_F(5), BYTE_G(6), BYTE_H(7);

		private final long mask;
		private final int shift;

		LongByte(final int shortShift) {
			this.shift = 56 - (shortShift << 3);
			this.mask = 0xff00000000000000L >>> (shortShift << 3);
		}

		public long isolateAsLong(final long l) {
			return (l & this.mask) >>> this.shift;
		}

		public byte isolateAsByte(final long l) {
			return (byte) isolateAsLong(l);
		}

		public long expand(final byte b) {
			return (b & 0xffL) << this.shift;
		}

		static long extractLong(final byte[] bigEndianByteArr, final int offset) {
			return LongByte.BYTE_A.expand(bigEndianByteArr[offset])
					| LongByte.BYTE_B.expand(bigEndianByteArr[offset + 1])
					| LongByte.BYTE_C.expand(bigEndianByteArr[offset + 2])
					| LongByte.BYTE_D.expand(bigEndianByteArr[offset + 3])
					| LongByte.BYTE_E.expand(bigEndianByteArr[offset + 4])
					| LongByte.BYTE_F.expand(bigEndianByteArr[offset + 5])
					| LongByte.BYTE_G.expand(bigEndianByteArr[offset + 6])
					| LongByte.BYTE_H.expand(bigEndianByteArr[offset + 7]);
		}
	}

	public static final int ADDRESS_BITS = 128;
	public static final int ADDRESS_BYTES = 16;
	public static final int ADDRESS_SHORTS = 8;
	public static final int HOLDER_BITS = 64;
	public static final Ipv6Address MIN_ADDR = Ipv6Address.of("::");
	public static final Ipv6Address MAX_ADDR = Ipv6Address.of("ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff");

	private static final long serialVersionUID = 2L;
	private static final BigInteger NEGATIVE_ONE = BigInteger.ONE.negate();

	/**
	 * Build an IPv6Address from two longs - upper and lower 64 bits in form of
	 * longs
	 *
	 * @param upper upper 64 bits of the IPv6Address
	 * @param lower lower 64 bits of the IPv6Address
	 * @return new IPv6Address instance
	 */
	public static Ipv6Address of(final long upper, final long lower) {
		return new Ipv6Address(upper, lower);
	}

	protected static void verifyArray(final byte[] bigEndianByteArr) {
		if (bigEndianByteArr == null) {
			throw new NullPointerException();
		}
		if (bigEndianByteArr.length != ADDRESS_BYTES) {
			throw new IllegalArgumentException("The given array must be 16 bytes long");
		}
	}

	/**
	 * Build an IPv6Address from a 16 byte long big-endian (highest byte first) byte
	 * array
	 *
	 * @param bigEndianByteArr 16 byte big-endian byte array
	 * @return new IPv6Address instance
	 */
	public static Ipv6Address of(final byte[] bigEndianByteArr) {
		verifyArray(bigEndianByteArr);
		return new Ipv6Address(LongByte.extractLong(bigEndianByteArr, 0), LongByte.extractLong(bigEndianByteArr, 8));
	}

	/**
	 * Build an IPv6Address from an Inet6Address
	 *
	 * @param address
	 * @return new IPv6Address instance
	 */
	public static Ipv6Address of(final Inet6Address address) {
		if (address.getScopeId() == 0 && address.getScopedInterface() == null) {
			return of(address.getAddress());
		}
		return ZonedIpv6Address.of(address);
	}

	/**
	 * Build an IPv6Address or a ZonedIPv6Address from a literal IPv6 address in
	 * String from such as "2001:1:2:3:4:5:6:7", "2001::", "[::]", "fe80::1%eth0",
	 * and similar valid forms
	 *
	 * @param address
	 * @return new IPv6Address instance
	 */
	public static Ipv6Address of(final String address) {
		// This (over-engineered) method parses and validates an IPv6 address in
		// String form in a single pass using only primitive types (except the
		// zone String).

		// The idea is to iterate over the address start to finish, accumulating
		// the current "part" (i.e. 16 bit piece between colons).
		// if we stumble upon a double colon (::), then we need to figure out
		// how many zeroes it represents.

		// To do that, we stop the forward iteration and start
		// iterating from the end, accumulating "parts" as we go along until we
		// reach the same double colon.

		// According to a JMH benchmark, this method parses randomly generated
		// addresses, half of which contain a double colon ("::"), about
		// 40% faster than Java's default InetAddress parsing

		// 0. Validate Not null
		if (address == null) {
			throw new IllegalArgumentException("Attempted to parse null address");
		}

		// 1. Validate Length
		int first = 0, last = address.length();
		if (address.length() < 2) {
			throw new IllegalArgumentException(
					String.format("Invalid length - the string %s is too short to be an IPv6 address", address));
		}
		if (address.charAt(0) == '[') {
			first++;
			if (!(address.charAt(--last) == ']')) {
				throw new IllegalArgumentException("The address begins with \"[\" but doesn't end with \"]\"");
			}
		}
		String zone = null;
		for (int i = last - 1; i > first; i--) {
			final char ch = address.charAt(i);
			if (ch == ':') { // Looks like a normal address, carry on parsing
				break;
			}
			if (ch == '%') { // This is a zoned address - take out the zone and
				// move the "last" index
				zone = address.substring(i + 1, last); // skip the "%" itself
				last = i;
				break;
			}
		}
		final int length = last - first;
		if (length > 39) {
			throw new IllegalArgumentException(
					String.format("Invalid length - the string %s is too long to be an IPv6 address. Length: %d",
							address, address.length()));
		}

		//@formatter:off
        //Holders
        long partAccumulator = 0; // Accumulator for the current address part, before it's added to the upper/lower accumulators
        long upperAccumulator = 0, lowerAccumulator = 0; //Accumulators for the upper and lower 64 bit parts of the address
        //Indexes
        int partIndex = 0; //Index of the current 16 bit part - should be 0 to 7
        int afterDoubleSemicolonIndex = last + 2; //Index after :: characters. Originally set past the string length
        //Counters
        int partCount = 1; //Total number of 16 bit address parts, for address verification
        int partHexDigitCount = 0; //Number of hex digits in current 16 bit part (should be up to 4)
        //@formatter:on

		// 2. Iterate start to finish or until a :: is encountered
		for (int i = first; i < last; i++) {
			final char c = address.charAt(i);
			if (isHexDigit(c)) {
				if (++partHexDigitCount > 4) {
					throw new IllegalArgumentException(
							"Address parts must contain no more than 16 bits (4 hex digits)");
				}
				// Add to part accumulator
				partAccumulator = (partAccumulator << 4) | (Character.digit(c, 16) & 0xffff);
			} else {
				if (c == ':') {
					// Reached end of current part. Add to accumulator
					if (partIndex < 4) {
						upperAccumulator |= partAccumulator << (48 - (partIndex << 4));
					} else {
						lowerAccumulator |= partAccumulator << (48 - ((partIndex - 4) << 4));
					}
					partIndex++;
					partCount++;
					partAccumulator = 0;
					partHexDigitCount = 0;
					// Is next char ":"?
					if (i < last - 1 && address.charAt(i + 1) == ':') {
						// Found :: - continue to (3) - iterate from the end
						afterDoubleSemicolonIndex = i + 2;
						break;
					}
					continue;
				}
				throw new IllegalArgumentException(String.format("Illegal character: %c at index %d", c, i));
			}
		}

		// 3. Iterate from the end until the ::
		final int lastFilledPartIndex = partIndex - 1;
		partIndex = 7;
		for (int i = last - 1; i >= afterDoubleSemicolonIndex; i--) {
			final char c = address.charAt(i);
			if (isHexDigit(c)) {
				if (partIndex <= lastFilledPartIndex) {
					throw new IllegalArgumentException("Too many parts. Expected 8 parts");
				}
				partAccumulator |= ((long) (Character.digit(c, 16) & 0xffff) << (partHexDigitCount << 2));
				if (++partHexDigitCount > 4) {
					throw new IllegalArgumentException(
							"Address parts must contain no more than 16 bits (4 hex digits)");
				}
			} else {
				if (c == ':') {
					if (partIndex < 4) {
						upperAccumulator |= partAccumulator << (48 - (partIndex << 4));
					} else {
						lowerAccumulator |= partAccumulator << (48 - ((partIndex - 4) << 4));
					}
					if (address.charAt(i - 1) == ':') {
						throw new IllegalArgumentException(String.format("Error at index %d - unexpected colon", i));
					}
					partCount++;
					partIndex--;
					partAccumulator = 0;
					partHexDigitCount = 0;
					continue;
				}
				throw new IllegalArgumentException(String.format("Illegal character: %c at index %d", c, i));
			}
		}

		// 4. Append last part
		if (partIndex < 4) {
			upperAccumulator |= partAccumulator << (48 - (partIndex << 4));
		} else {
			lowerAccumulator |= partAccumulator << (48 - ((partIndex - 4) << 4));
		}

		// 5. Check total number of parts
		if (partCount > ADDRESS_SHORTS || (partCount < ADDRESS_SHORTS && afterDoubleSemicolonIndex == last + 2)) {
			throw new IllegalArgumentException(String.format("Invalid number of parts. Expected 8, got %d", partCount));
		}
		return zone == null ? new Ipv6Address(upperAccumulator, lowerAccumulator)
				: new ZonedIpv6Address(upperAccumulator, lowerAccumulator, zone);
	}

	protected static boolean isHexDigit(final char c) {
		return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
	}

	protected static int unsignedCompare(final long a, final long b) {
		if (a == b) {
			return 0;
		}
		return (a + Long.MIN_VALUE) < (b + Long.MIN_VALUE) ? -1 : 1;
	}

	static boolean hasCarry(final long a, final long b, final long result) {
		final long aMSB = a >>> 63;
		final long bMSB = b >>> 63;
		final long resutlMSB = result >>> 63;

		/* @formatter:off
         * a b r Carry
         * 0 0 0 0
         * 0 0 1 0
         * 0 1 0 1
         * 0 1 1 0
         * 1 0 0 1
         * 1 0 1 0
         * 1 1 0 1
         * 1 1 1 1
         * @formatter:on
         */
		return ((aMSB & bMSB) == 1) || ((aMSB ^ bMSB) == 1 && resutlMSB == 0);
	}

	static boolean hasBorrow(final long a, final long b, final long result) {
		final long aMSB = a >>> 63;
		final long bMSB = b >>> 63;
		final long resutlMSB = result >>> 63;

		/* @formatter:off
         * a b r Borrow
         * 0 0 0 0
         * 0 0 1 1
         * 0 1 0 1
         * 0 1 1 1
         * 1 0 0 0
         * 1 0 1 0
         * 1 1 0 0
         * 1 1 1 1
         * @formatter:on
         */
		return ((aMSB & bMSB & resutlMSB) == 1) || (aMSB == 0 && (bMSB | resutlMSB) == 1);
	}

	protected final long upper;
	protected final long lower;

	/**
	 * Build an IPv6Address from two longs - upper and lower 64 bits in form of
	 * longs
	 *
	 * @param upper upper 64 bits of the IPv6Address
	 * @param lower lower 64 bits of the IPv6Address
	 */
	public Ipv6Address(final long upper, final long lower) {
		this.upper = upper;
		this.lower = lower;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (this.lower ^ (this.lower >>> 32));
		result = prime * result + (int) (this.upper ^ (this.upper >>> 32));
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Ipv6Address other = (Ipv6Address) obj;
		if (this.lower != other.lower) {
			return false;
		}
		return this.upper == other.upper;
	}

	/**
	 * return upper 64 bits of the address in long form
	 *
	 * @return upper 64 bits
	 */
	public long getUpper() {
		return this.upper;
	}

	/**
	 * return lower 64 bits of the address in long form
	 *
	 * @return lower 64 bits
	 */
	public long getLower() {
		return this.lower;
	}

	@Override
	public boolean is6To4() {
		return IPv6KnownRange.TRANSLATION_6_TO_4.contains(this);
	}

	@Override
	public boolean isMartian() {
		return isUnspecified() || isLoopback() || IPv6KnownRange.IPV4_MAPPED_IPV6.contains(this)
				|| IPv6KnownRange.IPV4_COMPATIBLE_IPV6_DEPRECATED.contains(this)
				|| IPv6KnownRange.IPV4_IPV6_TRANSLATION_WELL_KNOWN.contains(this) || isReserved() || isPrivate()
				|| isLinkLocal() || (isMulticast() && !IPv6KnownRange.GLOBAL_MULTICAST.contains(this));
	}

	/**
	 * Is this one of the IPv6 addresses reserved for IPv4-IPv6 translation -
	 * Teredo, IPv4-mapped-IPv6, 6to4, or IPv4-embedded-IPv6?
	 *
	 * @return true if this is a IPv4-IPv6 translation address
	 */
	public boolean isIPv4Translation() {
		return IPv6KnownRange.TEREDO.contains(this) || IPv6KnownRange.IPV4_MAPPED_IPV6.contains(this)
				|| IPv6KnownRange.TRANSLATION_6_TO_4.contains(this)
				|| IPv6KnownRange.IPV4_IPV6_TRANSLATION_WELL_KNOWN.contains(this);
	}

	@Override
	public boolean isLoopback() {
		return IPv6KnownRange.LOOPBACK.contains(this);
	}

	/**
	 * Is this an IPv6 global unicast address?
	 *
	 * @return true if this is a global unicast address
	 */
	public boolean isGlobalUnicast() {
		return IPv6KnownRange.GLOBAL_UNICAST.contains(this);
	}

	@Override
	public boolean isLinkLocal() {
		return IPv6KnownRange.LINK_LOCAL_UNICAST.contains(this);
	}

	@Override
	public boolean isMulticast() {
		return IPv6KnownRange.MULTICAST.contains(this);
	}

	@Override
	public boolean isPrivate() {
		return IPv6KnownRange.ULA.contains(this);
	}

	@Override
	public boolean isReserved() {
		return IPv6KnownRange.ORCHID.contains(this) || IPv6KnownRange.ORCHID_2.contains(this)
				|| IPv6KnownRange.DISCARD.contains(this) || IPv6KnownRange.DOCUMENTATION.contains(this);
	}

	@Override
	public boolean isUnspecified() {
		return IPv6KnownRange.UNSPECIFIED.contains(this);
	}

	@Override
	public Ipv6Address next() {
		return plus(1);
	}

	/**
	 * Return an address larger than the current one by n, with wraparound
	 *
	 * @param n
	 * @return an address larger by n
	 */
	public Ipv6Address plus(final long n) {
		if (n < 0) {
			return minus(-n);
		}
		final long newLower = this.lower + n;
		long newUpper = this.upper;

		if (hasCarry(this.lower, n, newLower)) {
			newUpper++;
		}

		return new Ipv6Address(newUpper, newLower);
	}

	@Override
	public Ipv6Address plus(final int n) {
		return plus((long) n);
	}

	@Override
	public Ipv6Address previous() {
		return minus(1);
	}

	@Override
	public Ipv6Address minus(final int n) {
		return minus((long) n);
	}

	/**
	 * Return an address smaller than the current one by n, with wraparound
	 *
	 * @param n
	 * @return an address smaller by n
	 */
	public Ipv6Address minus(final long n) {
		if (n < 0) {
			return plus(-n);
		}
		final long newLower = this.lower - n;
		long newUpper = this.upper;

		// If there's a borrow from the lower addition
		if (hasBorrow(this.lower, n, newLower)) {
			newUpper--;
		}
		return new Ipv6Address(newUpper, newLower);
	}

	@Override
	public byte[] toBigEndianArray() {
		return new byte[] { LongByte.BYTE_A.isolateAsByte(this.upper), LongByte.BYTE_B.isolateAsByte(this.upper),
				LongByte.BYTE_C.isolateAsByte(this.upper), LongByte.BYTE_D.isolateAsByte(this.upper),
				LongByte.BYTE_E.isolateAsByte(this.upper), LongByte.BYTE_F.isolateAsByte(this.upper),
				LongByte.BYTE_G.isolateAsByte(this.upper), LongByte.BYTE_H.isolateAsByte(this.upper),
				LongByte.BYTE_A.isolateAsByte(this.lower), LongByte.BYTE_B.isolateAsByte(this.lower),
				LongByte.BYTE_C.isolateAsByte(this.lower), LongByte.BYTE_D.isolateAsByte(this.lower),
				LongByte.BYTE_E.isolateAsByte(this.lower), LongByte.BYTE_F.isolateAsByte(this.lower),
				LongByte.BYTE_G.isolateAsByte(this.lower), LongByte.BYTE_H.isolateAsByte(this.lower) };
	}

	@Override
	public byte[] toLittleEndianArray() {
		return new byte[] { LongByte.BYTE_H.isolateAsByte(this.lower), LongByte.BYTE_G.isolateAsByte(this.lower),
				LongByte.BYTE_F.isolateAsByte(this.lower), LongByte.BYTE_E.isolateAsByte(this.lower),
				LongByte.BYTE_D.isolateAsByte(this.lower), LongByte.BYTE_C.isolateAsByte(this.lower),
				LongByte.BYTE_B.isolateAsByte(this.lower), LongByte.BYTE_A.isolateAsByte(this.lower),
				LongByte.BYTE_H.isolateAsByte(this.upper), LongByte.BYTE_G.isolateAsByte(this.upper),
				LongByte.BYTE_F.isolateAsByte(this.upper), LongByte.BYTE_E.isolateAsByte(this.upper),
				LongByte.BYTE_D.isolateAsByte(this.upper), LongByte.BYTE_C.isolateAsByte(this.upper),
				LongByte.BYTE_B.isolateAsByte(this.upper), LongByte.BYTE_A.isolateAsByte(this.upper) };
	}

	@Override
	public int compareTo(final IpAddress o) {
		if (o == null) {
			return 1; // Bigger than null
		}

		final Ipv6Address other = (Ipv6Address) o;
		if (other.isZoned()) {
			return -1;// Zoned addresses are "bigger"
		}

		return longCompare(other);
	}

	protected int longCompare(final Ipv6Address o) {
		final int upperCompare = unsignedCompare(this.upper, o.upper);
		return (upperCompare == 0) ? unsignedCompare(this.lower, o.lower) : upperCompare;
	}

	public Inet6Address toInet6Address() {
		return (Inet6Address) this.toInetAddress();
	}

	@Override
	public String toString() {
		return Integer.toHexString(LongShort.SHORT_A.isolateAsInt(this.upper)) + ":"
				+ Integer.toHexString(LongShort.SHORT_B.isolateAsInt(this.upper)) + ":"
				+ Integer.toHexString(LongShort.SHORT_C.isolateAsInt(this.upper)) + ":"
				+ Integer.toHexString(LongShort.SHORT_D.isolateAsInt(this.upper)) + ":"
				+ Integer.toHexString(LongShort.SHORT_A.isolateAsInt(this.lower)) + ":"
				+ Integer.toHexString(LongShort.SHORT_B.isolateAsInt(this.lower)) + ":"
				+ Integer.toHexString(LongShort.SHORT_C.isolateAsInt(this.lower)) + ":"
				+ Integer.toHexString(LongShort.SHORT_D.isolateAsInt(this.lower));
	}

	@Override
	public int version() {
		return 6;
	}

	/**
	 * does this address have a specific zone?
	 *
	 * @return false
	 */
	public boolean isZoned() {
		return false;
	}

	/**
	 * Return this address in /128 subnet form. Note that {@link Ipv6Subnet} is a
	 * type of {@link Ipv6Range}, so the returned value is also a single address
	 * range
	 *
	 * @return This address as a single /128 subnet
	 */
	public Ipv6Subnet toSubnet() {
		return Ipv6Subnet.of(this, ADDRESS_BITS);
	}

	/**
	 * Returns a range between this address and an arbitrary one This method takes
	 * care of comparing the addresses so they're always passed to the range factory
	 * in the right order
	 *
	 * @return an IPv6Range between this address and a given one
	 */
	public Ipv6Range toRange(final Ipv6Address address) {
		return this.compareTo(address) < 0 ? Ipv6Range.of(this, address) : Ipv6Range.of(address, this);
	}

	/**
	 * Returns true iff the given address is adjacent (above or below) the current
	 * one
	 *
	 * @return true iff the given address is adjacent to this one
	 */
	public boolean isAdjacentTo(final Ipv6Address other) {
		final BigInteger distance = distanceTo(other);
		return distance.equals(BigInteger.ONE) || distance.equals(NEGATIVE_ONE);
	}

	/**
	 * Returns the distance to the given address. If the provided address is bigger,
	 * the result will be positive. If it's smaller, the result will be negative.
	 * <p>
	 * For example, the distance from ::1 to ::3 is 2, the distance from ::3 to ::1
	 * is -2
	 *
	 * @return the distance between this address and the given one
	 */
	public BigInteger distanceTo(final Ipv6Address other) {
		return other.toBigInteger().subtract(this.toBigInteger());
	}

	/**
	 * Returns the address which is the results of a bitwise AND between this
	 * address and the given one. This operation is useful for masking and various
	 * low level bit manipulation
	 *
	 * @return a bitwise AND between this address and the given one
	 */
	public Ipv6Address and(final Ipv6Address other) {
		return Ipv6Address.of(this.upper & other.upper, this.lower & other.lower);
	}

	/**
	 * Returns the address which is the results of a bitwise OR between this address
	 * and the given one. This operation is useful for masking and various low level
	 * bit manipulation
	 *
	 * @return a bitwise OR between this address and the given one
	 */
	public Ipv6Address or(final Ipv6Address other) {
		return Ipv6Address.of(this.upper | other.upper, this.lower | other.lower);
	}

	/**
	 * Returns the address which is the results of a bitwise XOR between this
	 * address and the given one. This operation is useful for masking and various
	 * low level bit manipulation
	 *
	 * @return a bitwise XOR between this address and the given one
	 */
	public Ipv6Address xor(final Ipv6Address other) {
		return Ipv6Address.of(this.upper ^ other.upper, this.lower ^ other.lower);
	}

	/**
	 * Returns the address which is the results of a bitwise NOT of this address
	 * This operation is useful for masking and various low level bit manipulation
	 *
	 * @return a bitwise NOT of this address
	 */
	public Ipv6Address not() {
		return Ipv6Address.of(~this.upper, ~this.lower);
	}
}
