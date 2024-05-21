package common.repo;

import common.HourlyRates;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HourlyRatesRepo  extends JpaRepository<HourlyRates, Long> {
    List<HourlyRates> findAllByTargetCurrencyCharCode(String charCode);
}
