package grpc;

import grpc.Connect6Service.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import javafx.application.Platform;
import net.RMI.Client.ClientController;
import net.command.SerializableCommand;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class GameClient {
    private final Connect6ServiceGrpc.Connect6ServiceBlockingStub blockingStub;
    private final Connect6ServiceGrpc.Connect6ServiceStub asyncStub;
    private String clientId;
    private int color;
    private int move;
    private ClientController controller;

    public GameClient(ClientController clientController) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 8080)
                .usePlaintext()
                .build();

        blockingStub = Connect6ServiceGrpc.newBlockingStub(channel);
        asyncStub = Connect6ServiceGrpc.newStub(channel);
        clientId = UUID.randomUUID().toString();
        controller = clientController;

        ConnectionToServer();
    }

    public void ConnectionToServer() {
        ConnectionRequest connectRequest = ConnectionRequest.newBuilder().setClientId(clientId).build();
        System.out.println("Подключение к серверу...");

        asyncStub.connectionService(connectRequest, new StreamObserver<>() {
            @Override
            public void onNext(ConnectionResponse connectionResponse) {
                if (connectionResponse.getMessage().equals("connect")) {
                    System.out.println("Соединение с сервером установлено!");
                } else if (connectionResponse.getMessage().equals("start")) {
                    System.out.println("Начало игры");
                    Platform.runLater(() -> controller.updateGameStartedUI());
                }
                else if (connectionResponse.getMessage().equals("move")) {
                    System.out.println("Игрок сделал ход");
                    GameInfoResponse gameInfoResp = connectionResponse.getGameInfoResp();
                    Set<String> circles = new HashSet<>(gameInfoResp.getCirclesList());
                    int currentMove = gameInfoResp.getMove(); // [0; 3]
                    int currentWInner = gameInfoResp.getWinner();

                    Platform.runLater(() -> {
                        controller.updateMoveUI(new SerializableCommand(circles));
                        if (currentWInner == -1 && currentMove != -1)
                            controller.ProcessMove(currentMove);
                        else
                            controller.udpateAfterReset(currentWInner, currentMove);
                    });
                }
                else if (connectionResponse.getMessage().equals("reset")) {
                    System.out.println("Игра окончена");
                    ResetGameResponse resetGameResp = connectionResponse.getResetGameResp();
                    int currentWinner = resetGameResp.getWinner();

                    Platform.runLater(() -> {
                        controller.udpateAfterReset(currentWinner, move);
                    });
                }
                else if (connectionResponse.getMessage().equals("close")) {
                    System.out.println("Противник отключился");

                    Platform.runLater(() -> {
                        controller.udpateAfterReset(color, move);
                    });
                }
                System.out.println("- - - - - - - - - - - - - - - - - - - - - - - - - - - - - -");
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("Ошибка: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("Закрытие потока");
            }
        });
    }

    public int getColor() {
        return color;
    }

    public int getMove() {
        return move;
    }

    public void setMove(int move) {
        if (move == 1)
            this.move = 0;
        else
            this.move = 1;
    }

    public void setColor(int color) {
        this.color = color;
        Platform.runLater(() -> controller.updateClientColor(color));
    }

    public void startButtonClicked() {
        StartGameRequest startRequest = StartGameRequest.newBuilder().setClientId(clientId).build();
        StartGameResponse startResponse = blockingStub.startGameService(startRequest);

        boolean gameRejecting = startResponse.getReject();

        if (gameRejecting) controller.RejectRequest();
        setColor(startResponse.getColor());
    }

    public void setGameInfo(SerializableCommand command) {
        GameInfoRequest gameInfoRequest = GameInfoRequest.newBuilder()
                .addAllCircles(command.circles).build();
        GameInfoResponse gameInfoResponse = blockingStub.gameInfoService(gameInfoRequest);
    }

    public void resetButtonClicked(int clientColor) {
        ResetGameRequest resetGameRequest = ResetGameRequest.newBuilder()
                .setColor(clientColor)
                .build();
        ResetGameResponse resetGameResponse = blockingStub.resetGameService(resetGameRequest);
    }

    public void closeConnection() {
        CloseConnectionRequest closeConnectionRequest = CloseConnectionRequest.newBuilder()
                .setClientId(clientId)
                .build();
        CloseConnectionResponse closeConnectionResponse = blockingStub.closeConnectionService(closeConnectionRequest);
    }
}
