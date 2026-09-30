package ru.mdk.graphics;

import java.util.Objects;
import javafx.scene.shape.Shape;
import ru.mdk.graphics.strategy.ShapeAction;

/** Context: the UI changes the strategy, without replacing event handlers. */
public final class ShapeController {
    private ShapeAction action;
    public ShapeController(ShapeAction action) { setAction(action); }
    public void setAction(ShapeAction action) { this.action = Objects.requireNonNull(action); }
    public void apply(Shape shape) { action.apply(Objects.requireNonNull(shape)); }
}
