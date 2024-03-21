import kareltherobot.*;
import java.awt.Color;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

public class MiPrimerRobot implements Directions {
    private static final int NUMERO_DE_CALLES = 20;
    private static final int NUMERO_DE_AVENIDAS = 20;

    // Definición de la matriz de semáforos para todas las posiciones del mapa
    public static Semaphore[][] semaforos = new Semaphore[NUMERO_DE_CALLES][NUMERO_DE_AVENIDAS];
    static {
        // Inicialización de la matriz de semáforos
        for (int calle = 0; calle < NUMERO_DE_CALLES; calle++) {
            for (int avenida = 0; avenida < NUMERO_DE_AVENIDAS; avenida++) {
                semaforos[calle][avenida] = new Semaphore(1); // Inicialmente, todos los semáforos están libres
            }
        }
    }

    public static void main(String[] args) {
        World.readWorld("Mundo-100B.kwld");
        World.setVisible(true);
        // imprimirEstadoSemaforos();
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

    public static void imprimirEstadoSemaforos() {
        // Imprimir el estado de los semáforos
        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 20; j++) {
                Semaphore semaforo = semaforos[i][j];
                int permisosDisponibles = semaforo.availablePermits();
                System.out.println("Semaforo en la posición [" + i + "][" + j + "]: Permisos disponibles = "
                        + permisosDisponibles);
            }
        }
    }

    private static void mineros(int cantidadMineros) {
        int calle = 8;
        int avenida = 2;
        // Crear y ejecutar el comportamiento de los mineros
        for (int i = 0; i < cantidadMineros; i++) {
            Thread mineroThread = new Thread(new MineroRunnable());
            mineroThread.start();
            try {
                Thread.sleep(6000); // 1000 milisegundos = 1 segundo
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void trenes(int cantidadTrenes) {
        int calle = 9;
        int avenida = 2;
        // Crear y ejecutar el comportamiento de los trenes
        for (int i = 0; i < cantidadTrenes; i++) {
            Thread trenThread = new Thread(new TrenRunnable());
            trenThread.start();
            try {
                Thread.sleep(6000); // 1000 milisegundos = 1 segundo
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void extractor(int cantidadExtractores) {
        int calle = 10;
        int avenida = 2;
        // Crear y ejecutar el comportamiento de los extractores
        for (int i = 0; i < cantidadExtractores; i++) {
            Thread extractorThread = new Thread(new ExtractorRunnable());
            extractorThread.start();
            try {
                Thread.sleep(6000); // 1000 milisegundos = 1 segundo
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

class MineroRunnable implements Runnable {
    @Override
    public void run() {
        Minero minero = new Minero(8, 2, Directions.North, 0, Color.BLACK);
        minero.ingresarAlaMina();
        minero.iniciaProcesoDeMinado();
        System.out.println("------Ingresa a la mina");
    }
}

class Minero extends Robot {
    private static final int capacidad_max = 50;
    private static HashSet<String> posicionesOcupadas = new HashSet<>(); // Definir como estática
    private static final Object lock = new Object();
    public static final AtomicBoolean lockAtomic = new AtomicBoolean(false); // Renombrada a lockAtomic

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
                System.out.println("------ nuevaPosicion " + nuevaPosicion);
                String[] coordenadas = nuevaPosicion.split(",");
                int avenueNew = Integer.parseInt(coordenadas[1]);
                int streetNew = Integer.parseInt(coordenadas[0]);

                // Adquirir el semáforo de la nueva posición
                MiPrimerRobot.semaforos[streetNew][avenueNew].acquire();

                // Mover el robot a la nueva posición
                move();

                // Liberar el semáforo de la posición anterior
                MiPrimerRobot.semaforos[street][avenue].release();

                // Actualizar la posición del robot
                avenue = avenueNew;
                street = streetNew;
            } catch (Exception e) {
                // Manejar la excepción, si es necesario
                System.out.println("------ **ENTRA AL CATCH" + MiPrimerRobot.semaforos[11][15]);
                e.printStackTrace();
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

    public void iniciaProcesoDeMinado() {
        try {
            moverNposiciones(1);
            while (nextToABeeper()) {
                pickNBeeper(50);
                turnLeft();
                moverNposiciones(1);
                putNBeeper(50);
                turnLeft();
                moverNposiciones(1);
                turnLeft();
                moverNposiciones(1);
                turnLeft();
            }
            System.out.println("------ **finaliza este lado del while");

            extraerBeepers();

            putNBeeper(50);
            regresarAlpuntoDeEspera();

            extraerBeepers();

            putNBeeper(50);
            regresarAlpuntoDeEspera();

            extraerBeepers();

            putNBeeper(50);
            regresarAlpuntoDeEspera();

            extraerBeepers();

            putNBeeper(50);
            regresarAlpuntoDeEspera();

            extraerBeepers();

            putNBeeper(50);
            regresarAlpuntoDeEspera();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void extraerBeepers() {
        try {
            synchronized (lock) {
                while (lockAtomic.getAndSet(true)) {
                    lock.wait(); // Esperar hasta que sea su turno
                }
                moverNposiciones(1); // Mover al minero a la posición [10][14]
                // MiPrimerRobot.semaforos[10][14].release(); // Liberar el semáforo en la
                // posición [10][14]

                turnLeftNveces(3);
                moverNposiciones(1);

                for (int i = 0; i < 5; i++) {
                    if (nextToABeeper()) {
                        pickNBeeper(50);
                        regresarAlpuntoDeEntrega();
                        lockAtomic.set(false);
                        lock.notify();
                        return;
                    }
                    moverNposiciones(1);
                }
            }
        } catch (Exception e) {
            // Manejar la excepción, si es necesario
        }
    }

    private void regresarAlpuntoDeEntrega() {
        System.out.println("------ **regresarAlpuntoDeEntrega" + MiPrimerRobot.semaforos[11][14]);
        turnLeftNveces(2);
        while (avenue != 13) {
            MiPrimerRobot.semaforos[11][14].release();
            // MiPrimerRobot.semaforos[11][15].release();
            moverNposiciones(1);
            // avenue = avenue - 1;
        }
    }

    private void regresarAlpuntoDeEspera() {
        turnLeftNveces(1);
        moverNposiciones(1);
        // street = street - 1;
        turnLeftNveces(1);
        moverNposiciones(1);
        // avenue = avenue + 1;
        turnLeftNveces(1);
    }

    private void pickNBeeper(int nVeces) {
        for (int i = 0; i < nVeces; i++) {
            pickBeeper();
        }
    }

    private void putNBeeper(int nVeces) {
        for (int i = 0; i < nVeces; i++) {
            putBeeper();
        }
    }

    private boolean estaLaminaDesocupada() {
        if (MiPrimerRobot.semaforos[11][15].availablePermits() == 0
                || MiPrimerRobot.semaforos[11][16].availablePermits() == 0
                || MiPrimerRobot.semaforos[11][17].availablePermits() == 0
                || MiPrimerRobot.semaforos[11][18].availablePermits() == 0
                || MiPrimerRobot.semaforos[11][19].availablePermits() == 0) {
            System.out.println("------ **la mina esta ocupada");
            return false;
        } else {
            System.out.println("------ **la mina esta desocupada");
            return true;
        }
    }
}

class TrenRunnable implements Runnable {
    @Override
    public void run() {
        Tren tren = new Tren(9, 2, Directions.North, 0, Color.BLUE);
        tren.ingresarAlaMina();
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
                    // System.out.println("------ Se puede mover a la posicion");
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

class ExtractorRunnable implements Runnable {
    @Override
    public void run() {
        Extractor extractor = new Extractor(10, 2, Directions.North, 0, Color.RED);
        extractor.ingresarAlaMina();
        extractor.extraerBeepers();
        extractor.salirMina();
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
                    // System.out.println("------ Se puede mover a la posicion");
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