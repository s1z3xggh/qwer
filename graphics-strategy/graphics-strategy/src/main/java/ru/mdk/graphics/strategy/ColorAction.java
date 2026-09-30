package ru.mdk.graphics.strategy;

import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;

/** Stateless strategy; a Line uses stroke rather than fill. */
public final class ColorAction implements ShapeAction {
    @Override public void apply(Shape shape) {
        var current = shape instanceof Line ? shape.getStroke() : shape.getFill();
        var next = Color.CORNFLOWERBLUE.equals(current) ? Color.CORAL : Color.CORNFLOWERBLUE;
        if (shape instanceof Line) shape.setStroke(next);
        else shape.setFill(next);
    }
    @Override public String toString() { return "Сменить цвет"; }
}
