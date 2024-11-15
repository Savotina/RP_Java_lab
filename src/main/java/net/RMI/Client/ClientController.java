package net.RMI.Client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import net.command.SerializableCommand;

import java.rmi.RemoteException;
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
    private GraphicsContext gc;
    int cellSize;
    int columnCount;

    Color[] colors = {Color.BLACK, Color.WHITE};
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
        cellSize = (int) gameGrid.getPrefWidth() / gameGrid.getColumnConstraints().size();
        columnCount = gameGrid.getColumnConstraints().size();

        for (ColumnConstraints column : gameGrid.getColumnConstraints()) {
            column.setMinWidth(cellSize);
            column.setPrefWidth(cellSize);
        }
        for (RowConstraints row : gameGrid.getRowConstraints()) {
            row.setMinHeight(cellSize);
            row.setPrefHeight(cellSize);
        }

        drawnCircles = new HashSet<>();
        gc = gameCanvas.getGraphicsContext2D();

        gameCanvas.setOnMouseClicked(this::handleMouseClick);
    }

    private void handleMouseClick(MouseEvent event) {
        double x = event.getX();
        double y = event.getY();

        double[] intersection = isIntersectionClicked(x, y);

        if (intersection != null) {
            int[] centers = new int[intersection.length];
            for (int i = 0; i < intersection.length; ++i) {
                centers[i] = (int) Math.round(intersection[i]);
            }

            //System.out.println(centers[0] + ", " + centers[1]);
            if (!isCircleDrawed(centers[0], centers[1], client.getColor())) {
                drawCircle(gameCanvas.getGraphicsContext2D(), centers[0], centers[1], client.getColor());
                SerializableCommand circles_info = new SerializableCommand(drawnCircles);
                client.setGameInfo(circles_info, (int)gameCanvas.getHeight(), columnCount);
            }
        }
    }

    private boolean isCircleDrawed(int x, int y, int color) {
        boolean res = true;
        String info = "Кружок уже нарисован в этом месте";

        String circleKey_black = x + "," + y + "," + 0;
        String circleKey_white = x + "," + y + "," + 1;

        if (!drawnCircles.contains(circleKey_black) && !drawnCircles.contains(circleKey_white)) {
            res = false;
            String circleKey = x + "," + y + "," + color;
            drawnCircles.add(circleKey);
            info = "Кружок успешно нарисован";
        }

        //System.out.println(info);
        return res;
    }

    private double[] isIntersectionClicked(double x, double y) {
        int area = 10;

        double offsetX = (gameCanvas.getWidth() - gameGrid.getPrefWidth()) / 2;
        double offsetY = (gameCanvas.getHeight() - gameGrid.getPrefHeight()) / 2;

        double realX = x - offsetX;
        double realY = y - offsetY;

        int row = (int) (realY / cellSize);
        int col = (int) (realX / cellSize);

        double cornerX = col * cellSize + offsetX;
        double cornerY = row * cellSize + offsetY;

        boolean isTopLeftCorner = (realX >= cornerX - offsetX && realX <= cornerX - offsetX + area && realY >= cornerY - offsetY && realY <= cornerY - offsetY + area);
        boolean isTopRightCorner = (realX >= cornerX + cellSize - area - offsetX && realX <= cornerX + cellSize - offsetX && realY >= cornerY - offsetY && realY <= cornerY - offsetY + area);
        boolean isBottomLeftCorner = (realX >= cornerX - offsetX && realX <= cornerX - offsetX + area && realY >= cornerY + cellSize - area - offsetY && realY <= cornerY + cellSize - offsetY);
        boolean isBottomRightCorner = (realX >= cornerX + cellSize - area - offsetX && realX <= cornerX + cellSize - offsetX && realY >= cornerY + cellSize - area - offsetY && realY <= cornerY + cellSize - offsetY);

        if (isTopLeftCorner) {
            return new double[]{cornerX, cornerY};
        } else if (isTopRightCorner) {
            return new double[]{cornerX + cellSize, cornerY};
        } else if (isBottomLeftCorner) {
            return new double[]{cornerX, cornerY + cellSize};
        } else if (isBottomRightCorner) {
            return new double[]{cornerX + cellSize, cornerY + cellSize};
        }

        return null;
    }

    private void drawCircle(GraphicsContext gc, int x, int y, int color) {
        gc.setFill(colors[color]);
        gc.fillOval(x - 10, y - 10, 20, 20);
    }

    @FXML
    void onButtonClicked() {
        String button_txt = play_button.getText();
        try {
            if (button_txt.equals("Играть")) {
                System.out.println("Вы нажали кнопку 'Играть'");
                play_button.setDisable(true);

                String response = client.notifyButtonClicked();
                System.out.println(response);

            } else if (button_txt.equals("Сдаться")) {
                System.out.println("Вы сдались");
                client.resetButtonClicked(client.getColor());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void mouseButtonPressed(MouseEvent event) {
        //System.out.println("Button changed color");
        play_button.setStyle("-fx-border-color: black; -fx-border-radius: 10; -fx-background-color: #e1a75b; -fx-background-radius: 10; -fx-max-width: 150;"); // Красный цвет
    }

    @FXML
    void mouseButtonReleased(MouseEvent event) {
        //System.out.println("Button changed color");
        play_button.setStyle("-fx-border-color: black; -fx-border-radius: 10; -fx-background-color: #dc9942; -fx-background-radius: 10; -fx-max-width: 150;");
    }

    public void updateGameStartedUI() {
        System.out.println("Игра началась. Обновление игрового поля");

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
        //System.out.println("Обновляю игровое поле, координаты и цвета нарисованных кружков: " + command.circles);
        drawnCircles = command.circles;

        for (String circle : command.circles) {
            String[] coordinates = circle.split(",");
            int x = Integer.parseInt(coordinates[0]);
            int y = Integer.parseInt(coordinates[1]);
            int color = Integer.parseInt(coordinates[2]);
            drawCircle(gameCanvas.getGraphicsContext2D(), x, y, color);
        }
    }

    public void ProcessMove(int client_move) {
        int client_color = client.getColor();
        if (client_move == 2) {
            gameCanvas.setDisable(client_color == 0);
        }
        else if (client_move == 0) {
            gameCanvas.setDisable(client_color == 1);
        }

        System.out.println("Ход " + move_color[client_move]);
        client.setMove(client_color);
        move.setText(move_color[client_move]);
    }

    public void updateClientColor(int color) {
        if (color == 0) {
            your_circle.setFill(Color.BLACK);
            opponent_circle.setFill(Color.WHITE);
        } else if (color == 1) {
            your_circle.setFill(Color.WHITE);
            opponent_circle.setFill(Color.BLACK);
        }
        else {
            your_circle.setFill(Color.TRANSPARENT);
            opponent_circle.setFill(Color.TRANSPARENT);
        }
    }

    public void udpateAfterReset(int color, int mv) {
        drawnCircles.clear();
        play_button.setText("Играть");
        gameCanvas.setDisable(true);

        if (color == -1 && mv == -1) {
            label_state.setText("Результат:");
            move.setText(" ничья");
        }
        else {
            label_state.setText("Победитель");
            move.setText(win_color[color]);
            System.out.println("Победитель:" + win_color[color]);
        }
    }

    public void RejectRequest() {
        Platform.runLater(() -> {
            drawnCircles.clear();
            play_button.setDisable(false);
            gc.clearRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());

            label_state.setText("");
            move.setText("");

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Уведомление");
            alert.setHeaderText("Игра уже началась!");
            alert.setContentText("Вы не можете присоединиться к игре, так как игра уже идёт.");

            alert.showAndWait();
        });
    }

    public void CloseConnection() throws RemoteException {
        Platform.runLater(() -> {
            try {
                client.closeConnection();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
