package me.taubsie.dungeonhub.server.converter;

import dev.kord.common.DiscordBitSet;
import dev.kord.common.entity.Permissions;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;

// TODO: add entity and db columns
// h2: VARBINARY
// mariadb: VARBINARY(32)
// postgres: BYTEA
@Converter
public class PermissionsConverter implements AttributeConverter<Permissions, byte[]> {
    @Override
    public byte[] convertToDatabaseColumn(Permissions attribute) {
        if(attribute == null) {
            return null;
        }

        return longArrayToByteArray(attribute.getCode().getData$common());
    }

    byte[] longArrayToByteArray(long[] data) {
        ByteBuffer buffer = ByteBuffer.allocate(data.length * Long.BYTES);
        buffer.asLongBuffer().put(data);
        return buffer.array();
    }

    @Override
    public Permissions convertToEntityAttribute(byte[] dbData) {
        if(dbData == null) {
            return null;
        }

        return new Permissions(new DiscordBitSet(byteArrayToLongArray(dbData)));
    }

    long[] byteArrayToLongArray(byte[] bytes) {
        LongBuffer buffer = ByteBuffer.wrap(bytes).asLongBuffer();
        long[] data = new long[buffer.remaining()];
        buffer.get(data);
        return data;
    }
}