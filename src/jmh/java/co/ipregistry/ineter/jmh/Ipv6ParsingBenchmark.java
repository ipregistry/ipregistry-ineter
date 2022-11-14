/*
 * Copyright (c) 2020, ineter contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package co.ipregistry.ineter.jmh;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

import co.ipregistry.ineter.base.Ipv6Address;
import co.ipregistry.ineter.base.Ipv6AddressParseTest;

import com.google.common.net.InetAddresses;

@Measurement(iterations = 5)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
public class Ipv6ParsingBenchmark {
	private static final int ADDR_CNT = 1000;
	private List<String> addresses;

	@Setup(Level.Trial)
	public void setUp() {
		// no brackets, guava doesn't like it
		this.addresses = Ipv6AddressParseTest.ip6AddressStrings(0, ADDR_CNT, false);
	}

	@Benchmark
	public void ineterParsing(final Blackhole hole) {
		for (final String addr : this.addresses) {
			hole.consume(Ipv6Address.of(addr));
		}
	}

	@Benchmark
	public void inetAddressParsing(final Blackhole hole) {
		for (final String addr : this.addresses) {
			try {
				hole.consume(InetAddress.getAllByName(addr));
			} catch (final UnknownHostException e) {
				//
			}
		}
	}

	@Benchmark
	public void guavaAddressParsing(final Blackhole hole) {
		for (final String addr : this.addresses) {
			hole.consume(InetAddresses.forString(addr));
		}
	}

}
