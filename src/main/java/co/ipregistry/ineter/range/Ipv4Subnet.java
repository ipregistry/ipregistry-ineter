/*
 * Copyright (c) 2020, ineter contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package co.ipregistry.ineter.range;

import co.ipregistry.ineter.base.Ipv4Address;

public class Ipv4Subnet extends Ipv4Range implements IpSubnet<Ipv4Subnet, Ipv4Range, Ipv4Address, Long> {

	protected enum IPv4SubnetMask {

		// @formatter:off
        MASK_00, MASK_01, MASK_02, MASK_03,
        MASK_04, MASK_05, MASK_06, MASK_07,
        MASK_08, MASK_09, MASK_10, MASK_11,
        MASK_12, MASK_13, MASK_14, MASK_15,
        MASK_16, MASK_17, MASK_18, MASK_19,
        MASK_20, MASK_21, MASK_22, MASK_23,
        MASK_24, MASK_25, MASK_26, MASK_27,
        MASK_28, MASK_29, MASK_30, MASK_31, MASK_32;
        // @formatter:on

		public static IPv4SubnetMask fromMaskLen(final int maskLen) {
			if (maskLen >= 0 && maskLen <= Ipv4Address.ADDRESS_BITS) {
				return IPv4SubnetMask.values()[maskLen];
			}
			throw new IllegalArgumentException("The mask length must be between 0 and 32");
		}

		private final int mask;
		private final int bitCount;

		IPv4SubnetMask() {
			this.bitCount = ordinal();
			this.mask = this.bitCount != 0 ? 0xffffffff << (32 - this.bitCount) : 0;
		}

		public int mask() {
			return this.mask;
		}

		public int maskBitCount() {
			return this.bitCount;
		}

		public int and(final int ip) {
			return this.mask & ip;
		}

		public Ipv4Address and(final Ipv4Address ip) {
			return Ipv4Address.of(and(ip.toInt()));
		}

		public int orInverted(final int ip) {
			return (~this.mask) | ip;
		}

		public Ipv4Address orInverted(final Ipv4Address ip) {
			return Ipv4Address.of(orInverted(ip.toInt()));
		}

		public Ipv4Address toAddress() {
			return Ipv4Address.of(mask());
		}
	}

	private static final long serialVersionUID = 3L;

	public static Ipv4Subnet of(final String cidr) {
		final int slashIndex = cidr.indexOf('/');
		if (slashIndex == -1) {
			throw new IllegalArgumentException("Expected '/' in cidr");
		}
		return of(cidr.substring(0, slashIndex), cidr.substring(slashIndex + 1));
	}

	public static Ipv4Subnet of(final String address, final int maskLen) {
		return new Ipv4Subnet(Ipv4Address.of(address), IPv4SubnetMask.fromMaskLen(maskLen));
	}

	public static Ipv4Subnet of(final Ipv4Address address, final int maskLen) {
		return new Ipv4Subnet(address, IPv4SubnetMask.fromMaskLen(maskLen));
	}

	public static Ipv4Subnet of(final String address, final String maskLen) {
		return new Ipv4Subnet(Ipv4Address.of(address), IPv4SubnetMask.fromMaskLen(Integer.parseUnsignedInt(maskLen)));
	}

	public static Ipv4Subnet parse(final String from) throws IllegalArgumentException {
		return IpRangeUtils.parseSubnet(from, Ipv4Subnet::of, 32);
	}

	static Ipv4Subnet of(final String address, final Integer subnet) {
		return Ipv4Subnet.of(address, subnet.intValue());
	}

	protected final int networkBitCount;

	public Ipv4Subnet(final Ipv4Address address, final IPv4SubnetMask mask) {
		super(mask.and(address), mask.orInverted(address));
		this.networkBitCount = mask.maskBitCount();
	}

	@Override
	public String toString() {
		return super.firstAddress.toString() + '/' + this.networkBitCount;
	}

	@Override
	public int getNetworkBitCount() {
		return this.networkBitCount;
	}

	@Override
	public Ipv4Address getNetworkMask() {
		return IPv4SubnetMask.fromMaskLen(this.networkBitCount).toAddress();
	}

	@Override
	public int getHostBitCount() {
		return Ipv4Address.ADDRESS_BITS - this.networkBitCount;
	}

	@Override
	public Ipv4Address getNetworkAddress() {
		return getFirst();
	}

}
