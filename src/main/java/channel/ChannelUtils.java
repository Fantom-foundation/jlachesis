package channel;

import org.jcsp.lang.One2OneChannel;
import org.jcsp.lang.One2OneChannelInt;

import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

public class ChannelUtils {
	static {
		ChannelGroup allChannels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
	}

	public static <T>  void close(One2OneChannel<T> channel) {
		// TODO:

		// need to close / shutdown the channel
	}

	public static <T>  void close(One2OneChannelInt channel) {
		// TODO:

		// need to close / shutdown the channel
	}

	public static void run(Runnable run) {
		ExecService.go(() -> run.run());
//		new	org.jcsp.lang.CSProcess() {
//			@Override
//			public void run() {
//				run.run();
//			}
//		}.run();
	}
}
