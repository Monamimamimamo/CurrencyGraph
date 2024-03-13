package CurrencyRateAPI.service.models;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExRatesRepo extends JpaRepository<ExchangeRates, Long> {
    List<ExchangeRates> findAllByTargetCurrencyId(String targetCurrencyId);
}
