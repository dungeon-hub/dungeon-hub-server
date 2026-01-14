package me.taubsie.dungeonhub.server.converter;

import dev.kord.common.DiscordBitSetKt;
import dev.kord.common.entity.Permissions;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.math.BigInteger;

@Converter
public class PermissionsConverter implements AttributeConverter<Permissions, Long> {
    @Override
    public Long convertToDatabaseColumn(Permissions attribute) {
        if (attribute == null) {
            return null;
        }

        BigInteger value = new BigInteger(attribute.getCode().getValue(), 10);

        assert value.bitLength() <= 63 : "DiscordBitSet too large to store as BIGINT (" + value.bitLength() + " bits)";

        return value.longValueExact();
    }

    @Override
    public Permissions convertToEntityAttribute(Long dbData) {
        if (dbData == null) {
            return null;
        }

        return new Permissions(DiscordBitSetKt.DiscordBitSet(dbData.toString()));
    }
}