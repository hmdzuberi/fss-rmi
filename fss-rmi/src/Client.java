import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.Objects;

public class Client {

    private static final String SERVER_KEY = "PA1_SERVER";
    public static final String CMD_UPLOAD = "upload";
    public static final String CMD_DOWNLOAD = "download";
    public static final String CMD_SHUTDOWN = "shutdown";
    public static final String CMD_DIR = "dir";
    public static final String CMD_MKDIR = "mkdir";
    public static final String CMD_RMDIR = "rmdir";
    public static final String CMD_RM = "rm";

    private static IFileServer fileServer;

    public static void main(String[] args) {
        String command = args[0];
        String[] commandArgs = Arrays.copyOfRange(args, 1, args.length);

        try {
            String[] serverLocation = getServerLocation();
            String host = serverLocation[0];
            int port = Integer.parseInt(serverLocation[1]);

            Registry registry = LocateRegistry.getRegistry(host, port);
            fileServer = (IFileServer) registry.lookup("file-server");

            switch (command) {
                case CMD_UPLOAD -> upload(commandArgs[0], commandArgs[1]);
                case CMD_DOWNLOAD -> download(commandArgs[0], commandArgs[1]);
                case CMD_RM -> deleteFile(commandArgs[0]);
                case CMD_DIR -> listContents(commandArgs[0]);
                case CMD_MKDIR -> createDirectory(commandArgs[0]);
                case CMD_RMDIR -> deleteDirectory(commandArgs[0]);
                case CMD_SHUTDOWN -> shutdownServer();
                default -> throw new IllegalStateException("Unexpected value: " + command);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static void shutdownServer() throws RemoteException {
        fileServer.shutdown();
    }

    private static void deleteDirectory(String path) throws RemoteException {
        boolean success = fileServer.deleteDirectory(path);
        if (success) {
            System.out.println("Directory deleted");
        } else {
            System.err.println("Directory could not be deleted");
        }

    }

    private static void createDirectory(String path) throws RemoteException {
        boolean success = fileServer.createDirectory(path);
        if (success) {
            System.out.println("Directory created");
        } else {
            System.err.println("Directory could not be created");
        }
    }

    private static void listContents(String path) throws RemoteException {
        String[] contents = fileServer.listContents(path);
        if (contents != null) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < contents.length; i++) {
                sb.append(String.format("%d. %s\n", i + 1, contents[i]));
            }
            System.out.println(sb);
        }
    }

    private static void deleteFile(String path) throws RemoteException {
        boolean success = fileServer.deleteFile(path);
        if (success) {
            System.out.println("File deleted");
        } else {
            System.err.println("File could not be deleted");
        }

    }

    private static void download(String commandArg, String commandArg1) {

    }

    private static void upload(String commandArg, String commandArg1) {

    }

    private static String[] getServerLocation() throws Exception {
        String envVar = System.getenv(SERVER_KEY);
        if (Objects.isNull(envVar)) {
            throw new Exception("Server env not defined");
        }
        //System.out.println(SERVER_KEY + ": " + envVar);
        return envVar.split(":");
    }

}
