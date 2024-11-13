package net.RMI;

import net.RMI.RemoteConnect6_gameServer;
import net.RMI.Client.RemoteClientCallback;
import net.command.SerializableCommand;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Set;
import java.util.HashSet;

public class Server extends UnicastRemoteObject implements RemoteConnect6_gameServer {

    private Set<String> ready_clients = new HashSet<>();
    private Set<RemoteClientCallback> clientCallbacks = new HashSet<>();
    private SerializableCommand circles_info;
    private int winner;
    private int move;

    protected Server() throws RemoteException {
        super();
    }

    @Override
    public String CirclesInfo(SerializableCommand command, int grid_size) throws RemoteException {
        // Здесь выполняется логика обработки хода
        // Для примера, вернем строку с информацией о ходе
        //System.out.println("Move executed: x=" + command.x + ", y=" + command.y + ", color=" + command.color);

        // Передача хода всем клиентам

        winner = WinnerCheck(command, grid_size);

        move++;
        if (move == 4) {
            move = 0;
        }

        for (RemoteClientCallback clientCallback : clientCallbacks) {
            clientCallback.ReceiveCirclesInfo(command, move, winner);
        }

        circles_info = command;

        return "Circles info: " + command.circles;
    }

    @Override
    public String[] notifyButtonClicked(String clientId, RemoteClientCallback callback) throws RemoteException {
        // Добавляем клиента в список готовых
        //ready_clients.add(clientId);

        // Добавляем клиента в список готовых
        if (ready_clients.isEmpty()) {
            ready_clients.add("0");
            callback.SetColor(0);
        } else {
            ready_clients.add("1");
            callback.SetColor(1);
        }

        clientCallbacks.add(callback);

        // Проверяем, готовы ли оба клиента
        if (ready_clients.size() >= 2) {
            // Начинаем игру
            System.out.println("Game started");
            ready_clients.clear();
            for (RemoteClientCallback clientCallback : clientCallbacks) {
                clientCallback.startGame();
            }
            move = 1;
            winner = -1;
            String[] str = {"Game started", "1"};
            return str;
        } else {
            System.out.println("Waiting for other player");
            String[] str = {"Waiting for other player", "0"};
            return str;
        }
    }

    /*@Override
    public String notifyButtonClicked(String clientId) throws RemoteException {

        System.out.println("size = " + ready_clients.size());

        if (ready_clients.isEmpty()) {
            ready_clients.add("1");
        }
        else {
            ready_clients.add("2");
        }

        // Проверяем, готовы ли оба клиента
        System.out.println("size = " + ready_clients.size());
        if (ready_clients.size() >= 2) {
            // Начинаем игру
            System.out.println("Game started");
            ready_clients.clear();
            return "Game started";
        } else {
            System.out.println("Waiting for other player");
            return "Waiting for other player";
        }
    }*/

    @Override
    public void ResetGame(int color, RemoteClientCallback callback) throws RemoteException {
        if (color == 0)
            winner = 1;
        else winner = 0;

        circles_info.circles.clear();

        System.out.println("Resetting the game");

        clientCallbacks.add(callback);
        for (RemoteClientCallback clientCallback : clientCallbacks) {
            clientCallback.ResetClientsGame(circles_info, winner);
            System.out.println("callback");
        }

        ready_clients.clear();
        clientCallbacks.clear();

    }

    int WinnerCheck(SerializableCommand command, int canvas_size) {
        String black_circle, white_circle;
        int color = -1;

        // Проверка по строке
        for (int j = 25; j < canvas_size; j += 50) {
            for (int i = 25; i <= canvas_size - 5*50; i += 50) {
                black_circle = i + "," + j + "," + "0";
                white_circle = i + "," + j + "," + "1";
                if (command.circles.contains(black_circle)) color = 0;
                else if (command.circles.contains(white_circle)) color = 1;
                else color = -1;

                System.out.println(i + "," + j + "," + color);
                System.out.println((i+50) + "," + j + "," + color + '\n' + (i+100) + "," + j + "," + color + '\n' + (i+150) + "," + j + "," + color
                        + "\n" + (i+200) + "," + j + "," + color + "\n" + (i+250) + "," + j + "," + color + "\n");

                System.out.println(command.circles);

                System.out.println(command.circles.contains((i+50) + "," + j + "," + color));
                System.out.println(command.circles.contains((i+100) + "," + j + "," + color));
                System.out.println(command.circles.contains((i+150) + "," + j + "," + color));
                System.out.println(command.circles.contains((i+200) + "," + j + "," + color));
                System.out.println(command.circles.contains((i+250) + "," + j + "," + color));

                if (color != -1 &&
                        command.circles.contains((i+50) + "," + j + "," + color) &&
                        command.circles.contains((i+100) + "," + j + "," + color) &&
                        command.circles.contains((i+150) + "," + j + "," + color) &&
                        command.circles.contains((i+200) + "," + j + "," + color) &&
                        command.circles.contains((i+250) + "," + j + "," + color)) {
                    System.out.println("found");
                    System.out.println("Выиграл: " + color + " по строке");
                    return color;
                }
                else System.out.println("Not found");
            }
        }

        // Проверка по столбцу
        for (int i = 25; i < canvas_size; i += 50) {
            for (int j = 25; j <= canvas_size - 5*50; j += 50) {
                black_circle = i + "," + j + "," + "0";
                white_circle = i + "," + j + "," + "1";
                if (command.circles.contains(black_circle)) color = 0;
                else if (command.circles.contains(white_circle)) color = 1;
                else color = -1;

                /*System.out.println(i + "," + j + "," + color);
                System.out.println(i + "," + (j+50) + "," + color + '\n' + i + "," + (j+50) + "," + color + '\n' + i + "," + (j+150)
                        + "," + color + "\n" + i + "," + (j+200) + "," + color + "\n" + i + "," + (j+250) + "," + color + "\n");

                System.out.println(command.circles);

                System.out.println(command.circles.contains(i + "," + (j+50) + "," + color));
                System.out.println(command.circles.contains(i + "," + (j+100) + "," + color));
                System.out.println(command.circles.contains(i + "," + (j+150) + "," + color));
                System.out.println(command.circles.contains(i + "," + (j+200) + "," + color));
                System.out.println(command.circles.contains(i + "," + (j+250) + "," + color));*/

                if (color != -1 &&
                        command.circles.contains(i + "," + (j+50) + "," + color) &&
                        command.circles.contains(i + "," + (j+100) + "," + color) &&
                        command.circles.contains(i + "," + (j+150) + "," + color) &&
                        command.circles.contains(i + "," + (j+200) + "," + color) &&
                        command.circles.contains(i + "," + (j+250) + "," + color)) {
                    System.out.println("found");
                    System.out.println("Выиграл: " + color + " по столбцу");
                    return color;
                }
                else System.out.println("Not found");
            }
        }


        // Проверка по диагонали (слева направо)
        for (int i = 25; i < canvas_size - 5*50; i += 50) {
            for (int j = 25; j <= canvas_size - 5*50; j += 50) {
                black_circle = i + "," + j + "," + "0";
                white_circle = i + "," + j + "," + "1";
                if (command.circles.contains(black_circle)) color = 0;
                else if (command.circles.contains(white_circle)) color = 1;
                else color = -1;

                /*System.out.println(i + "," + j + "," + color);
                System.out.println(i + "," + (j+50) + "," + color + '\n' + i + "," + (j+50) + "," + color + '\n' + i + "," + (j+150)
                        + "," + color + "\n" + i + "," + (j+200) + "," + color + "\n" + i + "," + (j+250) + "," + color + "\n");

                System.out.println(command.circles);

                System.out.println(command.circles.contains(i + "," + (j+50) + "," + color));
                System.out.println(command.circles.contains(i + "," + (j+100) + "," + color));
                System.out.println(command.circles.contains(i + "," + (j+150) + "," + color));
                System.out.println(command.circles.contains(i + "," + (j+200) + "," + color));
                System.out.println(command.circles.contains(i + "," + (j+250) + "," + color));*/

                if (color != -1 &&
                        command.circles.contains((i+50) + "," + (j+50) + "," + color) &&
                        command.circles.contains((i+100) + "," + (j+100) + "," + color) &&
                        command.circles.contains((i+150) + "," + (j+150) + "," + color) &&
                        command.circles.contains((i+200) + "," + (j+200) + "," + color) &&
                        command.circles.contains((i+250) + "," + (j+250) + "," + color)) {
                    System.out.println("found");
                    System.out.println("Выиграл: " + color + " по диагонали (слева направо)");
                    return color;
                }
                else System.out.println("Not found");
            }
        }



        // Проверка по диагонали (справа налево)
        for (int i = 25; i < canvas_size - 5*50; i += 50) {
            for (int j = 6*50-25; j <= canvas_size; j += 50) {
                black_circle = i + "," + j + "," + "0";
                white_circle = i + "," + j + "," + "1";
                if (command.circles.contains(black_circle)) color = 0;
                else if (command.circles.contains(white_circle)) color = 1;
                else color = -1;

                /*System.out.println(i + "," + j + "," + color);
                System.out.println(i + "," + (j+50) + "," + color + '\n' + i + "," + (j+50) + "," + color + '\n' + i + "," + (j+150)
                        + "," + color + "\n" + i + "," + (j+200) + "," + color + "\n" + i + "," + (j+250) + "," + color + "\n");

                System.out.println(command.circles);

                System.out.println(command.circles.contains(i + "," + (j+50) + "," + color));
                System.out.println(command.circles.contains(i + "," + (j+100) + "," + color));
                System.out.println(command.circles.contains(i + "," + (j+150) + "," + color));
                System.out.println(command.circles.contains(i + "," + (j+200) + "," + color));
                System.out.println(command.circles.contains(i + "," + (j+250) + "," + color));*/

                if (color != -1 &&
                        command.circles.contains((i+50) + "," + (j-50) + "," + color) &&
                        command.circles.contains((i+100) + "," + (j-100) + "," + color) &&
                        command.circles.contains((i+150) + "," + (j-150) + "," + color) &&
                        command.circles.contains((i+200) + "," + (j-200) + "," + color) &&
                        command.circles.contains((i+250) + "," + (j-250) + "," + color)) {
                    System.out.println("found");
                    System.out.println("Выиграл: " + color + " по диагонали (справа налево)");
                    return color;
                }
                else System.out.println("Not found");
            }
        }

        /*// Проверка по вертикали
        for (int j = 0; j < canvas_size; j++) {
            for (int i = 0; i <= canvas_size - 6; i++) {
                int color = getColor(i, j);
                if (color != -1 &&
                        color == getColor(i + 1, j) &&
                        color == getColor(i + 2, j) &&
                        color == getColor(i + 3, j) &&
                        color == getColor(i + 4, j) &&
                        color == getColor(i + 5, j)) {
                    return color;
                }
            }
        }

        // Проверка по диагонали (слева направо)
        for (int i = 0; i <= canvas_size - 6; i++) {
            for (int j = 0; j <= canvas_size - 6; j++) {
                int color = getColor(i, j);
                if (color != -1 &&
                        color == getColor(i + 1, j + 1) &&
                        color == getColor(i + 2, j + 2) &&
                        color == getColor(i + 3, j + 3) &&
                        color == getColor(i + 4, j + 4) &&
                        color == getColor(i + 5, j + 5)) {
                    return color;
                }
            }
        }

        // Проверка по диагонали (справа налево)
        for (int i = 0; i <= canvas_size - 6; i++) {
            for (int j = 5; j < canvas_size; j++) {
                int color = getColor(i, j);
                if (color != -1 &&
                        color == getColor(i + 1, j - 1) &&
                        color == getColor(i + 2, j - 2) &&
                        color == getColor(i + 3, j - 3) &&
                        color == getColor(i + 4, j - 4) &&
                        color == getColor(i + 5, j - 5)) {
                    return color;
                }
            }
        }*/

        System.out.println("Никто не выиграл, ");
        return -1; // Никто не выиграл
    }

    public static void main(String[] args) {
        try {
            // Создание реестра на порту 1099
            Registry registry = LocateRegistry.createRegistry(8080);

            // Создание экземпляра удаленного объекта
            RemoteConnect6_gameServer service = new Server();

            // Регистрация удаленного объекта в реестре
            registry.rebind("RemoteConnect6_gameServer", service);

            System.out.println("Server is ready.");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}