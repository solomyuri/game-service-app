package com.solomyuri.game_service.mapper;

import com.solomyuri.game_service.model.dto.PageDto;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface PageMapper {

    @SuppressWarnings("unchecked")
    default <E, D> PageDto<D> pageToPageResponse(Page<E> page) {
	EntityPagingMapper<E, D> mapper = (EntityPagingMapper<E, D>) Mappers.getMapper(getMapperClass(page));
        return pageToPageResponse(page, mapper);
    }
    
    private <E, D> PageDto<D> pageToPageResponse(Page<E> page, EntityPagingMapper<E, D> mapper) {
	
        List<D> content = page.getContent()
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());

        return new PageDto<>(page.getTotalElements(), page.getNumber(), page.getSize(), content);
    }

    @SuppressWarnings("unchecked")
    private <E, D> Class<EntityPagingMapper<E, D>> getMapperClass(Page<E> page) {
        String className = page.getContent().get(0).getClass().getSimpleName() + "Mapper";
        try {
            return (Class<EntityPagingMapper<E, D>>) Class.forName("com.solomyuri.game_service.mapper." + className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Mapper not found for type: " + className, e);
        }
    }
}
