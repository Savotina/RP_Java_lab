module org.example.rp_lab1 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.rmi;


    opens net.RMI.Client to javafx.fxml;
    exports net.RMI.Client;
    exports net.RMI to java.rmi;
    exports net.command to java.rmi;
}