import kareltherobot.*;
import java.awt.Color;

public class MiPrimerRobot implements Directions {
    public static void main(String[] args) {
        // Usamos el archivo que creamos del mundo
        World.readWorld("Mundo-Final.kwld");
        World.setVisible(true);

        Color blue = new Color(0, 0, 255);
        Color red = new Color(255, 0, 0);
        Racer Karel = new Racer(1, 1, East, 0, red);
        Racer David = new Racer(1, 1, East, 0, blue);

        Karel.run();
        David.run();
    }
}

class Racer extends Robot {
    public Racer(int Street, int Avenue, Direction direction, int beepers, Color color) {
        super(Street, Avenue, direction, beepers, color);
        World.setupThread(this);
    }

    public void race() {
        // Mover el robot 4 pasos.
        move(4);

        // Recoger los 5 beepers
        pickBeeper(5);

        // Girar a la izquierda y salir de los muros
        boolean front = frontIsClear();
        if (front = true) {
            turnLeft();
        }
        move(2);

        // Poner los beepers fuera de los muros
        putBeeper(5);

        // Ponerse en otra posición y apagar el robot
        move();
        turnOff();
    }

    public void run() {
        race();
    }
}
