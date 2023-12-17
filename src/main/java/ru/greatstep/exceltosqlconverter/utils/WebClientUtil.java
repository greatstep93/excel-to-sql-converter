package ru.greatstep.exceltosqlconverter.utils;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.boot.web.server.WebServerException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import ru.greatstep.exceltosqlconverter.exception.FacadeWebClientException;
import ru.greatstep.exceltosqlconverter.exception.ValidationException;
import ru.greatstep.exceltosqlconverter.exception.WebClientException;
import ru.greatstep.exceltosqlconverter.models.ErrorResponse;
import ru.greatstep.exceltosqlconverter.models.FacadeErrorResponse;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class WebClientUtil {

    public static final String X_REQUEST_ID = "X-Request-Id";
    public static final String X_REQUEST_ID_V2 = "x-request-id";
    public static final String X_USER_ID = "X-User-Id";
    public static final String X_USER_NAME = "X-User-Name";
    public static final String X_USER_DEPARTMENT_GUID = "X-User-Department-Guid";
    public static final String X_API_KEY = "X-API-Key";
    public static final String X_FUNCTIONS = "X-Functions";
    public static final String LOGIN_AMDOCS = "X-Login-Amdocs";
    public static final String LOGIN_AMDOCS_V2 = "x-login-amdocs";
    public static final String X_LOGIN_CRMB2C = "x-login-crmb2c";
    public static final String X_SSO_ID = "x-sso-id";
    public static final String CONTACT_GUID = "contactGuid";
    public static final String CUSTOMER_ID = "customerId";
    public static final String CLIENT_ID = "clientId";
    public static final String OFFSET = "offset";
    public static final String COUNT = "count";
    public static final String PARENT_SERVICE_ID = "parentServiceId";
    public static final String IS_MAIN = "isMain";
    public static final String FIN_ACCOUNT_NUMBER = "finAccountNumber";
    public static final String FIN_ACCOUNT_SYS_INST = "finAccountSysInstId";
    public static final String DATE_START = "dateStart";
    public static final String DATE_END = "dateEnd";
    public static final String IS_NEED_PRODUCT = "isNeedProduct";
    public static final String SERVICE_NUMBER = "serviceNumber";
    public static final String SERVICE_EXT_ID = "serviceExtId";
    public static final String SERVICE_LOGIN = "serviceLogin";
    public static final String MRF_NAME = "mrfName";
    public static final String IS_REFRESH = "isRefresh";
    public static final String FIN_ACCOUNT_PAY_METHOD = "finAccountPayMethod";
    public static final String CONTACT_ID = "contactId";
    public static final String INTERACTION_NUMBER = "interactionNumber";
    public static final String TOPIC_GUID = "topicGuid";
    public static final String TOPICS = "topics";
    public static final String FIN_ACCOUNT_EXT_ID = "finAccountExtId";
    public static final String FIN_ACCOUNTS = "finAccounts";
    public static final String CASE_NUMBER = "caseNumber";
    public static final String CASE_CHANGE_STATUS = "changeStatus";
    public static final String SUBMIT = "submit";
    public static final String COMMUNICATION = "communication";
    public static final String WFM = "wfm";
    public static final String COMMENTS = "comments";
    public static final String CALCULATE_PRIORITY = "calculatePriority";
    public static final String MODE = "mode";
    public static final String AVAILABLE_ACTION_TYPE = "/availableActionType";
    public static final String FACADE = "/facade";
    public static final String CASES = "/cases";
    public static final String CASE_DICTIONARIES = "/dictionaries";
    public static final String CASE_IMPLEMENTER = "/implementer";
    public static final String CASE_IMPLEMENTER_SINGLE = "/single";
    public static final String CASE_IMPLEMENTER_TERRITORY = "/territory";
    public static final String CASE_IMPLEMENTER_GUID = "territoryGuid";
    public static final String CASE_EXTERNAL_SEND_ONLINE = "externalSendOnline";
    public static final String CASE_GENERATE_NUMBER = "generateCaseNumber";
    public static final String CASE_EXTERNAL_SYSTEM_INFO = "externalSystemInfo";
    public static final String CASE_EXTERNAL_SYSTEM_COMMENT = "externalSystemComment";
    public static final String CASE_SEARCH = "search";
    public static final String CASE_PREVIEW = "preview";
    public static final String CASE_HISTORY = "history";
    public static final String INTERACTIONS = "/interactions";
    public static final String SHORT_FINANCE = "/shortFinance";
    public static final String RECEIVABLE = "/receivable";
    public static final String FINAL_PAYMENT = "/final-payment";
    public static final String INFO_FINANCE = "/infoFinance";
    public static final String ACCRUALS = "/accruals";
    public static final String PAYMENTS = "/payments";
    public static final String PENALTIES = "/penalties";
    public static final String BLOCK_HISTORY = "/blockHistory";
    public static final String LINKS = "/links";
    public static final String CLIENTS = "/clients/";
    public static final String EQUIPMENT = "/equipment";
    public static final String PHONE_NUMBER_PARAMETER_NAME = "phoneNumber";
    public static final String SEND_MESSAGE = "sendMessage";

    public static final String ISO_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssXXX";
    public static final String LOCAL_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String BIRTHDAY_DATE_FORMAT = "yyyy-MM-dd";

    public static final String ERROR_MAP_KEY = "error";
    public static final String MESSAGE_MAP_KEY = "message";
    public static final String STACK_TRACE_MAP_KEY = "stackTrace";
    public static final String STATUS_MAP_KEY = "status";

    public static <T> Mono<ResponseEntity<T>> responseEntity(Mono<T> mono, String xRequestId) {
        return mono.flatMap(m -> Mono.just(ResponseEntity.ok().header(X_REQUEST_ID, xRequestId).body(m)));
    }

    public static Mono<ResponseEntity<Void>> bodilessResponseEntity(Mono<?> mono, String xRequestId) {
        return mono.thenReturn(ResponseEntity.ok().header(X_REQUEST_ID, xRequestId).build());
    }

    public static <T> Mono<T> fixedRetry(Supplier<Mono<T>> supplier, int maxAttempts, int fixedDelay) {
        return supplier.get().retryWhen(Retry.fixedDelay(maxAttempts, Duration.ofSeconds(fixedDelay))
                .filter(WebServerException.class::isInstance));
    }

    public static <T> Mono<T> fixedRetryWithThrow(Supplier<Mono<T>> supplier, int maxAttempts, int fixedDelay,
                                                  Throwable exception) {
        return supplier.get().retryWhen(Retry.fixedDelay(maxAttempts, Duration.ofSeconds(fixedDelay))
                .filter(WebServerException.class::isInstance)
                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> exception));
    }

    public static Map<String, String> headers(String apiKey, ServerRequest request, String... headerNames) {
        var headers = headers(request, headerNames);
        headers.put(X_API_KEY, apiKey);
        return headers;
    }

    public static Map<String, String> headers(ServerRequest request, String... headerNames) {
        return Arrays.stream(headerNames)
                .collect(Collectors.toMap(Function.identity(), headerName -> header(request, headerName, true)));
    }

    public static Consumer<HttpHeaders> headers(String xRequestId, String apiKey) {
        return httpHeaders -> {
            httpHeaders.set(X_REQUEST_ID, xRequestId);
            httpHeaders.set(X_API_KEY, apiKey);
        };
    }

    public static Consumer<HttpHeaders> headers(Map<String, String> headerMap) {
        return httpHeaders -> headerMap.forEach(httpHeaders::set);
    }

    public static String header(ServerRequest request, String headerName, boolean isHeaderRequired) {
        var headers = Optional.of(request.headers().header(headerName)).orElse(Collections.emptyList());
        if (isHeaderRequired) {
            if (headers.isEmpty() || headers.get(0) == null || headers.get(0).isBlank()) {
                throw new ValidationException("Header '" + headerName + "' is required");
            } else {
                return headers.get(0);
            }
        } else {
            return !headers.isEmpty() ? headers.get(0) : "";
        }
    }

    public static Map<String, String> getDefaultHeaderMap(String xRequestId, String apiKey) {
        return Map.of(X_REQUEST_ID, xRequestId, X_API_KEY, apiKey);
    }

    public static Map<String, String> requestIdHeaderMap(ServerRequest request, boolean isHeaderRequired) {
        return Map.of(X_REQUEST_ID, header(request, X_REQUEST_ID, isHeaderRequired));
    }

    public static Function<UriBuilder, URI> uriBuilder(String path, List<String> pathParts,
                                                       Map<String, Object> params) {
        return uriBuilder -> uriBuilder.path(
                        CollectionUtils.isEmpty(pathParts) ? path : String.format(path, pathParts.toArray()))
                .queryParams(new LinkedMultiValueMap<>(params.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey,
                                e -> Collections.singletonList(
                                        URLEncoder.encode(e.getValue().toString(), StandardCharsets.UTF_8)))))).build();
    }

    public static Consumer<HttpHeaders> listOfStringHeaders(List<String> headersList) {
        if (headersList == null) {
            return null;
        }
        return httpHeaders -> headersList.forEach(header -> httpHeaders.add(X_FUNCTIONS, header));
    }

    public static Function<ClientResponse,
            Mono<? extends Throwable>> exceptionFunction() {
        return (exception) -> exception.bodyToMono(ErrorResponse.class)
                .flatMap(errorBody -> Mono.error(new WebClientException(errorBody)));
    }

    public static Mono<FacadeWebClientException> createFacadeWebClientException(ClientResponse clientResponse) {
        return clientResponse.toEntity(FacadeErrorResponse.class)
                .map(responseEntity ->
                        new FacadeWebClientException(responseEntity.getBody(), responseEntity.getStatusCode()));
    }

}