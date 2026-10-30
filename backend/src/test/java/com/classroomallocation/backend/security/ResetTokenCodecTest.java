package com.classroomallocation.backend.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResetTokenCodecTest {

    private final ResetTokenCodec codec = new ResetTokenCodec();

    @Test
    void generatesTheExpectedUrlSafeFormat() {
        String token = codec.generateToken();

        assertTrue(token.matches("^[A-Za-z0-9_-]{43}$"));
    }

    @Test
    void generatesDifferentTokens() {
        assertNotEquals(codec.generateToken(), codec.generateToken());
    }

    @Test
    void hashesUsingTheKnownSha256TestVector() {
        assertEquals(
                "ba7816bf8f01cfea414140de5dae2223"
                        + "b00361a396177a9cb410ff61f20015ad",
                codec.hash("abc")
        );
    }
}