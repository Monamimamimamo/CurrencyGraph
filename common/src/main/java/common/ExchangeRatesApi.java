package common;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.hibernate.annotations.UuidGenerator;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@IdClass(ExchangeRatesApi.customID.class)
public class ExchangeRatesApi {
    @Data
    public static class customID implements Serializable {
        private UUID id;
        private LocalDateTime date;
    }

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    private UUID id;

    @Id
    @PastOrPresent(message = "Дата не может быть в будущем времени")
    private final LocalDateTime date;

    @ManyToOne
    @JoinColumn(name = "targetCurrencyCharCode", referencedColumnName = "charCode")
    @NotNull(message = "Целевая валюта не может быть null")
    private final Currencies_Api targetCurrency;

    @Column(nullable = false)
    @Positive(message = "Курс валюты не может быть отрицательным")
    private final double rate;

}
