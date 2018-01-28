package com.fonoster.sipio.core;

import com.fonoster.sipio.location.Locator;
import com.fonoster.sipio.registrar.Registrar;

public class Main {

    public static void main(String[] args) throws Exception {

        ConfigManager.load();
        Locator locator = new Locator();
        Registrar registrar = new Registrar(locator);
        new Server(locator, registrar, new ContextStorage()).start();
    }
}
