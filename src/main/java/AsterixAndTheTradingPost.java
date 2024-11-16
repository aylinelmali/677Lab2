import peer.IPeer;
import utils.Logger;
import utils.TraderState;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class AsterixAndTheTradingPost {

    public static int REGISTRY_ID = 1099;
    public static String CLASS_PATH = "build/classes/java/main";
    public static String BUYER_CLASS = "peer.Buyer";
    public static String SELLER_CLASS = "peer.Seller";

    public static void main(String[] args) throws IOException, InterruptedException, NotBoundException {

        TraderState.resetTraderState();

        int n = Integer.parseInt(args[0]);  // Number of peers

        Registry registry = LocateRegistry.createRegistry(REGISTRY_ID);

        Process[] processes = new Process[n];

        // initialize all peers
        for (int i = 0; i < n; i++) {

            ProcessBuilder processBuilder = new ProcessBuilder(
                    "java",
                    "-cp",
                    CLASS_PATH,
                    i % 2 == 0 ? BUYER_CLASS : SELLER_CLASS,
                    "" + i,
                    "" + n
            );
            Process process = processBuilder.start();
            var inputStream = process.getInputStream();
            var outputStream = System.out;
            processes[i] = process;

            // redirect output stream of peer process to main process
            new Thread(() -> {
                try (inputStream; outputStream) {
                    int byteData;
                    while ((byteData = inputStream.read()) != -1) {
                        outputStream.write(byteData);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

            // destroy process when stopping program
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (process.isAlive()) {
                    process.destroy();
                }
            }));
        }

        Thread.sleep(1000); // ensure that all peers are bound

        // retrieve proxies
        IPeer[] peers = new IPeer[n];
        for (int i = 0; i < n; i++) {
            peers[i] = (IPeer) registry.lookup("" + i);
        }

        Logger.log("########## START INITIAL SETUP ##########");
        for (int i = 0; i < n; i++) {
            peers[i].start();
        }
        Logger.log("########### END INITIAL SETUP ###########");

        // do initial election
        peers[0].election(new int[] {});

        for (int i = 0; i < n; i++) {
            processes[i].waitFor();
        }
    }
}
