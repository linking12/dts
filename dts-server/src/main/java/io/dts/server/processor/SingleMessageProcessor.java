package io.dts.server.processor;

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
 * @version AddProcessor.java, v 0.0.1 2017年9月6日 上午11:36:12 liushiming
 */
@SuppressWarnings("unused")
public class SingleMessageProcessor implements NettyRequestProcessor {

  private static final Logger logger = LoggerFactory.getLogger(SingleMessageProcessor.class);

  private final ExecutorService addProcessorExecutor;

  private final TcpServerController serverController;

  public SingleMessageProcessor(TcpServerController serverController,
      TcpServerProperties properties) {
    this.serverController = serverController;
    this.addProcessorExecutor = new ThreadPoolExecutor(//
        properties.getWriteThreadPoolQueueSize(), //
        properties.getWriteThreadPoolQueueSize(), //
        1000 * 60, //
        TimeUnit.MILLISECONDS, //
        Queues.newLinkedBlockingDeque(properties.getWriteThreadPoolQueueSize()), //
        new ThreadFactoryImpl("AddProcessorThread_"));
  }

  public ExecutorService getThreadPool() {
    return addProcessorExecutor;
  }

  @Override
  public RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand request)
      throws Exception {
    return null;
  }

}
