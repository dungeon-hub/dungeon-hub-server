package me.taubsie.carrylogs.server;

import me.taubsie.dungeonhub.common.ClassLoaderService;
import me.taubsie.dungeonhub.common.OnStart;
import me.taubsie.dungeonhub.common.ProgramOrigin;
import me.taubsie.dungeonhub.common.config.ConfigService;
import me.taubsie.dungeonhub.common.config.ConfigType;
import me.taubsie.carrylogs.server.service.DatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CarrylogsServerApplication extends ProgramOrigin {
    private static final Logger logger = LoggerFactory.getLogger(CarrylogsServerApplication.class);

    public static void main(String[] args) {
        //TODO fix -> class name start with "BOOT_INF.classes." also
        //ClassLoaderService.getInstance().loadStartupListeners();
        ClassLoaderService.getInstance().addStartupListener(ConfigService.getInstance(), ConfigService.class.getAnnotation(OnStart.class));
        ClassLoaderService.getInstance().executeStartup(new CarrylogsServerApplication());

        if(DatabaseService.getInstance().hasInvalidConfigValues()) {
            logger.error("Please enter correct values in the config-file.");
            return;
        }

        SpringApplication.run(CarrylogsServerApplication.class, args);
    }

    @Override
    public ConfigType getConfigType() {
        return ConfigType.SERVER;
    }

    @Override
    public void log(String message) {
        logger.info(message);
    }

    @Override
    public void warn(String message) {
        logger.warn(message);
    }

    @Override
    public void error(String message) {
        logger.error(message);
    }

    @Override
    public void error(String message, Throwable throwable) {
        logger.error(message, throwable);
    }

    @Override
    public void debug(String message) {
        logger.debug(message);
    }
}