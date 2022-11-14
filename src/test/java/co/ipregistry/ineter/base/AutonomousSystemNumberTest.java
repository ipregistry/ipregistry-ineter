package co.ipregistry.ineter.base;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public final class AutonomousSystemNumberTest {

    @Test
    public void testCompare1() {
        assertEquals(0, new AutonomousSystemNumber(7).compareTo(new AutonomousSystemNumber(7)));
    }

    @Test
    public void testCompare2() {
        assertEquals(-1, new AutonomousSystemNumber(3).compareTo(new AutonomousSystemNumber(7)));
    }

    @Test
    public void testCompare3() {
        assertEquals(1, new AutonomousSystemNumber(3222).compareTo(new AutonomousSystemNumber(7)));
    }

    @Test
    public void testConstructor1() {
        assertThrows(IllegalArgumentException.class, () -> new AutonomousSystemNumber(-1));
    }

    @Test
    public void testConstructor2() {
        assertThrows(IllegalArgumentException.class, () -> new AutonomousSystemNumber(-1L));
    }

    @Test
    public void testConstructor3() {
        assertThrows(IllegalArgumentException.class, () -> new AutonomousSystemNumber("AS-1"));
    }

    @Test
    public void testConstructor4() {
        assertThrows(IllegalArgumentException.class, () -> new AutonomousSystemNumber("ASX"));
    }

    @Test
    public void testConstructor5() {
        assertThrows(IllegalArgumentException.class, () -> new AutonomousSystemNumber(4294967297L));
    }

    @Test
    public void testConstructor6() {
        assertEquals(0, new AutonomousSystemNumber(0).getValue());
    }

    @Test
    public void testIsPrivate1() {
        assertTrue(new AutonomousSystemNumber(64512).isPrivate());
    }

    @Test
    public void testIsPrivate2() {
        assertFalse(new AutonomousSystemNumber(23).isPrivate());
    }

    @Test
    public void testIsPrivate3() {
        assertTrue(new AutonomousSystemNumber(65534).isPrivate());
    }

    @Test
    public void testIsPrivate4() {
        assertFalse(new AutonomousSystemNumber(65535).isPrivate());
    }

    @Test
    public void testIsPrivate5() {
        assertTrue(new AutonomousSystemNumber(4200000000L).isPrivate());
    }

    @Test
    public void testIsPrivate6() {
        assertTrue(new AutonomousSystemNumber(4294967294L).isPrivate());
    }

    @Test
    public void testToString() {
        assertEquals("AS7", new AutonomousSystemNumber(7).toString());
    }

}
