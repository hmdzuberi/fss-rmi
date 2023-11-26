import java.rmi.Remote;
import java.rmi.RemoteException;

public interface FileServer extends Remote {
    byte[] getFileChunk(String path, long position, int chunkSize) throws RemoteException;
    long getFileSize(String path) throws RemoteException;
    void startFileUpload(String path, long fileSize, boolean isResume) throws RemoteException;
    void uploadFileChunk(String path, byte[] chunk, int length) throws RemoteException;
    void completeFileUpload(String path) throws RemoteException;
    boolean deleteFile(String path) throws RemoteException;
    String[] listContents(String path) throws RemoteException;
    boolean createDirectory(String path) throws RemoteException;
    boolean deleteDirectory(String path) throws RemoteException;
    void shutdown() throws RemoteException;

}
