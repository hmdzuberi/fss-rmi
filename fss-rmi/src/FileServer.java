import java.io.File;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class FileServer implements IFileServer {

    private static Registry registry = null;

    public FileServer(Registry registry) throws RemoteException {
        FileServer.registry = registry;
    }

    @Override
    public File download(String path) {
        return null;
    }

    @Override
    public void upload(File file, String path) {

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
            boolean shutdown = false;
            while (!shutdown) {
                shutdown = UnicastRemoteObject.unexportObject(this, true);
            }
        } catch (RemoteException | NotBoundException e) {
            System.err.println("Exception unbinding Remote Object: " + e.getMessage());
        }
    }
}
