package com.fonoster.sipio;

import com.fonoster.sipio.core.ConfigManager;
import com.fonoster.sipio.core.ContextStorage;
import com.fonoster.sipio.core.handlers.Processor;
import com.fonoster.sipio.location.Locator;
import com.fonoster.sipio.registrar.Registrar;
import com.fonoster.sipio.registry.GatewayConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import javax.sip.SipProvider;

@SpringBootApplication
public class Application {

    static Logger logger = LoggerFactory.getLogger(Application.class);

    Locator locator;
    Registrar registrar;
    ContextStorage contextStorage;


    public static void main(String[] args) throws Exception {
        ConfigManager.load();
        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
        new Application().start(context);
    }

    public void start(ConfigurableApplicationContext context) {
        try {
            Locator locator = context.getBean(Locator.class);
            this.registrar = new Registrar(locator);
            SipProvider sipProvider = context.getBean(SipProvider.class);
            GatewayConnector gatewayConnector = new GatewayConnector(sipProvider);
            Processor processor = new Processor(sipProvider, locator, gatewayConnector, registrar, new ContextStorage());
            sipProvider.addSipListener(processor);
            locator.start();
            gatewayConnector.start();
        }
        catch (Exception ex){
            logger.error("Can't start application !!!",ex);
        }
    }

}
