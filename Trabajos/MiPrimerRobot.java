import kareltherobot.*;
import java.awt.Color;

public class MiPrimerRobot implements Directions {
    public static void main(String[] args) {
        // Usamos el archivo que creamos del mundo
        World.readWorld("Mundo-Final.kwld");
        World.setVisible(true);

        // Definimos los colores
        Color blue = new Color(0, 255, 225);
        Color red = new Color(255, 0, 0);
        Color black = new Color(0, 0, 0);

        // Definimos la cantidad predeterminada de robots si no se especifica por línea de comandos
        int cantidadMineros = 2;
        int cantidadTransportadores = 2;
        int cantidadExploradores = 2;

        // Verificamos si se especificaron argumentos de línea de comandos
        if (args.length >= 6 && args.length % 2 == 0) {
            for (int i = 0; i < args.length; i += 2) {
                String option = args[i];
                String value = args[i + 1];
                switch (option) {
                    case "-m":
                        cantidadMineros = Integer.parseInt(value);
                        break;
                    case "-t":
                        cantidadTransportadores = Integer.parseInt(value);
                        break;
                    case "-e":
                        cantidadExploradores = Integer.parseInt(value);
                        break;
                    default:
                        System.out.println("Opción no reconocida: " + option);
                }
            }
        } else {
            System.out.println("No se proporcionaron argumentos válidos. Se usarán valores predeterminados.");
        }

        // Creamos los robots según la cantidad especificada
        crearRobots(cantidadMineros, cantidadTransportadores, cantidadExploradores, blue, red, black);
    }

    private static void crearRobots(int cantidadMineros, int cantidadTransportadores, int cantidadExploradores,
                                    Color blue, Color red, Color black) {
        for (int i = 0; i < cantidadMineros; i++) {
            new Racer(8 + i, 1, South, 0, black).run();
        }

        for (int i = 0; i < cantidadTransportadores; i++) {
            new Racer(10 + i, 1, South, 0, blue).run();
        }

        for (int i = 0; i < cantidadExploradores; i++) {
            new Racer(13 + i, 1, South, 0, red).run();
        }
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