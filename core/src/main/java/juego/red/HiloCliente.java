package juego.red;
import juego.utilidades.Global;

import java.io.IOException;
import java.net.*;

public class HiloCliente extends Thread {

    private DatagramSocket conexion;
    private InetAddress ipserver;
    private int puerto = 30243;
    private boolean fin = false;
public HiloCliente() {

    try {
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
        do{
            byte[] data = new byte[1024];
            DatagramPacket dp = new DatagramPacket(data, data.length);
            try {
                conexion.receive(dp);
                procesarMensaje(dp);
            } catch (IOException e){
                e.printStackTrace();
            }
        }while(!fin);
    }
    private void procesarMensaje(DatagramPacket dp) {
        String mensaje = (new String(dp.getData())).trim();
        switch(mensaje){
            case "OK":
                System.out.println("Conectado al servidor");
                ipserver = dp.getAddress();
                break;
            case "Empieza":
                Global.empieza = true;
                break;
            default:
                if(mensaje.startsWith("ESTADO:")){
                    procesarEstadoPartida(mensaje);
                }
                break;
        }
    }

    private void procesarEstadoPartida(String mensaje) {
        // Remover "ESTADO:"
        mensaje = mensaje.replace("ESTADO:", "");

        // Split por ":"
        String[] partes = mensaje.split(":");

        int mano       = Integer.parseInt(partes[0].split("=")[1]);
        int puntosJ1   = Integer.parseInt(partes[1].split("=")[1]);
        int puntosJ2   = Integer.parseInt(partes[2].split("=")[1]);
        int turno      = Integer.parseInt(partes[3].split("=")[1]);

        // Guardar en Global o en un singleton de estado
        Global.mano = mano;
        Global.puntosJ1 = puntosJ1;
        Global.puntosJ2 = puntosJ2;
        Global.turno = turno;

        System.out.println("[CLIENTE] Estado actualizado:");
        System.out.println("Mano=" + mano +
                " P1=" + puntosJ1 +
                " P2=" + puntosJ2 +
                " Turno=" + turno);
    }

}
