package net.RMI;

import net.RMI.Client.RemoteClientCallback;
import net.command.SerializableCommand;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Set;
import java.util.HashSet;

public class Server extends UnicastRemoteObject implements RemoteConnect6_gameServer {

    private Set<String> clients_id;
    private Set<String> ready_clients;
    private Set<RemoteClientCallback> clientCallbacks;
    private SerializableCommand circles_info;
    private int winner;
    private int move;

    protected Server() throws RemoteException {
        super();

        circles_info = new SerializableCommand();
        clientCallbacks = new HashSet<>();
        clients_id = new HashSet<>();
        ready_clients = new HashSet<>();

        winner = -1;
        move = -1;
    }

    @Override
    public void gameInfo(SerializableCommand command, int grid_size) throws RemoteException {
        winner = WinnerCheck(command, grid_size);

        move++;
        if (move == 4) {
            move = 0;
        }

        circles_info = command;

        for (RemoteClientCallback clientCallback : clientCallbacks) {
            clientCallback.receiveGameInfo(circles_info, move, winner);
        }

        if (winner != -1) {
            circles_info.circles.clear();
            ready_clients.clear();
            clientCallbacks.clear();
            clients_id.clear();
        }
    }

    @Override
    public String notifyButtonClicked(String clientId, RemoteClientCallback callback) throws RemoteException {
        if (!clients_id.contains(clientId) && clients_id.size() == 2) {
            String str = "На сервере достаточное количество игроков";
            callback.setColor(-1);
            callback.rejectRequest();
            return str;
        }

        if (ready_clients.isEmpty()) {
            ready_clients.add("0");
            callback.setColor(0);
        } else {
            ready_clients.add("1");
            callback.setColor(1);
        }

        clients_id.add(clientId);
        clientCallbacks.add(callback);

        if (ready_clients.size() >= 2) {
            ready_clients.clear();
            for (RemoteClientCallback clientCallback : clientCallbacks) {
                clientCallback.startGame();
            }
            move = 1;
            winner = -1;

            String str = "Игра началась";
            System.out.println(str);
            return str;
        } else {
            String str = "Ждём других игроков";
            System.out.println(str);
            return str;
        }
    }

    @Override
    public void resetGame(int color, RemoteClientCallback callback) throws RemoteException {
        if (color == 0)
            winner = 1;
        else winner = 0;

        circles_info.circles.clear();

        System.out.println("Сброс игры");

        clientCallbacks.add(callback);
        for (RemoteClientCallback clientCallback : clientCallbacks) {
            clientCallback.resetClientsGame(circles_info, winner);
        }

        ready_clients.clear();
        clientCallbacks.clear();
        clients_id.clear();

    }

    @Override
    public void kickClient(RemoteClientCallback callback, int color) throws RemoteException {
        if (clientCallbacks.contains(callback)) {
            if (ready_clients.contains(callback))
                ready_clients.remove(callback);

            clientCallbacks.remove(callback);
            clients_id.clear();

            System.out.println("Клиент отключился");

            for (RemoteClientCallback clientCallback : clientCallbacks) {
                resetGame(color, clientCallback);
            }
        }
    }


    int WinnerCheck(SerializableCommand command, int canvas_size) {
        String black_circle, white_circle;
        int color;

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

                /*System.out.println(command.circles);

                System.out.println(command.circles.contains((i+50) + "," + j + "," + color));
                System.out.println(command.circles.contains((i+100) + "," + j + "," + color));
                System.out.println(command.circles.contains((i+150) + "," + j + "," + color));
                System.out.println(command.circles.contains((i+200) + "," + j + "," + color));
                System.out.println(command.circles.contains((i+250) + "," + j + "," + color));*/

                if (color != -1 &&
                        command.circles.contains((i+50) + "," + j + "," + color) &&
                        command.circles.contains((i+100) + "," + j + "," + color) &&
                        command.circles.contains((i+150) + "," + j + "," + color) &&
                        command.circles.contains((i+200) + "," + j + "," + color) &&
                        command.circles.contains((i+250) + "," + j + "," + color)) {
                    System.out.println("Нашёлся победитель");
                    System.out.println("Выиграл: " + color + " по строке");
                    return color;
                }
                else System.out.println("Игра продолжается");
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

                if (color != -1 &&
                        command.circles.contains(i + "," + (j+50) + "," + color) &&
                        command.circles.contains(i + "," + (j+100) + "," + color) &&
                        command.circles.contains(i + "," + (j+150) + "," + color) &&
                        command.circles.contains(i + "," + (j+200) + "," + color) &&
                        command.circles.contains(i + "," + (j+250) + "," + color)) {
                    System.out.println("Нашёлся победитель");
                    System.out.println("Выиграл: " + color + " по столбцу");
                    return color;
                }
                else System.out.println("Игра продолжается");
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

                if (color != -1 &&
                        command.circles.contains((i+50) + "," + (j+50) + "," + color) &&
                        command.circles.contains((i+100) + "," + (j+100) + "," + color) &&
                        command.circles.contains((i+150) + "," + (j+150) + "," + color) &&
                        command.circles.contains((i+200) + "," + (j+200) + "," + color) &&
                        command.circles.contains((i+250) + "," + (j+250) + "," + color)) {
                    System.out.println("Нашёлся победитель");
                    System.out.println("Выиграл: " + color + " по диагонали (слева направо)");
                    return color;
                }
                else System.out.println("Игра продолжается");
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

                if (color != -1 &&
                        command.circles.contains((i+50) + "," + (j-50) + "," + color) &&
                        command.circles.contains((i+100) + "," + (j-100) + "," + color) &&
                        command.circles.contains((i+150) + "," + (j-150) + "," + color) &&
                        command.circles.contains((i+200) + "," + (j-200) + "," + color) &&
                        command.circles.contains((i+250) + "," + (j-250) + "," + color)) {
                    System.out.println("Нашёлся победитель");
                    System.out.println("Выиграл: " + color + " по диагонали (справа налево)");
                    return color;
                }
                else System.out.println("Игра продолжается");
            }
        }

        System.out.println("Никто не выиграл");
        return -1;
    }

    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.createRegistry(8080);
            RemoteConnect6_gameServer service = new Server();
            registry.rebind("RemoteConnect6_gameServer", service);

            System.out.println("Server is ready.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}