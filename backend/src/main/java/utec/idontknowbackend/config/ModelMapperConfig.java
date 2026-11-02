package utec.idontknowbackend.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import utec.idontknowbackend.titular.DTO.TitularResponseDTO;
import utec.idontknowbackend.titular.model.Titular;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        // con STRICT no se llena solo mercadoId a partir de mercado.id
        mapper.typeMap(Titular.class, TitularResponseDTO.class)
                .addMappings(m -> m.map(src -> src.getMercado().getId(), TitularResponseDTO::setMercadoId));
        return mapper;
    }
}

