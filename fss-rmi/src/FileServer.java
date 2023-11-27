import java.rmi.Remote;
import java.rmi.RemoteException;

public interface FileServer extends Remote {
    String FILE_NOT_EXIST_SERVER = "File/Directory does not exist on Server";
    String DIRECTORY_NOT_EXIST_SERVER = "Directory does not exist on Server";
    String DIRECTORY_NOT_CREATED_SERVER = "Directory could not be created";
    String FILE_DIRECTORY_NOT_DELETED_SERVER = "File/Directory could not be deleted";
    String FILE_SERVER_RMI = "file-server";
    byte[] getFileChunk(String path, long position, int chunkSize) throws RemoteException;
    long getFileSize(String path) throws RemoteException;
    void startFileUpload(String path, long fileSize, boolean isResume) throws RemoteException;
    void uploadFileChunk(String path, byte[] chunk, int length) throws RemoteException;
    void completeFileUpload(String path) throws RemoteException;
    String[] listContents(String path) throws RemoteException;
    void createDirectory(String path) throws RemoteException;
    void deleteFileOrDirectory(String path) throws RemoteException;
    void shutdown() throws RemoteException;

}
