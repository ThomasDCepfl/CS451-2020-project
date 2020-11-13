package cs451;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {

    private static Process p;

    private static void handleSignal() {
        //immediately stop network packet processing
        System.out.println("Immediately stopping network packet processing.");
        p.end();

        //write/flush output file if necessary
        System.out.println("Writing output.");
        p.write();
    }

    private static void initSignalHandlers() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                handleSignal();
            }
        });
    }

    public static void main(String[] args) throws InterruptedException {
        Parser parser = new Parser(args);
        parser.parse();

        initSignalHandlers();

        // example
        long pid = ProcessHandle.current().pid();
        System.out.println("My PID is " + pid + ".");
        System.out.println("Use 'kill -SIGINT " + pid + " ' or 'kill -SIGTERM " + pid + " ' to stop processing packets.");

        Integer id = parser.myId();
        System.out.println("My id is " + parser.myId() + ".");
        System.out.println("List of hosts is:");


        System.out.println("Barrier: " + parser.barrierIp() + ":" + parser.barrierPort());
        System.out.println("Signal: " + parser.signalIp() + ":" + parser.signalPort());
        System.out.println("Output: " + parser.output());
        // if config is defined; always check before parser.config()
        List<String> configuration = null;
        Integer n = 0;
        if (parser.hasConfig()) {
            System.out.println("Config: " + parser.config());
            try (Stream<String> s = Files.lines(Paths.get(parser.config()))) {
                configuration = s.collect(Collectors.toList());
                if (configuration != null) n = Integer.parseInt(configuration.get(0));
            } catch (IOException e) {
                System.out.println("Configuration failed.");
            }
        }


        Coordinator coordinator = new Coordinator(parser.myId(), parser.barrierIp(), parser.barrierPort(), parser.signalIp(), parser.signalPort());

        System.out.println("Waiting for all processes for finish initialization");
        coordinator.waitOnBarrier();

        ArrayList<Host> hosts = new ArrayList<>(parser.hosts());
        for (Host host: hosts) {
            System.out.println(host.getId() + ", " + host.getIp() + ", " + host.getPort());
            Integer hid = host.getId();
            if(hid == id) {
                Integer port = host.getPort();
                InetAddress ip = null;
                try {
                    ip = InetAddress.getByName(host.getIp());
                } catch (UnknownHostException e) {
                    System.out.println("Couldn't find IP address");
                }
                p = new Process(hid, hosts, ip, port, n, parser.output());
            }
        }

        p.begin();

        System.out.println("Signaling end of broadcasting messages");
        coordinator.finishedBroadcasting();

        while (true) {
            // Sleep for 1 hour
            Thread.sleep(60 * 60 * 1000);
        }
    }
}
