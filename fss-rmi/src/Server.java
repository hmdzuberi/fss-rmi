import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Server {
    public static final String CMD_START = "start";

    private static boolean shutdown = false;

    public static void main(String[] args) {
        System.out.println("Base Directory: " + System.getProperty("user.dir"));
        String command = args[0];
        if (command.equals(Server.CMD_START)) {
            int serverPort = Integer.parseInt(args[1]);
            System.out.println("Starting server");
            try {
                Registry registry = LocateRegistry.createRegistry(serverPort);
                FileServer fileServer = new RemoteFileServer();
                FileServer stub = (FileServer) UnicastRemoteObject.exportObject(fileServer, 0);
                registry.rebind(FileServer.FILE_SERVER_RMI, stub);

                while (true) {
                    try {
                        Thread.sleep(2 * 1000);
                    } catch (InterruptedException ignored) {}
                    if (shutdown) {
                        System.out.println("Shutting down Server");
                        registry.unbind(FileServer.FILE_SERVER_RMI);
                        boolean hasShutdown = false;
                        while (!hasShutdown) {
                            hasShutdown = UnicastRemoteObject.unexportObject(fileServer, false);
                        }
                        System.exit(0);
                    }
                }
            } catch (Exception e) {
                System.err.println("Error initializing server: " + e.getMessage());
            }
        }
    }

    public static void setShutdown(boolean value) {
        shutdown = value;
    }
}