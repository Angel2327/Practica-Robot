import kareltherobot.*;
import java.awt.Color;
import org.apache.commons.cli.*;

public class MiPrimerRobot implements Directions {
    public static void main(String[] args) {
        World.readWorld("Mundo-Final.kwld");
        World.setVisible(true);
        Color blue = new Color(0, 255, 225);
        Color red = new Color(255, 0, 0);
        Color black = new Color(0, 0, 0);

        // Parseamos los argumentos de la línea de comandos
        CommandLine cmd = parseCommandLine(args);

        // Obtenemos la cantidad de cada tipo de robot
        int cantidadMineros = Integer.parseInt(cmd.getOptionValue("m", "2"));
        int cantidadTransportadores = Integer.parseInt(cmd.getOptionValue("t", "2"));
        int cantidadExploradores = Integer.parseInt(cmd.getOptionValue("e", "2"));

        // Creamos los robots según la cantidad especificada
        crearRobots(cantidadMineros, cantidadTransportadores, cantidadExploradores, blue, red, black);
    }

    private static CommandLine parseCommandLine(String[] args) {
        Options options = new Options();

        // Definir las opciones de línea de comandos
        Option minero = Option.builder("m")
                              .argName("cantidad_minerales")
                              .hasArg()
                              .desc("Cantidad de mineros (por defecto: 2)")
                              .build();

        Option transportador = Option.builder("t")
                                     .argName("cantidad_transportadores")
                                     .hasArg()
                                     .desc("Cantidad de transportadores (por defecto: 2)")
                                     .build();

        Option explorador = Option.builder("e")
                                   .argName("cantidad_exploradores")
                                   .hasArg()
                                   .desc("Cantidad de exploradores (por defecto: 2)")
                                   .build();

        options.addOption(minero);
        options.addOption(transportador);
        options.addOption(explorador);

        CommandLineParser parser = new DefaultParser();
        try {
            return parser.parse(options, args);
        } catch (ParseException e) {
            System.err.println("Error al analizar los argumentos de la línea de comandos: " + e.getMessage());
            System.exit(1);
            return null;
        }
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
