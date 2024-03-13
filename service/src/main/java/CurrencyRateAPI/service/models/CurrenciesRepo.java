package CurrencyRateAPI.service.models;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CurrenciesRepo extends JpaRepository<Currencies, Long> {
    Currencies findById(String id);
}
