import kareltherobot.*;
import java.awt.Color;
import java.util.HashSet;

public class MiPrimerRobot implements Directions {
    public static void main(String[] args) {
        World.readWorld("Mundo-Final.kwld");
        World.setVisible(true);

        // Definimos la cantidad predeterminada de robots si no se especifica por línea
        // de comandos
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
        mineros(cantidadMineros);
        trenes(cantidadTrenes);
        extractor(cantidadExtractores);
    }

    private static void mineros(int cantidadMineros) {
        int calle = 8;
        int avenida = 2;
        // Crear y ejecutar el comportamiento de los mineros
        for (int i = 0; i < cantidadMineros; i++) {
            Minero minero = new Minero(calle, avenida, North, 0, Color.BLACK);
            minero.ingresarAlaMina();
        }
    }

    private static void trenes(int cantidadTrenes) {
        int calle = 9;
        int avenida = 2;
        // Crear y ejecutar el comportamiento de los mineros
        for (int i = 0; i < cantidadTrenes; i++) {
            Tren tren = new Tren(calle, avenida, North, 0, Color.BLUE);
            tren.ingresarAlaMina();
        }
    }

    private static void extractor(int cantidadExtractores) {
        int calle = 10;
        int avenida = 2;
        // Crear y ejecutar el comportamiento de los extractores
        for (int i = 0; i < cantidadExtractores; i++) {
            Extractor extractor = new Extractor(calle, avenida, North, 0, Color.RED);
            extractor.ingresarAlaMina();
            extractor.extraerBeepers();
            extractor.salirMina();
        }
    }
}

class Minero extends Robot {
    private static final int capacidad_max = 50;
    private static HashSet<String> posicionesOcupadas = new HashSet<>(); // Definir como estática

    private int street;
    private int avenue;

    public Minero(int street, int avenue, Direction direction, int beepers, Color color) {
        super(street, avenue, direction, beepers, color);
        this.street = street;
        this.avenue = avenue;
        World.setupThread(this);
    }

    public void moverNposiciones(int nPosiciones) {
        for (int i = 0; i < nPosiciones; i++) {
            try {
                String nuevaPosicion = getNextPosition();
                System.out.println("------ nuevaPosicion " + nuevaPosicion + posicionesOcupadas);
                String currentPosition = street + "," + avenue;
                if (!posicionesOcupadas.contains(nuevaPosicion)) {
                    System.out.println("------ Se puede mover a la posicion");
                    move();
                    String[] coordenadas = nuevaPosicion.split(",");
                    avenue = Integer.parseInt(coordenadas[1]);
                    street = Integer.parseInt(coordenadas[0]);
                    posicionesOcupadas.remove(currentPosition);
                    posicionesOcupadas.add(nuevaPosicion);
                } else {
                    break;
                }
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
    }

    public String getNextPosition() {
        int newPosiblePositionAvenue = avenue;
        int newPosiblePositionStreet = street;
        if (facingEast()) {
            newPosiblePositionAvenue = newPosiblePositionAvenue + 1;
        } else if (facingWest()) {
            newPosiblePositionAvenue = newPosiblePositionAvenue - 1;
        } else if (facingNorth()) {
            newPosiblePositionStreet = newPosiblePositionStreet + 1;
        } else if (facingSouth()) {
            newPosiblePositionStreet = newPosiblePositionStreet - 1;
        }
        System.out
                .println("getNextPosition avenida " + newPosiblePositionAvenue + ", calle " + newPosiblePositionStreet);
        String posicion = newPosiblePositionStreet + "," + newPosiblePositionAvenue;
        return posicion;
    }

    public void turnLeftNveces(int nVeces) {
        for (int i = 0; i < nVeces; i++) {
            turnLeft();
        }
    }

    public int getStreet() {
        return street;
    }

    public int getAvenue() {
        return avenue;
    }

    public void ingresarAlaMina() {
        turnLeftNveces(2);
        moverNposiciones(1);
        turnLeftNveces(3);
        moverNposiciones(1);
        turnLeft();
        moverNposiciones(6);
        turnLeft();
        moverNposiciones(7);
        turnLeft();
        moverNposiciones(10);
        turnLeftNveces(3);
        moverNposiciones(5);
        turnLeftNveces(3);
        moverNposiciones(1);
        turnLeft();
        moverNposiciones(1);
        turnLeft();
    }
}

class Tren extends Robot {
    private static final int capacidad_max = 120;
    private static HashSet<String> posicionesOcupadas = new HashSet<>(); // Definir como estática

    private int street;
    private int avenue;

    public Tren(int street, int avenue, Direction direction, int beepers, Color color) {
        super(street, avenue, direction, beepers, color);
        this.street = street;
        this.avenue = avenue;
        World.setupThread(this);
    }

    public void moverNposiciones(int nPosiciones) {
        for (int i = 0; i < nPosiciones; i++) {
            try {
                String nuevaPosicion = getNextPosition();
                System.out.println("------ nuevaPosicion " + nuevaPosicion + posicionesOcupadas);
                String currentPosition = street + "," + avenue;
                if (!posicionesOcupadas.contains(nuevaPosicion)) {
                    System.out.println("------ Se puede mover a la posicion");
                    move();
                    String[] coordenadas = nuevaPosicion.split(",");
                    avenue = Integer.parseInt(coordenadas[1]);
                    street = Integer.parseInt(coordenadas[0]);
                    posicionesOcupadas.remove(currentPosition);
                    posicionesOcupadas.add(nuevaPosicion);
                } else {
                    break;
                }
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
    }

    public String getNextPosition() {
        int newPosiblePositionAvenue = avenue;
        int newPosiblePositionStreet = street;
        if (facingEast()) {
            newPosiblePositionAvenue = newPosiblePositionAvenue + 1;
        } else if (facingWest()) {
            newPosiblePositionAvenue = newPosiblePositionAvenue - 1;
        } else if (facingNorth()) {
            newPosiblePositionStreet = newPosiblePositionStreet + 1;
        } else if (facingSouth()) {
            newPosiblePositionStreet = newPosiblePositionStreet - 1;
        }
        System.out
                .println("getNextPosition avenida " + newPosiblePositionAvenue + ", calle " + newPosiblePositionStreet);
        String posicion = newPosiblePositionStreet + "," + newPosiblePositionAvenue;
        return posicion;
    }

    public void turnLeftNveces(int nVeces) {
        for (int i = 0; i < nVeces; i++) {
            turnLeft();
        }
    }

    public int getStreet() {
        return street;
    }

    public int getAvenue() {
        return avenue;
    }

    public void ingresarAlaMina() {
        turnLeftNveces(2);
        moverNposiciones(2);
        turnLeftNveces(3);
        moverNposiciones(1);
        turnLeft();
        moverNposiciones(6);
        turnLeft();
        moverNposiciones(7);
        turnLeft();
        moverNposiciones(10);
        turnLeftNveces(3);
        moverNposiciones(4);
    }
}

class Extractor extends Robot {
    private static final int capacidad_max = 50;
    private static HashSet<String> posicionesOcupadas = new HashSet<>(); // Definir como estática

    private int street;
    private int avenue;

    public Extractor(int street, int avenue, Direction direction, int beepers, Color color) {
        super(street, avenue, direction, beepers, color);
        this.street = street;
        this.avenue = avenue;
        World.setupThread(this);
    }

    public void moverNposiciones(int nPosiciones) {
        for (int i = 0; i < nPosiciones; i++) {
            try {
                String nuevaPosicion = getNextPosition();
                System.out.println("------ nuevaPosicion " + nuevaPosicion + posicionesOcupadas);
                String currentPosition = street + "," + avenue;
                if (!posicionesOcupadas.contains(nuevaPosicion)) {
                    System.out.println("------ Se puede mover a la posicion");
                    move();
                    String[] coordenadas = nuevaPosicion.split(",");
                    avenue = Integer.parseInt(coordenadas[1]);
                    street = Integer.parseInt(coordenadas[0]);
                    posicionesOcupadas.remove(currentPosition);
                    posicionesOcupadas.add(nuevaPosicion);
                } else {
                    break;
                }
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
    }

    public String getNextPosition() {
        int newPosiblePositionAvenue = avenue;
        int newPosiblePositionStreet = street;
        if (facingEast()) {
            newPosiblePositionAvenue = newPosiblePositionAvenue + 1;
        } else if (facingWest()) {
            newPosiblePositionAvenue = newPosiblePositionAvenue - 1;
        } else if (facingNorth()) {
            newPosiblePositionStreet = newPosiblePositionStreet + 1;
        } else if (facingSouth()) {
            newPosiblePositionStreet = newPosiblePositionStreet - 1;
        }
        System.out
                .println("getNextPosition avenida " + newPosiblePositionAvenue + ", calle " + newPosiblePositionStreet);
        String posicion = newPosiblePositionStreet + "," + newPosiblePositionAvenue;
        return posicion;
    }

    public void turnLeftNveces(int nVeces) {
        for (int i = 0; i < nVeces; i++) {
            turnLeft();
        }
    }

    public int getStreet() {
        return street;
    }

    public int getAvenue() {
        return avenue;
    }

    public void ingresarAlaMina() {
        turnLeftNveces(2);
        moverNposiciones(3);
        turnLeftNveces(3);
        moverNposiciones(1);
        turnLeft();
        moverNposiciones(6);
        turnLeft();
        moverNposiciones(1);
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
        turnLeftNveces(2);
        moverNposiciones(1);
        turnLeftNveces(3);
        moverNposiciones(6);
        turnLeftNveces(3);
        moverNposiciones(1);
        turnLeft();
        moverNposiciones(4);

        System.out.println("El extractor ha salido de la mina y se va a descansar.");
        informarSalidaBodega();
        turnOff();
    }

    private void informarSalidaBodega() {
        // Informar a otros robots que pueden salir de la bodega
        System.out.println("¡Los extractores pueden salir de la bodega!");
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