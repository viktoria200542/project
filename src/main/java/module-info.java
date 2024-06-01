module org.example.wmashine {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.wmashine to javafx.fxml;
    exports org.example.wmashine;
}