package me.taubsie.dungeonhub.server.service;

import lombok.AllArgsConstructor;
import me.taubsie.dungeonhub.common.entity.EntityService;
import me.taubsie.dungeonhub.common.enums.WarningType;
import me.taubsie.dungeonhub.common.exceptions.EntityUnknownException;
import me.taubsie.dungeonhub.common.model.warning.WarningActionModel;
import me.taubsie.dungeonhub.common.model.warning.WarningCreationModel;
import me.taubsie.dungeonhub.common.model.warning.WarningModel;
import me.taubsie.dungeonhub.common.model.warning.WarningUpdateModel;
import me.taubsie.dungeonhub.server.entities.DiscordServer;
import me.taubsie.dungeonhub.server.entities.DiscordUser;
import me.taubsie.dungeonhub.server.entities.Warning;
import me.taubsie.dungeonhub.server.entities.WarningPunishment;
import me.taubsie.dungeonhub.server.model.WarningInitializeModel;
import me.taubsie.dungeonhub.server.repositories.WarningPunishmentRepository;
import me.taubsie.dungeonhub.server.repositories.WarningRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Service
@AllArgsConstructor
@EnableScheduling
public class WarningService implements EntityService<Warning, WarningModel, WarningCreationModel, WarningInitializeModel, WarningUpdateModel> {
    private static final Logger logger = LoggerFactory.getLogger(WarningService.class);

    private final WarningRepository warningRepository;
    private final WarningPunishmentRepository warningPunishmentRepository;

    public List<Warning> findAllWarningsForUser(long server, long user) {
        return warningRepository.findAllByServer_IdAndUser_Id(server, user);
    }

    public List<Warning> findAllActiveWarningsForUser(long server, long user) {
        return warningRepository.findAllByServer_IdAndUser_IdAndActive(server, user, true);
    }

    @Override
    public Optional<Warning> loadEntityById(long id) {
        return warningRepository.findById(id);
    }

    @Override
    public Optional<Warning> loadEntityByName(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Warning> findAllEntities() {
        return warningRepository.findAll();
    }

    @Override
    public Warning createEntity(WarningInitializeModel initalizationModel) {
        return saveEntity(initalizationModel.toEntity());
    }

    @Override
    public boolean delete(long id) {
        return warningRepository.findById(id).map(entity ->
        {
            warningRepository.delete(entity);
            return true;
        }).orElse(false);
    }

    public Warning deactivateWarning(Warning warning) {
        warning.setActive(false);

        return warningRepository.save(warning);
    }

    @Override
    public Warning saveEntity(Warning entity) {
        return warningRepository.save(entity);
    }

    @Override
    public Function<WarningModel, Warning> toEntity() {
        return warningModel -> warningRepository.findById(warningModel.getId()).orElseThrow(() -> new EntityUnknownException(warningModel.getId()));
    }

    @Override
    public Function<Warning, WarningModel> toModel() {
        return Warning::toModel;
    }

    public List<WarningPunishment> loadPunishmentsForServer(DiscordServer discordServer) {
        return warningPunishmentRepository.findAllByServer(discordServer);
    }

    public List<WarningPunishment> loadPunishmentsForWarningType(DiscordServer discordServer, WarningType warningType) {
        return warningPunishmentRepository.findAllByServerAndWarningType(discordServer, warningType);
    }

    public List<WarningActionModel> getActions(DiscordServer discordServer, DiscordUser discordUser, WarningType warningType) {
        long activeWarnings = findAllActiveWarningsForUser(discordServer.getId(), discordUser.getId())
                .stream().filter(warning -> warning.getWarningType() == warningType).count();

        List<WarningPunishment> punishments = loadPunishmentsForWarningType(discordServer, warningType);

        return punishments.parallelStream()
                .filter(punishment -> punishment.applies(activeWarnings))
                .map(WarningPunishment::toAction)
                .toList();
    }

    @Scheduled(cron = "0 0 2 * * *")
    public void removeExpiredStrikes() {
        List<Warning> warnings = warningRepository.findAllByActiveAndWarningType(true, WarningType.Strike);

        warnings.stream().filter(Warning::isExpired)
                .forEach(warning -> {
                    warning.setActive(false);
                    warningRepository.save(warning);

                    logger.info("The strike warning with id was just deactivated: {}", warning.getId());
                });
    }
}