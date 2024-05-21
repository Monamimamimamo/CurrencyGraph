package common.repo;

import common.ExchangeRatesApi;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExRatesApiRepo extends JpaRepository<ExchangeRatesApi, Long> {
    List<ExchangeRatesApi> findAllByTargetCurrencyCharCode(String charCode);
}
