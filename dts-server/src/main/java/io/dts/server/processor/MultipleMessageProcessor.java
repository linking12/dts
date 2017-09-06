package io.dts.server.processor;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Queues;

import io.dts.common.ThreadFactoryImpl;
import io.dts.remoting.netty.NettyRequestProcessor;
import io.dts.remoting.protocol.RemotingCommand;
import io.dts.server.TcpServerController;
import io.dts.server.TcpServerProperties;
import io.netty.channel.ChannelHandlerContext;

/**
 * 
 * @author liushiming
 * @version GetProcessor.java, v 0.0.1 2017年9月6日 下午12:11:38 liushiming
 */
@SuppressWarnings("unused")
public class MultipleMessageProcessor implements NettyRequestProcessor {
  private static final Logger logger = LoggerFactory.getLogger("HybridServer");

  private TcpServerController serverController;

  private final ExecutorService getProcessorExecutor;

  public MultipleMessageProcessor(TcpServerController serverController,
      TcpServerProperties properties) {
    this.serverController = serverController;
    BlockingQueue<Runnable> workerThreadPoolQueue =
        Queues.newLinkedBlockingDeque(properties.getQueryThreadPoolQueueSize());
    this.getProcessorExecutor = new ThreadPoolExecutor(//
        properties.getQueryThreadPoolSize(), //
        properties.getQueryThreadPoolSize(), //
        1000 * 60, //
        TimeUnit.MILLISECONDS, //
        workerThreadPoolQueue, //
        new ThreadFactoryImpl("AddProcessorThread_"));
  }

  public ExecutorService getThreadPool() {
    return getProcessorExecutor;
  }

  @Override
  public RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand request)
      throws Exception {
    return null;
  }


}
