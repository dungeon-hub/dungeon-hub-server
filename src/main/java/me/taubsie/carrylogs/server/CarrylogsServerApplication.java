package me.taubsie.carrylogs.server;

import me.taubsie.carrylogs.ClassLoaderService;
import me.taubsie.carrylogs.ProgramOrigin;
import me.taubsie.carrylogs.config.ConfigType;
import me.taubsie.carrylogs.server.service.DatabaseService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CarrylogsServerApplication extends ProgramOrigin {
    public static void main(String[] args) {
        ClassLoaderService.getInstance().loadStartupListeners();
        ClassLoaderService.getInstance().executeStartup(new CarrylogsServerApplication());

        if(DatabaseService.getInstance().hasInvalidConfigValues()) {
            System.out.println("Please enter correct values in the config-file.");
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
        System.out.println(message);
    }

    @Override
    public void warn(String message) {
        System.out.println(message);
    }

    @Override
    public void error(String message) {
        System.out.println(message);
    }

    @Override
    public void error(String message, Throwable throwable) {
        System.out.println(message);
        throwable.printStackTrace();
    }
}