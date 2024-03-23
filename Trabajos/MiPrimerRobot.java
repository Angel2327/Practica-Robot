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
    public static final int cantidadDeMinasEnElMap = 12000;
    public static int cantidadMineros = 2;
    public static int cantidadTrenes = 2;
    public static int cantidadExtractores = 2;

    // Definición de la matriz de semáforos para todas las posiciones del mapa
    public static Semaphore[][] semaforos = new Semaphore[NUMERO_DE_CALLES][NUMERO_DE_AVENIDAS];
    public static Semaphore semaforoTrenesRecoger = new Semaphore(0);
    public static Semaphore semaforoExtractoresRecoger = new Semaphore(0);
    public static Semaphore semaforoAvisarSalida = new Semaphore(0);

    static {
        // Inicialización de la matriz de semáforos
        for (int calle = 0; calle < NUMERO_DE_CALLES; calle++) {
            for (int avenida = 0; avenida < NUMERO_DE_AVENIDAS; avenida++) {
                semaforos[calle][avenida] = new Semaphore(1); // Inicialmente, todos los semáforos están libres
            }
        }
    }

    public static void main(String[] args) {
        World.readWorld("Mundo-Final.kwld");
        World.setVisible(true);
        World.setDelay(5);
        // World.showSpeedControl(true);
        // Definimos la cantidad predeterminada de robots si no se especifica por línea
        // de comandos
        // Verificamos si se especificaron argumentos de línea de comandos
        if (args.length >= 6 && args.length % 2 == 0) {
            for (int i = 0; i < args.length; i += 2) {
                String option = args[i];
                String value = args[i + 1];
                switch (option) {
                    case "-m":
                        int cantidadMinerosTemp = Integer.parseInt(value);
                        if (cantidadMinerosTemp > 2) {
                            System.out.println("Error: El número de mineros no puede ser mayor a 2.");
                            return; // Salir del método main si hay un error
                        }
                        cantidadMineros = cantidadMinerosTemp;
                        break;
                    case "-t":
                        int cantidadTrenesTemp = Integer.parseInt(value);
                        if (cantidadTrenesTemp > 10) {
                            System.out.println("Error: El número de trenes no puede ser mayor a 10.");
                            return; // Salir del método main si hay un error
                        }
                        cantidadTrenes = cantidadTrenesTemp;
                        break;
                    case "-e":
                        int cantidadExtractoresTemp = Integer.parseInt(value);
                        if (cantidadExtractoresTemp > 4) {
                            System.out.println("Error: El número de extractores no puede ser mayor a 4.");
                            return; // Salir del método main si hay un error
                        }
                        cantidadExtractores = cantidadExtractoresTemp;
                        break;
                    default:
                        System.out.println("Opción no reconocida: " + option);
                        break;
                }
            }
        } else {
            System.out.println("Argumentos inválidos. Deben ser proporcionados en pares como -m 2 -t 2 -e 2");
            return; // Salir del método main si los argumentos son inválidos
        }

        // Creamos los robots según la cantidad especificada
        mineros(cantidadMineros);
        trenes(cantidadTrenes);
        extractor(cantidadExtractores);
    }

    private static void mineros(int cantidadMineros) {
        // Crear y ejecutar el comportamiento de los mineros
        for (int i = 0; i < cantidadMineros; i++) {
            Thread mineroThread = new Thread(new MineroRunnable());
            mineroThread.start();
            try {
                Thread.sleep(1000); // 1000 milisegundos = 1 segundo
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void trenes(int cantidadTrenes) {
        // Crear y ejecutar el comportamiento de los trenes
        for (int i = 0; i < cantidadTrenes; i++) {
            Thread trenThread = new Thread(new TrenRunnable());
            trenThread.start();
            try {
                Thread.sleep(1000); // 1000 milisegundos = 1 segundo
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void extractor(int cantidadExtractores) {
        // Crear y ejecutar el comportamiento de los extractores
        for (int i = 0; i < cantidadExtractores; i++) {
            Thread extractorThread = new Thread(new ExtractorRunnable());
            extractorThread.start();
            try {
                Thread.sleep(1000); // 1000 milisegundos = 1 segundo
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

class MineroRunnable implements Runnable {
    @Override
    public void run() {
        int street = 8;
        int avenue = 1;
        Minero minero = new Minero(street, avenue, Directions.North, 0, Color.BLACK);
        minero.ingresarAlaMina();
        minero.iniciaProcesoDeMinado();
        minero.salidaMineros();
    }
}

class Minero extends Robot {
    private static final int capacidad_max_minero = 50;
    private static int beepersEnPosicion1113 = 0;
    private static boolean losMinerosHanFinalizado = false;
    private static final Object lock = new Object();
    public static final AtomicBoolean lockAtomic = new AtomicBoolean(false); // Renombrada a lockAtomic
    private static int ordenMinero = 1;
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
        String posicion = newPosiblePositionStreet + "," + newPosiblePositionAvenue;
        return posicion;
    }

    public void turnLeftNveces(int nVeces) {
        for (int i = 0; i < nVeces; i++) {
            turnLeft();
        }
    }

    public void ingresarAlaMina() {
        turnLeftNveces(2);
        while (frontIsClear()) {
            moverNposiciones(1);
        }
        turnLeftNveces(1);
        moverNposiciones(1);
        turnLeftNveces(3);
        moverNposiciones(1);
        turnLeftNveces(3);
        moverNposiciones(1);
        turnLeftNveces(1);
        moverNposiciones(6);
        turnLeftNveces(1);
        moverNposiciones(7);
        turnLeftNveces(1);
        moverNposiciones(10);
        turnLeftNveces(3);
        moverNposiciones(5);
        turnLeftNveces(3);
        moverNposiciones(1);
        turnLeftNveces(1);
        moverNposiciones(1);
        turnLeftNveces(1);
    }

    public void iniciaProcesoDeMinado() {
        try {
            int numeroDevecesARecoger = (MiPrimerRobot.cantidadDeMinasEnElMap / 50) / 2;

            for (int i = 0; i < numeroDevecesARecoger; i++) {
                extraerBeepers();
                putNBeeper(50);
                regresarAlpuntoDeEspera();
            }

            prepararMinerosParaSalir();
            losMinerosHanFinalizado = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void notificarAlTren() {
        if (beepersEnPosicion1113 >= 120 || losMinerosHanFinalizado) {
            MiPrimerRobot.semaforoTrenesRecoger.release();
        }
    }

    public void prepararMinerosParaSalir() {
        if (ordenMinero == 1) {
            try {
                synchronized (lock) {
                    while (lockAtomic.getAndSet(true)) {
                        lock.wait(); // Esperar hasta que sea su turno
                    }
                    moverNposiciones(1);
                    ordenMinero = ordenMinero + 1;
                    lockAtomic.set(false);
                    lock.notify();
                }
            } catch (Exception e) {
                // Manejar la excepción, si es necesario
            }
        }
    }

    public void salidaMineros() {
        try {
            MiPrimerRobot.semaforoAvisarSalida.acquire();

            if (!frontIsClear()) {
                turnLeftNveces(1);
                moverNposiciones(1);
                turnLeftNveces(1);
                moverNposiciones(5);
            } else {
                turnLeftNveces(1);
                moverNposiciones(1);
                turnLeftNveces(1);
                moverNposiciones(4);
            }
            turnLeftNveces(3);
            moverNposiciones(10);
            turnLeftNveces(1);
            moverNposiciones(5);
            turnLeftNveces(3);
            moverNposiciones(2);
            turnLeftNveces(3);
            moverNposiciones(6);
            turnLeftNveces(3);
            moverNposiciones(1);
            turnLeftNveces(1);
            moverNposiciones(5);
            if (ordenMinero == 2) {
                moverNposiciones(1);
                ordenMinero = ordenMinero + 1;
            }
            turnOff();

        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    private void extraerBeepers() {
        try {
            synchronized (lock) {
                while (lockAtomic.getAndSet(true)) {
                    lock.wait(); // Esperar hasta que sea su turno
                }
                moverNposiciones(1); // Mover al minero a la posición [10][14]

                for (int i = 0; i < 6; i++) {

                    if (nextToABeeper() && avenue == 14) {
                        pickNBeeper(50);
                        regresarAlpuntoDeEntrega();
                        lockAtomic.set(false);
                        lock.notify();
                        return;
                    } else {
                        if (avenue == 14) {
                            turnLeftNveces(3);
                            moverNposiciones(1);
                            if (nextToABeeper()) {
                                pickNBeeper(50);
                                regresarAlpuntoDeEntrega();
                                lockAtomic.set(false);
                                lock.notify();
                                return;
                            } else {
                                moverNposiciones(1);
                                if (nextToABeeper()) {
                                    pickNBeeper(50);
                                    regresarAlpuntoDeEntrega();
                                    lockAtomic.set(false);
                                    lock.notify();
                                    return;
                                } else {
                                    moverNposiciones(1);
                                    if (nextToABeeper()) {
                                        pickNBeeper(50);
                                        regresarAlpuntoDeEntrega();
                                        lockAtomic.set(false);
                                        lock.notify();
                                        return;
                                    } else {
                                        moverNposiciones(1);
                                        if (nextToABeeper()) {
                                            pickNBeeper(50);
                                            regresarAlpuntoDeEntrega();
                                            lockAtomic.set(false);
                                            lock.notify();
                                            return;
                                        } else {
                                            moverNposiciones(1);
                                            if (nextToABeeper()) {
                                                pickNBeeper(50);
                                                regresarAlpuntoDeEntrega();
                                                lockAtomic.set(false);
                                                lock.notify();
                                                return;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            // Manejar la excepción, si es necesario
        }
    }

    private void regresarAlpuntoDeEntrega() {

        if (avenue != 14) {
            turnLeftNveces(2);
        } else {
            turnLeftNveces(1);
        }

        while (avenue != 13) {
            MiPrimerRobot.semaforos[11][14].release();
            moverNposiciones(1);
        }
    }

    private void regresarAlpuntoDeEspera() {
        turnLeftNveces(1);
        moverNposiciones(1);
        notificarAlTren();
        turnLeftNveces(1);
        moverNposiciones(1);
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
        beepersEnPosicion1113 = beepersEnPosicion1113 + nVeces;
    }

    public static int getBeepersEnPosicion1113() {
        return beepersEnPosicion1113;
    }

    public static void setLessBeepersEnPosicion(int beepers) {
        beepersEnPosicion1113 = beepersEnPosicion1113 - beepers;
    }

    public static boolean getLosMinerosHanFinalizado() {
        return losMinerosHanFinalizado;
    }
}

class TrenRunnable implements Runnable {
    @Override
    public void run() {
        int street = 8;
        int avenue = 1;
        Tren tren = new Tren(street, avenue, Directions.North, 0, Color.BLUE);
        tren.ingresarAlaMina();
        while (!Minero.getLosMinerosHanFinalizado()) {
            tren.iniciaProcesoDeTransporte();
        }
        tren.salidaTrenes();
    }
}

class Tren extends Robot {
    private static final int capacidad_max_tren = 120;
    private static HashSet<String> posicionesOcupadas = new HashSet<>(); // Definir como estática
    private static int beepersParaExtraccion = 0;
    private static final Object lock = new Object();
    public static final AtomicBoolean lockAtomic = new AtomicBoolean(false); // Renombrada a lockAtomic
    private static int ordenTrenes = 0;
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
        String posicion = newPosiblePositionStreet + "," + newPosiblePositionAvenue;
        return posicion;
    }

    public void turnLeftNveces(int nVeces) {
        for (int i = 0; i < nVeces; i++) {
            turnLeft();
        }
    }

    public void ingresarAlaMina() {
        turnLeftNveces(2);
        while (frontIsClear()) {
            moverNposiciones(1);
        }
        turnLeftNveces(1);
        moverNposiciones(1);
        turnLeftNveces(3);
        moverNposiciones(1);
        turnLeftNveces(3);
        moverNposiciones(1);
        turnLeftNveces(1);
        moverNposiciones(6);
        turnLeftNveces(1);
        moverNposiciones(7);
        turnLeftNveces(1);
        moverNposiciones(10);
        turnLeftNveces(3);
        moverNposiciones(4);
    }

    public void iniciaProcesoDeTransporte() {
        try {
            MiPrimerRobot.semaforoTrenesRecoger.acquire();
            moverNposiciones(1);
            pickNBeeper(capacidad_max_tren);
            Minero.setLessBeepersEnPosicion(capacidad_max_tren);
            regresarAlpuntoDeEntregaYDescargar();
            regresarAlpuntoDeEspera();
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    public void regresarAlpuntoDeEspera() {
        moverNposiciones(4);
        turnLeftNveces(1);
        moverNposiciones(10);
        turnLeftNveces(3);
        moverNposiciones(4);
    }

    public void regresarAlpuntoDeEntregaYDescargar() {
        turnLeftNveces(3);
        moverNposiciones(5);
        turnLeftNveces(3);
        moverNposiciones(10);
        turnLeftNveces(1);
        if (Tren.getBeepersParaExtraccion() == 0) {
            MiPrimerRobot.semaforoExtractoresRecoger.release();
        }
        moverNposiciones(5);
        turnLeftNveces(3);
        moverNposiciones(1);
        putNBeeper(capacidad_max_tren);
        turnLeftNveces(2);
        moverNposiciones(2);
    }

    public void salidaTrenes() {
        try {
            MiPrimerRobot.semaforoAvisarSalida.acquire();
            moverNposiciones(1);
            turnLeftNveces(3);
            moverNposiciones(5);
            turnLeftNveces(3);
            moverNposiciones(10);
            turnLeftNveces(1);
            moverNposiciones(5);
            turnLeftNveces(3);
            moverNposiciones(2);
            turnLeftNveces(3);
            moverNposiciones(6);
            turnLeftNveces(3);
            moverNposiciones(1);
            turnLeftNveces(1);
            moverNposiciones(2);
            turnLeftNveces(3);
            moverNposiciones(1);
            turnLeftNveces(1);
            if (ordenTrenes < MiPrimerRobot.cantidadTrenes) {
                ordenTrenes = ordenTrenes + 1;
                moverNposiciones(11 - ordenTrenes);
                turnOff();
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
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
        beepersParaExtraccion = beepersParaExtraccion + nVeces;
    }

    public static int getBeepersParaExtraccion() {
        return beepersParaExtraccion;
    }

    public static void setLessBeepersParaExtraccion(int beepers) {
        beepersParaExtraccion = beepersParaExtraccion - beepers;
    }
}

class ExtractorRunnable implements Runnable {
    @Override
    public void run() {
        int street = 17;
        int avenue = 1;
        Extractor extractor = new Extractor(street, avenue, Directions.North, 0, Color.RED);
        extractor.posicionarseParaInicio();
        while (MiPrimerRobot.cantidadDeMinasEnElMap - Extractor.getBeepersEnBodega() >= 50
                * (MiPrimerRobot.cantidadExtractores - 1)) {
            extractor.iniciaProcesoDeExtraccion();
        }
        extractor.informarSalidaMina();
    }
}

class Extractor extends Robot {
    private static final int capacidad_max_extractor = 50;
    private static HashSet<String> posicionesOcupadas = new HashSet<>(); // Definir como estática
    private static int beepersEnBodega = 0;
    private static boolean laExtraccionHaFinalizado = false;
    private static int ordenExtractor = 0;
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

        String posicion = newPosiblePositionStreet + "," + newPosiblePositionAvenue;
        return posicion;
    }

    public void turnLeftNveces(int nVeces) {
        for (int i = 0; i < nVeces; i++) {
            turnLeft();
        }
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
        beepersEnBodega = beepersEnBodega + nVeces;
    }

    public static int getBeepersEnBodega() {
        return beepersEnBodega;
    }

    public static void setLessBeepersEnBodega(int beepers) {
        beepersEnBodega = beepersEnBodega - beepers;
    }

    public void iniciaProcesoDeExtraccion() {
        try {
            MiPrimerRobot.semaforoExtractoresRecoger.acquire();
            turnLeftNveces(1);
            moverNposiciones(1);
            turnLeftNveces(3);
            moverNposiciones(2);
            turnLeftNveces(3);
            moverNposiciones(1);
            turnLeftNveces(1);
            moverNposiciones(6);
            turnLeftNveces(1);
            moverNposiciones(1);
            pickNBeeper(capacidad_max_extractor);
            regresarAlaBodegaYDescargar();
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    public void regresarAlaBodegaYDescargar() {
        turnLeftNveces(2);
        moverNposiciones(1);
        turnLeftNveces(3);
        moverNposiciones(6);
        turnLeftNveces(3);
        moverNposiciones(1);
        turnLeftNveces(1);
        moverNposiciones(1);
        turnLeftNveces(3);
        if (MiPrimerRobot.cantidadDeMinasEnElMap - beepersEnBodega != 50) {
            MiPrimerRobot.semaforoExtractoresRecoger.release();
        }
        if (beepersEnBodega < 3000) {
            moverNposiciones(1);
        } else if (beepersEnBodega < 6000) {
            moverNposiciones(2);
        } else if (beepersEnBodega < 9000) {
            moverNposiciones(3);
        } else if (beepersEnBodega < 12000) {
            moverNposiciones(4);
        }
        turnLeftNveces(3);
        moverNposiciones(1);
        putNBeeper(capacidad_max_extractor);
        regresarAlpuntoDeEspera();
    }

    public void regresarAlpuntoDeEspera() {
        turnLeftNveces(2);
        moverNposiciones(MiPrimerRobot.cantidadExtractores + 2);
        turnLeftNveces(1);
        if (beepersEnBodega <= 3000) {
            moverNposiciones(2);
        } else if (beepersEnBodega <= 6000) {
            moverNposiciones(3);
        } else if (beepersEnBodega <= 9000) {
            moverNposiciones(4);
        } else if (beepersEnBodega <= 12000) {
            moverNposiciones(5);
        }
        turnLeftNveces(3);
        posicionarseParaEntrar();
    }

    public void posicionarseParaEntrar() {
        turnLeftNveces(2);
        if ((MiPrimerRobot.cantidadDeMinasEnElMap - Extractor.getBeepersEnBodega() <= 50
                * (MiPrimerRobot.cantidadExtractores - 1))) {

            if (ordenExtractor < MiPrimerRobot.cantidadExtractores) {
                ordenExtractor = ordenExtractor + 1;
                moverNposiciones(MiPrimerRobot.cantidadExtractores - ordenExtractor);
                turnOff();
            }

        } else {
            moverNposiciones(MiPrimerRobot.cantidadExtractores);
        }
    }

    public void posicionarseParaInicio() {
        turnLeftNveces(2);
        moverNposiciones(8);
    }

    public void informarSalidaMina() {
        if (beepersEnBodega == MiPrimerRobot.cantidadDeMinasEnElMap) {
            for (int i = 0; i < MiPrimerRobot.cantidadTrenes + MiPrimerRobot.cantidadMineros; i++) {
                MiPrimerRobot.semaforoAvisarSalida.release();
            }
            turnOff();
        }
    }
}
