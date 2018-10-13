package me.exrates.exchange;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Maps;
import me.exrates.exchange.exceptions.ExchangerException;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.Valid;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.nonNull;
import static me.exrates.exchange.utils.CollectionUtil.isNotEmpty;

public class ttt {

    public static void main(String[] args) throws ExchangerException {
        Set<WorldCoinIndexMarket> usd = getDataFromMarket("USD");
        Set<WorldCoinIndexMarket> btc = getDataFromMarket("BTC");
    }

    private static Set<WorldCoinIndexMarket> getDataFromMarket(String fiat) throws ExchangerException {
        final MultiValueMap<String, String> requestParameters = new LinkedMultiValueMap<>();
        requestParameters.add("key", "GGJokOdeHrwab8hR8AdVDSn6k3kg4P");
        requestParameters.add("fiat", fiat);
        UriComponents builder = UriComponentsBuilder
                .fromHttpUrl("https://www.worldcoinindex.com/apiservice/v2getmarkets")
                .queryParams(requestParameters)
                .build();

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<WorldCoinIndexResponse> responseEntity = restTemplate.getForEntity(builder.toUriString(), WorldCoinIndexResponse.class);
        if (responseEntity.getStatusCodeValue() != 200) {
            throw new ExchangerException("WorldCoinIndex server is not available");
        }
        WorldCoinIndexResponse body = responseEntity.getBody();

        return nonNull(body) && isNotEmpty(body.markets) && isNotEmpty(body.markets.get(0))
                ? new HashSet<>(body.markets.get(0))
                : Collections.emptySet();
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    private static class WorldCoinIndexResponse {

        @JsonProperty("Markets")
        @Valid
        List<List<WorldCoinIndexMarket>> markets;

    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    private static class WorldCoinIndexMarket {

        @JsonProperty("Label")
        String label;
        @JsonProperty("Price")
        double price;
    }
}
