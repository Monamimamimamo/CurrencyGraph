package common;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Currencies_Api {

    @Id
    @NotBlank(message = "Код валюты не может быть пустым")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Код валюты должен состоять из трех [A-Z] букв")
    private String charCode;

    @NotBlank(message = "Имя валюты не может быть пустым")
    private String name;
}
