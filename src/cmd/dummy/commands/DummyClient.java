package dummy.commands;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.apache.log4j.Level;

import autils.Logger;
import common.error;
import io.netty.util.concurrent.GlobalEventExecutor;
import lachesis.ConfigUtils;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;


@Command(name = "dummy", header = "%n@|Dummy Socket Client for Lachesis|@")
public class DummyClient implements Runnable {

	@Option(names = {"dummy"}, description="Dummy Socket Client for Lachesis")
	private String appName = "dummy";
	CLIConfig config = new CLIConfig();

	@Option(names = { "-n", "--name"}, description= "Client name")
    private String name;

    @Option(names = {"--proxyaddr"}, description= "IP:Port to bind Proxy Serve")
    private String clientAddr = config.ClientAddr;

    @Option(names = {"-clientaddr"}, description= "IP:Port to connect to Lachesis proxy")
    private String proxyAddr = config.ProxyAddr;

    @Option(names = {"--log"}, description= "debug, info, warn, error, fatal, panic")
    private String log = config.LogLevel;

	public void print() {
		System.out.println("Options:");
        System.out.println("          name: " + this.name);
        System.out.println("  clientListen: " + this.clientAddr);
        System.out.println("  proxyConnect: " + this.proxyAddr);
        System.out.println("           log: " + this.log);
	}

	public static void main(String[] args) {
		Object exec = GlobalEventExecutor.INSTANCE;
        CommandLine.run(new DummyClient(), args);
    }

//	public static void disableWarning() {
//	    try {
//	        Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
//	        theUnsafe.setAccessible(true);
//	        Unsafe u = (Unsafe) theUnsafe.get(null);
//
//	        Class cls = Class.forName("jdk.internal.module.IllegalAccessLogger");
//	        Field logger = cls.getDeclaredField("logger");
//	        u.putObjectVolatile(cls, u.staticFieldOffset(logger), null);
//	    } catch (Exception e) {
//	        // ignore
//	    }
//	}


	public void run() {
		Logger logger = newLogger();
		logger.setLevel(ConfigUtils.LogLevel(this.log));

		String name = this.name;
		String address = this.proxyAddr;

		logger.field("name", name)
			.field("proxy_addr", address)
			.debug("RUN");

		// Create and run Dummy Socket Client
		dummy.DummyClient client = dummy.DummyClient.NewDummySocketClient(address, logger);

		// Listen for input messages from tty
		error err;
		Scanner scanner = new Scanner(System.in);

		while (scanner.hasNext()) {
			System.out.print("Enter your text: ");
			String text = scanner.nextLine();
			String message = String.format("%s: %s", name, text);
			err = client.SubmitTx(message.getBytes());
			if (err != null) {
				System.err.printf("Error in SubmitTx: %v\n", err);
			}
		}
	}

	public Logger newLogger() {
		Logger logger = Logger.getLogger(DummyClient.class);
		Map<Level,String> pathMap = new HashMap<Level,String>();

		OutputStream out;
		try {
			out = Files.newOutputStream(Paths.get("info.log"), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
		} catch (IOException e) {
			logger.info("Failed to open info.log file, using default stderr");
		}
		pathMap.put(Level.INFO, "info.log");

		try {
			out = Files.newOutputStream(Paths.get("debug.log"), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
		} catch (IOException e) {
			logger.info("Failed to open debug.log file, using default stderr");
		}
		pathMap.put(Level.DEBUG, "debug.log");

//		if (err == null) {
//			logger.Out = ioutil.Discard;
//		}
		return logger;
	}
}
