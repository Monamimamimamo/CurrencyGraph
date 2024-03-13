package CurrencyRateAPI.service.service;

import CurrencyRateAPI.service.models.Currencies;
import CurrencyRateAPI.service.models.CurrenciesRepo;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

@Service
@AllArgsConstructor
public class CurrencyService extends cbr_service{

    private final CurrenciesRepo currenciesRepo;


    public Mono<List<Currencies>> takeValutesData(Mono<NodeList> valuteList) {
        return valuteList.map(valuteList1 -> {
            List<Currencies> result = new ArrayList<>();
            for (int i = 0; i < valuteList1.getLength(); i++) {
                try {
                    Element valute = (Element) valuteList1.item(i);
                    Currencies currency = new Currencies();
                    currency.setId(valute.getAttribute("ID"));
                    currency.setName(valute.getElementsByTagName("Name").item(0).getTextContent());

                    currenciesRepo.save(currency);
                    result.add(currency);
                } catch (Exception e) {
                    continue;
                }
            }
            return result;
        });
    }
}
