package com.xxl.job.core.util;

import com.xxl.job.core.biz.model.JobResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CompletableFuture;

import static java.time.temporal.ChronoUnit.SECONDS;


public class AsyncHttpUtil {
    private static final Logger logger = LoggerFactory.getLogger(AsyncHttpUtil.class);
    public static final String XXL_JOB_ACCESS_TOKEN = "XXL-JOB-ACCESS-TOKEN";
    private static final HttpClient newHttpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.of(3, SECONDS) )
            .version(HttpClient.Version.HTTP_1_1)
            .build();

    public static CompletableFuture<JobResponseEntity> postBody(String url,
                                                                String accessToken,
                                                                int timeout,
                                                                Object requestObj) {
        XxlAssert.notNull(requestObj, "reqBody cannot null");

        try {
            var reqBuilder = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .header("Content-Type", "application/json;charset=UTF-8")
                    .header("Accept-Charset", "application/json;charset=UTF-8")
                    .timeout(Duration.of(timeout, SECONDS));

            if (accessToken != null && accessToken.trim().length() > 0) {
                reqBuilder.header(XXL_JOB_ACCESS_TOKEN, accessToken);
            }

            String requestBody = GsonTool.toJson(requestObj);

            var httpRequest = reqBuilder
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();
            return newHttpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        if (response.statusCode() == 200) {
                            return new JobResponseEntity(200, response.body());
                        } else {
                            return new JobResponseEntity(JobResponseEntity.FAIL_CODE,
                                    "xxl-job remoting fail, StatusCode(" + response.statusCode() + ") invalid. for url : " + url);
                        }
                    });
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            var errRtn = new JobResponseEntity(JobResponseEntity.FAIL_CODE, "xxl-job remoting error(" + e.getMessage() + "), for url : " + url);
            return CompletableFuture.completedFuture(errRtn);
        }
    }
}
