package ru.greatstep.exceltosqlconverter.utils;

import static io.netty.handler.codec.http.HttpHeaders.Values.APPLICATION_JSON;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.TEXT_HTML;
import static ru.greatstep.exceltosqlconverter.utils.WebClientUtil.X_REQUEST_ID;
import static ru.greatstep.exceltosqlconverter.utils.WebClientUtil.headers;
import static ru.greatstep.exceltosqlconverter.utils.WebClientUtil.uriBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Logger;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.Builder;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.DefaultUriBuilderFactory.EncodingMode;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class WebClientHelper {

    private final ObjectMapper objectMapper;
    private final Logger logger;

    public WebClient getWebClient(String url) {
        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(url);
        factory.setEncodingMode(EncodingMode.NONE);
        return getBuilder()
                .uriBuilderFactory(factory)
                .build();
    }

    public Builder getBuilder() {
        return WebClient.builder()
                .codecs(clientDefaultCodecsConfigurer -> {
                    clientDefaultCodecsConfigurer.defaultCodecs()
                            .jackson2JsonEncoder(new Jackson2JsonEncoder(objectMapper, MediaType.APPLICATION_JSON));
                    clientDefaultCodecsConfigurer.defaultCodecs()
                            .jackson2JsonDecoder(new Jackson2JsonDecoder(objectMapper, MediaType.APPLICATION_JSON));
                })
                .exchangeStrategies(exchangeStrategies());
    }

    private ExchangeStrategies exchangeStrategies() {
        return ExchangeStrategies
                .builder()
                .codecs(configurer -> {
                    configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024);
                    configurer.customCodecs().register(new Jackson2JsonEncoder(new ObjectMapper(), TEXT_HTML));
                    configurer.customCodecs().register(new Jackson2JsonDecoder(new ObjectMapper(), TEXT_HTML));
                })
                .build();
    }

    public <T> Mono<T> webClientGetRequest(String url,
                                           String urlPath,
                                           Map<String, String> headers,
                                           List<String> pathVariables,
                                           Map<String, Object> params,
                                           Class<T> responseClass) {
        return getWebClient(url).get().uri(uriBuilder(urlPath, pathVariables, params)).headers(headers(headers))
                .header(CONTENT_TYPE, APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::isError, WebClientUtil::createFacadeWebClientException)
                .bodyToMono(responseClass)
                .doOnSuccess(httpSuccessConsumer(headers.get(X_REQUEST_ID)));
    }

    public <T> Mono<ResponseEntity<T>> webClientGetRequestToEntity(String url,
                                                                   String urlPath,
                                                                   Map<String, String> headers,
                                                                   List<String> pathVariables,
                                                                   Map<String, Object> params,
                                                                   Class<T> responseClass) {
        return getWebClient(url).get().uri(uriBuilder(urlPath, pathVariables, params)).headers(headers(headers))
                .header(CONTENT_TYPE, APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::isError, WebClientUtil::createFacadeWebClientException)
                .toEntity(responseClass)
                .doOnSuccess(httpSuccessConsumer(headers.get(X_REQUEST_ID)));
    }

    public <U, T> Mono<T> webClientPostRequest(String url,
                                               String urlPath,
                                               Map<String, String> headers,
                                               List<String> pathVariables,
                                               Map<String, Object> params,
                                               U request,
                                               Class<U> requestClass,
                                               Class<T> responseClass) {
        return getWebClient(url).post().uri(uriBuilder(urlPath, pathVariables, params)).headers(headers(headers))
                .header(CONTENT_TYPE, APPLICATION_JSON)
                .body(Mono.just(request), requestClass)
                .retrieve()
                .onStatus(HttpStatusCode::isError, WebClientUtil::createFacadeWebClientException)
                .bodyToMono(responseClass)
                .doOnSuccess(httpSuccessConsumer(headers.get(X_REQUEST_ID)));
    }

    public <U, T> Mono<ResponseEntity<T>> webClientPostRequestToEntity(String url,
                                                                       String urlPath,
                                                                       Map<String, String> headers,
                                                                       List<String> pathVariables,
                                                                       Map<String, Object> params,
                                                                       U request,
                                                                       Class<U> requestClass,
                                                                       Class<T> responseClass) {
        return getWebClient(url).post().uri(uriBuilder(urlPath, pathVariables, params)).headers(headers(headers))
                .header(CONTENT_TYPE, APPLICATION_JSON)
                .body(Mono.just(request), requestClass)
                .retrieve()
                .onStatus(HttpStatusCode::isError, WebClientUtil::createFacadeWebClientException)
                .toEntity(responseClass)
                .doOnSuccess(httpSuccessConsumer(headers.get(X_REQUEST_ID)));
    }

    public <U, T> Mono<T> webClientPutRequest(String url,
                                              String urlPath,
                                              Map<String, String> headers,
                                              List<String> pathVariables,
                                              Map<String, Object> params,
                                              U request,
                                              Class<U> requestClass,
                                              Class<T> responseClass) {
        return getWebClient(url).put().uri(uriBuilder(urlPath, pathVariables, params)).headers(headers(headers))
                .header(CONTENT_TYPE, APPLICATION_JSON)
                .body(Mono.just(request), requestClass)
                .retrieve()
                .onStatus(HttpStatusCode::isError, WebClientUtil::createFacadeWebClientException)
                .bodyToMono(responseClass)
                .doOnSuccess(httpSuccessConsumer(headers.get(X_REQUEST_ID)));
    }

    public <U, T> Mono<ResponseEntity<T>> webClientPutRequestToEntity(String url,
                                                                      String urlPath,
                                                                      Map<String, String> headers,
                                                                      List<String> pathVariables,
                                                                      Map<String, Object> params,
                                                                      U request,
                                                                      Class<U> requestClass,
                                                                      Class<T> responseClass) {
        return getWebClient(url).put().uri(uriBuilder(urlPath, pathVariables, params)).headers(headers(headers))
                .header(CONTENT_TYPE, APPLICATION_JSON)
                .body(Mono.just(request), requestClass)
                .retrieve()
                .onStatus(HttpStatusCode::isError, WebClientUtil::createFacadeWebClientException)
                .toEntity(responseClass)
                .doOnSuccess(httpSuccessConsumer(headers.get(X_REQUEST_ID)));
    }

    public <U, T> Mono<T> webClientPatchRequest(String url,
                                                String urlPath,
                                                Map<String, String> headers,
                                                List<String> pathVariables,
                                                Map<String, Object> params,
                                                U request,
                                                Class<U> requestClass,
                                                Class<T> responseClass) {
        return getWebClient(url).patch().uri(uriBuilder(urlPath, pathVariables, params)).headers(headers(headers))
                .header(CONTENT_TYPE, APPLICATION_JSON)
                .body(Mono.just(request), requestClass)
                .retrieve()
                .onStatus(HttpStatusCode::isError, WebClientUtil::createFacadeWebClientException)
                .bodyToMono(responseClass)
                .doOnSuccess(httpSuccessConsumer(headers.get(X_REQUEST_ID)));
    }

    public <U, T> Mono<ResponseEntity<T>> webClientPatchRequestToEntity(String url,
                                                                        String urlPath,
                                                                        Map<String, String> headers,
                                                                        List<String> pathVariables,
                                                                        Map<String, Object> params,
                                                                        U request,
                                                                        Class<U> requestClass,
                                                                        Class<T> responseClass) {
        return getWebClient(url).patch().uri(uriBuilder(urlPath, pathVariables, params)).headers(headers(headers))
                .header(CONTENT_TYPE, APPLICATION_JSON)
                .body(Mono.just(request), requestClass)
                .retrieve()
                .onStatus(HttpStatusCode::isError, WebClientUtil::createFacadeWebClientException)
                .toEntity(responseClass)
                .doOnSuccess(httpSuccessConsumer(headers.get(X_REQUEST_ID)));
    }

    private Consumer<Object> httpSuccessConsumer(String xRequestId) {
        return response -> logger
                .info(String.format("Successfully received response x-request-id = %s, response = %s",
                        xRequestId, response));
    }

}
