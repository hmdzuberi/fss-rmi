import java.io.File;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IFileServer extends Remote {

    public File download(String path) throws RemoteException;
    public void upload(File file, String path) throws RemoteException;
    public boolean deleteFile(String path) throws RemoteException;
    public String[] listContents(String path) throws RemoteException;
    public boolean createDirectory(String path) throws RemoteException;
    public boolean deleteDirectory(String path) throws RemoteException;
    public void shutdown() throws RemoteException;

}
