package me.taubsie.dungeonhub.server.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import net.dungeonhub.model.ticket_panel.TicketPanelPermissionsModel;
import net.dungeonhub.service.GsonService;

/**
 * Stores the permissions of a ticket panel as JSON, using the same Gson instance as the HTTP layer.
 */
@Converter
public class TicketPanelPermissionsConverter implements AttributeConverter<TicketPanelPermissionsModel, String> {
    @Override
    public String convertToDatabaseColumn(TicketPanelPermissionsModel attribute) {
        if (attribute == null) {
            return null;
        }

        //noinspection deprecation
        return GsonService.INSTANCE.getGson().toJson(attribute);
    }

    @Override
    public TicketPanelPermissionsModel convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }

        //noinspection deprecation
        return GsonService.INSTANCE.getGson().fromJson(dbData, TicketPanelPermissionsModel.class);
    }
}
