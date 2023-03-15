package szathmary.peter.bakalarka.util.mapper;

import org.modelmapper.ModelMapper;
import szathmary.peter.bakalarka.dto.TemperatureDto;
import szathmary.peter.bakalarka.entity.Temperature;

public class TemperatureMapper {
  private static final ModelMapper MODEL_MAPPER = new ModelMapper();

  public static TemperatureDto toDto(Temperature entity) {
    return MODEL_MAPPER.map(entity, TemperatureDto.class);
  }

  public static Temperature toEntity(TemperatureDto dto) {
    return MODEL_MAPPER.map(dto, Temperature.class);
  }
}
