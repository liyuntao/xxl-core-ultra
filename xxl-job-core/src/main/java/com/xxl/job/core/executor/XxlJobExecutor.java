package com.xxl.job.core.executor;

import com.xxl.job.core.biz.AdminBiz;
import com.xxl.job.core.biz.client.AdminBizClient;
import com.xxl.job.core.metrics.JobDurationRecorder;
import com.xxl.job.core.schedule.BackgroundTaskManager;
import com.xxl.job.core.schedule.XxlScheduler;
import com.xxl.job.core.server.EmbedServer;
import com.xxl.job.core.util.IpUtil;
import com.xxl.job.core.util.NetUtil;
import com.xxl.job.core.util.XxlAssert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class XxlJobExecutor  {
    private static final Logger logger = LoggerFactory.getLogger(XxlJobExecutor.class);

    // ---------------------- param ----------------------
    private final String adminAddresses;
    private final String accessToken;
    private final String appname;
    private final String address;
    private final String ip;
    private final int port;
//    private String mdcTraceKey = "TRACE_LOG_ID";
    private final JobDurationRecorder jobDurationRecorder;
    public final JobTaskManager taskManager;
    public final BackgroundTaskManager backgroundTaskManager;
    public final XxlScheduler xxlScheduler;

    protected XxlJobExecutor(Builder builder) {
        this.adminAddresses = builder.adminAddresses;
        this.accessToken = builder.accessToken;
        this.appname = builder.appname;

        this.ip = (builder.ip != null && builder.ip.trim().length() > 0) ? builder.ip : IpUtil.getIp();
        this.port = builder.port > 0 ? builder.port : NetUtil.findAvailablePort(9999);

        // generate address
        if (builder.address == null || builder.address.trim().length() == 0) {
            String ip_port_address = IpUtil.getIpPort(ip, port);
            // registry-address：default use address to registry , otherwise use ip:port if address is null
            this.address = "http://{ip_port}/".replace("{ip_port}", ip_port_address);
        } else {
            this.address = builder.address;
        }

        if (builder.jobDurationRecorder != null) {
            this.jobDurationRecorder = builder.jobDurationRecorder;
        } else {
            this.jobDurationRecorder = JobDurationRecorder.disabled();
        }

        this.backgroundTaskManager = new BackgroundTaskManager(this.appname, this.address);
        this.xxlScheduler = new XxlScheduler(2, "xxl-schedule-");
        this.taskManager = new JobTaskManager(this.xxlScheduler, this.jobDurationRecorder);
    }

    public JobDurationRecorder getJobDurationRecorder() {
        return jobDurationRecorder;
    }

    public static Builder builder() {
        return new XxlJobExecutor.Builder();
    }

    public static class Builder {
        private String adminAddresses;
        private String accessToken;
        private String appname;
        private String address;
        private String ip;
        private int port;
        private JobDurationRecorder jobDurationRecorder;

        private Builder() {
        }

        public Builder adminAddresses(String adminAddresses) {
            XxlAssert.notEmpty(adminAddresses, "adminAddresses must not be empty");
            this.adminAddresses = adminAddresses;
            return this;
        }

        public Builder accessToken(String accessToken) {
            this.accessToken = accessToken;
            return this;
        }

        public Builder appname(String appname) {
            XxlAssert.notEmpty(adminAddresses, "adminAddresses must not be empty");
            this.appname = appname;
            return this;
        }

        public Builder address(String address) {
            this.address = address;
            return this;
        }

        public Builder ip(String ip) {
            this.ip = ip;
            return this;
        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public Builder jobDurationCollector(JobDurationRecorder jobDurationRecorder) {
            this.jobDurationRecorder = jobDurationRecorder;
            return this;
        }

        public XxlJobExecutor build() {
            return new XxlJobExecutor(this);
        }

        public XxlJobSpringExecutor buildSpringExecutor() {
            return new XxlJobSpringExecutor(this);
        }
    }


    // ---------------------- start + stop ----------------------
    public void start() throws Exception {
        // init config & static
        initAdminBizList(this.adminAddresses, this.accessToken);
        // init server
        initEmbedServer(port, accessToken);

        xxlScheduler.startup();
        backgroundTaskManager.execRegister();
        xxlScheduler.schedule("xxl-reg", backgroundTaskManager::execRegister, 30, 30, TimeUnit.SECONDS);
        xxlScheduler.schedule("xxl-callback", backgroundTaskManager::syncJobResultToAdmin, 10, 5, TimeUnit.SECONDS);
    }

    public void destroy() {
        try {
            xxlScheduler.shutdown();
        } catch (InterruptedException e) {
        }
        backgroundTaskManager.execUnRegister();
        stopEmbedServer();
    }

    // ---------------------- admin-client (rpc invoker) ----------------------
    private static final List<AdminBiz> adminBizList = new ArrayList<>(2);

    private void initAdminBizList(String adminAddresses, String accessToken) {
        if (adminAddresses != null && adminAddresses.trim().length() > 0) {
            for (String address : adminAddresses.trim().split(",")) {
                if (address != null && address.trim().length() > 0) {
                    AdminBiz adminBiz = new AdminBizClient(address.trim(), accessToken);
                    adminBizList.add(adminBiz);
                }
            }
        }
    }

    public static List<AdminBiz> getAdminBizList(){
        return adminBizList;
    }

    // ---------------------- executor-server (rpc provider) ----------------------
    private EmbedServer embedServer = null;

    private void initEmbedServer(int port, String accessToken) throws Exception {
        // accessToken
        if (accessToken == null || accessToken.trim().length() == 0) {
            logger.warn(">>>>>>>>>>> xxl-job accessToken is empty. To ensure system security, please set the accessToken.");
        }

        // start
        embedServer = new EmbedServer(this);
        embedServer.startAsync(port, accessToken);
    }

    private void stopEmbedServer() {
        // stop provider factory
        if (embedServer != null) {
            try {
                embedServer.stop();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

}
