package CurrencyRateAPI.service.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.Id;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ExchangeRates {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String tradingDate;

    @ManyToOne
    @JoinColumn(name = "targetCurrencyID", referencedColumnName = "id")
    private Currencies targetCurrency;

    private double rate;

}
