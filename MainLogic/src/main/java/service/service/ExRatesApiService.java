package service.service;

import common.Currencies_Api;
import common.ExchangeRatesApi;
import common.HourlyRates;
import lombok.AllArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import common.repo.CurrenciesApiRepo;
import common.repo.ExRatesApiRepo;
import common.repo.HourlyRatesRepo;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
@Service
@AllArgsConstructor
public class ExRatesApiService{
    private final ExRatesApiRepo exRatesApiRepo;
    private final CurrenciesApiRepo currenciesApiRepo;
    private final HourlyRatesRepo hourlyRatesRepo;
    private final JdbcTemplate jdbcTemplate;
    private final ReplyingKafkaTemplate<String, HourlyRates, HourlyRates> replyingKafkaTemplate;

    public Mono<Object> updateDB(Mono<Map<String, Object>> exchangesList, LocalDate date) {
        return exchangesList.map(exchanges -> {
            try {
                Map<String, Object> ratesMap = (Map<String, Object>) exchanges.get("rates");
                String baseCurrency = (String) exchanges.get("base");

                ratesMap.forEach((currencyCode, rateObject) -> {
                    Double rate = convertToDouble(rateObject);
                    if (rate != null && !currencyCode.equals(baseCurrency)) {
                        Currencies_Api currency = currenciesApiRepo.findByCharCode(currencyCode);
                        if (currency != null) {
                            ExchangeRatesApi exchange = new ExchangeRatesApi(date.atStartOfDay(), currency, rate);
                            exRatesApiRepo.save(exchange);
                        }
                    }
                });
            } catch (Exception e) {
                System.out.println("Ошибка при обновлении базы данных: " + e.getMessage());
            }
            return Mono.empty();
        });
    }


    public Mono<Object> hourly_update_db(Mono<Map<String, Object>> exchangesList) {
        return exchangesList.map(exchanges -> {
            try {
                Map<String, Object> rates = (Map<String, Object>) exchanges.get("rates");
                String baseCurrency = (String) exchanges.get("base");
                ZonedDateTime nowInYekaterinburg = ZonedDateTime.now(ZoneId.of("UTC+5"));

                rates.forEach((currencyCode, rateObject) -> {
                    Double rate = convertToDouble(rateObject);

                    if (!currencyCode.equals(baseCurrency)) {
                        Currencies_Api currency = currenciesApiRepo.findByCharCode(currencyCode);
                        if (currency != null) {
                            HourlyRates exchange = new HourlyRates(nowInYekaterinburg.toLocalDateTime(), currency, rate);
                            ProducerRecord<String, HourlyRates> record = new ProducerRecord<>("ws_topic", exchange);
                            record.headers().add("charCode", currency.getCharCode().getBytes());
                            replyingKafkaTemplate.send(record);
                            hourlyRatesRepo.save(exchange);
                        }
                    }
                });
            } catch (Exception e) {
                System.out.println("Ошибка при обновлении базы данных: " + e.getMessage());
            }
            return Mono.empty();
        });
    }

    public Mono<Object> dailyUpdate(Mono<Map<String, Object>> exchangesList, LocalDate date){
        return exchangesList.map(exchanges -> {
            try {
                Map<String, Object> ratesMap = (Map<String, Object>) exchanges.get("rates");
                String baseCurrency = (String) exchanges.get("base");

                ratesMap.forEach((currencyCode, rateObject) -> {
                    Double rate = convertToDouble(rateObject);
                    if (rate != null && !currencyCode.equals(baseCurrency)) {
                        Currencies_Api currency = currenciesApiRepo.findByCharCode(currencyCode);
                        if (currency != null) {
                            ExchangeRatesApi exchange = new ExchangeRatesApi(date.atStartOfDay(), currency, rate);
                            exRatesApiRepo.save(exchange);
                        }
                    }
                });
            } catch (Exception e) {
                System.out.println(STR."Ошибка при обновлении базы данных: \{e.getMessage()}");
            }
            return Mono.empty();
        });
    }


    public List<ExchangeRatesApi> dailyCurrency(String charCode, int size) {
        Currencies_Api currencies = currenciesApiRepo.findByCharCode(charCode);
        if (currencies != null) {
            List<ExchangeRatesApi> exchangeRatesList = exRatesApiRepo.findAllByTargetCurrencyCharCode(charCode);
            exchangeRatesList.sort(Comparator.comparing(ExchangeRatesApi::getDate).reversed());
            if (exchangeRatesList.size() > size) {
                exchangeRatesList = exchangeRatesList.subList(0, size);
            }

            return exchangeRatesList;
        }
        return null;
    }

    public List<Map<String, Object>> weeklyCurrency(String charCode, int size) {
        return getCurrencyRates("weekly_exchange_rates_unique", charCode, size);
    }

    public List<Map<String, Object>> monthlyCurrency(String charCode, int size) {
        return getCurrencyRates("monthly_exchange_rates_unique", charCode, size);
    }

    public List<Map<String, Object>> yearlyCurrency(String charCode, int size) {
        return getCurrencyRates("yearly_exchange_rates_unique", charCode, size);
    }

    public List<Map<String, Object>> getCurrencyRates(String tableName, String charCode, int size) {
        String sqlRequest = String.format("SELECT * FROM %s WHERE target_currency_char_code = ?", tableName);
        List<Map<String, Object>> exchangeRatesList = jdbcTemplate.queryForList(sqlRequest, charCode);
        sortAndLimit(exchangeRatesList, size);
        return exchangeRatesList;
    }

    private void sortAndLimit(List<Map<String, Object>> exchangeRatesList, int size) {
        exchangeRatesList.sort((map1, map2) -> ((Date) map2.get("week_start")).compareTo((Date) map1.get("week_start")));
        if (exchangeRatesList.size() > size) {
            exchangeRatesList = exchangeRatesList.subList(0, size);
        }
    }



    private Double convertToDouble(Object rateObject) {
        Double rate = null;
        if (rateObject instanceof Integer) {
            rate = ((Integer) rateObject).doubleValue();
        } else if (rateObject instanceof Double) {
            rate = (Double) rateObject;
        }
        return rate;
    }
}
