package com.turn.browser.utils;

import com.alibaba.fastjson.JSON;
import com.turn.browser.exception.HttpRequestException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @Description:
 */
@Slf4j
public class HttpUtil {
    private HttpUtil(){}

    private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(1024);

    public static <T> CompletableFuture<T> postAsync(String url, String param, Class<T> clazz){
        CompletableFuture<T> future = new CompletableFuture<>();
        EXECUTOR.submit(()->{
            try {
                T result = post(url,param,clazz);
                future.complete(result);
            } catch (HttpRequestException e) {
                log.error("Error in query[{}]!",url);
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    public static <T> CompletableFuture<T> getAsync(String url,Class<T> clazz){
        CompletableFuture<T> future = new CompletableFuture<>();
        EXECUTOR.submit(()->{
            try {
                T result = get(url,clazz);
                future.complete(result);
            } catch (HttpRequestException e) {
                log.error("There was an error in query [{}], will try again!",url);
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    /**
     * Send POST request
     * @param url
     * @param param
     * @param clazz
     * @param <T>
     * @return
     * @throws HttpRequestException
     */
    public static  <T> T post(String url,String param,Class<T> clazz) throws HttpRequestException {
        MediaType mediaType = MediaType.parse("application/json;charset=UTF-8");
        Request request = new Request.Builder().post(RequestBody.create(mediaType, param)).url(url).build();
        return resolve(request,url,clazz);
    }

    /**
     * Send GET request
     * @param url
     * @param clazz
     * @param <T>
     * @return
     * @throws HttpRequestException
     */
    public static <T> T get(String url,Class<T> clazz) throws HttpRequestException {
        Request request = new Request.Builder().url(url).build();
        return resolve(request,url,clazz);
    }

    /**
     * Parse results
     * @param request
     * @param url
     * @param clazz
     * @param <T>
     * @return
     * @throws HttpRequestException
     */
    @SuppressWarnings("unchecked")
	private static <T> T resolve(Request request,String url,Class<T> clazz) throws HttpRequestException {
        Response response=null;
        try {
            response = CLIENT.newCall(request).execute();
            if(response.isSuccessful()){
                String res = Objects.requireNonNull(response.body()).string();
                res = res.replace("\n","");
                if(clazz==String.class) return (T)res;
                return JSON.parseObject(res,clazz);
            }else{
                throw new HttpRequestException("Request address ["+url+"] failed: "+response.message());
            }
        } catch (IOException e) {
            throw new HttpRequestException("Error in request address ["+url+"]: "+e.getMessage());
        } finally {
            if(response!=null){
                response.close();
            }
        }
    }
}
