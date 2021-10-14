/*
 * Copyright (c) 2020, ineter contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package co.ipregistry.ineter.range;

import java.math.BigInteger;

import co.ipregistry.ineter.base.Ipv6Address;

public class Ipv6Subnet extends Ipv6Range implements IpSubnet<Ipv6Subnet, Ipv6Range, Ipv6Address, BigInteger> {

	enum IPv6SubnetMask {

		//@formatter:off
        MASK_000, MASK_001, MASK_002, MASK_003, MASK_004, MASK_005, MASK_006, MASK_007,
        MASK_008, MASK_009, MASK_010, MASK_011, MASK_012, MASK_013, MASK_014, MASK_015,
        MASK_016, MASK_017, MASK_018, MASK_019, MASK_020, MASK_021, MASK_022, MASK_023,
        MASK_024, MASK_025, MASK_026, MASK_027, MASK_028, MASK_029, MASK_030, MASK_031,
        MASK_032, MASK_033, MASK_034, MASK_035, MASK_036, MASK_037, MASK_038, MASK_039,
        MASK_040, MASK_041, MASK_042, MASK_043, MASK_044, MASK_045, MASK_046, MASK_047,
        MASK_048, MASK_049, MASK_050, MASK_051, MASK_052, MASK_053, MASK_054, MASK_055,
        MASK_056, MASK_057, MASK_058, MASK_059, MASK_060, MASK_061, MASK_062, MASK_063,
        MASK_064, MASK_065, MASK_066, MASK_067, MASK_068, MASK_069, MASK_070, MASK_071,
        MASK_072, MASK_073, MASK_074, MASK_075, MASK_076, MASK_077, MASK_078, MASK_079,
        MASK_080, MASK_081, MASK_082, MASK_083, MASK_084, MASK_085, MASK_086, MASK_087,
        MASK_088, MASK_089, MASK_090, MASK_091, MASK_092, MASK_093, MASK_094, MASK_095,
        MASK_096, MASK_097, MASK_098, MASK_099, MASK_100, MASK_101, MASK_102, MASK_103,
        MASK_104, MASK_105, MASK_106, MASK_107, MASK_108, MASK_109, MASK_110, MASK_111,
        MASK_112, MASK_113, MASK_114, MASK_115, MASK_116, MASK_117, MASK_118, MASK_119,
        MASK_120, MASK_121, MASK_122, MASK_123, MASK_124, MASK_125, MASK_126, MASK_127, MASK_128;
        //@formatter:on

		public static IPv6SubnetMask fromMaskLen(final int maskLen) {
			if (maskLen >= 0 && maskLen <= Ipv6Address.ADDRESS_BITS) {
				return IPv6SubnetMask.values()[maskLen];
			}
			throw new IllegalArgumentException("The mask length must be between 0 and 128");
		}

		private final long maskUpper, maskLower;
		private final int bitCount;

		IPv6SubnetMask() {
			this.bitCount = ordinal();
			final int upperCount = Math.min(64, this.bitCount);
			final int lowerCount = this.bitCount - upperCount;
			this.maskUpper = upperCount != 0 ? 0x8000000000000000L >> upperCount - 1 : 0;
			this.maskLower = lowerCount != 0 ? 0x8000000000000000L >> lowerCount - 1 : 0;
		}

		public int maskBitCount() {
			return this.bitCount;
		}

		public Ipv6Address and(final Ipv6Address ip) {
			return Ipv6Address.of(ip.getUpper() & this.maskUpper, ip.getLower() & this.maskLower);
		}

		public Ipv6Address orInverted(final Ipv6Address ip) {
			return Ipv6Address.of(ip.getUpper() | ~this.maskUpper, ip.getLower() | ~this.maskLower);
		}

		public Ipv6Address toAddress() {
			return Ipv6Address.of(this.maskUpper, this.maskLower);
		}
	}

	private static final long serialVersionUID = 3L;

	public static Ipv6Subnet of(final String cidr) {
		final int slashIndex = cidr.indexOf('/');
		if (slashIndex == -1) {
			throw new IllegalArgumentException("Expected '/' in cidr");
		}
		return of(cidr.substring(0, slashIndex), cidr.substring(slashIndex + 1));
	}

	public static Ipv6Subnet parse(final String from) {
		return IpRangeUtils.parseSubnet(from, Ipv6Subnet::of, 128);
	}

	public static Ipv6Subnet of(final String address, final int maskLen) {
		return new Ipv6Subnet(Ipv6Address.of(address), IPv6SubnetMask.fromMaskLen(maskLen));
	}

	public static Ipv6Subnet of(final Ipv6Address address, final int maskLen) {
		return new Ipv6Subnet(address, IPv6SubnetMask.fromMaskLen(maskLen));
	}

	public static Ipv6Subnet of(final String address, final String maskLen) {
		return new Ipv6Subnet(Ipv6Address.of(address), IPv6SubnetMask.fromMaskLen(Integer.parseUnsignedInt(maskLen)));
	}

	protected final int networkBitCount;

	public Ipv6Subnet(final Ipv6Address address, final IPv6SubnetMask mask) {
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
	public Ipv6Address getNetworkMask() {
		return IPv6SubnetMask.fromMaskLen(this.networkBitCount).toAddress();
	}

	@Override
	public int getHostBitCount() {
		return Ipv6Address.ADDRESS_BITS - this.networkBitCount;
	}

	@Override
	public Ipv6Address getNetworkAddress() {
		return getFirst();
	}
}
