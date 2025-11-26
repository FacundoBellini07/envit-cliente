package juego.red;

import juego.elementos.EstadoTurno;
import juego.elementos.Palo;
import juego.interfaces.GameController;
import juego.personajes.TipoJugador;

import java.io.IOException;
import java.net.*;
import java.util.Timer;
import java.util.TimerTask;

public class HiloCliente extends Thread {

    private DatagramSocket conexion;
    private InetAddress ipserver;
    private int puerto = 30243;
    private boolean fin = false;
    private InetAddress ipBroadcast;

    private GameController listener;
    private long ultimoMensajeServidor = System.currentTimeMillis();
    private Timer timerCheckerServidor;
    private final long TIEMPO_LIMITE_SERVER = 4000;
    private boolean conectado = false;
    private Timer timerConexion;


    public HiloCliente(GameController listener) {
        try {
            iniciarCheckerServidor();
            this.listener = listener;
           ipBroadcast = InetAddress.getByName("255.255.255.255");
            ipserver = ipBroadcast;
            conexion = new DatagramSocket();

            // ✅ NUEVO: Iniciar reintentos de conexión
            iniciarReintentos();

        } catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
        }
    }
    private void iniciarCheckerServidor() {
        timerCheckerServidor = new Timer(true);
        timerCheckerServidor.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (conectado && (System.currentTimeMillis() - ultimoMensajeServidor > TIEMPO_LIMITE_SERVER)) {
                    System.out.println("[CLIENTE] 🚨 TIMEOUT del SERVIDOR. Asumiendo desconexión.");
                    detenerCheckerServidor();
                    detener();
                    if (listener != null) {
                        listener.onServidorDesconectado(); // 🚨 NUEVO MÉTODO
                    }
                }
            }
        }, 1000, 1000); // Chequear cada 1 segundo
    }
    private void detenerCheckerServidor() {
        if (timerCheckerServidor != null) {
            timerCheckerServidor.cancel();
            timerCheckerServidor = null;
        }
    }
    private void iniciarReintentos() {
        timerConexion = new Timer();
        timerConexion.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!conectado) {
                    System.out.println("[CLIENTE] Enviando solicitud de conexión...");
                    enviarMensaje("Conexion");
                }
            }
        }, 0, 1000); // Intentar cada 1 segundo
    }

    private void detenerReintentos() {
        if (timerConexion != null) {
            timerConexion.cancel();
            timerConexion = null;
        }
    }

    public void enviarMensaje(String mensaje) {
        byte[] data = mensaje.getBytes();
        DatagramPacket dp = new DatagramPacket(data, data.length, ipserver, puerto);
        try {
            conexion.send(dp);
            System.out.println("[CLIENTE] Enviado: " + mensaje);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        do {
            byte[] data = new byte[1024];
            DatagramPacket dp = new DatagramPacket(data, data.length);
            try {
                conexion.receive(dp);
                procesarMensaje(dp);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } while (!fin);
    }

    private void procesarMensaje(DatagramPacket dp) {
        ultimoMensajeServidor = System.currentTimeMillis();
        String mensaje = (new String(dp.getData())).trim();
        System.out.println("[CLIENTE] <<<< RECIBIDO: " + mensaje);

        if (mensaje.equals("OK")) {
            System.out.println("[CLIENTE] ✅ Conectado al servidor");

            // ✅ NUEVO: Marcar como conectado y detener reintentos
            conectado = true;
            detenerReintentos();

            ipserver = dp.getAddress();

            // ✅ Mantener PING activo durante la partida
            new Timer().scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if (!fin) {
                        enviarMensaje("PING");
                    }
                }
            }, 1000, 1000);
        }
        else if (mensaje.equals("FULL")) {
            System.out.println("[CLIENTE] El servidor está lleno. Reintentar en 2 segundos...");
            // Los reintentos continuarán automáticamente
        }
        else if (mensaje.startsWith("ID:")) {
            procesarID(mensaje);
        }
        else if (mensaje.startsWith("EMPIEZA:")) {
            procesarEmpieza(mensaje);
        }
        else if (mensaje.startsWith("ESTADO:")) {
            procesarEstadoPartida(mensaje);
        }
        else if (mensaje.startsWith("CARTA_RIVAL:")) {
            procesarCartaRival(mensaje);
        }
        else if (mensaje.equals("TRUCO_RIVAL")) {
            if (listener != null) {
                listener.onTrucoRival();
            }
        }
        else if (mensaje.equals("PING")) {
            return;
        }
        else if (mensaje.startsWith("CARTAS:")) {
            System.out.println("[CLIENTE] ✅ Mensaje CARTAS recibido!");
            procesarCartasRecibidas(mensaje);
        }
        else if (mensaje.equals("NUEVA_RONDA")) {
            System.out.println("[CLIENTE] ✅ NUEVA_RONDA recibida");
            if (listener != null) {
                listener.onNuevaRonda();
            }
        }
        else if (mensaje.startsWith("RESPUESTA_TRUCO:")) {
            String[] partes = mensaje.split(":");
            String respuesta = partes[1]; // QUIERO o SUBIDA
            String nuevoEstado = partes.length > 2 ? partes[2] : "";
            if (listener != null) listener.onTrucoRespondido(respuesta, nuevoEstado);
        }
        else if (mensaje.startsWith("GANADOR:")) {
            String idStr = mensaje.split(":")[1];
            int idGanador = Integer.parseInt(idStr);
            System.out.println("[CLIENTE] Recibido mensaje de victoria. Ganador ID: " + idGanador);

            if (listener != null) {
                listener.onJuegoTerminado(idGanador);
            }
        }
        else if (mensaje.equals("RIVAL_SE_FUE")) {
            System.out.println("[CLIENTE] El rival se desconectó. Cerrando hilo...");


            fin = true;
            detenerReintentos();

            if (conexion != null && !conexion.isClosed()) {
                conexion.close(); // Cierra el socket
            }

            if (listener != null) {
                listener.onVolverAlMenu(); // La UI se encarga de permitir crear uno nuevo
            }
        }
        else {
            System.out.println("[CLIENTE] ⚠️ Mensaje desconocido: " + mensaje);
        }
    }

    private void procesarID(String mensaje) {
        mensaje = mensaje.replace("ID:", "");
        listener.onConectado(Integer.parseInt(mensaje));
        System.out.println("[CLIENTE] ID asignado: " + mensaje);
    }

    private void procesarEmpieza(String mensaje) {
        String idStr = mensaje.split(":")[1];
        int idMano = Integer.parseInt(idStr);

        System.out.println("[CLIENTE] Partida inicia, empieza jugador: " + idMano);

        if (listener != null) {
            listener.startGame(idMano);
        }
    }

    private void procesarEstadoPartida(String mensaje) {
        mensaje = mensaje.replace("ESTADO:", "");
        String[] partes = mensaje.split(":");

        TipoJugador jugadorMano = TipoJugador.valueOf(partes[4]);

        listener.onEstadoActualizado(
                Integer.parseInt(partes[0]),
                Integer.parseInt(partes[1]),
                Integer.parseInt(partes[2]),
                EstadoTurno.valueOf(partes[3]),
                jugadorMano
        );

        if (partes.length >= 8) {
            String estadoTrucoStr = partes[5];
            int manoTruco = Integer.parseInt(partes[6]);
            String ultimoCantoStr = partes[7].equals("null") ? null : partes[7];

            // ✅ ESTO DESBLOQUEARÁ EL BOTÓN AUTOMÁTICAMENTE
            listener.onTrucoActualizado(estadoTrucoStr, manoTruco, ultimoCantoStr);

            System.out.println("[CLIENTE] Truco actualizado: " + estadoTrucoStr +
                    ", mano=" + manoTruco + ", último=" + ultimoCantoStr);
        }
    }

    private void procesarCartaRival(String mensaje) {
        String[] partes = mensaje.split(":");
        if (partes.length >= 3) {
            try {
                int valor = Integer.parseInt(partes[1]);
                Palo palo = Palo.valueOf(partes[2]);
                listener.onCartaRival(valor, palo);

                System.out.println("[CLIENTE] Rival jugó: " + valor + " de " + palo);
            } catch (Exception e) {
                System.err.println("[CLIENTE] Error procesando carta rival: " + e.getMessage());
            }
        }
    }
    public void notificarTrucoEnviado() {
        if (listener != null) {
            // Llama al nuevo método en PantallaPartida para que se bloquee localmente.
            listener.onTrucoEnviadoLocal();
        }
    }
    private void procesarCartasRecibidas(String mensaje) {
        mensaje = mensaje.replace("CARTAS:", "");
        String[] cartas = mensaje.split(",");

        System.out.println("[CLIENTE] ========================================");
        System.out.println("[CLIENTE] Procesando " + cartas.length + " cartas");
        System.out.println("[CLIENTE] Mensaje completo: " + mensaje);
        System.out.println("[CLIENTE] ========================================");

        for (int i = 0; i < cartas.length; i++) {
            String cartaStr = cartas[i].trim();
            System.out.println("[CLIENTE] Procesando carta " + (i+1) + ": '" + cartaStr + "'");

            String[] partes = cartaStr.split(":");

            if (partes.length >= 2) {
                try {
                    int valor = Integer.parseInt(partes[0].trim());
                    Palo palo = Palo.valueOf(partes[1].trim());

                    if (listener != null) {
                        listener.onCartaRecibida(valor, palo);
                        System.out.println("[CLIENTE] ✅ Carta agregada: " + valor + " de " + palo);
                    }
                } catch (Exception e) {
                    System.err.println("[CLIENTE] ❌ Error procesando carta: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.err.println("[CLIENTE] ❌ Formato inválido para carta: " + cartaStr);
            }
        }

        System.out.println("[CLIENTE] ========================================");
        System.out.println("[CLIENTE] Fin del procesamiento de cartas");
        System.out.println("[CLIENTE] ========================================");
    }
    public void reiniciarBusqueda() {
        System.out.println("[CLIENTE] Reiniciando búsqueda...");

        detener();
        try {
            this.join(2000);
            if (this.isAlive()) {
                System.err.println("[CLIENTE] ⚠️ Thread no terminó, forzando...");
                this.interrupt();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        try {
            if (conexion != null && !conexion.isClosed()) {
                conexion.close();
            }
            conexion = new DatagramSocket();
        } catch (SocketException e) {
            System.err.println("[CLIENTE] Error al reabrir socket: " + e.getMessage());
            return;
        }

        fin = false;
        conectado = false;
        ipserver = ipBroadcast;
        iniciarReintentos();

        this.start();
    }

    public void detener() {
        System.out.println("[CLIENTE] Iniciando detención del hilo...");

        fin = true;

        detenerReintentos();
        detenerCheckerServidor();

        if (conexion != null && !conexion.isClosed()) {
            conexion.close();
            System.out.println("[CLIENTE] Socket cerrado");
        }

        if (this.isAlive() && !this.isInterrupted()) {
            this.interrupt();
            System.out.println("[CLIENTE] Thread interrumpido");
        }
    }
}