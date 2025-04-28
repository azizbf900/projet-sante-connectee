module vitalink {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.base;
    requires java.desktop;

    requires java.sql;
    requires mysql.connector.j;

    requires java.net.http;

    opens test to javafx.fxml; // Package containing MainFX
    opens controllers to javafx.fxml; // If you have controllers
    exports test;
    exports controllers; // If needed

    opens models to javafx.fxml, com.google.gson;
    exports models;

    opens services to javafx.fxml, com.google.gson;
    exports services;

    opens utils to javafx.fxml;
    exports utils;
}