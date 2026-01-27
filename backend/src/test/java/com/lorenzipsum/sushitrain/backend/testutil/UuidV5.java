package com.lorenzipsum.sushitrain.backend.testutil;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public final class UuidV5 {

    private UuidV5() {
    }

    public static UUID uuidV5(UUID namespace, String name) {
        try {
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            sha1.update(toBytes(namespace));
            sha1.update(name.getBytes(StandardCharsets.UTF_8));
            byte[] hash = sha1.digest();

            // Take first 16 bytes
            hash[6] = (byte) ((hash[6] & 0x0F) | 0x50); // version 5
            hash[8] = (byte) ((hash[8] & 0x3F) | 0x80); // IETF variant

            ByteBuffer bb = ByteBuffer.wrap(hash, 0, 16);
            long msb = bb.getLong();
            long lsb = bb.getLong();
            return new UUID(msb, lsb);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-1 not available", e);
        }
    }

    private static byte[] toBytes(UUID uuid) {
        ByteBuffer bb = ByteBuffer.allocate(16);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }
}
