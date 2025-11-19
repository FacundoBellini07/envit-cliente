package juego.red;

import com.badlogic.gdx.Game;
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
            listener.onConectado(Integer.parseInt(mensaje));
            System.out.println("ID asignado por el servidor: " + Integer.parseInt(mensaje));
        }
        else if (mensaje.startsWith("EMPIEZA:")) {
            String idStr = mensaje.split(":")[1]; // Obtener el número después de los dos puntos
            int idMano = Integer.parseInt(idStr);

            if (listener != null) {
                listener.startGame(idMano); // <-- Pasamos el ID al listener
            }
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
                jugadorMano // <-- Pasamos el nuevo valor
        );

    }

    private void procesarCartaRival(String mensaje) {
        // Formato: "CARTA_RIVAL:1:ESPADAS"
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

    public void detener() {
        fin = true;
        if (conexion != null && !conexion.isClosed()) {
            conexion.close();
        }
    }
}