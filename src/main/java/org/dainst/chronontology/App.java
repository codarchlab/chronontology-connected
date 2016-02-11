package org.dainst.chronontology;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.dainst.chronontology.config.*;
import org.dainst.chronontology.controller.DocumentModel;

import java.util.Properties;

/**
 * Main class. Handles wiring of application components.
 *
 * @author Daniel M. de Oliveira
 */
public class App {

    final static Logger logger = Logger.getLogger(App.class);
    private static final String DEFAULT_PROPERTIES_FILE_PATH = "config.properties";
    private Router router = null;

    public static void main(String [] args) {

        Properties props= PropertiesLoader.loadConfiguration(DEFAULT_PROPERTIES_FILE_PATH);
        AppConfig appConfig= new AppConfig();
        if ((props==null) || (appConfig.validate(props)==false)) {
            for (String err: appConfig.getConstraintViolations()) {
                logger.error(err);
            }
            System.exit(1);
        }

        new AppConfigurator().configure(appConfig);
    }

    public App(Router router) {
        this.router= router;
    }

    public Router getRouter() {
        return router;
    }
}
