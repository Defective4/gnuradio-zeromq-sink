package io.github.defective4.sdr.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class MessagePair {
    private final String key;
    private final Object value;

    public MessagePair(String key, Object value) {
        Objects.requireNonNull(value);
        if (!(value instanceof String) && !(value instanceof Integer) && !(value instanceof Double))
            throw new IllegalArgumentException("value must be of type String, Integer, or Double");
        this.key = key;
        this.value = value;
    }

    public double getAsDouble() {
        return (double) value;
    }

    public int getAsInt() {
        return (int) value;
    }

    public String getAsString() {
        return (String) value;
    }

    public Class<?> getContainedType() {
        return value.getClass();
    }

    public String getKey() {
        return key;
    }

    public Object getValue() {
        return value;
    }

    public boolean isDouble() {
        return value instanceof Double || value instanceof Float;
    }

    public boolean isInteger() {
        return value instanceof Integer || value instanceof Byte || value instanceof Short;
    }

    public boolean isString() {
        return value instanceof String;
    }

    public byte[] toBytes() {
        try (ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                DataOutputStream wrapper = new DataOutputStream(buffer)) {
            if (key != null) {
                byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
                wrapper.writeByte(7);
                wrapper.writeByte(2);
                wrapper.writeShort(keyBytes.length);
                wrapper.write(keyBytes);
            }
            byte type = (byte) (isString() ? 2 : isInteger() ? 3 : 4);
            wrapper.writeByte(type);
            switch (type) {
                case 2 -> {
                    byte[] string = getAsString().getBytes(StandardCharsets.UTF_8);
                    wrapper.writeShort(string.length);
                    wrapper.write(string);
                }
                case 3 -> wrapper.writeInt(getAsInt());
                case 4 -> wrapper.writeDouble(getAsDouble());
                default -> {}
            }
            return buffer.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
