import kareltherobot.*;
import java.awt.Color;

public class MiPrimerRobot implements Directions {
    public static void main(String[] args) {
        World.readWorld("Mundo-Final.kwld");
        World.setVisible(true);
        Color blue = new Color(0, 255, 225);
        Color red = new Color(255, 0, 0);
        Color black = new Color(0, 0, 0);

        // Definimos la cantidad predeterminada de robots si no se especifica por línea de comandos
        int cantidadMineros = 2;
        int cantidadTrenes = 2;
        int cantidadExtractores = 2;

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
                        cantidadTrenes = Integer.parseInt(value);
                        break;
                    case "-e":
                        cantidadExtractores = Integer.parseInt(value);
                        break;
                    default:
                        System.out.println("Opción no reconocida: " + option);
                }
            }
        } else {
            System.out.println("No se proporcionaron argumentos válidos. Se usarán valores predeterminados.");
        }

        // Creamos los robots según la cantidad especificada
        crearRobots(cantidadMineros, cantidadTrenes, cantidadExtractores, blue, red, black);
        mineros(cantidadMineros);
        trenes(cantidadTrenes);
        extractor(cantidadExtractores);
    }

    private static void crearRobots(int cantidadMineros, int cantidadTrenes, int cantidadExtractores,
                                    Color blue, Color red, Color black) {
        for (int i = 0; i < cantidadMineros; i++) {
            new Minero(8 + i, 1, South, black).run();
        }

        for (int i = 0; i < cantidadTrenes; i++) {
            new Racer(10 + i, 1, South, 0, blue).run();
        }

        for (int i = 0; i < cantidadExtractores; i++) {
            new Extractor(13 + i, 1, South,red).run();
        }
    }

    private static void extractor(int cantidadExtractores) {
        int puntoExtraccion = 11;
        int avenidaSuperficie = 2;

        // Crear y ejecutar el comportamiento de los extractores
        for (int i = 0; i < cantidadExtractores; i++) {
            Extractor extractor = new Extractor(puntoExtraccion, avenidaSuperficie, South, Color.RED);
            extractor.extraerBeepers();
            extractor.salirMina();
        }
    }

    private static void mineros(int cantidadMineros) {
        int puntoExtraccion = 11;
        int avenidaSuperficie = 2;
        // Crear y ejecutar el comportamiento de los mineros
        for (int i = 0; i < cantidadMineros; i++) {
            Minero minero = new Minero(puntoExtraccion, avenidaSuperficie, South, Color.BLACK);
            minero.ingresarAlaMina();
        }
    }

    private static void trenes(int cantidadTrenes) {

        // Crear y ejecutar el comportamiento de los mineros
        for (int i = 0; i < cantidadTrenes; i++) {

        }
    }
}

class Extractor extends Robot {
    private static final int capacidad_max = 50;

    public Extractor(int Street, int Avenue, Direction direction, Color color) {
        super(Street, Avenue, direction, 0, color);
        World.setupThread(this);
    }

    public void extraerBeepers() {
        int cantidadExtraida = 0;
        while (nextToABeeper() && cantidadExtraida < capacidad_max) {
            pickBeeper();
            cantidadExtraida++;
        }
        System.out.println("El extractor ha extraído " + cantidadExtraida + " beepers.");
    }

    public void salirMina() {
        while (!frontIsClear()) {
            turnLeft();
        }
        move();
        while (frontIsClear()) {
            move();
        }
        turnLeft();
        turnLeft();
        while (frontIsClear()) {
            move();
        }
        System.out.println("El extractor ha salido de la mina y se va a descansar.");
        informarSalidaBodega();
        turnOff();
    }

    private void informarSalidaBodega() {
        // Informar a otros robots que pueden salir de la bodega
        System.out.println("¡Los extractores pueden salir de la bodega!");
    }

    public void race() {
        // Apagarse
        turnOff();
    }

    public void run() {
        race();
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

class Minero extends Robot {
    private static final int capacidad_max = 50;

    public Minero(int Street, int Avenue, Direction direction, Color color) {
        super(Street, Avenue, North, 0, color);
        World.setupThread(this);
    }

    public void moverNposiciones(int nPosiciones){
        for(int i = 0; i < nPosiciones;i++){
            move();
        }
    }

    public void turnLeftNveces(int nVeces){
        for(int i = 0; i < nVeces;i++){
            turnLeft();
        }
    }

    public void ingresarAlaMina() {
        turnLeftNveces(2);
        moverNposiciones(4);
        turnLeftNveces(3);
        move();
        turnLeft();
        moverNposiciones(6);
        turnLeft();
        moverNposiciones(7);
        turnLeft();
        moverNposiciones(10);
        turnLeftNveces(3);
        moverNposiciones(5);

    }
}