package me.taubsie.dungeonhub.server.service;

import lombok.AllArgsConstructor;
import me.taubsie.dungeonhub.common.entity.EntityService;
import me.taubsie.dungeonhub.common.enums.WarningType;
import me.taubsie.dungeonhub.common.exceptions.EntityUnknownException;
import me.taubsie.dungeonhub.common.model.warning.WarningCreationModel;
import me.taubsie.dungeonhub.common.model.warning.WarningModel;
import me.taubsie.dungeonhub.common.model.warning.WarningUpdateModel;
import me.taubsie.dungeonhub.server.entities.Warning;
import me.taubsie.dungeonhub.server.model.WarningInitializeModel;
import me.taubsie.dungeonhub.server.repositories.WarningRepository;
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
    private WarningRepository warningRepository;

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

    @Scheduled(cron = "0 0 2 * * *")
    public void test() {
        List<Warning> warnings = warningRepository.findAllByActiveAndWarningType(true, WarningType.Strike);

        warnings.stream().filter(Warning::isExpired)
                .forEach(warning -> {
                    warning.setActive(false);
                    warningRepository.save(warning);
                });
    }
}