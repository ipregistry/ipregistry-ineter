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

    public static boolean isPrivate(final long value) {
        return (value >= 64512 && value <= 65534) ||
                (value >= 4200000000L && value <= 4294967294L);
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
        return (int) (value ^ (value >>> 32));
    }

    @Override
    public String toString() {
        return "AS" + value;
    }

}
