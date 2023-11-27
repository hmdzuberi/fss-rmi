import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class RemoteFileServer implements FileServer {
    private final Map<String, FileOutputStream> fileOutputStreamMap = new HashMap<>();

    public RemoteFileServer() throws RemoteException {
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
        if (!file.isFile()) {
            throw new RemoteException(FILE_NOT_EXIST_SERVER);
        }
        return file.length();
    }

    @Override
    public void startFileUpload(String path, long fileSize, boolean isResume) throws RemoteException {
        try {
            File file = new File(path);
            FileOutputStream fos;
            fos = isResume ? new FileOutputStream(file, true) : new FileOutputStream(file);
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
            fos.flush();
        } catch (IOException e) {
            throw new RemoteException("Error writing file chunk", e);
        }
    }


    @Override
    public void completeFileUpload(String path) throws RemoteException {
        try {
            FileOutputStream fos = fileOutputStreamMap.remove(path);
            if (fos != null) {
                fos.flush();
                fos.close();
            }
        } catch (IOException e) {
            throw new RemoteException("Error closing file", e);
        }
    }

    @Override
    public String[] listContents(String path) throws RemoteException {
        File directory = new File(path);
        if (!directory.exists()) {
            throw new RemoteException(FileServer.DIRECTORY_NOT_EXIST_SERVER);
        }
        return directory.list();
    }

    @Override
    public void createDirectory(String path) throws RemoteException {
        File directory = new File(path);
        boolean success = directory.mkdir();
        if (!success) {
            throw new RemoteException(FileServer.DIRECTORY_NOT_CREATED_SERVER);
        }
    }

    @Override
    public void deleteFileOrDirectory(String path) throws RemoteException {
        File directory = new File(path);
        boolean success = directory.delete();
        if (!success) {
            throw new RemoteException(FileServer.FILE_DIRECTORY_NOT_DELETED_SERVER);
        }
    }

    @Override
    public void shutdown() {
        Server.setShutdown(true);
    }
}
