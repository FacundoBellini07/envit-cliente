package juego.red;

import juego.elementos.EstadoTurno;
import juego.elementos.Palo;
import juego.interfaces.GameController;
import juego.personajes.TipoJugador;

import java.io.IOException;
import java.net.*;

public class HiloCliente extends Thread {

    private DatagramSocket conexion;
    private InetAddress ipserver;
    private int puerto = 30243;
    private boolean fin = false;

    private GameController listener;

    public HiloCliente(GameController listener) {
        try {
            this.listener = listener;
            ipserver = InetAddress.getByName("255.255.255.255");
            conexion = new DatagramSocket();
        } catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
        }
        enviarMensaje("Conexion");
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
        String mensaje = (new String(dp.getData())).trim();
        System.out.println("[CLIENTE] <<<< RECIBIDO: " + mensaje);

        if (mensaje.equals("OK")) {
            System.out.println("[CLIENTE] Conectado al servidor");
            ipserver = dp.getAddress();
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

        // Formato antiguo: mano:p1:p2:estadoTurno:jugadorMano
        // Formato nuevo: mano:p1:p2:estadoTurno:jugadorMano:trucoUsado:manoTruco

        TipoJugador jugadorMano = TipoJugador.valueOf(partes[4]);

        listener.onEstadoActualizado(
                Integer.parseInt(partes[0]),  // mano
                Integer.parseInt(partes[1]),  // p1
                Integer.parseInt(partes[2]),  // p2
                EstadoTurno.valueOf(partes[3]), // estadoTurno
                jugadorMano                     // jugadorMano
        );

        // ✅ NUEVO: Procesar estado del truco si está presente
        if (partes.length >= 7) {
            boolean trucoUsado = partes[5].equals("1");
            int manoTruco = Integer.parseInt(partes[6]);

            listener.onTrucoActualizado(trucoUsado, manoTruco);

            System.out.println("[CLIENTE] Truco actualizado: usado=" + trucoUsado + ", mano=" + manoTruco);
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

    public void detener() {
        fin = true;
        if (conexion != null && !conexion.isClosed()) {
            conexion.close();
        }
    }
}