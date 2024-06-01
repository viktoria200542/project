package org.example.wmashine;

import javafx.animation.AnimationTimer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

public class WMachine {
    // Machine
    private static final ArrayList<BaseCloth> clothsPrototype = new ArrayList<>();
    private ArrayList<BaseCloth> cloths = new ArrayList<>();
    private static boolean working;
    private static boolean enabled;
    private int temperature;
    private int mode;
    private int time;
    private final int maxWeight = 10;
    private int totalWeight;
    // JavaFX
    private Stage stageContainer;
    private Group group;
    private Circle container;
    private Pane containerCloths;
    private Circle buttonEnable;
    private Circle buttonStartStop;
    private Button buttonTemp;
    private Button buttonMode;
    private Button buttonTime;
    private Label errorLabel;


    static {
        clothsPrototype.add(new BaseCloth("Jacket", 1));
        clothsPrototype.add(new BaseCloth("Dress", 2));
        clothsPrototype.add(new BaseCloth("TShirt", 1));
        clothsPrototype.add(new BaseCloth("Shoes", 2));
        clothsPrototype.add(new BaseCloth("Socks", 1));

        for (BaseCloth cloth : clothsPrototype) {
            cloth.init();
        }
    }


    private final String[] modes = {"Прання", "Віджим", "Полоскання"};
    private final int[] times = {20, 10, 5};
    private final int[] modesSpeed = {15, 1, 10};
    private final int[] temperatures = {40, 70, 17, 120};
    private final Color[] temperaturesColor = {Color.BLUE, Color.AQUA, Color.SKYBLUE, Color.RED};
    private final int[] rotations = {360, 10, 360};
    private final boolean[] errors = new boolean[3];
    private final String[] errorsDescription = new String[]{"Відкритий барабан", "Закипіла вода", "Барабан перевантажений"};
    long startTime = -1;

    public void init() {
        ImageView background = new ImageView(new Image("/org/example/wmashine/background.jpg"));
        containerCloths = new Pane();
        containerCloths.setLayoutX(150);
        containerCloths.setLayoutY(290);
        container = new Circle(116);
        container.setCenterX(195);
        container.setCenterY(307);
        container.setCursor(Cursor.HAND);

        container.setOnMouseClicked(event -> {
            if (stageContainer == null) {
                stageContainer = new Stage();
            } else {
                stageContainer.setIconified(false);
                stageContainer.show();
            }
            errors[0] = true;

            ListView<BaseCloth> listView = new ListView<>();
            AtomicReference<ObservableList<BaseCloth>> items = new AtomicReference<>(FXCollections.observableArrayList(cloths));
            listView.setItems(items.get());

            listView.setCellFactory(param -> new ListCell<>() {
                @Override
                public void updateItem(BaseCloth cloth, boolean empty) {
                    super.updateItem(cloth, empty);
                    if (empty) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        cloth.init();
                        setText(cloth.getName() + " " + cloth.getWeight() + "kg");
                        setGraphic(cloth.getImageView());
                    }
                }
            });

            listView.setOnMouseClicked(click -> {
                if (click.getClickCount() == 2) {
                    BaseCloth cloth = listView.getSelectionModel().getSelectedItem();
                    cloths.remove(cloth);
                    items.set(FXCollections.observableArrayList(cloths));
                    listView.setItems(items.get());
                    updateClothes();
                }
            });

            Button add = new Button("Додати одежу");
            add.setOnMouseClicked(event1 -> {
                addCloth(() -> {
                    items.set(FXCollections.observableArrayList(cloths));
                    listView.setItems(items.get());
                    updateClothes();
                    return null;
                });
            });

            Button clear = new Button("Очистити");
            clear.setOnMouseClicked(event1 -> {
                cloths.clear();
                items.set(FXCollections.observableArrayList(cloths));
                listView.setItems(items.get());
                updateClothes();
            });
            VBox sortBox = new VBox();
            sortBox.setSpacing(10);
            Button buttonName = new Button("Ім'я");
            Button buttonWeight = new Button("Вага");

            buttonName.setOnMouseClicked(event1 -> {
                sort(0, listView);
            });

            buttonWeight.setOnMouseClicked(event1 -> {
                sort(1, listView);
            });

            sortBox.getChildren().addAll(buttonName, buttonWeight);

            HBox hBox = new HBox(add, clear, sortBox);
            hBox.setAlignment(Pos.CENTER);
            hBox.setSpacing(15);
            VBox box = new VBox(listView, hBox);
            box.setAlignment(Pos.CENTER);
            Scene scene = new Scene(box, 300, 400);
            stageContainer.setScene(scene);
            stageContainer.setOnCloseRequest(closeRequest -> {
                errors[0] = false;
            });

            stageContainer.show();
        });

        Label labelWork = new Label("Старт|Стоп");
        Label labelEnable = new Label("On|Off");

        labelEnable.setLayoutX(45);
        labelEnable.setLayoutY(11);
        labelWork.setLayoutX(290);
        labelWork.setLayoutY(11);

        String modePrefix = "Режим: ";
        String tempPrefix = "Температура: ";
        String timePrefix = "Час: ";

        buttonMode = new Button();
        buttonTemp = new Button();
        buttonTime = new Button();
        buttonEnable = new Circle(63, 52, 26);
        buttonStartStop = new Circle(319, 52, 26);

        buttonMode.setLayoutX(145);
        buttonMode.setLayoutY(22);

        buttonTemp.setLayoutX(145);
        buttonTemp.setLayoutY(52);

        buttonTime.setLayoutX(145);
        buttonTime.setLayoutY(82);


        buttonEnable.setOnMouseEntered(event -> buttonEnable.setCursor(Cursor.HAND));
        buttonEnable.setOnMouseExited(event -> buttonEnable.setCursor(Cursor.DEFAULT));
        buttonEnable.setOnMouseClicked(event -> {
            enabled = !enabled;
            if (enabled) {
                buttonEnable.setFill(Color.GREEN);
                buttonStartStop.setFill(Color.RED);
            } else {
                buttonEnable.setFill(Color.RED);
                buttonStartStop.setFill(Color.GRAY);
            }
        });
        buttonEnable.setFill(Color.RED);

        buttonStartStop.setOnMouseEntered(event -> buttonStartStop.setCursor(Cursor.HAND));
        buttonStartStop.setOnMouseExited(event -> buttonStartStop.setCursor(Cursor.DEFAULT));
        buttonStartStop.setFill(Color.GRAY);
        buttonStartStop.setOnMouseClicked(event -> {
            working = !working;
            if (working) {
                startTime = System.currentTimeMillis();
                buttonStartStop.setFill(Color.GREEN);
            } else {
                buttonStartStop.setFill(Color.RED);
                startTime = -1;
            }
        });

        buttonMode.setOnMouseClicked(event -> {
            if (++mode > modes.length - 1) {
                mode = 0;
            }
            temperature = mode;
            time = mode;
            buttonMode.setText(modePrefix + modes[mode]);
            buttonTemp.setText(tempPrefix + temperatures[temperature]);
            buttonTime.setText(timePrefix + times[time]);

        });
        buttonMode.setText(modePrefix + modes[mode]);
        buttonTemp.setText(tempPrefix + temperatures[temperature]);
        buttonTime.setText(timePrefix + times[time]);

        buttonTemp.setOnMouseClicked(event -> {
            if (++temperature > temperatures.length - 1) {
                temperature = 0;
            }
            buttonTemp.setText(tempPrefix + temperatures[temperature]);
        });

        buttonTime.setOnMouseClicked(event -> {
            if (++time > times.length - 1) {
                time = 0;
            }
            buttonTime.setText(timePrefix + times[time]);
        });
        errorLabel = new Label();
        errorLabel.fontProperty().set(new Font(20));
        errorLabel.setLayoutX(5);
        errorLabel.setLayoutY(110);
        errorLabel.setCursor(Cursor.HAND);
        errorLabel.setOnMouseClicked(event -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("");
            alert.setTitle("Захист від поломки");
            for (int i = 0; i < errors.length; i++) {
                if (errors[i]) {
                    alert.setContentText(errorsDescription[i]);
                    break;
                }
            }
            alert.show();
        });

        group = new Group();
        group.getChildren().addAll(background, container, containerCloths, buttonMode, buttonTemp, buttonTime, buttonEnable, buttonStartStop, labelEnable, labelWork, errorLabel);
        final int[] rotate = {0};

        new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (working && enabled) {
                    container.setEffect(new GaussianBlur());
                    container.setFill(temperaturesColor[temperature]);

                    errors[1] = temperatures[temperature] >= 100;
                    errors[2] = totalWeight > maxWeight;

                    if (now % 4800 == 0) {
                        errorLabel.setTextFill(Color.TRANSPARENT);

                        for (int i = 0; i < errors.length; i++) {
                            boolean error = errors[i];
                            if (error) {
                                errorLabel.setTextFill(Color.RED);
                                errorLabel.setText("E:" + i);
                                break;
                            }
                        }
                    }

                    for (boolean error : errors) {
                        if (error) {
                            return;
                        }
                    }

                    if (now % modesSpeed[mode] == 0) {
                        if (rotate[0]++ > rotations[mode]) {
                            rotate[0] = 0;
                        }
                        containerCloths.setRotate(rotate[0]);
                    }
                    if (startTime != -1) {
                        if (times[time] * 1000L <= System.currentTimeMillis() - startTime) {
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setHeaderText("");
                            alert.setTitle("Увага");
                            alert.setContentText("Час прання закінчився");
                            alert.show();
                            buttonStartStop.setFill(Color.RED);
                            working = false;
                            startTime = -1;
                        }
                    }
                }
            }
        }.start();
    }

    public void addCloth(Callable<Boolean> callable) {
        Stage stage = new Stage();
        stage.setTitle("Виберіть одежу");

        ListView<BaseCloth> listView = new ListView<>();
        listView.setItems(FXCollections.observableArrayList(clothsPrototype));

        listView.setCellFactory(param -> new ListCell<>() {
            @Override
            public void updateItem(BaseCloth cloth, boolean empty) {
                super.updateItem(cloth, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(cloth.getName() + " " + cloth.getWeight() + "kg");
                    setGraphic(cloth.getImageView());
                }
            }
        });

        listView.setOnMouseClicked(click -> {
            if (click.getClickCount() == 2) {
                BaseCloth cloth = listView.getSelectionModel().getSelectedItem();
                cloths.add(cloth);
                try {
                    callable.call();
                } catch (Exception ignored) {
                }
                stage.close();
            }
        });
        VBox box = new VBox(listView);
        box.setAlignment(Pos.CENTER);
        Scene scene = new Scene(box, 200, 400);
        stage.setScene(scene);

        stage.show();
    }

    public void updateClothes() {
        containerCloths.getChildren().clear();
        totalWeight = 0;
        for (BaseCloth cloth : cloths) {
            cloth.init();
            totalWeight += cloth.getWeight();
            containerCloths.getChildren().add(cloth.getGroup());
        }
    }

    public void sort(int sort, ListView<BaseCloth> listView) {
        ObservableList<BaseCloth> items;
        ArrayList<BaseCloth> cloths = new ArrayList<>();
        switch (sort) {
            case 0 -> {
                containerCloths.getChildren().clear();
                totalWeight = 0;
                cloths = new ArrayList<>(this.cloths);
                cloths.sort(Comparator.comparing(BaseCloth::getName));
            }
            case 1 -> {
                containerCloths.getChildren().clear();
                totalWeight = 0;
                cloths = new ArrayList<>(this.cloths);
                cloths.sort(Comparator.comparing(BaseCloth::getWeight));
            }
        }

        items = FXCollections.observableArrayList(cloths);
        listView.setItems(items);
    }

    public Group getGroup() {
        return group;
    }

    public boolean isWorking() {
        return working;
    }

    public void setWorking(boolean working) {
        this.working = working;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public ArrayList<BaseCloth> getCloths() {
        return cloths;
    }

    public void setCloths(ArrayList<BaseCloth> cloths) {
        this.cloths = cloths;
    }

    public int getTemperature() {
        return temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }
}
