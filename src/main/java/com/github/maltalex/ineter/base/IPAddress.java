/**
 * Copyright (c) 2018, Ineter Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.github.maltalex.ineter.base;

import java.io.Serializable;
import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Abstract class that represents a single IP address
 *
 * @author maltalex
 */
public abstract class IPAddress<C extends IPAddress> implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * Returns either an IPv4 or an IPv6 address
	 *
	 * @param bigEndianByteArr array of 4 or 16 bytes
	 * @return new IPv4Address / IPv6Address
	 * @throws IllegalArgumentException if the given array isn't 4 or 16 bytes long
	 */
	public static IPAddress of(byte[] bigEndianByteArr) {
		if (bigEndianByteArr.length == IPv4Address.ADDRESS_BYTES) {
			return IPv4Address.of(bigEndianByteArr);
		}
		if (bigEndianByteArr.length == IPv6Address.ADDRESS_BYTES) {
			return IPv6Address.of(bigEndianByteArr);
		}

		throw new IllegalArgumentException("Array length must be 4 or 16. Given legth: " + bigEndianByteArr.length);
	}

	/**
	 * Returns either an IPv4 or an IPv6 address
	 *
	 * @param ip an IPv4 or IPv6 address in literal String form
	 * @return new IPv4Address / IPv6Address
	 * @throws IllegalArgumentException if the given array isn't an IPv4/IPv6 address
	 */
	public static IPAddress of(String ip) {
		if (ip.length() >= 2 && ip.length() <= 41) {
			// Either a "." or ":" have to appear within the first 6 characters:
			// [1234: or 123.
			for (int i = 0; i < 6; i++) {
				char c = ip.charAt(i);
				if (c == '.') {
					return IPv4Address.of(ip);
				}
				if (c == ':') {
					return IPv6Address.of(ip);
				}
			}
		}
		throw new IllegalArgumentException(String.format("The string %s is not a valid ip address", ip));
	}

	/**
	 * Returns either an IPv4 or an IPv6 address built from an InetAddress
	 *
	 * @param address to copy from
	 * @return IPv4Address or IPv6Address instance
	 */
	public static IPAddress of(InetAddress address) {
		if (address instanceof Inet6Address) {
			return IPv6Address.of((Inet6Address) address);
		}

		return IPv4Address.of((Inet4Address) address);
	}

	/**
	 * Checks whether the given address is a 6to4 address
	 *
	 * @return true if the give address is a 6to4 address, false otherwise
	 */
	public abstract boolean is6To4();

	/**
	 * Martian addresses are reserved and private addresses that should not
	 * appear on the public internet
	 *
	 * @return true if the address is martian, false otherwise
	 */
	public abstract boolean isMartian();

	/**
	 * Checks whether the address is a loopback address
	 *
	 * @return true if the address is for a loopback, false otherwise
	 */
	public abstract boolean isLoopback();

	/**
	 * Checks whether the address is part of a range reserved for multicast
	 *
	 * @return true if the address is reserved for multicast, false otherwise
	 */
	public abstract boolean isMulticast();

	/**
	 * Checks whether the address is private
	 *
	 * @return true if the address is private, false otherwise
	 */
	public abstract boolean isPrivate();

	/**
	 * Checks whether the address part of a reserved range
	 *
	 * @return true if the address is part of a reserved range, false otherwise
	 */
	public abstract boolean isReserved();

	/**
	 * Checks whether the address is "unspecified"
	 *
	 * @return true if the address is "unspecified", false otherwise
	 */
	public abstract boolean isUnspecified();

	/**
	 * Checks whether the address is link-local
	 *
	 * @return true if the address is link-local, false otherwise
	 */
	public abstract boolean isLinkLocal();

	/**
	 * Returns the version of the IP address
	 *
	 * @return 4 for IPv4, 6 for IPv6
	 */
	public abstract int version();

	/**
	 * Returns the following address, with wraparound
	 *
	 * @return the address following this one
	 */
	public abstract IPAddress next();

	/**
	 * Return an address larger than the current one by n, with wraparound
	 *
	 * @param n
	 * @return an address larger by n
	 */
	public abstract IPAddress plus(int n);

	/**
	 * Returns the previous address, with wraparound
	 *
	 * @return the address before this one
	 */
	public abstract IPAddress previous();

	/**
	 * Return an address smaller than the current one by n, with wraparound
	 *
	 * @param n
	 * @return an address smaller by n
	 */
	public abstract IPAddress minus(int n);

	/**
	 * The address as an array of bytes, with the highest byte first
	 *
	 * @return big-endian byte array
	 */
	public abstract byte[] toBigEndianArray();

	/**
	 * The address as an array of bytes, with the highest byte last
	 *
	 * @return little-endian byte array
	 */
	public abstract byte[] toLittleEndianArray();

	/**
	 * The address as an array of bytes, with the highest byte first
	 *
	 * @return big-endian byte array
	 */
	public byte[] toArray() {
		return toBigEndianArray();
	}

	/**
	 * The address as a BigInteger, unsigned
	 *
	 * @return The address as an BigInteger
	 */
	public BigInteger toBigInteger() {
		return new BigInteger(1, toBigEndianArray());
	}

	/**
	 * The address as a BigInteger, signed
	 *
	 * @return The address as an BigInteger
	 */
	public BigInteger toSignedBigInteger() {
		return new BigInteger(toBigEndianArray());
	}

	/**
	 * Checks whether this and other addresses are adjacent to each other
	 *
	 * @param other another address to compare
	 * @return true - are adjacent, false - not adjacent
	 */
	public abstract boolean isAdjacentTo(C other);

	/**
	 * The address as an InetAddress
	 *
	 * @return The address as an InetAddress
	 */
	public InetAddress toInetAddress() {
		try {
			return InetAddress.getByAddress(toBigEndianArray());
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
	}
}