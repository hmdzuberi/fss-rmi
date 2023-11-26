import java.io.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class RemoteFileServer implements FileServer {

    private static Registry registry = null;
    private Map<String, FileOutputStream> fileOutputStreamMap = new HashMap<>();

    public RemoteFileServer(Registry registry) throws RemoteException {
        RemoteFileServer.registry = registry;
    }

    @Override
    public byte[] getFileChunk(String path, long position, int chunkSize) throws RemoteException {
        try (RandomAccessFile file = new RandomAccessFile(path, "r")) {
            file.seek(position);
            byte[] buffer = new byte[chunkSize];
            int bytesRead = file.read(buffer);
            if (bytesRead < chunkSize) {
                return Arrays.copyOf(buffer, bytesRead);
            }
            return buffer;
        } catch (IOException e) {
            throw new RemoteException("Error reading file", e);
        }
    }

    @Override
    public long getFileSize(String path) throws RemoteException {
        File file = new File(path);
        return file.length();
    }

    @Override
    public void startFileUpload(String path, long fileSize) throws RemoteException {
        try {
            File file = new File(path);
            FileOutputStream fos = new FileOutputStream(file);
            fileOutputStreamMap.put(path, fos);
        } catch (IOException e) {
            throw new RemoteException("Error opening file for writing", e);
        }
    }

    @Override
    public void uploadFileChunk(String path, byte[] chunk, int length) throws RemoteException {
        FileOutputStream fos = fileOutputStreamMap.get(path);
        if (fos == null) {
            throw new RemoteException("FileOutputStream not initialized for this file");
        }
        try {
            fos.write(chunk, 0, length);
        } catch (IOException e) {
            throw new RemoteException("Error writing file chunk", e);
        }
    }

    @Override
    public void completeFileUpload(String path) throws RemoteException {
        try {
            FileOutputStream fos = fileOutputStreamMap.remove(path);
            if (fos != null) {
                fos.close();
            }
        } catch (IOException e) {
            throw new RemoteException("Error closing file", e);
        }
    }

    @Override
    public boolean deleteFile(String path) {
        File file = new File(path);
        return file.delete();
    }

    @Override
    public String[] listContents(String path) {
        File directory = new File(path);
        return directory.list();
    }

    @Override
    public boolean createDirectory(String path) {
        File directory = new File(path);
        return directory.mkdir();
    }

    @Override
    public boolean deleteDirectory(String path) {
        File directory = new File(path);
        return directory.delete();
    }

    @Override
    public synchronized void shutdown() {
        try {
            registry.unbind("file-server");
            boolean isShutdown = false;
            while (!isShutdown) {
                isShutdown = UnicastRemoteObject.unexportObject(this, true);
            }
        } catch (RemoteException | NotBoundException e) {
            System.err.println("Exception unbinding Remote Object: " + e.getMessage());
        }
    }
}
