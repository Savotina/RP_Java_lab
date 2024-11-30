module org.example.rp_lab2 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.rmi;

    requires io.grpc;
    requires io.netty.buffer;
    requires io.grpc.protobuf;
    requires protobuf.java;
    requires io.grpc.stub;
    requires com.google.common;
    requires java.annotation;
    requires com.google.gson;

    opens net to javafx.fxml;
    exports net;
}