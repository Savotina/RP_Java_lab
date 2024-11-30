package grpc;

import grpc.Connect6Service.*;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.util.*;

import io.grpc.stub.StreamObserver;

public class GameServer extends Connect6ServiceGrpc.Connect6ServiceImplBase {
    private Set<String> readyClients;
    private Set<String> circlesInfo;
    private int winner;
    private int move;
    private final Map<String, StreamObserver<ConnectionResponse>> streamObservers = new HashMap<>();

    private final int canvasSize = 550;
    private final int columnCount = 10;

    public GameServer() {
        circlesInfo = new HashSet<>();
        readyClients = new HashSet<>();

        winner = -1;
        move = -1;
    }

    @Override
    public void connectionService(grpc.Connect6Service.ConnectionRequest request,
                               io.grpc.stub.StreamObserver<grpc.Connect6Service.ConnectionResponse> responseObserver) {
        String clientId = request.getClientId();
        System.out.println("Клиент подключился, его clientId: " + clientId);

        streamObservers.put(clientId, responseObserver);
        ConnectionResponse connectionResponse = ConnectionResponse.newBuilder()
                .setMessage("CONNECTION")
                .build();
        responseObserver.onNext(connectionResponse);

    }

    @Override
    public void startGameService(grpc.Connect6Service.StartGameRequest request,
                                 io.grpc.stub.StreamObserver<grpc.Connect6Service.StartGameResponse> responseObserver) {
        String clientId = request.getClientId();
        StartGameResponse startGameResp;
        int color = -1;
        if (!readyClients.contains(clientId) && readyClients.size() == 2) {
            System.out.println("В игре достаточное количество игроков");

            startGameResp = StartGameResponse.newBuilder()
                    .setReject(true)
                    .setColor(color)
                    .build();
        } else {
            if (readyClients.isEmpty()) color = 0;
            else color = 1;

            readyClients.add(clientId);

            startGameResp = StartGameResponse.newBuilder()
                    .setColor(color)
                    .build();

            if (readyClients.size() >= 2) {
                System.out.println("Игра началась");
                for (String client: readyClients) {
                    for (Map.Entry<String, StreamObserver<ConnectionResponse>> entry : streamObservers.entrySet()) {
                        String observerClientId = entry.getKey();
                        if (Objects.equals(client, observerClientId)) {
                            StreamObserver<ConnectionResponse> observer = entry.getValue();
                            ConnectionResponse connectionResponse = ConnectionResponse.newBuilder()
                                    .setMessage("START")
                                    .build();
                            observer.onNext(connectionResponse);
                        }
                    }
                }
                move = 1;
                winner = -1;
            } else {
                System.out.println("Ждём других игроков");
            }
        }
        responseObserver.onNext(startGameResp);
        responseObserver.onCompleted();
    }

    @Override
    public void gameInfoService(grpc.Connect6Service.GameInfoRequest request,
                                io.grpc.stub.StreamObserver<grpc.Connect6Service.GameInfoResponse> responseObserver) {
        circlesInfo = new HashSet<>(request.getCirclesList());
        winner = WinnerCheck();

        move++;
        if (move == 4) {
            move = 0;
        }

        int cellCount = (columnCount + 1) * (columnCount + 1);
        if (winner == -1 && circlesInfo.size() == cellCount) {
            move = -1;
            System.out.println("Результат игры: ничья");
        }

        GameInfoResponse gameInfoResp = GameInfoResponse.newBuilder()
                .addAllCircles(circlesInfo)
                .setMove(move)
                .setWinner(winner)
                .build();

        for (String client: readyClients) {
            for (Map.Entry<String, StreamObserver<ConnectionResponse>> entry : streamObservers.entrySet()) {
                String observerClientId = entry.getKey();
                if (Objects.equals(client, observerClientId)) {
                    StreamObserver<ConnectionResponse> observer = entry.getValue();

                    ConnectionResponse connectResp = ConnectionResponse.newBuilder()
                            .setMessage("MOVE")
                            .setGameInfoResp(gameInfoResp)
                            .build();
                    observer.onNext(connectResp);
                }
            }
        }

        if (winner != -1 || circlesInfo.size() == cellCount) {
            circlesInfo.clear();
            readyClients.clear();
        }

        responseObserver.onNext(gameInfoResp);
        responseObserver.onCompleted();
    }

    int WinnerCheck() {
        String black_circle, white_circle;
        int color;

        // Проверка по строке
        for (int j = 25; j < canvasSize; j += 50) {
            for (int i = 25; i <= canvasSize - 5*50; i += 50) {
                black_circle = i + "," + j + "," + "0";
                white_circle = i + "," + j + "," + "1";
                if (circlesInfo.contains(black_circle)) color = 0;
                else if (circlesInfo.contains(white_circle)) color = 1;
                else color = -1;

                /*System.out.println(i + "," + j + "," + color);
                System.out.println((i+50) + "," + j + "," + color + '\n' + (i+100) + "," + j + "," + color + '\n' + (i+150) + "," + j + "," + color
                        + "\n" + (i+200) + "," + j + "," + color + "\n" + (i+250) + "," + j + "," + color + "\n");

                System.out.println(command.circles);

                System.out.println(command.circles.contains((i+50) + "," + j + "," + color));
                System.out.println(command.circles.contains((i+100) + "," + j + "," + color));
                System.out.println(command.circles.contains((i+150) + "," + j + "," + color));
                System.out.println(command.circles.contains((i+200) + "," + j + "," + color));
                System.out.println(command.circles.contains((i+250) + "," + j + "," + color));*/

                if (color != -1 &&
                        circlesInfo.contains((i+50) + "," + j + "," + color) &&
                        circlesInfo.contains((i+100) + "," + j + "," + color) &&
                        circlesInfo.contains((i+150) + "," + j + "," + color) &&
                        circlesInfo.contains((i+200) + "," + j + "," + color) &&
                        circlesInfo.contains((i+250) + "," + j + "," + color)) {
                    System.out.println("Нашёлся победитель");
                    System.out.println("Выиграл: " + color + " по строке");
                    return color;
                }
                //else System.out.println("Игра продолжается");
            }
        }

        // Проверка по столбцу
        for (int i = 25; i < canvasSize; i += 50) {
            for (int j = 25; j <= canvasSize - 5*50; j += 50) {
                black_circle = i + "," + j + "," + "0";
                white_circle = i + "," + j + "," + "1";
                if (circlesInfo.contains(black_circle)) color = 0;
                else if (circlesInfo.contains(white_circle)) color = 1;
                else color = -1;

                if (color != -1 &&
                        circlesInfo.contains(i + "," + (j+50) + "," + color) &&
                        circlesInfo.contains(i + "," + (j+100) + "," + color) &&
                        circlesInfo.contains(i + "," + (j+150) + "," + color) &&
                        circlesInfo.contains(i + "," + (j+200) + "," + color) &&
                        circlesInfo.contains(i + "," + (j+250) + "," + color)) {
                    System.out.println("Нашёлся победитель");
                    System.out.println("Выиграл: " + color + " по столбцу");
                    return color;
                }
                //else System.out.println("Игра продолжается");
            }
        }


        // Проверка по диагонали (слева направо)
        for (int i = 25; i < canvasSize - 5*50; i += 50) {
            for (int j = 25; j <= canvasSize - 5*50; j += 50) {
                black_circle = i + "," + j + "," + "0";
                white_circle = i + "," + j + "," + "1";
                if (circlesInfo.contains(black_circle)) color = 0;
                else if (circlesInfo.contains(white_circle)) color = 1;
                else color = -1;

                if (color != -1 &&
                        circlesInfo.contains((i+50) + "," + (j+50) + "," + color) &&
                        circlesInfo.contains((i+100) + "," + (j+100) + "," + color) &&
                        circlesInfo.contains((i+150) + "," + (j+150) + "," + color) &&
                        circlesInfo.contains((i+200) + "," + (j+200) + "," + color) &&
                        circlesInfo.contains((i+250) + "," + (j+250) + "," + color)) {
                    System.out.println("Нашёлся победитель");
                    System.out.println("Выиграл: " + color + " по диагонали (слева направо)");
                    return color;
                }
                //else System.out.println("Игра продолжается");
            }
        }


        // Проверка по диагонали (справа налево)
        for (int i = 25; i < canvasSize - 5*50; i += 50) {
            for (int j = 6*50-25; j <= canvasSize; j += 50) {
                black_circle = i + "," + j + "," + "0";
                white_circle = i + "," + j + "," + "1";
                if (circlesInfo.contains(black_circle)) color = 0;
                else if (circlesInfo.contains(white_circle)) color = 1;
                else color = -1;

                if (color != -1 &&
                        circlesInfo.contains((i+50) + "," + (j-50) + "," + color) &&
                        circlesInfo.contains((i+100) + "," + (j-100) + "," + color) &&
                        circlesInfo.contains((i+150) + "," + (j-150) + "," + color) &&
                        circlesInfo.contains((i+200) + "," + (j-200) + "," + color) &&
                        circlesInfo.contains((i+250) + "," + (j-250) + "," + color)) {
                    System.out.println("Нашёлся победитель");
                    System.out.println("Выиграл: " + color + " по диагонали (справа налево)");
                    return color;
                }
                //else System.out.println("Игра продолжается");
            }
        }

        //System.out.println("Никто не выиграл. Игра продолжается");
        return -1;
    }

    @Override
    public void resetGameService(grpc.Connect6Service.ResetGameRequest request,
                                 io.grpc.stub.StreamObserver<grpc.Connect6Service.ResetGameResponse> responseObserver) {
        int color = request.getColor();

        if (color == 0)
            winner = 1;
        else winner = 0;

        circlesInfo.clear();

        System.out.println("Игрок " + color + " сдался");

        ResetGameResponse resetGameResp = ResetGameResponse.newBuilder()
                .setWinner(winner)
                .build();

        for (String client: readyClients) {
            for (Map.Entry<String, StreamObserver<ConnectionResponse>> entry : streamObservers.entrySet()) {
                String observerClientId = entry.getKey();
                if (Objects.equals(client, observerClientId)) {
                    StreamObserver<ConnectionResponse> observer = entry.getValue();
                    System.out.println("Сброс игры");

                    ConnectionResponse connectionResponse = ConnectionResponse.newBuilder()
                            .setMessage("RESET")
                            .setResetGameResp(resetGameResp)
                            .build();
                    observer.onNext(connectionResponse);
                }
            }
        }

        readyClients.clear();

        responseObserver.onNext(resetGameResp);
        responseObserver.onCompleted();
    }

    @Override
    public void closeConnectionService(grpc.Connect6Service.CloseConnectionRequest request,
                                       io.grpc.stub.StreamObserver<grpc.Connect6Service.CloseConnectionResponse> responseObserver) {
        String clientId = request.getClientId();
        CloseConnectionResponse closeConnectionResp = CloseConnectionResponse.newBuilder().build();

        Map<String, StreamObserver<ConnectionResponse>> copyStreamObservers = new HashMap<>(streamObservers);

        for (Map.Entry<String, StreamObserver<ConnectionResponse>> entry : copyStreamObservers.entrySet()) {
            String observerClientId = entry.getKey();
            StreamObserver<ConnectionResponse> observer = entry.getValue();

            if (observerClientId.equals(clientId)) {
                streamObservers.remove(clientId);
                observer.onCompleted();

                System.out.println("Игрок " + observerClientId + " покинул сервер");
            }
        }

        if (readyClients.contains(clientId)) {
            readyClients.remove(clientId);
            for (String client : readyClients) {
                StreamObserver<ConnectionResponse> observer = streamObservers.get(client);
                if (observer != null) {
                    ConnectionResponse connectionResponse = ConnectionResponse.newBuilder()
                            .setMessage("CLOSE")
                            .build();
                    observer.onNext(connectionResponse);
                }
            }
            readyClients.clear();
        }

        responseObserver.onNext(closeConnectionResp);
        responseObserver.onCompleted();
    }

    public static void main(String[] args) throws Exception {
        grpc.GameServer connect6Server = new GameServer();
        Server server = ServerBuilder.forPort(8080).addService(connect6Server).build();

        System.out.println("Сервер запущен");

        server.start();
        server.awaitTermination();
    }
}
