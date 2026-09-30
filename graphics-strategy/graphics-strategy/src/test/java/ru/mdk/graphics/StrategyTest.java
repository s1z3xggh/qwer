package ru.mdk.graphics;

import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import org.junit.jupiter.api.Test;
import ru.mdk.graphics.strategy.*;
import static org.junit.jupiter.api.Assertions.*;

class StrategyTest {
    @Test void contextSwitchChangesOperationWithoutReplacingShape() {
        var shape = new Circle(30);
        shape.setFill(Color.CORNFLOWERBLUE);
        var context = new ShapeController(new ColorAction());
        context.apply(shape);
        assertEquals(Color.CORAL, shape.getFill());
        context.setAction(new ResizeAction());
        context.apply(shape);
        assertEquals(1.4, shape.getScaleX());
        assertEquals(Color.CORAL, shape.getFill());
    }
    @Test void lineColorChangesStroke() {
        var line = new Line(0, 0, 100, 100);
        line.setStroke(Color.CORNFLOWERBLUE);
        var action = new ColorAction();
        action.apply(line);
        assertEquals(Color.CORAL, line.getStroke());
        action.apply(line);
        assertEquals(Color.CORNFLOWERBLUE, line.getStroke());
    }
    @Test void repeatedResizeIsBoundedAndReversible() {
        var shape = new Rectangle(50, 50);
        var action = new ResizeAction();
        for (int i = 0; i < 100; i++) {
            action.apply(shape);
            assertTrue(shape.getScaleX() == 1 || shape.getScaleX() == 1.4);
            assertEquals(shape.getScaleX(), shape.getScaleY());
        }
        assertEquals(1, shape.getScaleX());
    }
    @Test void nullStrategyIsRejected() {
        assertThrows(NullPointerException.class, () -> new ShapeController(null));
    }
}
