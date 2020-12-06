package cs451;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
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
        System.out.println("My id is " + id + ".");
        System.out.println("List of hosts is:");
        ArrayList<Host> hosts = new ArrayList<>(parser.hosts());
        for (Host h: hosts) {
            System.out.println(h.getId() + ", " + h.getIp() + ", " + h.getPort());
        }

        System.out.println("Barrier: " + parser.barrierIp() + ":" + parser.barrierPort());
        System.out.println("Signal: " + parser.signalIp() + ":" + parser.signalPort());
        System.out.println("Output: " + parser.output());
        // if config is defined; always check before parser.config()
        List<String> configuration = null;
        Set<Integer> causal = null;
        Integer n = 0;

        if (parser.hasConfig()) {
            System.out.println("Config: " + parser.config());
            try (Stream<String> s = Files.lines(Paths.get(parser.config()))) {
                configuration = s.collect(Collectors.toList());
            } catch (IOException e) {
                System.out.println("Configuration failed.");
            }
        }

        if (configuration != null) {
            n = Integer.parseInt(configuration.get(0));
            Integer configSize = configuration.size();
            if(configSize > 1) {
                for(String conf: configuration.subList(1, configSize)) {
                    String [] words = conf.split(" ");
                    if(Integer.parseInt(words[0]) == parser.myId()) {
                        causal = Arrays.stream(words).map(Integer::valueOf).collect(Collectors.toSet());
                        break;
                    }
                }
            }
        }

        Coordinator coordinator = new Coordinator(parser.myId(), parser.barrierIp(), parser.barrierPort(), parser.signalIp(), parser.signalPort());

        System.out.println("Waiting for all processes for finish initialization");
        coordinator.waitOnBarrier();

        for (Host h: hosts) {
            Integer hid = h.getId();
            if(hid == id) {
                Integer port = h.getPort();
                if(causal == null) { // FIFO
                    p = new Process(hid, hosts, port, n, parser.output());
                } else { // LC
                    p = new Process(hid, hosts, port, n, parser.output(), causal);
                    System.out.println(causal);
                }
            }
        }

        System.out.println("Broadcasting messages...");
        p.begin();

        System.out.println("Signaling end of broadcasting messages");
        coordinator.finishedBroadcasting();

        while (true) {
            // Sleep for 1 hour
            Thread.sleep(60 * 60 * 1000);
        }
    }
}
