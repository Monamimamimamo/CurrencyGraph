package service.controller;

import common.Currencies_Api;
import common.repo.CurrenciesApiRepo;
import lombok.AllArgsConstructor;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import service.service.CurrencyApiService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/currency/api")
@AllArgsConstructor
public class CurrencyApiController {
    private final CurrencyApiService currencyApiService;
    private final CurrenciesApiRepo currenciesApiRepo;
    private final ReplyingKafkaTemplate<String, String, String> replyingKafkaTemplate;
    @GetMapping
    List<Currencies_Api> getAll(){
        return  currenciesApiRepo.findAll();
    }


    @PutMapping("/update_db")
    public Mono<List<Currencies_Api>> getAllCurrenciesApi() throws Exception {
        Map<String, Object> responseMap = currencyApiService.getLatestFromApi(replyingKafkaTemplate);
        return currencyApiService.takeValutesData_api(Mono.just(responseMap));
    }
}
