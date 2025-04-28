module tn.esprit.pdevuser {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires jakarta.mail;
    requires java.net.http;
    requires org.json;

    opens tn.esprit.models to javafx.base;
    opens tn.esprit.utils;
    opens tn.esprit.test;
    opens tn.esprit.pdevuser;
    opens tn.esprit.controller;
    opens tn.esprit.view;

    exports tn.esprit.utils;
    exports tn.esprit.test;
    exports tn.esprit.pdevuser;
    exports tn.esprit.controller;
}