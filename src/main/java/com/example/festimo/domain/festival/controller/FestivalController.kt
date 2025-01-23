package com.example.festimo.domain.festival.controller;

import com.example.festimo.domain.festival.dto.FestivalTO;
import com.example.festimo.domain.festival.repository.FestivalRepository;
import com.example.festimo.domain.festival.service.FestivalService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RequiredArgsConstructor
@Controller
@Tag(name = "축제 API", description = "축제 관련 API")
public class FestivalController {

    @Value("${KAKAO_MAP_API_KEY}")
    private String KAKAO_MAP_API_KEY;

    @ResponseBody
    @GetMapping("/api/map-key")
    @Hidden
    public String getApiKey() {
        return KAKAO_MAP_API_KEY;
    }

    @Autowired
    private FestivalService festivalService;

    @Autowired
    private PagedResourcesAssembler<FestivalTO> pagedResourcesAssembler;

    // 수동으로 축제 api를 불러올 수 있는 방법
    @GetMapping("/manuallyGetAllEvents")
    @ResponseBody
    public ResponseEntity<String> manuallyGetAllEvents() {
        festivalService.refreshEvents();
        return ResponseEntity.ok("모든 축제 api를 성공적으로 불러왔습니다");
    }

    @ResponseBody
    @GetMapping("/api/events")
    @Operation(summary = "전체 축제 조회")
    public PagedModel<FestivalTO> getAllEvents(@RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "28") int size,
                                               @RequestParam(required = false) Integer year,
                                               @RequestParam(required = false) Integer month,
                                               @RequestParam(required = false) String region,
                                               @RequestParam(required = false) String keyword) {

        Pageable pageable = PageRequest.of(page, size);


        Page<FestivalTO> paginatedEvent;
        if (year != null && month != null) {
            paginatedEvent = festivalService.filterByMonth(year, month, pageable);
        } else if (region != null) {
            paginatedEvent = festivalService.filterByRegion(region, pageable);
        } else if (keyword != null && !keyword.isEmpty()) {
            paginatedEvent = festivalService.search(keyword, pageable);
        } else {
            paginatedEvent = festivalService.findPaginatedWithCache(pageable);

        }

        PagedModel<EntityModel<FestivalTO>> pagedModel = pagedResourcesAssembler.toModel(paginatedEvent, festival -> EntityModel.of(festival));
        PagedModel<FestivalTO> pagination = PagedModel.of(pagedModel.getContent().stream()
                .map(entityModel -> entityModel.getContent())
                .collect(Collectors.toList()), pagedModel.getMetadata());
        return pagination;
    }

    @ResponseBody
    @GetMapping("/api/events/{eventId}")
    @Operation(summary = "축제 상세 조회")
    public FestivalTO getEvent(@PathVariable Integer eventId) {
        FestivalTO to = festivalService.findById(eventId);
        return to;
    }

    @ResponseBody
    @GetMapping("/api/events/search")
    @Operation(summary = "축제 검색")
    public Page<FestivalTO> search(
            @RequestParam String keyword,
            @RequestParam int page,
            @RequestParam int size) {
        Pageable pageable = PageRequest.of(page, size);
        return festivalService.search(keyword, pageable);
    }

    @ResponseBody
    @GetMapping("/api/events/filter/month")
    @Operation(summary = "축제 날짜별 필터링")
    public Page<FestivalTO> filterByMonth(
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam int page,
            @RequestParam int size) {
        Pageable pageable = PageRequest.of(page, size);
        return festivalService.filterByMonth(year, month, pageable);
    }

    @ResponseBody
    @GetMapping("/api/events/filter/region")
    @Operation(summary = "축제 지역별 필터링")
    public Page<FestivalTO> filterByRegion(
            @RequestParam String region,
            @RequestParam int page,
            @RequestParam int size) {
        Pageable pageable = PageRequest.of(page, size);
        return festivalService.filterByRegion(region, pageable);
    }


}
