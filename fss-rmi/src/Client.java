import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.Objects;

public class Client {

    public static final String CMD_UPLOAD = "upload";
    public static final String CMD_DOWNLOAD = "download";
    public static final String CMD_SHUTDOWN = "shutdown";
    public static final String CMD_DIR = "dir";
    public static final String CMD_MKDIR = "mkdir";
    public static final String CMD_RMDIR = "rmdir";
    public static final String CMD_RM = "rm";
    private static final String SERVER_KEY = "PA1_SERVER";
    private static FileServer fileServer;

    public static void main(String[] args) {
        String command = args[0];
        String[] commandArgs = Arrays.copyOfRange(args, 1, args.length);

        try {
            String[] serverLocation = getServerLocation();
            String host = serverLocation[0];
            int port = Integer.parseInt(serverLocation[1]);

            Registry registry = LocateRegistry.getRegistry(host, port);
            fileServer = (FileServer) registry.lookup(FileServer.FILE_SERVER_RMI);

            switch (command) {
                case CMD_UPLOAD -> uploadFile(commandArgs[0], commandArgs[1]);
                case CMD_DOWNLOAD -> downloadFile(commandArgs[0], commandArgs[1]);
                case CMD_RM, CMD_RMDIR -> deleteFileOrDirectory(commandArgs[0]);
                case CMD_DIR -> listContents(commandArgs[0]);
                case CMD_MKDIR -> createDirectory(commandArgs[0]);
                case CMD_SHUTDOWN -> shutdownServer();
                default -> throw new IllegalStateException("Unexpected operation: " + command);
            }
        } catch (Exception e) {
            System.err.printf("Error while performing %s operation: %s", command, e.getMessage());
            System.exit(1);
        }

    }

    private static void shutdownServer() throws RemoteException {
        fileServer.shutdown();
    }

    private static void deleteFileOrDirectory(String path) throws RemoteException {
        try {
            fileServer.deleteFileOrDirectory(path);
        } catch (RemoteException e) {
            if (e.getCause().getMessage().equals(FileServer.FILE_DIRECTORY_NOT_DELETED_SERVER)) {
                System.err.println(FileServer.FILE_DIRECTORY_NOT_DELETED_SERVER);
                System.exit(1);
            } else throw e;
        }
        System.out.println("File/Directory deleted");
    }

    private static void createDirectory(String path) throws RemoteException {
        try {
            fileServer.createDirectory(path);
        } catch (RemoteException e) {
            if (e.getCause().getMessage().equals(FileServer.DIRECTORY_NOT_CREATED_SERVER)) {
                System.err.println(FileServer.DIRECTORY_NOT_CREATED_SERVER);
                System.exit(1);
            } else throw e;
        }
        System.out.println("Directory created");
    }

    private static void listContents(String path) throws RemoteException {
        String[] contents = new String[0];
        try {
            contents = fileServer.listContents(path);
        } catch (RemoteException e) {
            if (e.getCause().getMessage().equals(FileServer.DIRECTORY_NOT_EXIST_SERVER)) {
                System.err.println(FileServer.DIRECTORY_NOT_EXIST_SERVER);
                System.exit(1);
            } else throw e;
        }
        if (contents.length != 0) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < contents.length; i++) {
                sb.append(String.format("%d. %s\n", i + 1, contents[i]));
            }
            System.out.println(sb);
        }
    }

    private static void downloadFile(String sourcePath, String destinationPath) throws IOException {
        long fileSize = 0;
        try {
            fileSize = fileServer.getFileSize(sourcePath);
        } catch (RemoteException e) {
            if (e.getCause().getMessage().equals(FileServer.FILE_NOT_EXIST_SERVER)) {
                System.err.println(FileServer.FILE_NOT_EXIST_SERVER);
                System.exit(1);
            } else throw e;
        }
        File file = new File(destinationPath);
        long fileSizeOnClient = file.length();
        boolean isResume = fileSizeOnClient != 0 && fileSizeOnClient < fileSize;
        if (isResume) {
            System.out.println("Resuming Download");
        }
        int chunkSize = getChunkSize(fileSize);
        try (FileOutputStream outputStream = isResume ? new FileOutputStream(file, true) : new FileOutputStream(file)) {
            long position = isResume ? fileSizeOnClient : 0;
            while (position < fileSize) {
                byte[] chunk = fileServer.getFileChunk(sourcePath, position, chunkSize);
                outputStream.write(chunk);
                position += chunk.length;
                printPercentageAndWait(position, fileSize);
            }
        }
        System.out.println("\nFile download completed");
    }

    private static void uploadFile(String sourcePath, String destinationPath) throws IOException {
        File file = new File(sourcePath);
        if (!file.exists()) {
            System.err.println("File/Directory doesn't exist on Client");
            System.exit(1);
        }
        long fileSize = file.length();
        long fileSizeOnServer = 0;
        try {
            fileSizeOnServer = fileServer.getFileSize(destinationPath);
        } catch (RemoteException e) {
            if (!e.getCause().getMessage().equals(FileServer.FILE_NOT_EXIST_SERVER)) {
                throw e;
            } // else ignore
        }
        boolean isResume = fileSizeOnServer != 0 && fileSizeOnServer < fileSize;
        if (isResume) {
            System.out.println("Resuming Upload");
        }
        fileServer.startFileUpload(destinationPath, fileSize, isResume);
        int chunkSize = getChunkSize(fileSize);
        try (FileInputStream inputStream = new FileInputStream(file)) {
            if (isResume) {
                inputStream.skip(fileSizeOnServer);
            }
            byte[] buffer = new byte[chunkSize];
            int bytesRead;
            int position = isResume ? (int) fileSizeOnServer : 0;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                fileServer.uploadFileChunk(sourcePath, buffer, bytesRead);
                position += bytesRead;
                printPercentageAndWait(position, fileSize);
            }
        } finally {
            fileServer.completeFileUpload(destinationPath);
        }
        System.out.println("\nFile upload completed");
    }

    private static String[] getServerLocation() throws Exception {
        String envVar = System.getenv(SERVER_KEY);
        if (Objects.isNull(envVar)) {
            throw new Exception("Server env not defined");
        }
        //System.out.println(SERVER_KEY + ": " + envVar);
        return envVar.split(":");
    }

    private static int getChunkSize(long fileSize) {
        int chunkSize = (int) Math.floorDiv(fileSize, 10);
        return chunkSize != 0 ? chunkSize : 1;
    }

    private static void printPercentageAndWait(float completed, float total) {
        int percentage = (int) (completed / total * 100);
        if (percentage > 100) {
            percentage = 100;
        }
        System.out.print("\rIn Progress: " + percentage + "%");
        try {
            Thread.sleep(500);
        } catch (InterruptedException ignored) {
        }
    }

}
