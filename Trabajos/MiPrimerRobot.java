import kareltherobot.*;
import java.awt.Color;

public class MiPrimerRobot implements Directions {
    public static void main(String[] args) {
        // Usamos el archivo que creamos del mundo
        World.readWorld("Mundo-Final.kwld");
        World.setVisible(true);

        Color blue = new Color(0, 255, 225);
        Color red = new Color(255, 0, 0);
        Color black = new Color(0, 0, 0);
        Racer Minero1 = new Racer(8, 1, South, 0, black);
        Racer Minero2 = new Racer(9, 1, South, 0, black);
        Racer Tren1 = new Racer(10, 1, South, 0, blue);
        Racer Tren2 = new Racer(11, 1, South, 0, blue);
        Racer Tren3 = new Racer(12, 1, South, 0, blue);
        Racer Extractor = new Racer(13, 1, South, 0, red);

        Minero1.run();
        Minero2.run();
        Tren1.run();
        Tren2.run();
        Tren3.run();
        Extractor.run();
    }
}

class Racer extends Robot {
    public Racer(int Street, int Avenue, Direction direction, int beepers, Color color) {
        super(Street, Avenue, direction, beepers, color);
        World.setupThread(this);
    }

    public void race() {
        // Apagarse
        turnOff();
    }

    public void run() {
        race();
    }
}
