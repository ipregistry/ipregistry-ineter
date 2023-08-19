![Build Status](https://github.com/ipregistry/ipregistry-ineter/actions/workflows/gradle.yml/badge.svg)

# ineter

## What?

ineter (pronounced "Eye-netter") is a Java library for working with:

- Individual IP addresses - `IPv4Address`, `IPv6Address`/`ZonedIPv6Address`
- IP address ranges - `Ipv4Range`, `Ipv6Range`
- IP subnets - `Ipv4Subnet`, `Ipv6Subnet`

## Why?

- Low memory (and GC) footprint: *ineter* uses primitive types to represent addresses - an `int` for IPv4, two `long`
  fields for IPv6. For comparison, Java's `InetAddress` uses an `InetAddressHolder` with two `String` fields and
  two `int` fields just for IPv4
- Immutability: *ineter* is immutable and thread-safe by default
- Speed: *ineter* is written with performance in mind
- Rich set of supported operations
- MPL-2.0 license, allowing commercial use as well as re-licensing under GNU

## Where?

#### Maven:

```xml
<dependency>
    <groupId>co.ipregistry</groupId>
    <artifactId>ipregistry-ineter</artifactId>
    <version>2.3.1</version>
</dependency>
```
#### Gradle:

```groovy
compile 'co.ipregistry:ipregistry-ineter:2.3.1'
```

## How?

### Individual IPv4/IPv6 addresses

```java
IPv4Address ipv4 = IPv4Address.of("10.0.0.1");
IPv6Address ipv6 = IPv6Address.of("2001::1234:4321");

ipv4.isPrivate(); // true
ipv4.isMulticast(); // false
ipv6.isGlobalUnicast(); // true
ipv4.compareTo(IPv4Address.of(0x0a000001)); // addresses are comparable
ipv6.toInetAddress(); // addresses can be converted to other forms
ipv6.toSubnet(); // IPv6Address as a single /128 subnet
ipv4.plus(5); // 10.0.0.6
ipv4.distanceTo(IPv4Address.of("10.0.0.100")); // 99
ipv4.previous(); // 10.0.0.0
```
### Arbitrary address ranges

```java
Ipv4Range ipv4Range = Ipv4Range.parse("192.168.100.0-192.168.101.127");
Ipv6Range ipv6Range = Ipv6Range.of("2001::","2001::1000"); //Build using first and last address
Ipv6Range singletonRange = Ipv6Range.parse("2002::"); // A single address in range form

ipv6Range.contains(IPv6Address.of("2001::1000")); //true
ipv4Range.overlaps(Ipv4Range.of("10.0.0.1", "10.0.0.10")); //false
ipv4Range.toSubnets(); // Returns the list of subnets that make up the range
ipv4Range.withLast(IPv4Address.of("192.168.102.0")); //range with different last address
ipv6Range.length(); //4097
Ipv6Range.merge(ipv6Range, singletonRange); //ranges can be merged
```

### Subnets

```java
Ipv4Subnet ipv4Subnet = Ipv4Subnet.of("192.168.0.0/16");
Ipv6Range ipv6Subnet = Ipv6Range.parse("2001::/64"); // subnets are ranges too!

ipv4Subnet.getNetworkMask(); //255.255.0.0
ipv4Subnet.getNetworkBitCount(); //16
```

## Acknowledgements

This repository original work has been forked from https://github.com/maltalex/ineter.

The fork is from commit _6187c20887c177fb09a6e5b6316844d1d5808e06_.
