package org.example.wmashine;

import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class BaseCloth {
    private ImageView imageView;
    private Group group;

    private final String name;
    private final int weight;

    private int x;
    private int y;

    public BaseCloth(String name, int weight) {
        this.name = name;
        this.weight = weight;
    }

    public void init() {
        imageView = new ImageView(new Image("./org/example/wmashine/" + name + ".png"));
        group = new Group();
        group.getChildren().add(imageView);
    }

    public Group getGroup() {
        return group;
    }

    public String getName() {
        return name;
    }

    public int getWeight() {
        return weight;
    }

    public ImageView getImageView() {
        return imageView;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

}
