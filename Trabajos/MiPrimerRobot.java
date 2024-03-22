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
    public static final int cantidadDeMinasEnElMap = 600;

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
        World.readWorld("Mundo-100B.kwld");
        World.setVisible(true);
        World.setDelay(15);
        // World.showSpeedControl(true);
        // Definimos la cantidad predeterminada de robots si no se especifica por línea
        // de comandos
        int cantidadMineros = 2;
        int cantidadTrenes = 3;
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
        // Crear y ejecutar el comportamiento de los mineros
        for (int i = 0; i < cantidadMineros; i++) {
            Thread mineroThread = new Thread(new MineroRunnable());
            mineroThread.start();
            try {
                Thread.sleep(3000); // 1000 milisegundos = 1 segundo
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
                Thread.sleep(3000); // 1000 milisegundos = 1 segundo
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
                Thread.sleep(3000); // 1000 milisegundos = 1 segundo
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
        System.out.println("------Minero ingresa a la mina");
        minero.iniciaProcesoDeMinado();
        System.out.println("------Minero inicia la extraccion");
        // minero.salirDeLaMina();
        // System.out.println("------Minero sale de la mina");
    }
}

class Minero extends Robot {
    private static final int capacidad_max_minero = 50;
    private static int beepersEnPosicion1113 = 0;
    private static boolean losMinerosHanFinalizado = false;
    private static final Object lock = new Object();
    public static final AtomicBoolean lockAtomic = new AtomicBoolean(false); // Renombrada a lockAtomic
    private static int orden = 1;
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
        System.out.println("notify " + beepersEnPosicion1113);
        if (beepersEnPosicion1113 >= 120 || losMinerosHanFinalizado) {
            MiPrimerRobot.semaforoTrenesRecoger.release();
        }
    }

    public void prepararMinerosParaSalir() {
        if (orden == 1) {
            try {
                synchronized (lock) {
                    while (lockAtomic.getAndSet(true)) {
                        lock.wait(); // Esperar hasta que sea su turno
                    }
                    moverNposiciones(1);
                    orden = orden + 1;
                    lockAtomic.set(false);
                    lock.notify();
                }
            } catch (Exception e) {
                // Manejar la excepción, si es necesario
            }
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
            System.out.println("regresar 1");
            turnLeftNveces(2);
        } else {
            System.out.println("regresar  2");
            turnLeftNveces(1);
        }

        while (avenue != 13) {
            MiPrimerRobot.semaforos[11][14].release();
            // MiPrimerRobot.semaforos[11][15].release();
            moverNposiciones(1);
            // avenue = avenue - 1;
        }
    }

    private void regresarAlpuntoDeEspera() {
        System.out.println("regresar al punto de espera");
        turnLeftNveces(1);
        moverNposiciones(1);
        notificarAlTren();
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
        beepersEnPosicion1113 = beepersEnPosicion1113 + nVeces;
        System.out.println("beepersEnPosicion1113 ---" + beepersEnPosicion1113);
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
        tren.SalidaTrenes();
        System.out.println("------Trenes terminan");
        // tren.iniciaProcesoDeTransporte();
        // System.out.println("------Tren inicia el transporte");
        // tren.salirDeLaMina();
        // System.out.println("------Tren sale de la mina");
    }
}

class Tren extends Robot {
    private static final int capacidad_max_tren = 120;
    private static HashSet<String> posicionesOcupadas = new HashSet<>(); // Definir como estática
    private static int beepersParaExtraccion = 0;
    private static final Object lock = new Object();
    public static final AtomicBoolean lockAtomic = new AtomicBoolean(false); // Renombrada a lockAtomic

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
            System.out.println("antes adq");
            MiPrimerRobot.semaforoTrenesRecoger.acquire();
            System.out.println("siguiente adq");
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

    public void SalidaTrenes() {
        try {
            MiPrimerRobot.semaforoAvisarSalida.acquire();
            turnLeftNveces(3);
            moverNposiciones(5);
            turnLeftNveces(3);
            moverNposiciones(10);
            turnLeftNveces(1);
            moverNposiciones(5);
            turnLeftNveces(3);
            moverNposiciones(1);
            turnLeftNveces(2);
            moverNposiciones(1);
            turnLeftNveces(3);
            moverNposiciones(6);
            turnLeftNveces(3);
            moverNposiciones(1);
            turnLeftNveces(1);
            moverNposiciones(2);
            turnLeftNveces(3);
            moverNposiciones(1);
            turnLeftNveces(1);
            moverNposiciones(8);
            turnOff();
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
        while (Extractor.getBeepersEnBodega() != MiPrimerRobot.cantidadDeMinasEnElMap) {
            extractor.iniciaProcesoDeExtraccion();
        }
        System.out.println("------Extractor inicia la extraccion");
        // extractor.informarSalidaMina();
        // extractor.salirDeLaMina();
        // System.out.println("------Extractor sale de la mina");
    }
}

class Extractor extends Robot {
    private static final int capacidad_max_extractor = 50;
    private static HashSet<String> posicionesOcupadas = new HashSet<>(); // Definir como estática
    private static int beepersEnBodega = 0;
    private static boolean laExtraccionHaFinalizado = false;

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
                System.out.println("------ nuevaPosicion " + nuevaPosicion);
                String[] coordenadas = nuevaPosicion.split(",");
                int avenueNew = Integer.parseInt(coordenadas[1]);
                int streetNew = Integer.parseInt(coordenadas[0]);

                // Adquirir el semáforo de la nueva posición

                MiPrimerRobot.semaforos[streetNew][avenueNew].acquire();
                System.out.println("nueva posicion extractor " + avenueNew + streetNew);
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
            System.out.println("release semaforoExtractoresRecoger adquire" + MiPrimerRobot.semaforoExtractoresRecoger);
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
        if (beepersEnBodega < 150) {
            moverNposiciones(1);
        } else if (beepersEnBodega < 300) {
            moverNposiciones(2);
        } else if (beepersEnBodega < 450) {
            moverNposiciones(3);
        } else if (beepersEnBodega < 600) {
            moverNposiciones(4);
        }
        turnLeftNveces(3);
        moverNposiciones(1);
        putNBeeper(capacidad_max_extractor);
        regresarAlpuntoDeEspera();
        informarSalidaMina();
    }

    public void regresarAlpuntoDeEspera() {
        turnLeftNveces(2);
        moverNposiciones(3);
        turnLeftNveces(1);
        if (beepersEnBodega <= 150) {
            moverNposiciones(2);
        } else if (beepersEnBodega <= 300) {
            moverNposiciones(3);
        } else if (beepersEnBodega <= 450) {
            moverNposiciones(4);
        } else if (beepersEnBodega <= 600) {
            moverNposiciones(5);
        }
        turnLeftNveces(3);
        posicionarseParaEntrar();
    }

    public void posicionarseParaEntrar() {
        System.out.println("posicionarseParaEntrar");
        turnLeftNveces(2);

        moverNposiciones(1);

    }

    public void posicionarseParaInicio() {
        System.out.println("posicionarseParaEntrar");
        turnLeftNveces(2);

        moverNposiciones(8);

    }

    public void informarSalidaMina() {
        if (beepersEnBodega == MiPrimerRobot.cantidadDeMinasEnElMap) {
            MiPrimerRobot.semaforoAvisarSalida.release();
        }
    }
}