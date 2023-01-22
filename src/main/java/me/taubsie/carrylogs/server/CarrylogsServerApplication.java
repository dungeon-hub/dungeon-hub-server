package me.taubsie.carrylogs.server;

import me.taubsie.carrylogs.config.ConfigService;
import me.taubsie.carrylogs.config.ConfigType;
import me.taubsie.carrylogs.server.service.DatabaseService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CarrylogsServerApplication
{
    public static void main(String[] args)
    {
        ConfigService.setInstance(ConfigType.SERVER);

        if (DatabaseService.getInstance().hasInvalidConfigValues())
        {
            System.out.println("Please enter correct values in the config-file.");
            return;
        }

        SpringApplication.run(CarrylogsServerApplication.class, args);
    }
}