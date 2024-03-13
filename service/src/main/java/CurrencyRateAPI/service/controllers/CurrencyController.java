package CurrencyRateAPI.service.controllers;

import CurrencyRateAPI.service.models.Currencies;
import CurrencyRateAPI.service.models.CurrenciesRepo;
import CurrencyRateAPI.service.service.CurrencyService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.w3c.dom.NodeList;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/currency")
@AllArgsConstructor
public class CurrencyController {
    private final CurrencyService currencyService;
    private final CurrenciesRepo currenciesRepo;

    @GetMapping
    List<Currencies> getAll(){
        return  currenciesRepo.findAll();
    }

    @PutMapping("/update_db")
    Mono<List<Currencies>> getToday() throws Exception {
        Mono<NodeList> valutesList = currencyService.getCurrencyNamesAndValues("/scripts/XML_val.asp?d=0", "Item");
        return currencyService.takeValutesData(valutesList);
    }
}
