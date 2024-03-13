package CurrencyRateAPI.service.service;

import CurrencyRateAPI.service.models.Currencies;
import CurrencyRateAPI.service.models.CurrenciesRepo;
import CurrencyRateAPI.service.models.ExRatesRepo;
import CurrencyRateAPI.service.models.ExchangeRates;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
@AllArgsConstructor
public class ExRatesService extends cbr_service{
    private final ExRatesRepo exRatesRepo;
    private final CurrenciesRepo currenciesRepo;

    public Mono<Object> updateDB(Mono<NodeList> exchangesList){
        return exchangesList.map(exchanges -> {
            for (int i = 0; i < exchanges.getLength(); i++) {
                try {
                    Element valute = (Element) exchanges.item(i);
                    ExchangeRates exchange = new ExchangeRates();
                    exchange.setRate(Double.parseDouble(valute.getElementsByTagName("Value").item(0).getTextContent().replace(',', '.')));
                    exchange.setTradingDate(valute.getAttribute("Date"));
                    Currencies currency = currenciesRepo.findById(valute.getAttribute("Id"));
                    exchange.setTargetCurrency(currency);
                    exRatesRepo.save(exchange);
                } catch (Exception e) {
                    System.out.println("POO!!!");
                }
            }
            return Mono.empty();
        });
    }

    public Mono<Object> daily_update_db(Mono<NodeList> exchangesList, String date){
        return exchangesList.map(exchanges -> {
            for (int i = 0; i < exchanges.getLength(); i++) {
                try {
                    Element valute = (Element) exchanges.item(i);
                    ExchangeRates exchange = new ExchangeRates();
                    exchange.setRate(Double.parseDouble(valute.getElementsByTagName("Value").item(0).getTextContent().replace(',', '.')));
                    exchange.setTradingDate(date);
                    Currencies currency = currenciesRepo.findById(valute.getAttribute("ID"));
                    exchange.setTargetCurrency(currency);
                    exRatesRepo.save(exchange);
                } catch (Exception e) {
                    System.out.println("POO!!!");
                }
            }
            return Mono.empty();
        });
    }

    public List<ExchangeRates> dailyCurrency(String curId, int size){
        Currencies currencies = currenciesRepo.findById(curId);
        System.out.println(currencies);
        if (currencies != null){;
            List<ExchangeRates> exchangeRatesList = exRatesRepo.findAllByTargetCurrencyId(curId);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy"); // Укажите формат даты
            Collections.sort(exchangeRatesList, Comparator.comparing(e -> LocalDate.parse(e.getTradingDate(), formatter).atStartOfDay()));

            Collections.reverse(exchangeRatesList);
            if (exchangeRatesList.size() > size) {
                exchangeRatesList = exchangeRatesList.subList(0, size);
            }

            return exchangeRatesList;
        }
        return null;
    }
}
