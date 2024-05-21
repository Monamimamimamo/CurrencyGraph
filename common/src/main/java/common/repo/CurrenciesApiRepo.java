package common.repo;

import common.Currencies_Api;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CurrenciesApiRepo extends JpaRepository<Currencies_Api, Long> {
    Currencies_Api findByCharCode(String charCode);
}
