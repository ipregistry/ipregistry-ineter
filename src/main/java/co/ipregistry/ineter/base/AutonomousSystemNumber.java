package co.ipregistry.ineter.base;

import java.io.Serializable;

public final class AutonomousSystemNumber implements Comparable<AutonomousSystemNumber>, Serializable {

    private final long value;

    public AutonomousSystemNumber(final String value) {
        String number = value;
        if ((value.charAt(0) != 'A' || value.charAt(0) != 'a') &&
                (value.charAt(1) != 'S' || value.charAt(1) != 's')) {
            number = value.substring(2);
        }

        try {
            this.value = Long.parseLong(number);

            checkBounds(this.value);
        } catch (final Throwable t) {
            throw new IllegalArgumentException("Invalid AS number: " + number);
        }
    }

    public AutonomousSystemNumber(final int value) {
        checkBounds(value);
        this.value = value;
    }

    public AutonomousSystemNumber(final long value) {
        checkBounds(value);
        this.value = value;
    }

    private static void checkBounds(final long number) {
        if (!(number >= 0 && number <= 4294967295L)) {
            throw new IllegalArgumentException("AS number is not within [0, 4294967295]: " + number);
        }
    }

    @Override
    public int compareTo(final AutonomousSystemNumber as) {
        return Long.compare(value, as.value);
    }

    public long getValue() {
        return value;
    }

    public boolean isPrivate() {
        return isPrivate(value);
    }

    public static boolean isPrivate(final long number) {
        return ((number >= 64_512L && number <= 65_534L)
                || (number >= 4_200_000_000L && number <= 4_294_967_294L));
    }

    /**
     * Returns {@code true} if the ASN must never be routed by ordinary
     * networks, mirroring the notion of “reserved / non-globally-routable”
     * IP address blocks (RFC 1918, 6890, etc.).
     *
     * Keeps private-use, documentation, invalid and marker ASNs.
     * Ignores special-purpose ASNs that are expected to appear
     * (e.g. AS112, AS_TRANS).
     */
    public static boolean isNonRoutable(long number) {
        // ── Invalid (RFC 7607)
        if (number == 0L) return true;

        // ── Documentation/example blocks (RFC 5398)
        if ((number >= 64_496L && number <= 64_511L)  // 64496-64511
                || (number >= 65_536L && number <= 65_551L)) // 65536-65551
            return true;

        // ── Private-use blocks (RFC 6996)
        if (isPrivate(number)) {
            return true;
        }

        // ── Marker / last values (RFC 7300)
        if (number == 65_535L || number == 4_294_967_295L) {
            return true;
        }

        // 112 (AS112) and 23456 (AS_TRANS) are *not* treated as “reserved”
        // here because they legitimately appear in the DFZ.

        return false; // public or special-purpose but globally visible
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final AutonomousSystemNumber that = (AutonomousSystemNumber) o;

        return value == that.value;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(value);
    }

    @Override
    public String toString() {
        return "AS" + value;
    }

}
