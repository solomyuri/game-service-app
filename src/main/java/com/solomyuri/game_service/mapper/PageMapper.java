package com.solomyuri.game_service.mapper;

import com.solomyuri.game_service.model.dto.PageDto;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PageMapper {

    default <T> PageDto<T> pageToPageResponse(Page<T> page) {
        List<T> content = page.getContent();
        return new PageDto<>(page.getTotalElements(), page.getNumber(), page.getSize(), content);
    }
}
