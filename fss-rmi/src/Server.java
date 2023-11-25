import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Server {
    public static final String CMD_START = "start";

    public static void main(String[] args) {
        System.out.println("Base Directory: " + System.getProperty("user.dir"));
        String command = args[0];
        if (command.equals(Server.CMD_START)) {
            int serverPort = Integer.parseInt(args[1]);
            System.out.println("Starting server");
            try {
                Registry registry = LocateRegistry.createRegistry(serverPort);
                IFileServer fileServer = new FileServer(registry);
                IFileServer stub = (IFileServer) UnicastRemoteObject.exportObject(fileServer, 0);
                registry.rebind("file-server", stub);
            } catch (Exception e) {
                System.err.println("Error initializing server: " + e.getMessage());
            }
        }
    }
}