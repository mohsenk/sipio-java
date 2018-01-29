package com.fonoster.sipio;

import com.fonoster.sipio.core.ConfigManager;
import com.fonoster.sipio.core.ContextStorage;
import com.fonoster.sipio.core.Server;
import com.fonoster.sipio.location.Locator;
import com.fonoster.sipio.registrar.Registrar;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class Application {

    public static void main(String[] args) throws Exception {
        ConfigManager.load();
        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
        Locator locator = context.getBean(Locator.class);
        Registrar registrar = new Registrar(locator);
        new Server(locator, registrar, new ContextStorage()).start();

    }

}
