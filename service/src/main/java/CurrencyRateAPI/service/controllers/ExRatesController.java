package CurrencyRateAPI.service.controllers;

import CurrencyRateAPI.service.models.CurrenciesRepo;
import CurrencyRateAPI.service.models.ExRatesRepo;
import CurrencyRateAPI.service.models.ExchangeRates;
import CurrencyRateAPI.service.service.ExRatesService;
import lombok.AllArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.NodeList;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("/exchange_rates")
public class ExRatesController {
    private final ExRatesService exRatesService;
    private final ExRatesRepo exRatesRepo;
    private final CurrenciesRepo currenciesRepo;

    @PutMapping("/update_db/{year}")
    public Mono<String> update_db(@PathVariable("year") int year) {
        return Mono.fromCallable(() -> currenciesRepo.findAll())
                .flatMapMany(Flux::fromIterable)
                .flatMap(currencies -> Flux.range(year, 1)
                        .flatMap(i -> exRatesService.getCurrencyNamesAndValues(
                                String.format("/scripts/XML_dynamic.asp?date_req1=01/01/%s&date_req2=31/12/%s&VAL_NM_RQ=%s", i, i, currencies.getId()), "Record")
                        ))
                .flatMap(nodeList -> exRatesService.updateDB(Mono.justOrEmpty(nodeList)))
                .then(Mono.just("Обновление завершено"));
    }

    @PutMapping("/daily/update_db")
    @Scheduled(cron = "0 20 18 * * ?")
    public Mono<String> daily_update_db() {
        LocalDate date = LocalDate.now();
        Mono<NodeList> valutesList = exRatesService.getCurrencyNamesAndValues(
                String.format("/scripts/XML_daily.asp?date_req=%s", date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))),
                "Valute");
        return exRatesService.daily_update_db(valutesList, date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))
                .then(Mono.just("Обновление завершено"));
    }

    @MessageMapping("/day/{id}")
    @SendTo("/topic/day")
    public List<ExchangeRates> dailyCurrency(@DestinationVariable String id, @Payload Map<String, Integer> payload) {
        return exRatesService.dailyCurrency(id, payload.get("size"));
    }






    @GetMapping
    List<ExchangeRates> getAll() {
        return exRatesRepo.findAll();
    }

}
