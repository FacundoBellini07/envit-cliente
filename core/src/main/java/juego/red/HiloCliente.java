package juego.red;

import juego.elementos.Palo;
import juego.utilidades.Global;
import juego.interfaces.EventoRedListener;
import java.io.IOException;
import java.net.*;

public class HiloCliente extends Thread {

    private DatagramSocket conexion;
    private InetAddress ipserver;
    private int puerto = 30243;
    private boolean fin = false;

    private EventoRedListener listener;

    public HiloCliente() {
        try {
            ipserver = InetAddress.getByName("255.255.255.255");
            conexion = new DatagramSocket();
        } catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
        }
        enviarMensaje("Conexion");
    }

    public void setEventoListener(EventoRedListener listener) {
        this.listener = listener;
    }

    public void enviarMensaje(String mensaje) {
        byte[] data = mensaje.getBytes();
        DatagramPacket dp = new DatagramPacket(data, data.length, ipserver, puerto);
        try {
            conexion.send(dp);
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

        if (mensaje.equals("OK")) {
            System.out.println("Conectado al servidor");
            ipserver = dp.getAddress();
        }
        else if(mensaje.startsWith("ID:")){
            mensaje = mensaje.replace("ID:", "");
            Global.idJugadorLocal = Integer.parseInt(mensaje);
            System.out.println("ID asignado por el servidor: " + Global.idJugadorLocal);
        }
        else if (mensaje.equals("Empieza")) {
            Global.empieza = true;
        }
        else if (mensaje.startsWith("ESTADO:")) {
            procesarEstadoPartida(mensaje);
        }
        // ✅ NUEVO: Procesar carta del rival
        else if (mensaje.startsWith("CARTA_RIVAL:")) {
            procesarCartaRival(mensaje);
        }
        // ✅ NUEVO: Procesar truco del rival
        else if (mensaje.equals("TRUCO_RIVAL")) {
            if (listener != null) {
                listener.onTrucoRivalRecibido();
            }
        }
    }

    private void procesarEstadoPartida(String mensaje) {
        mensaje = mensaje.replace("ESTADO:", "");
        String[] partes = mensaje.split(":");

        Global.manoActual = Integer.parseInt(partes[0]);
        Global.puntosJ1 = Integer.parseInt(partes[1]);
        Global.puntosJ2 = Integer.parseInt(partes[2]);
        Global.estadoTurno = Global.EstadoTurno.valueOf(partes[3]);


        System.out.println("[CLIENTE] Estado actualizado: Mano=" + Global.manoActual +
                " P1=" + Global.puntosJ1 + " P2=" + Global.puntosJ2 + " Turno=" + Global.estadoTurno);
    }

    private void procesarCartaRival(String mensaje) {
        // Formato: "CARTA_RIVAL:1:ESPADAS"
        String[] partes = mensaje.split(":");
        if (partes.length >= 3) {
            try {
                int valor = Integer.parseInt(partes[1]);
                Palo palo = Palo.valueOf(partes[2]);

                if (listener != null) {
                    listener.onCartaRivalRecibida(valor, palo);
                }

                System.out.println("[CLIENTE] Rival jugó: " + valor + " de " + palo);
            } catch (Exception e) {
                System.err.println("[CLIENTE] Error procesando carta rival: " + e.getMessage());
            }
        }
    }

    public void detener() {
        fin = true;
        if (conexion != null && !conexion.isClosed()) {
            conexion.close();
        }
    }
}