package net.RMI.Client;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import net.command.SerializableCommand;

import java.util.HashSet;
import java.util.Set;

public class ClientController {

    @FXML
    private Canvas gameCanvas;
    @FXML
    private GridPane gameGrid;
    @FXML
    private Circle your_circle;
    @FXML
    private Circle opponent_circle;
    @FXML
    private Label label_state;
    @FXML
    private Label move;
    @FXML
    private Button play_button;

    private Set<String> drawnCircles;
    //SerializableCommand circles_info;

    private GraphicsContext gc;

    Color[] colors = {Color.BLACK, Color.WHITE};
    int count = 0;
    private final String[] move_color = {"чёрных", "чёрных", "белых", "белых"};
    private final String[] win_color = {" чёрный", " белый"};

    private Client client;

    public ClientController() {
        try {
            this.client = new Client(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void initialize() {
        // Устанавливаем размеры ячеек сетки
        int cellSize = 50;
        for (ColumnConstraints column : gameGrid.getColumnConstraints()) {
            column.setMinWidth(cellSize);
            column.setPrefWidth(cellSize);
        }
        for (RowConstraints row : gameGrid.getRowConstraints()) {
            row.setMinHeight(cellSize);
            row.setPrefHeight(cellSize);
        }

        gc = gameCanvas.getGraphicsContext2D();
        drawnCircles = new HashSet<>();

        gameCanvas.setOnMouseClicked(this::handleMouseClick);
    }

    private void handleMouseClick(MouseEvent event) {
        double x = event.getX();
        double y = event.getY();

        // Определите, попал ли клик на пересечение линий сетки
        double[] intersection = isIntersectionClicked(x, y);

        if (intersection != null) {
            int[] centers = new int[intersection.length];
            for (int i = 0; i < intersection.length; ++i) {
                centers[i] = (int) Math.round(intersection[i]);
            }

            System.out.println(centers[0] + ", " + centers[1]);
            if (!isCircleDrawed(centers[0], centers[1], client.getColor())) {
                drawCircle(gameCanvas.getGraphicsContext2D(), centers[0], centers[1], client.getColor());
                SerializableCommand circles_info = new SerializableCommand(drawnCircles);
                client.SetCirclesInfo(circles_info, (int)gameCanvas.getHeight());
            }
        }
    }

    private boolean isCircleDrawed(int x, int y, int color) {
        boolean res = true;

        String circleKey_black = x + "," + y + "," + 0;
        String circleKey_white = x + "," + y + "," + 1;
        System.out.println("circleKey_black: " + circleKey_black);
        System.out.println("circleKey_white: " + circleKey_white);

        if (!drawnCircles.contains(circleKey_black) && !drawnCircles.contains(circleKey_white)) {
            res = false;
            String circleKey = x + "," + y + "," + color;
            drawnCircles.add(circleKey);
            System.out.println("No");
        }
        else {
            System.out.println("Yes");
        }
        return res;
    }

    private double[] isIntersectionClicked(double x, double y) {
        int tolerance = 5; // Размер области пересечения, в которую нужно кликнуть

        // Получаем размеры ячеек сетки
        double cellWidth = gameGrid.getPrefWidth() / gameGrid.getColumnConstraints().size();
        double cellHeight = gameGrid.getPrefHeight() / gameGrid.getRowConstraints().size();

        System.out.println(cellWidth + ", " + cellHeight);

        // Вычисляем смещение между GridPane и Canvas
        double offsetX = (gameCanvas.getWidth() - gameGrid.getPrefWidth()) / 2;
        double offsetY = (gameCanvas.getHeight() - gameGrid.getPrefHeight()) / 2;

        System.out.println("offsets:" + offsetX + ", " + offsetY);

        // Применяем смещение к координатам клика
        double adjustedX = x - offsetX;
        double adjustedY = y - offsetY;

        // Проверяем, попал ли клик на пересечение линий сетки
        int row = (int) (adjustedY / cellHeight);
        int col = (int) (adjustedX / cellWidth);

        double cornerX = col * cellWidth + offsetX;
        double cornerY = row * cellHeight + offsetY;

        // Проверяем, попал ли клик в одну из областей углов ячейки
        boolean isTopLeftCorner = (adjustedX >= cornerX - offsetX && adjustedX <= cornerX - offsetX + tolerance && adjustedY >= cornerY - offsetY && adjustedY <= cornerY - offsetY + tolerance);
        boolean isTopRightCorner = (adjustedX >= cornerX + cellWidth - tolerance - offsetX && adjustedX <= cornerX + cellWidth - offsetX && adjustedY >= cornerY - offsetY && adjustedY <= cornerY - offsetY + tolerance);
        boolean isBottomLeftCorner = (adjustedX >= cornerX - offsetX && adjustedX <= cornerX - offsetX + tolerance && adjustedY >= cornerY + cellHeight - tolerance - offsetY && adjustedY <= cornerY + cellHeight - offsetY);
        boolean isBottomRightCorner = (adjustedX >= cornerX + cellWidth - tolerance - offsetX && adjustedX <= cornerX + cellWidth - offsetX && adjustedY >= cornerY + cellHeight - tolerance - offsetY && adjustedY <= cornerY + cellHeight - offsetY);

        if (isTopLeftCorner) {
            return new double[]{cornerX, cornerY};
        } else if (isTopRightCorner) {
            return new double[]{cornerX + cellWidth, cornerY};
        } else if (isBottomLeftCorner) {
            return new double[]{cornerX, cornerY + cellHeight};
        } else if (isBottomRightCorner) {
            return new double[]{cornerX + cellWidth, cornerY + cellHeight};
        }

        return null;
    }

    private void drawCircle(GraphicsContext gc, int x, int y, int color) {
        /*if (count >= colors.length)
            count = 0;*/
        gc.setFill(colors[color]);
        gc.fillOval(x - 10, y - 10, 20, 20); // Рисуем кружок радиусом 5 пикселей
        //count++;
    }

    @FXML
    void onButtonClicked() {
        System.out.println("Button was clicked");

        String button_txt = play_button.getText();
        try {
            if (button_txt.equals("Играть")) {
                play_button.setDisable(true);

                // Отправка запроса на сервер

                    String[] response = client.notifyButtonClicked();
                    System.out.println("Server response: " + response[0]);

                    /*if (response[1].equals("0")) {
                        your_circle.setFill(Color.BLACK);
                        opponent_circle.setFill(Color.WHITE);
                        gameCanvas.setDisable(false);
                    }

                    else {
                        your_circle.setFill(Color.WHITE);
                        opponent_circle.setFill(Color.BLACK);
                        gameCanvas.setDisable(true);
                    }*/

                    /*if (response[0].equals("Game started")) {
                        // Логика для начала игры
                        System.out.println("Game started!");
                        //move.setText(move_color[client.getMove()]);
                        //label_state.setText("Ход");
                    }*/


            } else if (button_txt.equals("Сдаться")) {
                System.out.println("Reset the game?");
                client.ResetButtonClicked(client.getColor());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void mouseButtonPressed(MouseEvent event) {
        System.out.println("Button changed color");
        play_button.setStyle("-fx-border-color: black; -fx-border-radius: 10; -fx-background-color: #e1a75b; -fx-background-radius: 10; -fx-max-width: 150;"); // Красный цвет
    }

    @FXML
    void mouseButtonReleased(MouseEvent event) {
        System.out.println("Button changed color");
        play_button.setStyle("-fx-border-color: black; -fx-border-radius: 10; -fx-background-color: #dc9942; -fx-background-radius: 10; -fx-max-width: 150;");
    }

    // Обновление интерфейса пользователя при начале игры
    public void updateGameStartedUI() {
        System.out.println("Updating UI for game start");
        gc.clearRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());
        move.setText(move_color[client.getMove()]);
        label_state.setText("Ход ");
        play_button.setText("Сдаться");
        play_button.setDisable(false);

        if (client.getColor() == 0) {
            gameCanvas.setDisable(false);
        }
        else if (client.getColor() == 1) {
            gameCanvas.setDisable(true);
        }

    }

    public void updateMoveUI(SerializableCommand command) {
        // Логика для обновления интерфейса пользователя при получении хода
        System.out.println("Updating UI for received move: " + command.circles);
        // Здесь вы можете обновить интерфейс пользователя для отображения хода

        drawnCircles = command.circles;

        for (String circle : command.circles) {
            String[] coordinates = circle.split(",");
            int x = Integer.parseInt(coordinates[0]);
            int y = Integer.parseInt(coordinates[1]);
            int color = Integer.parseInt(coordinates[2]);
            drawCircle(gameCanvas.getGraphicsContext2D(), x, y, color);
        }
    }

    // Определение хода
    public void ProcessMove(int client_move) {

        //int mv = client.getMove();
        //System.out.println("move:" + client.getMove());
        //System.out.println("color:" + client.getColor());

        int client_color = client.getColor();
        if (client_move == 2) {
            gameCanvas.setDisable(client_color == 0);
            //System.out.println("setDisable: false");
        }
        else if (client_move == 0) {
            gameCanvas.setDisable(client_color == 1);
            //System.out.println("setDisable: true");
        }

        client.setMove(client_color);
        this.move.setText(move_color[client_move]);
    }

    public void updateClientColor(int color) {
        // Логика для обновления интерфейса пользователя при изменении цвета клиента
        if (color == 0) {
            your_circle.setFill(Color.BLACK);
            opponent_circle.setFill(Color.WHITE);
        } else {
            your_circle.setFill(Color.WHITE);
            opponent_circle.setFill(Color.BLACK);
        }
    }

    // Обновление после сброса игры (использовать, если игрок победил?)
    public void udpateAfterReset(int color) {
        drawnCircles.clear();
        play_button.setText("Играть");
        //your_circle.setFill(Color.TRANSPARENT);
        //opponent_circle.setFill(Color.TRANSPARENT);
        gameCanvas.setDisable(true);
        label_state.setText("Победитель");
        move.setText(win_color[color]);
    }
}
